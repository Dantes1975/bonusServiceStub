package com.example.bonusservicestub.entity.phone.byClientId;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdditionPhoneGetDetailsResponse {

    @JsonProperty("status")
    private String status;
    @JsonProperty("actualTimestamp")
    private String actualTimestamp;
    @JsonProperty("error")
    private AdditionPhoneDetailError error;
    @JsonProperty("data")
    private AdditionPhoneGetDetailResponseData data;

}
