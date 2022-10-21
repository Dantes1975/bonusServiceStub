package com.example.bonusservicestub.entity.phone.byClientId;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdditionPhoneGetDetailsRequest {

    @JsonProperty("data")
    private AdditionPhoneGetDetailsData data;

}
