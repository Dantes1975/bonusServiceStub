package com.example.bonusservicestub.entity.phone.changeOper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePhoneDetails {

    @JsonProperty("guid")
    private String guid;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("acceptedTime")
    private Long acceptedTime;
    @JsonProperty("scenario")
    private ChangePhoneScenario scenario;
    @JsonProperty("svfe")
    private ChangePhoneSvfe svfe;

}
