package com.example.bonusservicestub.entity.phone.cardByPhone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardAdditionPhoneDetailsData {

    @JsonProperty("systemId")
    private String systemId;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("mobilePhone")
    private String mobilePhone;
    @JsonProperty("clientType")
    private String clientType;

}
