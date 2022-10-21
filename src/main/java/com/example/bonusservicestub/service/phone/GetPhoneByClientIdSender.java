package com.example.bonusservicestub.service.phone;

import com.example.bonusservicestub.entity.phone.byClientId.AdditionPhoneGetDetailsRequest;
import org.springframework.stereotype.Service;

@Service
public interface GetPhoneByClientIdSender {
    void sendGetPhoneByClientIdResponse(AdditionPhoneGetDetailsRequest request,
                                        String correlationID,
                                        Long timeToLive,
                                        String transactionID);
}
