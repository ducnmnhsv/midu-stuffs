package com.techx.tradex.ekycadmin.models.lotte;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techx.tradex.common.utils.StringUtils;
import com.techx.tradex.ekycadmin.models.enums.LotteLangCode;
import com.techx.tradex.ekycadmin.models.request.EKycAddReq;
import lombok.Data;
import org.apache.commons.lang3.EnumUtils;

@Data
public class LotteEKycUpdateAccountReq extends LotteReq {

    @JsonProperty("seq_no")
    private String seqNo;

    @JsonProperty("cust_nm")
    private String custNm;

    @JsonProperty("idno")
    private String idno;

    @JsonProperty("birth_dt")
    private String birthDt;

    @JsonProperty("sex_tp")
    private String sexTp; // 1: Nam, 2: Nữ

    @JsonProperty("idno_iss_dt")
    private String idnoIssDt;

    @JsonProperty("idno_iss_orga")
    private String idnoIssOrga;

    @JsonProperty("home_addr")
    private String homeAddr;

    @JsonProperty("conct_addr")
    private String conctAddr;

    @JsonProperty("brch_cd")
    private String brchCd;

    @JsonProperty("acnt_mrgn_tp")
    private String acntMrgnTp; // Đăng ký mở tài khoản giao dịch ký quỹ - 1:Không, 2:Có

    @JsonProperty("trd_onl_yn")
    private String trdOnlyn; // Giao dịch qua internet - Y:Có, N:Không

    @JsonProperty("cert_tp")
    private String certTp; // Đăng ký phương thức xác thực - 1: OTP (disable: trd_onl_yn === N), 2: Token

    @JsonProperty("otp_recv_tp")
    private String otpRecvTp; // Phương thức nhận thẻ OTP - Y: email (disable: trd_onl_yn === N), N: Chuyển phát nhanh (disable: trd_onl_yn === N)

    @JsonProperty("auto_pia_tp")
    private String autoPiaTp; // Ứng trước tiền bán chứng khoán - Y: Có, N: Không

    @JsonProperty("sms_tp")
    private String smsTp; // Phương thức nhận thông báo SMS - 1: SMS cơ bản, 2: SMS nâng cao

    @JsonProperty("email_yn")
    private String emailYn; // Phương thức nhận thông báo Email - Y: Có, N: Không

    @JsonProperty("notif_yn")
    private String notifYn; // Phương thức nhận thông báo App - Y: Có, N: Không

    @JsonProperty("bank_cd_off_1")
    private String bankCdOff1;

    @JsonProperty("bank_acnt_no_1")
    private String bankAcntNo1;

    @JsonProperty("bank_acnt_nm_1")
    private String bankAcntNm1;

    @JsonProperty("bank_brch_cd_1")
    private String bankBrchCd1;

    @JsonProperty("bank_cd_off_2")
    private String bankCdOff2;

    @JsonProperty("bank_acnt_no_2")
    private String bankAcntNo2;

    @JsonProperty("bank_acnt_nm_2")
    private String bankAcntNm2;

    @JsonProperty("bank_brch_cd_2")
    private String bankBrchCd2;

    @JsonProperty("bank_cd_off_3")
    private String bankCdOff3;

    @JsonProperty("bank_acnt_no_3")
    private String bankAcntNo3;

    @JsonProperty("bank_acnt_nm_3")
    private String bankAcntNm3;

    @JsonProperty("bank_brch_cd_3")
    private String bankBrchCd3;

    @JsonProperty("bank_cd_off_4")
    private String bankCdOff4;

    @JsonProperty("bank_acnt_no_4")
    private String bankAcntNo4;

    @JsonProperty("bank_acnt_nm_4")
    private String bankAcntNm4;

    @JsonProperty("bank_brch_cd_4")
    private String bankBrchCd4;

    @JsonProperty("rcm_emp_no_tp")
    private String rcmEmpNoTp; // Người giới thiệu - 1: Nhân viên/CTV, 2: Khách hàng, 3: Quảng cáo, 4: Khác (disable nếu có partner)

    @JsonProperty("ifno_idno")
    private String ifnoIdno; // Id của partner - ctvtimo: Timo, ctvvpb: VPB, woori1: Worri 1, woori2: Worri 2, accesstrade: Accesstrade

    @JsonProperty("mng_emp_yn")
    private String mngEmpYn; // Lựa chọn mô hình quản lý tài khoản (disable nếu có partner) - Y: Có nhân viên chăm sóc tài khoản, N: Không có

    @JsonProperty("mng_emp_no")
    private String mngEmpNo; // Id của partner - ctvtimo: Timo, ctvvpb: VPB, woori1: Worri 1, woori2: Worri 2, accesstrade: Accesstrade

    @JsonProperty("fatca_yn")
    private String fatcaYn; // Tôi không thuộc các trường hợp là công dân Mỹ hoặc đối tượng cư trú tại Mỹ, có nơi sinh/địa chỉ nhận thư hoặc địa chỉ lưu trú/số điện thoại liên lạc/địa chỉ - Y: Không thuộc, N: Có thuộc

