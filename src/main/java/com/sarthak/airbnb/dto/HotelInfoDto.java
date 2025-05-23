package com.sarthak.airbnb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HotelInfoDto
{
    private HotelDto hotelDto;
    private List<RoomDto> rooms;
}
