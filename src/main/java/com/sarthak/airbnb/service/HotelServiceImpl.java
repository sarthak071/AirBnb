package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.HotelDto;
import com.sarthak.airbnb.entity.Hotel;
import com.sarthak.airbnb.entity.Room;
import com.sarthak.airbnb.exceptions.ResourceNotFoundException;
import com.sarthak.airbnb.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating a new Hotel with name : {}",hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);//onboarding
        hotel = hotelRepository.save(hotel);
        log.info("Created a new Hotel with id : {}",hotel.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting a hotel with id: {}",id);
        Hotel hotel =hotelRepository
                .findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id : "+id));
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating a hotel with id: {}",id);
        Hotel hotel =hotelRepository
                .findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id ${id} : "+id));
        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long hotelId) {
        Hotel hotel =hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id : "+hotelId));
        hotelRepository.deleteById(hotelId);

        for(Room room: hotel.getRooms()){
            inventoryService.deleteFutureInventories(room);
        }
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating a hotel with id: {}",hotelId);
        Hotel hotel =hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id : "+hotelId));
        hotel.setActive(true);

        //assuming only do it once
        for(Room room : hotel.getRooms()){
            inventoryService.initializeForYear(room);
        }
    }
}
