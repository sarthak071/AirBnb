package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.BookingDto;
import com.sarthak.airbnb.dto.HotelDto;
import com.sarthak.airbnb.dto.HotelInfoDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);
    HotelDto getHotelById(Long id);
    HotelDto updateHotelById(Long id, HotelDto hotelDto);
    void deleteHotelById(Long id);
    void activateHotel(Long hotelId);
    HotelInfoDto getHotelInfoById(Long hotelId);

    List<HotelDto> getAllHotels();

}
