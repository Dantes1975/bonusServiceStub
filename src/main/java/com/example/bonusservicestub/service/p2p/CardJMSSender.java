package com.example.bonusservicestub.service.p2p;

import org.springframework.stereotype.Service;

@Service
public interface CardJMSSender {
    void sendRequestGuid(String guid);

    void sendAdditionalRequestGuid();
}
