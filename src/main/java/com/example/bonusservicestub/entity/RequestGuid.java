package com.example.bonusservicestub.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestGuid {
    private RequestGuidMeta meta;
    private RequestGuidData data;
}
