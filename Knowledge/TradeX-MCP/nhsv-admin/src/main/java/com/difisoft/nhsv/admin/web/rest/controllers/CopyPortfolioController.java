package com.difisoft.nhsv.admin.web.rest.controllers;

import com.difisoft.nhsv.admin.domain.request.PortfolioUploadRequest;
import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailsRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailsCustomService;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailsQueryService;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailsService;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailsDTO;
import com.difisoft.nhsv.admin.web.rest.CopyPortfolioDetailsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/copy-trading")
public class CopyPortfolioController extends CopyPortfolioDetailsResource {

    private static final String ENTITY_NAME = "copyPortfolio";
    private final CopyPortfolioDetailsCustomService copyPortfolioDetailsCustomService;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    @Autowired
    public CopyPortfolioController(
        CopyPortfolioDetailsService copyPortfolioDetailsService
        , CopyPortfolioDetailsRepository copyPortfolioDetailsRepository
        , CopyPortfolioDetailsQueryService copyPortfolioDetailsQueryService
        , CopyPortfolioDetailsCustomService copyPortfolioDetailsCustomService
    ) {
        super(copyPortfolioDetailsService, copyPortfolioDetailsRepository, copyPortfolioDetailsQueryService);
        this.copyPortfolioDetailsCustomService = copyPortfolioDetailsCustomService;
    }

    @GetMapping("/portfolio-detail")
    public ResponseEntity<List<CopyPortfolioDetailsDTO>> getAllByMlId(
        @RequestParam(value = "mlID") @NotBlank Long mlID,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CopyPortfolioDetailsDTO> pageResult = copyPortfolioDetailsCustomService.findAllByMlId(mlID, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), pageResult);
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }

    @PostMapping("/portfolio/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> uploadMLPortfolio(
        @RequestBody @Valid PortfolioUploadRequest request
    ) {
        copyPortfolioDetailsCustomService.uploadPortfolio(request);
        return ResponseEntity
            .created(URI.create("/api/copy-trading/portfolio/upload"))
            .headers(HeaderUtil.createAlert(applicationName, "Upload portfolio success", "portfolioUpload"))
            .body(null);
    }
}
