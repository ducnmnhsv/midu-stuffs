package com.difisoft.nhsv.admin.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ChatRoomImageDTO implements Serializable {
    private Long id;
    private String groupName;
    private MultipartFile file;

}
