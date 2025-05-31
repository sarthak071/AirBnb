package com.sarthak.airbnb.repository;

import com.sarthak.airbnb.entity.Hotel;
import com.sarthak.airbnb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel,Long> {
    List<Hotel> findByOwner(User user);
}
