package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.HotelDto;
import com.sarthak.airbnb.dto.HotelSearchRequest;
import com.sarthak.airbnb.entity.Hotel;
import com.sarthak.airbnb.entity.Inventory;
import com.sarthak.airbnb.entity.Room;
import com.sarthak.airbnb.repository.InventoryRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public void initializeForYear(Room room) {

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for(; !today.isAfter(endDate); today = today.plusDays(1))
        {
            log.info("Creating inventory for Hotel id : {} and Room id : {}",room.getHotel().getId(),room.getId());
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with id: {}",room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels for {} city, from {} to {}",hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate());
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(),hotelSearchRequest.getSize());
        Long dateCount =ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate())+1;
        Page<Hotel> hotelPage= inventoryRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(),hotelSearchRequest.getRoomsCount(),dateCount,pageable);
        return  hotelPage.map(hotel -> modelMapper.map(hotel,HotelDto.class));
    }
}