    @JsonProperty("vnpt_point")
    private String vnptPoint; // Điểm eKYC của VNPT - eKYC chấm

    @JsonProperty("track_id")
    private String trackId;

    @JsonProperty("tax_cd")
    private String taxCd;

    @JsonProperty("send_email_cntr_yn")
    private String sendEmailCntrYn;

    @JsonProperty("cli_mac_addr")
    private String cliMacAddr;

    @JsonProperty("idno_expr_dt")
    private String idnoExprDt;

    @JsonProperty("acnt_dr_tp")
    private String acntDrTp; // 1: not register derivatives, 2: register

    @JsonProperty("job_detail")
    private String jobDetail;


    public LotteEKycUpdateAccountReq update(EKycAddReq req, String langCode) throws Exception {
        this.setSeqNo(req.getEKycId());
        this.setCustNm(req.getFullName());
        this.setIdno(req.getIdentifierId());
        this.setBirthDt(req.getBirthDay());
        if ("male".equalsIgnoreCase(req.getGender()) || "nam".equalsIgnoreCase(req.getGender()) || "m".equalsIgnoreCase(req.getGender())) {
            this.setSexTp("1");
        } else if (
            "female".equalsIgnoreCase(req.getGender()) || "nữ".equalsIgnoreCase(req.getGender()) || "f".equalsIgnoreCase(req.getGender())
        ) {
            this.setSexTp("2");
        }
        this.setIdnoIssDt(req.getIssueDate());
        this.setIdnoIssOrga(req.getIssuePlace());
        this.setHomeAddr(req.getPermanentAddress());
        this.setConctAddr(req.getContactAddress());
        this.setBrchCd(req.getBranch());
        this.setAcntMrgnTp(req.getMarginInclued() ? "2" : "1");
        this.setTrdOnlyn(req.getOnlineTrading() ? "Y" : "N");
        if (req.getAuthenMethod().equals("otp")) {
            this.setCertTp("1");
        } else if (req.getAuthenMethod().equals("token")) {
            this.setCertTp("2");
        }
        if (req.getOtpReceiveMethod().equals("email")) {
            this.setOtpRecvTp("Y");
        } else if (req.getOtpReceiveMethod().equals("express")) {
            this.setOtpRecvTp("N");
        }
        this.setAutoPiaTp(req.getAdvancedCashIncluded() ? "Y" : "N");
        if (req.getSmsMethod().equals("basic")) {
            this.setSmsTp("1");
        } else if (req.getSmsMethod().equals("advanced")) {
            this.setSmsTp("2");
        }
        this.setEmailYn(req.getEmailNotification() ? "Y" : "N");
        this.setNotifYn("Y");
        for (int i = 0; i < req.getBankList().size(); i++) {
            EKycAddReq.BankList bank = req.getBankList().get(i);
            int bankIndex = i + 1; // Adjust index to start from 1
            String bankCdOff = "BankCdOff" + bankIndex;
            String bankAcntNo = "BankAcntNo" + bankIndex;
            String bankAcntNm = "BankAcntNm" + bankIndex;
            String bankBrchCd = "BankBrchCd" + bankIndex;
            this.getClass().getMethod("set" + bankCdOff, String.class).invoke(this, bank.getBankId());
            this.getClass().getMethod("set" + bankAcntNo, String.class).invoke(this, bank.getBankAccNo());
            this.getClass().getMethod("set" + bankAcntNm, String.class).invoke(this, bank.getOwnerName());
            this.getClass().getMethod("set" + bankBrchCd, String.class).invoke(this, bank.getBranchId());
        }
        this.setRcmEmpNoTp(req.getReferral());
        this.setIfnoIdno(req.getPartnerId());
        this.setMngEmpYn(req.getCustomerSupport() ? "Y" : "N");
        if (req.getCustomerSupport()) {
            this.setMngEmpNo(req.getCsPartnerId());
        } else {
            this.setMngEmpNo("");
        }
        this.setFatcaYn(req.getFatca() ? "Y" : "N");
        this.setVnptPoint(String.valueOf(req.getMatchingRate()));
        this.setTrackId("");
        this.setLangCode(EnumUtils.isValidEnum(LotteLangCode.class, langCode) ? LotteLangCode.valueOf(langCode).getCode() : LotteLangCode.vi.getCode());
        if (StringUtils.isNotEmpty(req.getTaxNo())) {
            this.setTaxCd(req.getTaxNo());
        } else {
            this.setTaxCd(req.getIdentifierId());
        }
        this.setSendEmailCntrYn("N");
        this.setCliMacAddr(req.getDeviceUniqueId());
        this.setIdnoExprDt(req.getExpireDate());
        this.setAcntDrTp("1");
        this.setJobDetail(" ");
        return this;
    }
}
