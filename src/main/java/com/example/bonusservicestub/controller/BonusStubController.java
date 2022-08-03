package com.example.bonusservicestub.controller;

import com.example.bonusservicestub.entity.GuidFromPostman;
import com.example.bonusservicestub.service.CardJMSSender;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


import static lombok.AccessLevel.PRIVATE;

@RestController
@RequestMapping("/guid")
@Slf4j
public class BonusStubController {

    @Autowired
    @Getter(PRIVATE) private CardJMSSender cardJMSSender;

    @PostMapping(value = "/send", consumes = MediaType.ALL_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    public void confirming(@RequestBody GuidFromPostman request) {
        final String guid = request.getRequestGuid();
        log.info("Recieved reguestGuid: " + request.getRequestGuid());
        cardJMSSender.sendRequestGuid(guid);
    }

}
