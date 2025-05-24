package com.sarthak.airbnb.dto;

import com.sarthak.airbnb.entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class HotelPriceDto {

    public HotelPriceDto(Hotel hotel, double price) {
        this.hotel = hotel;
        this.price = price;
    }
    private Hotel hotel;
    private double price;
}
