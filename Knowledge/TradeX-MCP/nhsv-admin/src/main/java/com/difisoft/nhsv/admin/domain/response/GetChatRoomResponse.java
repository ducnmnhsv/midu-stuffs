package com.difisoft.nhsv.admin.domain.response;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.domain.CreatedChatRoom;
import com.difisoft.nhsv.admin.domain.SocialLink;

import lombok.Data;

@Data
public class GetChatRoomResponse {
    private String name;
    private String introduction;
    private String photo;
    private String contact;
    private Long totalView;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private BrokerInfo brokerInfo;
    private List<SocialLinks> socialLinks;

    @Data
    private static class BrokerInfo {
        private Long brokerId;
        private String email;
        private String fullName;
        private Boolean isDynamic;
        private String photo;
    }

    @Data
    private static class SocialLinks {
        private String type;
        private String link;
    }

    public static GetChatRoomResponse toGetChatRoomResponse(CreatedChatRoom chatRoom, Broker broker) {
        GetChatRoomResponse getChatRoomResponse = new GetChatRoomResponse();
        getChatRoomResponse.setName(chatRoom.getGroupName());
        getChatRoomResponse.setIntroduction(chatRoom.getIntroduction());
        getChatRoomResponse.setPhoto(chatRoom.getPhoto() == null ? null : chatRoom.getPhoto().replace(" ", "%20"));
        getChatRoomResponse.setContact(chatRoom.getBrokerContact());
        getChatRoomResponse.setTotalView(chatRoom.getTotalView());
        getChatRoomResponse.setCreatedAt(chatRoom.getCreatedAt());
        getChatRoomResponse.setUpdatedAt(chatRoom.getUpdatedAt());
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setBrokerId(broker.getId());
        brokerInfo.setEmail(broker.getEmail());
        brokerInfo.setFullName(broker.getFullname());
        brokerInfo.setIsDynamic(broker.getIsDynamic());
        brokerInfo.setPhoto(broker.getPhoto() == null ? null : broker.getPhoto().replace(" ", "%20"));
        getChatRoomResponse.setBrokerInfo(brokerInfo);
        List<SocialLinks> socialLinksList = new ArrayList<>();
        for (SocialLink socialLink : chatRoom.getSocialLinks()) {
            SocialLinks socialLinks = new SocialLinks();
            socialLinks.setType(socialLink.getType());
            socialLinks.setLink(socialLink.getLink());
            socialLinksList.add(socialLinks);
        }
        getChatRoomResponse.setSocialLinks(socialLinksList);
        return getChatRoomResponse;
    }
}