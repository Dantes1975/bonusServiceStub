package com.example.bonusservicestub.entity.phone.byClientId;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AdditionPhoneGetDetailResponseData {

    @JsonProperty("phonesList")
    private List<AdditionClientPhone> phonesList;

}
