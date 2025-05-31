package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.*;
import com.sarthak.airbnb.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    void initializeForYear(Room room);
    void deleteAllInventories(Room room);
    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
