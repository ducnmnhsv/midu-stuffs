package com.techx.tradex.ekycadmin.models.ttl;

import com.techx.tradex.ekycadmin.domain.EKyc;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


@Data
public class OpenAccountRes extends TTLRes {
    private String mainResult;
    private String clientID;
    private boolean registeredEqtAcc;
    private boolean registeredFnoAcc;
}
