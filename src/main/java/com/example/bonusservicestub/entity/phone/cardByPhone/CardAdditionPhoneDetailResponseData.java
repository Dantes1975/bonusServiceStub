package com.example.bonusservicestub.entity.phone.cardByPhone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardAdditionPhoneDetailResponseData {

    @JsonProperty("cards")
    private List<CardAdditionPhone> cards;

}
