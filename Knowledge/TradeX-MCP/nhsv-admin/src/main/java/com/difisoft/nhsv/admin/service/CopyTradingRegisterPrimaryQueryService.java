package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.CopyTradingRegister;
import com.difisoft.nhsv.admin.domain.CopyTradingRegister_;
import com.difisoft.nhsv.admin.repository.CopyTradingRegisterRepository;
import com.difisoft.nhsv.admin.service.criteria.CopyTradingRegisterPrimaryCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyTradingRegisterDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyTradingRegisterMapper;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@Transactional(readOnly = true)
@Primary
public class CopyTradingRegisterPrimaryQueryService extends QueryService<CopyTradingRegister> {
    private final Logger log = LoggerFactory.getLogger(CopyTradingRegisterPrimaryQueryService.class);
    private final CopyTradingRegisterRepository copyTradingRegisterRepository;
    private final CopyTradingRegisterMapper copyTradingRegisterMapper;

    public CopyTradingRegisterPrimaryQueryService(CopyTradingRegisterRepository copyTradingRegisterRepository, CopyTradingRegisterMapper copyTradingRegisterMapper) {
        this.copyTradingRegisterRepository = copyTradingRegisterRepository;
        this.copyTradingRegisterMapper = copyTradingRegisterMapper;
    }

    @Transactional(readOnly = true)
    public List<CopyTradingRegisterDTO> findByCriteria(CopyTradingRegisterPrimaryCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopyTradingRegister> specification = createSpecification(criteria);
        return copyTradingRegisterMapper.toDto(copyTradingRegisterRepository.findAll(specification));
    }

    @Transactional(readOnly = true)
    public Page<CopyTradingRegister> findByCriteria(CopyTradingRegisterPrimaryCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopyTradingRegister> specification = createSpecification(criteria);
        return copyTradingRegisterRepository.findAll(specification, page);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(CopyTradingRegisterPrimaryCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopyTradingRegister> specification = createSpecification(criteria);
        return copyTradingRegisterRepository.count(specification);
    }

    protected Specification<CopyTradingRegister> createSpecification(CopyTradingRegisterPrimaryCriteria criteria) {
        Specification<CopyTradingRegister> specification = Specification.where(null);
        if (criteria != null) {

            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopyTradingRegister_.id));
            }
            if (criteria.getAccountNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAccountNumber(), CopyTradingRegister_.accountNumber));
            }
            if (criteria.getSubAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSubAccount(), CopyTradingRegister_.subAccount));
            }
            if (criteria.getCustomerName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCustomerName(), CopyTradingRegister_.customerName));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), CopyTradingRegister_.status));
            }
            if (criteria.getCreateAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreateAt(), CopyTradingRegister_.createAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), CopyTradingRegister_.updatedAt));
            }
        }
        return specification;
    }

    public ByteArrayInputStream exportChatRoomsToExcel(
        List<CopyTradingRegister> copyTradingRegisters,
        LocalDate startDate,
        LocalDate endDate,
        Boolean status,
        String accountNumber,
        String subAccount) {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("CopyTradingRegisters");

            CellStyle borderedCellStyle = workbook.createCellStyle();
            borderedCellStyle.setBorderBottom(BorderStyle.THIN);
            borderedCellStyle.setBorderTop(BorderStyle.THIN);
            borderedCellStyle.setBorderLeft(BorderStyle.THIN);
            borderedCellStyle.setBorderRight(BorderStyle.THIN);

            Row titleRow0 = sheet.createRow(0);
            Cell titleCell0 = titleRow0.createCell(0);
            titleCell0.setCellValue(String.format("Từ ngày %s - Đến ngày %s",
                startDate != null ? startDate.toString() : "N/A",
                endDate != null ? endDate.toString() : "N/A"));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell0.setCellStyle(titleStyle);

            Row titleRow1 = sheet.createRow(1);
            Cell titleCell1 = titleRow1.createCell(0);

            String accountText = String.format("Tài khoản: %s", accountNumber != null ? accountNumber : "ALL");
            String subText = String.format("Sub: %s", subAccount != null ? subAccount : "ALL");
            String statusText = String.format("Trạng thái: %s", status != null ? (status ? "Thành công" : "Thất bại") : "ALL");

            titleCell1.setCellValue(String.format("%-30s %-30s %-30s", accountText, subText, statusText));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

            CellStyle infoStyle = workbook.createCellStyle();
            infoStyle.setAlignment(HorizontalAlignment.LEFT);
            titleCell1.setCellStyle(infoStyle);

            Row headerRow = sheet.createRow(2);
            String[] headers = {"ID", "Thời gian đăng kí", "Số tài khoản", "Tên KH", "Trạng thái"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(borderedCellStyle);
            }

            // Dữ liệu
            int rowNum = 3;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (CopyTradingRegister copyTradingRegister : copyTradingRegisters) {
                Row row = sheet.createRow(rowNum++);
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(copyTradingRegister.getId());
                cell0.setCellStyle(borderedCellStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(copyTradingRegister.getCreateAt().format(formatter));
                cell1.setCellStyle(borderedCellStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(copyTradingRegister.getAccountNumber());
                cell2.setCellStyle(borderedCellStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(copyTradingRegister.getCustomerName());
                cell3.setCellStyle(borderedCellStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(copyTradingRegister.getStatus() == null || !copyTradingRegister.getStatus() ? "Thất bại" : "Thành công");
                cell4.setCellStyle(borderedCellStyle);
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return new ByteArrayInputStream(out.toByteArray());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage());
        }
    }
}
