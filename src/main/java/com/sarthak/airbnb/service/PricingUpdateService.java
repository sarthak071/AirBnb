package com.sarthak.airbnb.service;


import com.sarthak.airbnb.entity.Hotel;
import com.sarthak.airbnb.entity.HotelMinPrice;
import com.sarthak.airbnb.entity.Inventory;
import com.sarthak.airbnb.repository.HotelMinPriceRepository;
import com.sarthak.airbnb.repository.HotelRepository;
import com.sarthak.airbnb.repository.InventoryRepository;
import com.sarthak.airbnb.strategy.PricingService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    //Scheduler to update the inventory and hotel min price table every hour

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    @Scheduled(cron = "*/5 * * * * *")
    public void updatePrices(){
        int page = 0;
        int batchSize = 100;
        while (true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page,batchSize));
            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrice);
            page++;
        }
    }

    public void updateHotelPrice(Hotel hotel){
        log.info("Updating Hotel prices for hotel id : {}",hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);
        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);
        updateInventoryPrice(inventoryList);
        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);
    }
    public void updateHotelMinPrice(Hotel hotel,List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate){
        Map<LocalDate,BigDecimal> dailyMinPrice = inventoryList.stream()
                .collect(
                        Collectors.groupingBy(
                                Inventory::getDate,
                                Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                        )
                )
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e->e.getValue().orElse(BigDecimal.ZERO)));
        List< HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrice.forEach(((date, price) ->{
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date).orElse(new HotelMinPrice(hotel,date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        } ));

        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    public void updateInventoryPrice(List<Inventory> inventoryList){
       inventoryList.forEach(inventory ->{
           BigDecimal dynamicPrice  = pricingService.calculateDynamicPrice(inventory);
           inventory.setPrice(dynamicPrice);
       });
       inventoryRepository.saveAll(inventoryList);
    }
}
