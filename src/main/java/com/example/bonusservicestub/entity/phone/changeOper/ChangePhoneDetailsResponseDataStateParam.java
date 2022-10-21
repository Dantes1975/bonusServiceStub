package com.example.bonusservicestub.entity.phone.changeOper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePhoneDetailsResponseDataStateParam {

    @JsonProperty("key")
    private String key;
    @JsonProperty("value")
    private String value;
    @JsonProperty("type")
    private String type;

}
