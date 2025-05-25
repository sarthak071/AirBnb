package com.sarthak.airbnb.service;


import com.sarthak.airbnb.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
