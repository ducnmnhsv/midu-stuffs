package com.difisoft.nhsv.admin.domain.request;

import lombok.Data;

@Data
public class GetAllChatRoomRequest {
    private String keyword;
    private Integer pageNumber = 0;
    private Integer pageSize = 20;
}
