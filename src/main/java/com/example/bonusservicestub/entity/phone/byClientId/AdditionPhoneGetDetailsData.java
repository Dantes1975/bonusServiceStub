package com.example.bonusservicestub.entity.phone.byClientId;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdditionPhoneGetDetailsData {

    @JsonProperty("systemId")
    private String systemID;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("cardholderId")
    private String cardholderId;

}
