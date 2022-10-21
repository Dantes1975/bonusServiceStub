package com.example.bonusservicestub.entity.phone.cardByPhone;

import com.example.bonusservicestub.entity.phone.byClientId.AdditionPhoneDetailError;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardAdditionPhoneDetailsResponse {

    @JsonProperty("status")
    private String status;
    @JsonProperty("actualTimestamp")
    private String actualTimestamp;
    @JsonProperty("error")
    private AdditionPhoneDetailError error;
    @JsonProperty("data")
    private CardAdditionPhoneDetailResponseData data;

}
