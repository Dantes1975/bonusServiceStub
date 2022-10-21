package com.example.bonusservicestub.entity.phone.byClientId;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdditionPhoneDetailError {

    @JsonProperty("systemId")
    private String systemId;
    @JsonProperty("code")
    private String code;
    @JsonProperty("message")
    private String message;

}
