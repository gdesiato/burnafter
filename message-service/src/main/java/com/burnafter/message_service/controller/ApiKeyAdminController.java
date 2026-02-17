package com.burnafter.message_service.controller;

import com.burnafter.message_service.service.ApiKeyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class ApiKeyAdminController {

    private final ApiKeyService service;

    public ApiKeyAdminController(ApiKeyService service) {
        this.service = service;
    }

    @PostMapping("/apikey")
    public String create() {
        return service.generateApiKey(1000, "baseline");
    }
}
