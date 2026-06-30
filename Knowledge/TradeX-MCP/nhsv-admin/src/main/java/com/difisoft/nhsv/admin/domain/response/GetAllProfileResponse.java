package com.difisoft.nhsv.admin.domain.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class GetAllProfileResponse {
    private int total;
    private int pageNumber;
    private int pageSize;
    private List<Broker> brokers;

    @Data
    public static class Broker {
        private Long id;
        private String fullName;
        private String photo;
        private Boolean isDynamic;
        private Integer rank;
    }

    public static GetAllProfileResponse toGetAllProfileResponse(Page<com.difisoft.nhsv.admin.domain.Broker> brokers) {
        GetAllProfileResponse response = new GetAllProfileResponse();
        response.setTotal((int) brokers.getTotalElements());
        response.setPageNumber(brokers.getNumber());
        response.setPageSize(brokers.getSize());
        response.setBrokers(brokers.map(broker -> {
            Broker b = new Broker();
            b.setId(broker.getId());
            b.setFullName(broker.getFullname());
            b.setPhoto(broker.getPhoto() == null ? null : broker.getPhoto().replace(" ", "%20"));
            b.setIsDynamic(broker.getIsDynamic() == null ? false : broker.getIsDynamic());
            b.setRank(broker.getCurrentRank());
            return b;
        }).getContent());
        return response;
    }
}
