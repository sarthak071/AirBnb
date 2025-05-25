package com.sarthak.airbnb.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
public class StripeConfig {

    public StripeConfig(@Value("${stripe.secret.key}")String secretKey)
    {
        Stripe.apiKey = secretKey;
    }
}
