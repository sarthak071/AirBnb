package com.sarthak.airbnb.strategy;

import com.sarthak.airbnb.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);
}
