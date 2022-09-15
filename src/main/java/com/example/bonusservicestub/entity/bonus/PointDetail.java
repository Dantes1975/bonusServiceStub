package com.example.bonusservicestub.entity.bonus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointDetail {
    private String mcc;
    private String merchName;
    private String merchId;
    private String transDate;
    @JsonProperty("UTRNNO")
    private String utrnno;
    @JsonProperty("BOUTRNNO")
    private String boutrnno;
}
