package com.difisoft.nhsv.admin.web.rest.controllers;

import com.difisoft.nhsv.admin.domain.response.GenericResponse;
import com.difisoft.nhsv.admin.security.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/v1")
public class PublicController {

    private final TokenProvider tokenProvider;

    @Autowired
    public PublicController(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/validation/token")
    public ResponseEntity<GenericResponse<Boolean>> validationToken(
        @RequestBody String token
    ) {
        GenericResponse<Boolean> response = GenericResponse.success("");
        response.setData(tokenProvider.validateToken(token));
        return ResponseEntity.ok(response);
    }
}
