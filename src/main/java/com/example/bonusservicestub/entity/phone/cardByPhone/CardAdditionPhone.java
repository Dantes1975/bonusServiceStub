package com.example.bonusservicestub.entity.phone.cardByPhone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardAdditionPhone {

    @JsonProperty("maskNum")
    private String maskNum;
    @JsonProperty("virtualNum")
    private String virtualNum;
    @JsonProperty("product")
    private String product;

}
