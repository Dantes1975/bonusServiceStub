package com.example.bonusservicestub.entity.phone.changeOper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePhoneDetailsError {

    @JsonProperty("id")
    private String id;
    @JsonProperty("code")
    private String code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("displayMessage")
    private String displayMessage;
    @JsonProperty("techInfo")
    private String techInfo;
    @JsonProperty("systemId")
    private String systemId;

}
