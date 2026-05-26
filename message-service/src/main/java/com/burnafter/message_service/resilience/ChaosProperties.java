package com.burnafter.message_service.resilience;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "resilience.chaos")
public class ChaosProperties {

    private boolean enabled;

    private double failureRate;

    private double delayRate;

    private long delayMs;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public double getDelayRate() {
        return delayRate;
    }

    public void setDelayRate(double delayRate) {
        this.delayRate = delayRate;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }
}