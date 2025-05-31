package com.sarthak.airbnb.controller;

import com.sarthak.airbnb.dto.HotelDto;
import com.sarthak.airbnb.dto.HotelInfoDto;
import com.sarthak.airbnb.dto.HotelPriceDto;
import com.sarthak.airbnb.dto.HotelSearchRequest;
import com.sarthak.airbnb.service.HotelService;
import com.sarthak.airbnb.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    @Operation(summary = "Search hotels", tags = {"Browse Hotels"})
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest)
    {
        var page = inventoryService.searchHotels(hotelSearchRequest);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping("/{hotelId}/info")
    @Operation(summary = "Get a hotel info by hotelId", tags = {"Browse Hotels"})
    public ResponseEntity<HotelInfoDto> searchHotels(@PathVariable Long hotelId)
    {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}
