package com.sarthak.airbnb.controller;

import com.sarthak.airbnb.dto.BookingDto;
import com.sarthak.airbnb.dto.HotelDto;
import com.sarthak.airbnb.dto.HotelReportDto;
import com.sarthak.airbnb.service.BookingService;
import com.sarthak.airbnb.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new hotel", tags = {"Admin Hotel"})
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto){
        HotelDto hotel = hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    @Operation(summary = "Get a hotel by Id", tags = {"Admin Hotel"})
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId){
        HotelDto hotelDto = hotelService.getHotelById(hotelId);
        return new ResponseEntity<>(hotelDto, HttpStatus.OK);
    }

    @PutMapping("/{hotelId}")
    @Operation(summary = "Update a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId,@RequestBody HotelDto hotelDto){
        HotelDto updatedHotel = hotelService.updateHotelById(hotelId, hotelDto);
        return new ResponseEntity<>(updatedHotel, HttpStatus.OK);
    }
    @DeleteMapping("/{hotelId}")
    @Operation(summary = "Delete a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId){
        hotelService.deleteHotelById(hotelId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PatchMapping("/{hotelId}/activate")
    @Operation(summary = "Activate a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId){
        hotelService.activateHotel(hotelId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Get all hotels owned by admin", tags = {"Admin Hotel"})
    public  ResponseEntity<List<HotelDto>> getAllHotels(){
        return new ResponseEntity<>(hotelService.getAllHotels(),HttpStatus.OK);
    }

    @GetMapping("/{hotelId}/bookings")
    @Operation(summary = "Get all bookings of a hotel", tags = {"Admin Bookings"})
    public  ResponseEntity<List<BookingDto>> getAllBookingsByHotelId(@PathVariable Long hotelId){
        return new ResponseEntity<>(bookingService.getAllBookingByHotelId(hotelId),HttpStatus.OK);
    }

    @GetMapping("/{hotelId}/reports")
    @Operation(summary = "Generate a bookings report of a hotel", tags = {"Admin Bookings"})
    public  ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId,
                                                          @RequestParam(required = false)LocalDate startDate,
                                                          @RequestParam(required = false)LocalDate endDate)
    {
        if(startDate ==null) startDate = LocalDate.now().minusMonths(1);
        if(endDate == null) endDate = LocalDate.now();
        return  ResponseEntity.ok(bookingService.getHotelReport(hotelId,startDate,endDate));
    }

}
