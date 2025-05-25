package com.sarthak.airbnb.dto;

import com.sarthak.airbnb.entity.Hotel;
import com.sarthak.airbnb.entity.Room;
import com.sarthak.airbnb.entity.User;
import com.sarthak.airbnb.entity.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {
    private Long id;
//    private Hotel hotel;
//    private Room room;
//    private User user;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
    private BigDecimal amount;
}
