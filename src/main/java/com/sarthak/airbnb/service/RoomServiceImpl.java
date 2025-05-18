package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.RoomDto;
import com.sarthak.airbnb.entity.Hotel;
import com.sarthak.airbnb.entity.Room;
import com.sarthak.airbnb.exceptions.ResourceNotFoundException;
import com.sarthak.airbnb.repository.HotelRepository;
import com.sarthak.airbnb.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    @Override
    public RoomDto createNewRoom(Long hotelId,RoomDto roomDto) {
        log.info("Creating a new Room with id : {}",roomDto.getId());
        Hotel hotel =hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id : "+hotelId));
        Room room = modelMapper.map(roomDto,Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        if(hotel.getActive()){
            inventoryService.initializeForYear(room);
        }
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms with hotel id : {}",hotelId);
        Hotel hotel =hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id : "+hotelId));
        return hotel.getRooms()//List<Room>
                .stream().map(room -> modelMapper.map(room,RoomDto.class))
                .toList();
    }



    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with hotel id : {}",roomId);
        Room room =roomRepository
                .findById(roomId)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id : "+roomId));
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public void deleteRoomById(Long roomId) {
        Room room =roomRepository
                .findById(roomId)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id : "+roomId));
        inventoryService.deleteFutureInventories(room);
        roomRepository.deleteById(roomId);
    }
}
