package com.sarthak.airbnb.service;

import com.sarthak.airbnb.entity.Room;

public interface InventoryService {

    void initializeForYear(Room room);
    void deleteFutureInventories(Room room);
}
