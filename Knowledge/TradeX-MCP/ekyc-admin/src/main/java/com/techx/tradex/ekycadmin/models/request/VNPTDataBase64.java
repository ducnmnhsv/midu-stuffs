package com.techx.tradex.ekycadmin.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VNPTDataBase64 {
    private Img imgs;
    private String message;
    @JsonProperty("server_version")
    private String serverVersion;
    private VNPTObject object;
    @JsonProperty("status_code")
    private int statusCode;
    @JsonProperty("challenge_code")
    private String challengeCode;

    @Data
    public static class Img {
        @JsonProperty("img_back")
        private String imgBack;
        @JsonProperty("img_front")
        private String imgFront;
    }

    @Data
    public static class VNPTObject {
        @JsonProperty("origin_location")
        private String originLocation;
        private String msg;
        @JsonProperty("cover_prob")
        private double nameProb;
        @JsonProperty("cover_prob_front")
        private double coverProbFront;
        @JsonProperty("back_type_id")
        private int backTypeId;
        @JsonProperty("address_fake_warning")
        private boolean addressFakeWarning;
        @JsonProperty("checking_result_back")
        private CheckingResult checkingResultBack;
        @JsonProperty("nation_policy")
        private String nationPolicy;
//        @JsonProperty("general_warning")
//        private List<String> generalWarning;
        private String features;
        @JsonProperty("dupplication_warning")
        private boolean dupplicationWarning;
        @JsonProperty("quality_back")
        private Quality qualityBack;
        @JsonProperty("checking_result_front")
        private CheckingResult checkingResultFront;
        @JsonProperty("back_corner_warning")
        private String backCornerWarning;
        private String id;
        @JsonProperty("back_expire_warning")
        private String backExpireWarning;
        @JsonProperty("msg_back")
        private String msgBack;
        @JsonProperty("birth_day_prob")
        private double birthDayProb;
        @JsonProperty("recent_location")
        private String recentLocation;
        @JsonProperty("id_fake_warning")
        private String idFakeWarning;
//        @JsonProperty("name_probs")
//        private List<Double> nameProbs;
        @JsonProperty("issue_date_prob")
        private double issueDateProb;
        @JsonProperty("citizen_id")
        private String citizenId;
        @JsonProperty("recent_location_prob")
        private double recentLocationProb;
        @JsonProperty("issue_place_prob")
        private double issuePlaceProb;
        private String nationality;
        private String name;
        private String gender;
        @JsonProperty("name_fake_warning_prob")
        private double nameFakeWarningProb;
        @JsonProperty("expire_warning")
        private String expireWarning;
//        @JsonProperty("issue_date_probs")
//        private List<Double> issueDateProbs;
        @JsonProperty("valid_date_prob")
        private double validDateProb;
        @JsonProperty("origin_location_prob")
        private double originLocationProb;
        @JsonProperty("corner_warning")
        private String cornerWarning;
        @JsonProperty("mrz_valid_score")
        private int mrzValidScore;
        @JsonProperty("valid_date")
        private String validDate;
        @JsonProperty("issue_date")
        private String issueDate;
        @JsonProperty("id_fake_prob")
        private double idFakeProb;
        @JsonProperty("mrz_prob")
        private double mrzProb;
//        @JsonProperty("id_probs")
//        private List<Double> idProbs;
        @JsonProperty("citizen_id_prob")
        private double citizenIdProb;
        @JsonProperty("dob_fake_warning_prob")
        private double dobFakeWarningProb;
        @JsonProperty("features_prob")
        private double featuresProb;
        @JsonProperty("issue_place")
        private String issuePlace;
        @JsonProperty("dob_fake_warning")
        private boolean dobFakeWarning;
        @JsonProperty("name_fake_warning")
        private String nameFakeWarning;
        @JsonProperty("type_id")
        private int typeId;
//        @JsonProperty("mrz_probs")
//        private List<Double> mrzProbs;
        @JsonProperty("card_type")
        private String cardType;
        @JsonProperty("quality_front")
        private Quality qualityFront;
        @JsonProperty("match_front_back")
        private MatchFrontBack matchFrontBack;
        @JsonProperty("birth_day")
        private String birthDay;
//        private List<String> mrz;
        @JsonProperty("issuedate_fake_warning")
        private boolean issuedateFakeWarning;
        private Tampering tampering;
    }

    @Data
    public static class CheckingResult {
        @JsonProperty("corner_cut_result")
        private String cornerCutResult;
        @JsonProperty("edited_prob")
        private double editedProb;
        @JsonProperty("recaptured_result")
        private String recapturedResult;
        @JsonProperty("check_photocopied_result")
        private String checkPhotocopiedResult;
//        @JsonProperty("corner_cut_prob")
//        private List<Double> cornerCutProb;
        @JsonProperty("edited_result")
        private double editedResult;
        @JsonProperty("recaptured_prob")
        private double recapturedProb;
    }

    @Data
    public static class Quality {
        @JsonProperty("blur_score")
        private double blurScore;
        @JsonProperty("bright_spot_param")
        private BrightSpotParam brightSpotParam;
        @JsonProperty("luminance_score")
        private double luminanceScore;
        @JsonProperty("final_result")
        private FinalResult finalResult;
        @JsonProperty("bright_spot_score")
        private double brightSpotScore;
//        @JsonProperty("resolution")
//        private List<Integer> resolution;
    }

    @Data
    public static class BrightSpotParam {
        @JsonProperty("average_intensity")
        private double averageIntensity;
        @JsonProperty("bright_spot_threshold")
        private double brightSpotThreshold;
        @JsonProperty("total_bright_spot_area")
        private int totalBrightSpotArea;
    }

    @Data
    public static class FinalResult {
        @JsonProperty("bad_luminance_likelihood")
        private String badLuminanceLikelihood;
        @JsonProperty("low_resolution_likelihood")
        private String lowResolutionLikelihood;
        @JsonProperty("blurred_likelihood")
        private String blurredLikelihood;
        @JsonProperty("bright_spot_likelihood")
        private String brightSpotLikelihood;
    }

    @Data
    public static class MatchFrontBack {
        @JsonProperty("match_sex")
        private String matchSex;
        @JsonProperty("match_bod")
        private String matchBod;
        @JsonProperty("match_id")
        private String matchId;
        @JsonProperty("match_valid_date")
        private String matchValidDate;
        @JsonProperty("match_name")
        private String matchName;
    }

    @Data
    public static class Tampering {
        @JsonProperty("is_legal")
        private String isLegal;
//        private List<String> warning;
    }
}
