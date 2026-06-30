package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.domain.ChatRoom;
import com.difisoft.nhsv.admin.domain.CreatedChatRoom;

import org.springframework.stereotype.Service;

@Service
public class CreateChatRoomMapper {

    public static CreatedChatRoom toCreatedChatRoom(ChatRoom chatRoom, CreatedChatRoom createdChatRoom, Broker broker) {
        if (createdChatRoom == null) {
            createdChatRoom = new CreatedChatRoom();
            createdChatRoom.setTotalView(0L);
        }
        createdChatRoom.setId(chatRoom.getId());
        if (broker != null) {
            createdChatRoom.setBrokerId(broker.getId());
        }
        createdChatRoom.setGroupName(chatRoom.getGroupName());
        createdChatRoom.setGroupOwner(chatRoom.getGroupOwner());
        createdChatRoom.setIntroduction(chatRoom.getIntroduction());
        createdChatRoom.setPhoto(chatRoom.getPhoto());
        createdChatRoom.setBrokerContact(chatRoom.getBrokerContact());
        createdChatRoom.setStatus(chatRoom.getStatus());
        createdChatRoom.setCreatedBy(chatRoom.getCreatedBy());
        createdChatRoom.setCreatedAt(chatRoom.getCreatedAt());
        createdChatRoom.setUpdatedAt(chatRoom.getUpdatedAt());
        createdChatRoom.setApprovedAt(chatRoom.getApprovedAt());
        createdChatRoom.setApprovedBy(chatRoom.getApprovedBy());
        createdChatRoom.setBrokerName(chatRoom.getBrokerName());
        createdChatRoom.setBrokerPhoto(chatRoom.getBrokerPhoto());
        return createdChatRoom;
    }
}
