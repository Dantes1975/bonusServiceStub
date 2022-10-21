package com.example.bonusservicestub.entity.phone.changeOper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePhoneSvfe {

    @JsonProperty("pan")
    private String pan;
    @JsonProperty("virtualNum")
    private String virtualNum;
    @JsonProperty("type")
    private String type;
    @JsonProperty("contactPhoneClient")
    private String contactPhoneClient;
    @JsonProperty("previousPhone")
    private String previousPhone;
    @JsonProperty("action")
    private String action;
    @JsonProperty("clientType")
    private String clientType;
    @JsonProperty("stateReason")
    private String stateReason;
    @JsonProperty("userFullName")
    private String userFullName;
    @JsonProperty("userLogin")
    private String userLogin;

}
