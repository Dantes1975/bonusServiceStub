package com.example.bonusservicestub.entity.phone.changeOper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePhoneDetailsResponseData {

    @JsonProperty("rawId")
    private String rawId;
    @JsonProperty("state")
    private ChangePhoneDetailsResponseDataState state;

}
