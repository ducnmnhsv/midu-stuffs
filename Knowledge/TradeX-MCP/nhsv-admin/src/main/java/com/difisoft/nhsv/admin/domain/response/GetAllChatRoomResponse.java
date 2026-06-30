package com.difisoft.nhsv.admin.domain.response;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

import com.difisoft.nhsv.admin.domain.CreatedChatRoom;

import lombok.Data;

@Data
public class GetAllChatRoomResponse {
    private int totalChatRooms;
    private int currentPage;
    private int pageSize;
    private List<ChatRooms> chatRooms;

    @Data
    public static class ChatRooms {
        private Long id;
        private String groupName;
        private String introduction;
        private String photo;
        private Long totalView;
        private BrokerInfo brokerInfo;
        private List<SocialLinks> socialLinks;
    }

    @Data
    public static class BrokerInfo {
        private Long brokerId;
        private String brokerName;
        private String brokerPhoto;
    }

    @Data
    public static class SocialLinks {
        private String type;
        private String link;
    }

    public static GetAllChatRoomResponse toGetAllChatRoomResponse(Page<CreatedChatRoom> page) {
        GetAllChatRoomResponse response = new GetAllChatRoomResponse();
        response.setTotalChatRooms((int) page.getTotalElements());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        List<ChatRooms> chatRooms = new ArrayList<>();
        page.getContent().forEach(chatRoom -> {
            ChatRooms chatRoomResponse = new ChatRooms();
            chatRoomResponse.setId(chatRoom.getId());
            chatRoomResponse.setGroupName(chatRoom.getGroupName());
            chatRoomResponse.setIntroduction(chatRoom.getIntroduction());
            chatRoomResponse.setPhoto(chatRoom.getPhoto() == null ? null : chatRoom.getPhoto().replace(" ", "%20"));
            BrokerInfo brokerInfo = new BrokerInfo();
            brokerInfo.setBrokerId(chatRoom.getBrokerId());
            brokerInfo.setBrokerName(chatRoom.getGroupOwner());
            brokerInfo.setBrokerPhoto(
                    chatRoom.getBrokerPhoto() == null ? null
                            : chatRoom.getBrokerPhoto().replace(" ", "%20"));
            chatRoomResponse.setBrokerInfo(brokerInfo);
            chatRoomResponse.setTotalView(chatRoom.getTotalView());
            List<SocialLinks> socialLinks = new ArrayList<>();
            if (chatRoom.getSocialLinks() != null && chatRoom.getSocialLinks().size() > 0) {
                chatRoom.getSocialLinks().forEach(socialLink -> {
                    SocialLinks socialLinkResponse = new SocialLinks();
                    socialLinkResponse.setType(socialLink.getType());
                    socialLinkResponse.setLink(socialLink.getLink());
                    socialLinks.add(socialLinkResponse);
                });
            }
            chatRoomResponse.setSocialLinks(socialLinks);
            chatRooms.add(chatRoomResponse);
        });
        response.setChatRooms(chatRooms);
        return response;
    }
}
