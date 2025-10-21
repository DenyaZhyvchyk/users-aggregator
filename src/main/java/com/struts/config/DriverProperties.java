package com.struts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@ConfigurationProperties(prefix = "app")
public class DriverProperties {
    private Map<String, String> drivers = new HashMap<>();

    public String resolve(String strategy) {
        return Optional.ofNullable(drivers.get(strategy.toLowerCase()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown driver for: " + strategy));
    }

    public Map<String, String> getDrivers() {
        return drivers;
    }

    public void setDrivers(Map<String, String> drivers) {
        this.drivers = drivers;
    }
}
