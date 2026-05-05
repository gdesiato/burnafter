package com.burnafter.message_service.controller;

import com.burnafter.message_service.metrics.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final MetricsService metricsService;

    public DebugController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/metric")
    public String debugMetric() {
        metricsService.incrementMessagesCreated();
        System.out.println(">>> METRIC INCREMENTED");
        return "ok";
    }
}