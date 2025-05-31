package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.*;
import com.sarthak.airbnb.entity.Hotel;
import com.sarthak.airbnb.entity.Inventory;
import com.sarthak.airbnb.entity.Room;
import com.sarthak.airbnb.entity.User;
import com.sarthak.airbnb.exceptions.ResourceNotFoundException;
import com.sarthak.airbnb.exceptions.UnAuthorizedException;
import com.sarthak.airbnb.repository.HotelMinPriceRepository;
import com.sarthak.airbnb.repository.InventoryRepository;
import com.sarthak.airbnb.repository.RoomRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.sarthak.airbnb.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;

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
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels for {} city, from {} to {}",hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate());
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(),hotelSearchRequest.getSize());
        Long dateCount =ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate())+1;

        return hotelMinPriceRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(),hotelSearchRequest.getRoomsCount(),dateCount,pageable);
    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Getting all inventory by room for room with ud : {}",roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id :" + roomId));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This is user does not own this hotel with ID :" +room.getHotel().getId());
        }

        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map(inventory-> modelMapper.map(inventory, InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId , UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating all inventory by the room with room with id : {} between date range : {} - {}",roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id :" + roomId));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This is user does not own this hotel with ID :" +room.getHotel().getId());
        }

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());
        inventoryRepository.updateInventory(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate()
                                            ,updateInventoryRequestDto.getClosed(),updateInventoryRequestDto.getSurgeFactor());
    }
}
