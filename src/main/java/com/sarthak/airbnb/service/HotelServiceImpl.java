package com.sarthak.airbnb.service;

import com.sarthak.airbnb.dto.BookingDto;
import com.sarthak.airbnb.dto.HotelDto;
import com.sarthak.airbnb.dto.HotelInfoDto;
import com.sarthak.airbnb.dto.RoomDto;
import com.sarthak.airbnb.entity.Hotel;
import com.sarthak.airbnb.entity.Room;
import com.sarthak.airbnb.entity.User;
import com.sarthak.airbnb.exceptions.ResourceNotFoundException;
import com.sarthak.airbnb.exceptions.UnAuthorizedException;
import com.sarthak.airbnb.repository.BookingRepository;
import com.sarthak.airbnb.repository.HotelRepository;
import com.sarthak.airbnb.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sarthak.airbnb.util.AppUtils.getCurrentUser;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating a new Hotel with name : {}",hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
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
        checkUser(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating a hotel with id: {}",id);
        Hotel hotel =hotelRepository
                .findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id ${id} : "+id));
        checkUser(hotel);
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
        checkUser(hotel);


        for(Room room: hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(hotelId);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating a hotel with id: {}",hotelId);
        Hotel hotel =hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id : "+hotelId));
        hotel.setActive(true);
        checkUser(hotel);

        //assuming only do it once
        for(Room room : hotel.getRooms()){
            inventoryService.initializeForYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel =hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->new ResourceNotFoundException("Hotel not found with id ${id} : "+hotelId));
        List<RoomDto> rooms = hotel.getRooms().stream().map(room -> modelMapper.map(room,RoomDto.class)).toList();
        return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {

        User user = getCurrentUser();
        log.info("Getting all hotels for the admin with id : {}",user.getId());
        List<Hotel> hotels = hotelRepository.findByOwner(user);
        return  hotels
                .stream()
                .map((element)->modelMapper.map(element,HotelDto.class))
                .collect(Collectors.toList());
    }



    void checkUser(Hotel hotel){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This is user does not own this hotel with ID :" +hotel.getId());
        }
    }
}
