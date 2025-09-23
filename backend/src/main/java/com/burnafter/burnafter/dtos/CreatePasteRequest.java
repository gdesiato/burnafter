package com.burnafter.burnafter.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePasteRequest {
    @NotBlank public String kind;
    @NotBlank
    @Size(max = 20000) public String content;
    @Size(max = 200) public String password;
    @Min(1) @Max(10) public int views = 1;
    @Size(max = 10) public String expiresIn = "24h"; // "30min","1h","24h","7d"
    public boolean burnAfterRead = false;
}
