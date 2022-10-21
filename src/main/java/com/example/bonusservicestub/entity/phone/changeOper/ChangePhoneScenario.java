package com.example.bonusservicestub.entity.phone.changeOper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePhoneScenario {

    @JsonProperty("code")
    private String code;
    @JsonProperty("systemExecute")
    private String systemExecute;

}
