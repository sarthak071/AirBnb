package com.sarthak.airbnb.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequestDto {
    private Long hotelId;
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkoutDate;
    private Integer roomsCount;
}
