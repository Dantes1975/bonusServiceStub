package com.example.bonusservicestub.service;

import org.springframework.stereotype.Service;

@Service
public interface CardJMSSender {
    void sendRequestGuid(String guid);
}
