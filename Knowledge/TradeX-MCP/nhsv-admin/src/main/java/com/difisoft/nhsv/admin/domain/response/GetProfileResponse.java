package com.difisoft.nhsv.admin.domain.response;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.domain.CreatedChatRoom;

import lombok.Data;

@Data
public class GetProfileResponse {
    private String username;
    private String fullName;
    private String introduction;
    private String email;
    private String status;
    private String photo;
    private Integer rank;
    private Boolean isDynamic;
    private Long totalChatRooms;
    private Long totalChatRoomsViews;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deactivatedAt;
    private String deactivatedBy;
    private String invitedBy;
    private List<Chatroom> chatRooms;

    @Data
    private static class Chatroom {
        private Long id;
        private String groupName;
        private String introduction;
        private String photo;
        private Long totalView;
        private Set<SocialLinks> socialLinks;
    }

    @Data
    private static class SocialLinks {
        private String type;
        private String link;
    }

    public static GetProfileResponse toGetProfileResponse(Broker user, List<CreatedChatRoom> chatRoom) {
        GetProfileResponse getProfileResponse = new GetProfileResponse();
        getProfileResponse.setUsername(user.getUsername());
        getProfileResponse.setFullName(user.getFullname());
        getProfileResponse.setIntroduction(user.getIntroduction());
        getProfileResponse.setEmail(user.getEmail());
        getProfileResponse.setStatus(user.getStatus() ? "ACTIVE" : "DEACTIVATED");
        getProfileResponse.setCreatedAt(user.getCreatedAt());
        getProfileResponse.setUpdatedAt(user.getUpdatedAt());
        getProfileResponse.setDeactivatedAt(user.getDeactivatedAt());
        getProfileResponse.setDeactivatedBy(user.getDeactivatedBy());
        getProfileResponse.setInvitedBy(user.getInvitedBy());
        getProfileResponse.setRank(user.getCurrentRank());
        getProfileResponse.setIsDynamic(user.getIsDynamic());
        getProfileResponse.setTotalChatRooms(user.getTotalChatRoom());
        getProfileResponse.setTotalChatRoomsViews(user.getTotalViewdChatRoom());
        getProfileResponse.setPhoto(user.getPhoto() == null ? null : user.getPhoto().replace(" ", "%20"));
        List<Chatroom> chatRoomList = new ArrayList<>();
        if (chatRoom != null && !chatRoom.isEmpty()) {
            chatRoom.forEach(room -> {
                Chatroom chatRooms = new Chatroom();
                chatRooms.setId(room.getId());
                chatRooms.setGroupName(room.getGroupName());
                chatRooms.setIntroduction(room.getIntroduction());
                chatRooms.setPhoto(room.getPhoto());
                chatRooms.setTotalView(room.getTotalView());
                Set<SocialLinks> socialLinks = new HashSet<>();
                if (room.getSocialLinks() != null) {
                    room.getSocialLinks().forEach(link -> {
                        SocialLinks socialLink = new SocialLinks();
                        socialLink.setType(link.getType());
                        socialLink.setLink(link.getLink());
                        socialLinks.add(socialLink);
                    });
                }
                chatRooms.setSocialLinks(socialLinks);
                chatRoomList.add(chatRooms);
            });
        }
        getProfileResponse.setChatRooms(chatRoomList);
        return getProfileResponse;
    }
}
