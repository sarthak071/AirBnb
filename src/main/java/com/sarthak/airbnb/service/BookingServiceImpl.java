package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.BookingDto;
import com.sarthak.airbnb.dto.BookingRequestDto;
import com.sarthak.airbnb.dto.GuestDto;
import com.sarthak.airbnb.entity.*;
import com.sarthak.airbnb.entity.enums.BookingStatus;
import com.sarthak.airbnb.exceptions.ResourceNotFoundException;
import com.sarthak.airbnb.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

        for(Inventory inventory : inventoryList)
        {
            inventory.setReservedCount((inventory.getReservedCount() +bookingRequestDto.getRoomsCount()));
        }

        inventoryRepository.saveAll(inventoryList);


        //Create a booking
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckoutDate())
//                .user(getCurrentUser())
                .roomsCount(bookingRequestDto.getRoomsCount())
                .amount(BigDecimal.TEN)
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

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        if(booking.getBookingStatus()!= BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state ,cannot aff guest");
        }

        for(GuestDto guestDto: guestDtoList){
            Guest guest = modelMapper.map(guestDto ,Guest.class);
//            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);
    }

    private boolean hasBookingExpired(Booking booking){
        return  booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){
        User user = new User();
        user.setId(1L);
        return user;
    }
}
