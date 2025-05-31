package com.sarthak.airbnb.controller;

import com.sarthak.airbnb.dto.BookingDto;
import com.sarthak.airbnb.dto.BookingRequestDto;
import com.sarthak.airbnb.dto.GuestDto;
import com.sarthak.airbnb.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    @Operation(summary = "Initiate the booking", tags = {"Booking Flow"})
    public ResponseEntity<BookingDto> intitialiseBooking(@RequestBody BookingRequestDto bookingRequestDto)
    {
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequestDto));
    }

    @PostMapping("/{bookingId}/addGuests")
    @Operation(summary = "Add guest Ids to the booking", tags = {"Booking Flow"})
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId, @RequestBody List<GuestDto> guestDtoList) {
        return ResponseEntity.ok(bookingService.addGuests(bookingId, guestDtoList));
    }

    @PostMapping("/{bookingId}/payments")
    @Operation(summary = "Initiate payments flow for the booking", tags = {"Booking Flow"})
    public ResponseEntity<Map<String,String>> initiatePayment(@PathVariable Long bookingId) {
        String sessionUrl = bookingService.initiatePayment(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl",sessionUrl));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel the booking", tags = {"Booking Flow"})
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}/status")
    @Operation(summary = "Check the status of the booking", tags = {"Booking Flow"})
    public ResponseEntity<Map<String,String>> getStatus(@PathVariable Long bookingId) {
        return ResponseEntity.ok(Map.of("status",bookingService.getBookingStatus(bookingId)));
    }
}
