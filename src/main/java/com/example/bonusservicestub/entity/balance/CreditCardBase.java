package com.example.bonusservicestub.entity.balance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreditCardBase {
    private String virtualNum;
    private String maskNum;
    private String pan;
    private String issueType;
    private String design;
    private CreditCardState state;
    private CreditCardState originalState;
    private List<CreditCardBalance> balance;
    @JsonProperty("isPayPass")
    private boolean payPass;
    @JsonProperty("isLoyalty")
    private boolean loyalty;
    @JsonProperty("isMobilePay")
    private boolean mobilePay;
    @JsonIgnore
    private String resp;
    @JsonIgnore
    private String respDescr;
    @JsonIgnore
    private String respDescrCyr;
    @JsonProperty("private")
    private CreditCardPrivate cardPrivate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "GMT+3")
    private Date issueDateTime;
}
