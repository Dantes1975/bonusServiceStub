package com.example.bonusservicestub.entity.balance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor
@NoArgsConstructor
public class CardInfoDetails {

   private String startDate;
   private String activateDate;
    @JsonProperty("is3DSecure")
    private boolean is3DSecure;
    private String priorityPassNum;

}
