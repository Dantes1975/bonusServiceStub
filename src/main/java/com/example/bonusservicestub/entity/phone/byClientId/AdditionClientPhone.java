package com.example.bonusservicestub.entity.phone.byClientId;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdditionClientPhone {

    @JsonProperty("phone")
    private String phone;
    @JsonProperty("clientType")
    private String clientType;

}
