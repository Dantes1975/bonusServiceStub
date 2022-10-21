package com.example.bonusservicestub.service.phone;

import com.example.bonusservicestub.entity.phone.cardByPhone.CardAdditionPhoneDetailsRequest;
import org.springframework.stereotype.Service;

@Service
public interface CardAdditionPhoneSender {

    void sendCardAdditionPhoneDetailsResponse(CardAdditionPhoneDetailsRequest request,
                                              String correlationID,
                                              Long timeToLive,
                                              String transactionID);

}
