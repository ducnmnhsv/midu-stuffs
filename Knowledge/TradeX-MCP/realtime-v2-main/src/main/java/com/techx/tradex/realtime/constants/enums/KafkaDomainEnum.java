package com.techx.tradex.realtime.constants.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum KafkaDomainEnum {
    PAAVE("paave");
    private String key;
}
