package com.example.bonusservicestub.entity.phone.changeOper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePhoneDetailsResponse {

    @JsonProperty("status")
    private String status;
    @JsonProperty("actualTimestamp")
    private String actualTimestamp;
    @JsonProperty("error")
    private ChangePhoneDetailsError error;
    @JsonProperty("data")
    private ChangePhoneDetailsResponseData data;

}
