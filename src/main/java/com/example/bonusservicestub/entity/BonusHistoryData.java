package com.example.bonusservicestub.entity;

import lombok.Data;

@Data
public class BonusHistoryData {
    private String systemID;
    private String channel;
    private String requestID;
    private String memberId;
    private String loyaltyId;
    private String startDate;
    private String endDate;
    private Integer pageNum;
    private Integer pointsCapacity;
}
