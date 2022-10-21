package com.example.bonusservicestub.entity.phone.changeOper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePhoneDetailsResponseDataState {

    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("isFinal")
    private Boolean isFinal;
    @JsonProperty("params")
    private List<ChangePhoneDetailsResponseDataStateParam> params;

}
