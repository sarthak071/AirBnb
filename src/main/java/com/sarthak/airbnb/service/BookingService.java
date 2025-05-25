package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.BookingDto;
import com.sarthak.airbnb.dto.BookingRequestDto;
import com.sarthak.airbnb.dto.GuestDto;
import com.stripe.model.Event;

import java.util.List;
import java.util.Map;

public interface BookingService {
    BookingDto initialiseBooking(BookingRequestDto bookingRequestDto);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayment(Long bookingId);

    void capturePaymentEvent(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);
}
