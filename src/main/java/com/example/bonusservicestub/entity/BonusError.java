package com.example.bonusservicestub.entity;

import lombok.Data;

@Data
public class BonusError {
    private String systemId;
    private String code;
    private String message;
}
