package com.programacao_web.rpg_market.dto;

import lombok.Data;

@Data
public class ProfileRequest {
    private String email;
    private String characterClass;
    private String croppedImage;
}