package com.techx.tradex.ekycadmin.models.ttl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.service.TTlBankService;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class OpenAccountReq extends TtlOperatorReq {

    private static final DateTimeFormatter ttlDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String firstName;
    /*
            """1"": ""ID Certificate"",
            ""2"": ""Trading Code"",
            ""3"": ""Business Registration License"",
            ""4"": ""Others"",
            ""5"": ""Passport"""
     */
    private String typeID;
    private String numberID; // số cmnd, cccd
    private String baseCCY = "VND";
    private String placeIssue = "VN"; // VN. nếu là nước ngoài thì sao?
    private String dateIssue; // định dạng là: yyyy-MM-dd
    private String cityIssue; //
    private String registrationType = "1"; // ko hỗ trợ người nước ngoài
    private String accountType = "N";
    private String countryOfResidence = "VN"; // ko ho tro nguoi nuoc ngoai
    private String sex; // F, M, ''
    private String nationID = "VN";
    private String birthday;
    private String address;
    private String mobile;
    private String email;
    private String language;
    private String isApproval = "N"; // auto approval?
    private boolean existAgent = false; // not yet support agent
    private AgentInfo agentInfo = null; // not yet support agent
    private String subAccountName;
    private List<String> listRegisServices; // "X" , "M"

    @JsonProperty("isEKYC")
    private boolean isEKYC = true;

    private List<BankItem> bankList;
    private String aeID;
    private String clientIntroducer;
    private String fileName = "chuki_image;";
    private String description = "SIGN;";
    private String fileValue = "";
    private String inputCusChannelID = "";

    @Data
    public static class BankItem {

        private String bankID;
        private String bankPlaceIssue;
        private String bankName;
        private String bankAcID;
    }

    @Data
    public static class AgentInfo {

        private String title;
        private String firstName;
        private String status;
        private String typeID;
        private String numberID;
        private String placeIssue;
        private String cityIssue;
        private String dateIssue;
        private String attorney;
        private String mobile;
        private String address;
        private String country;
    }

    private String convertDateToTTL(String dateOnly) {
        return String.format("%s-%s-%s", dateOnly.substring(0, 4), dateOnly.substring(4, 6), dateOnly.substring(6));
    }

    private String convertDateToTTL(LocalDate dateOnly) {
        return ttlDateTimeFormatter.format(dateOnly);
    }

    private String convertImagetoBase64(String path, Integer width, Integer height, Float quality) {
        String encodedfile = null;
        try {
            URL url = new URL(path);
            BufferedImage img = ImageIO.read(url);
            Dimension origin = new Dimension(img.getWidth(), img.getHeight());
            Dimension bond = new Dimension(width, height);
            Dimension newD = getScaledDimension(origin, bond);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Image resultingImage = img.getScaledInstance(newD.width, newD.height, Image.SCALE_DEFAULT);
            BufferedImage outputImage = new BufferedImage(newD.width, newD.height, BufferedImage.TYPE_INT_RGB);
            outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
            ImageWriter writer = ImageIO.getImageWritersBySuffix("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            IIOImage image = new IIOImage(outputImage, new ArrayList<>(), null);
            writer.setOutput(ImageIO.createImageOutputStream(bos));
            writer.write(null, image, param);
            byte[] imageBytes = bos.toByteArray();
            encodedfile = Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new GeneralException("CAN_NOT_LOAD_SIGNATURE_IMAGE_URL").source(e);
        }
        return encodedfile;
    }

    public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }
        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }
        return new Dimension(new_width, new_height);
    }

    public OpenAccountReq update(EKyc req, TTlBankService tTlBankService, AppConf appConf) {
        this.setFirstName(req.getFullName());
        this.setTypeID("1");
        this.setNumberID(req.getIdentifierId());
        this.dateIssue = convertDateToTTL(req.getIssueDate());
        //        this.cityIssue = req.getIssuePlace();
        if ("male".equalsIgnoreCase(req.getGender()) || "nam".equalsIgnoreCase(req.getGender()) || "m".equalsIgnoreCase(req.getGender())) {
            this.sex = "M";
        } else if (
            "female".equalsIgnoreCase(req.getGender()) || "nữ".equalsIgnoreCase(req.getGender()) || "f".equalsIgnoreCase(req.getGender())
        ) {
            this.sex = "F";
        } else {
            if (appConf.isEnableRequireGender()) {
                throw new GeneralException("INVALID_GENDER_VALUE");
            }
            this.sex = "";
        }
        this.birthday = convertDateToTTL(req.getBirthDay());
        Pattern pattern = appConf.getAddressRegexPattern();
        if (pattern != null) {
            if (!pattern.matcher(req.getAddress()).matches()) {
                throw new GeneralException("INVALID_ADDRESS_VALUE");
            }
        }
        this.address = req.getAddress();
        this.mobile = req.getPhoneNo();
        this.email = req.getEmail();
        this.language = "V";
        this.subAccountName = req.getAccountName();
        this.listRegisServices = Arrays.asList("X");
        if (req.getIsMargin() != null && req.getIsMargin()) {
            this.listRegisServices.add("M");
        }
        if (!StringUtils.isEmpty(req.getReferrerIdName())) {
            this.aeID = req.getReferrerIdName();
            this.clientIntroducer = req.getReferrerIdName();
        }
        if (
            !StringUtils.isEmpty(req.getBankAccount()) &&
            !StringUtils.isEmpty(req.getBankName()) &&
            !StringUtils.isEmpty(req.getBranch()) &&
            !StringUtils.isEmpty(req.getAccountName())
        ) {
            this.bankList = new ArrayList<>();
            BankItem bankItem = new BankItem();
            if (req.getBranchId() != null) {
                ListBankBranchResponse.Item branch = tTlBankService.findBranch(req.getBranch());
                bankItem.setBankPlaceIssue(req.getBranchId());
                bankItem.setBankID(branch.getBankID());
                bankItem.setBankName(req.getBankName());
            } else {
                ListBankBranchResponse.Item branch = tTlBankService.findBranch(req.getBankName(), req.getBranch());
                if (branch == null) {
                    throw new GeneralException("No bank branch founded");
                }
                bankItem.setBankPlaceIssue(branch.getBankBranch());
                bankItem.setBankID(branch.getBankID());
                bankItem.setBankName(req.getBankName());
            }
            bankItem.setBankAcID(req.getBankAccount());
            this.bankList.add(bankItem);
        }
        if (appConf.isEnableAddSignatureToCore()) {
            this.fileValue =
                convertImagetoBase64(
                    req.getSignatureImageUrl(),
                    appConf.getResizeSignature().getWidth(),
                    appConf.getResizeSignature().getHeigth(),
                    appConf.getResizeSignature().getQuality()
                );
        }
        if (req.getChannelId() != null) {
            this.inputCusChannelID = req.getChannelId();
        }
        return this;
    }
}
