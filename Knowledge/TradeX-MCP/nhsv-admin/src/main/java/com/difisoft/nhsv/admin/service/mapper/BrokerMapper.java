package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.service.dto.AdminUserDTO;

import org.springframework.stereotype.Service;

@Service
public class BrokerMapper {

    public static Broker toBroker(User user, Broker broker) {
        if (broker == null) {
            broker = new Broker();
            broker.setTotalChatRoom(0L);
            broker.setTotalViewdChatRoom(0L);
        }
        broker.setId(user.getId());
        broker.setUsername(user.getLogin());
        broker.setFullname(user.getFullName());
        broker.setIntroduction(user.getIntroduction());
        broker.setEmail(user.getEmail());
        broker.setStatus(user.isActivated());
        broker.setCreatedAt(user.getCreatedDate());
        broker.setUpdatedAt(user.getLastModifiedDate());
        broker.setDeactivatedAt(user.getDeactivatedAt());
        broker.setDeactivatedBy(user.getDeactivatedBy());
        broker.setInvitedBy(user.getInvitedBy());
        broker.setPhoto(user.getPhoto());
        if (broker.getId() != null) {
            broker.setIsPersisted();
        }
        return broker;
    }

    public static Broker toBroker(AdminUserDTO user, Broker broker) {
        if (broker == null) {
            broker = new Broker();
            broker.setTotalChatRoom(0L);
            broker.setTotalViewdChatRoom(0L);
        }
        broker.setId(user.getId());
        broker.setUsername(user.getLogin());
        broker.setFullname(user.getFullName());
        broker.setIntroduction(user.getIntroduction());
        broker.setEmail(user.getEmail());
        broker.setStatus(user.isActivated());
        broker.setCreatedAt(user.getCreatedDate());
        broker.setUpdatedAt(user.getLastModifiedDate());
        broker.setDeactivatedAt(user.getDeactivatedAt());
        broker.setDeactivatedBy(user.getDeactivatedBy());
        broker.setInvitedBy(user.getInvitedBy());
        broker.setPhoto(user.getPhotoLink());
        if (broker.getId() != null) {
            broker.setIsPersisted();
        }
        return broker;
    }
}
