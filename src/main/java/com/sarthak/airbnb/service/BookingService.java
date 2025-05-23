package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.BookingDto;
import com.sarthak.airbnb.dto.BookingRequestDto;
import com.sarthak.airbnb.dto.GuestDto;

import java.util.List;

public interface BookingService {
    BookingDto initialiseBooking(BookingRequestDto bookingRequestDto);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
