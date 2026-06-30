package com.difisoft.nhsv.admin.web.rest.controllers;


import com.difisoft.nhsv.admin.service.CopySubscriberCustomService;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.util.List;

@RestController
@RequestMapping("/api/copy-trading")
@RequiredArgsConstructor
public class CopySubscriberController {

    private final CopySubscriberCustomService copySubscriberCustomService;

    @GetMapping("/ml-subscribers")
    public ResponseEntity<List<CopySubscriberDTO>> getAllByMlId(
        @RequestParam(value = "mlID") Long mlID,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CopySubscriberDTO> pageResult = copySubscriberCustomService.findAllByMlId(mlID, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), pageResult);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }
}
