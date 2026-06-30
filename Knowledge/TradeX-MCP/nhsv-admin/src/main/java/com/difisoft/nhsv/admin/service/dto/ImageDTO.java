package com.difisoft.nhsv.admin.service.dto;

import com.difisoft.nhsv.admin.constant.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ImageDTO implements Serializable {
    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;
    private MultipartFile photo;

}
