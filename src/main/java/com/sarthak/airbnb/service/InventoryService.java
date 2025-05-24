package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.HotelDto;
import com.sarthak.airbnb.dto.HotelPriceDto;
import com.sarthak.airbnb.dto.HotelSearchRequest;
import com.sarthak.airbnb.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeForYear(Room room);
    void deleteAllInventories(Room room);
    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
