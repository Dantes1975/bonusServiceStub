package com.example.bonusservicestub.service.phone;

import com.example.bonusservicestub.entity.phone.changeOper.ChangePhoneDetailsRequest;
import org.springframework.stereotype.Service;

@Service
public interface ChangePhoneDetailsSender {
    void sendChangePhoneDetailsResponse(ChangePhoneDetailsRequest request,
                                        String correlationID,
                                        Long timeToLive,
                                        String transactionID);
}
