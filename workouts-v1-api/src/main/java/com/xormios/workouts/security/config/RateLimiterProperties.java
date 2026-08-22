package com.xormios.workouts.security.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimiterProperties {

    private int loginCapacity;
    private long loginRefillPeriodSeconds;
    private int registerCapacity;
    private long registerRefillPeriodSeconds;
}
