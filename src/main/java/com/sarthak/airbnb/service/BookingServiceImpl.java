package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.*;
import com.sarthak.airbnb.entity.*;
import com.sarthak.airbnb.entity.enums.BookingStatus;
import com.sarthak.airbnb.exceptions.ResourceNotFoundException;
import com.sarthak.airbnb.exceptions.UnAuthorizedException;
import com.sarthak.airbnb.repository.*;
import com.sarthak.airbnb.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;

import static com.sarthak.airbnb.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontEndUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequestDto bookingRequestDto) {

        log.info("Initialising info for hotel : {}, room: {} , date {}-{}",bookingRequestDto.getHotelId()
                ,bookingRequestDto.getRoomId(),bookingRequestDto.getCheckInDate(),bookingRequestDto.getCheckoutDate());
        Hotel hotel = hotelRepository.findById(bookingRequestDto.getHotelId())
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with the id : "+ bookingRequestDto.getHotelId()));
        Room room = roomRepository.findById(bookingRequestDto.getRoomId())
                .orElseThrow(()->new ResourceNotFoundException("Room not found with the id : "+ bookingRequestDto.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(),
                bookingRequestDto.getCheckInDate(),bookingRequestDto.getCheckoutDate(),bookingRequestDto.getRoomsCount());
        long daysCount = ChronoUnit.DAYS.between(  bookingRequestDto.getCheckInDate(),bookingRequestDto.getCheckoutDate())+1;

        if(inventoryList.size() != daysCount)
        {
            throw new IllegalStateException("Room is not available anymore");
        }

        inventoryRepository.initBooking(
                room.getId(),
                bookingRequestDto.getCheckInDate(),
                bookingRequestDto.getCheckoutDate(),
                bookingRequestDto.getRoomsCount()
        );

        BigDecimal priceOfOneRoom =  pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceOfOneRoom.multiply(BigDecimal.valueOf(bookingRequestDto.getRoomsCount()));

        //Create a booking
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckoutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequestDto.getRoomsCount())
                .amount(totalPrice)
                .build();
        booking = bookingRepository.save(booking);
        return  modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking with id {}",bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with the id : "+ bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        if(booking.getBookingStatus()!= BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state ,cannot aff guest");
        }

        for(GuestDto guestDto: guestDtoList){
            Guest guest = modelMapper.map(guestDto ,Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with the id : "+ bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,frontEndUrl+"/payments/success",frontEndUrl+"/payments/failure");
        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePaymentEvent(Event event) {
        log.info("type outside: {}",event.getType());
        if ("checkout.session.completed".equals(event.getType())) {
            log.info("type: {}",event.getType());
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session == null) return;
            String sessionId = session.getId();
            Booking booking = bookingRepository
                    .findByPaymentSessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found for session ID: " + sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(
                    booking.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getRoomsCount()
            );
            inventoryRepository.confirmBooking(
                    booking.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getRoomsCount()
            );


            log.info("Booking confirmed for Booking ID: {}", booking.getId());
        } else {
            log.warn("Unhandled event type : {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        log.info("Adding guests for booking with id {}",bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with the id : "+ bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomsCount()
        );
        inventoryRepository.cancelBooking(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomsCount()
        );

        //handle refund

        try{
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundParams);
        }catch (StripeException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with the id : "+ bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingByHotelId(Long hotelId) {
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel not found with Id: "+hotelId));
        User user=getCurrentUser();

        log.info("Getting all bookings with hotel id {}",hotelId);
        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with hotelid: "+hotelId);
        List<Booking> bookings = bookingRepository.findByHotel(hotel);
        return bookings.stream().map(booking -> modelMapper.map(booking,BookingDto.class))
                .collect(Collectors.toList());


    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel not found with Id: "+hotelId));
        User user=getCurrentUser();

        log.info("Generating Report all books with hotel id {}",hotelId);
        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with hotel id: "+hotelId);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);
        Long totalConfirmedBookings = bookings.stream().filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream().
                filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO , BigDecimal::add);

        BigDecimal avgRevenue = totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings,totalRevenueOfConfirmedBookings,avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.findByUser(user).stream()
                .map(booking -> modelMapper.map(booking,BookingDto.class))
                .collect(Collectors.toList());
    }

    private boolean hasBookingExpired(Booking booking){
        return  booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

}
