package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.Authority;
import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.domain.ChatRoom;
import com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails;
import com.difisoft.nhsv.admin.domain.CreatedChatRoom;
import com.difisoft.nhsv.admin.domain.InviteUser;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.domain.enumeration.InviteStatusEnum;
import com.difisoft.nhsv.admin.repository.BrokerRepository;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderDetailsRepository;
import com.difisoft.nhsv.admin.repository.InviteUserRepository;
import com.difisoft.nhsv.admin.repository.UserRepository;
import com.difisoft.nhsv.admin.repository.primary.ChatRoomPrimaryRepository;
import com.difisoft.nhsv.admin.repository.primary.CreatedChatRoomPrimaryRepository;
import com.difisoft.nhsv.admin.repository.primary.InviteUserPrimaryRepository;
import com.difisoft.nhsv.admin.security.AuthoritiesConstants;
import com.difisoft.nhsv.admin.service.mapper.BrokerMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link InviteUser}.
 */
@Service
@Primary
public class InviteUserServiceImp extends InviteUserService {

    private final InviteUserPrimaryRepository inviteUserPrimaryRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final BrokerRepository brokerRepository;

    private final ChatRoomPrimaryRepository chatRoomRepository;

    private final CreatedChatRoomPrimaryRepository createdChatRoomRepository;

    private final CopyMarketLeaderDetailsRepository copyMarketLeaderDetailsRepository;

    private final Logger log = LoggerFactory.getLogger(InviteUserService.class);

    public InviteUserServiceImp(InviteUserRepository inviteUserRepository, UserRepository userRepository,
            InviteUserPrimaryRepository inviteUserPrimaryRepository,
            PasswordEncoder passwordEncoder, BrokerRepository brokerRepository,
            ChatRoomPrimaryRepository chatRoomRepository,
            CreatedChatRoomPrimaryRepository createdChatRoomRepository,
            CopyMarketLeaderDetailsRepository copyMarketLeaderDetailsRepository) {
        super(inviteUserRepository);
        this.userRepository = userRepository;
        this.inviteUserPrimaryRepository = inviteUserPrimaryRepository;
        this.passwordEncoder = passwordEncoder;
        this.brokerRepository = brokerRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.createdChatRoomRepository = createdChatRoomRepository;
        this.copyMarketLeaderDetailsRepository = copyMarketLeaderDetailsRepository;
    }

    public InviteUser save(InviteUser inviteUser) {
        log.info("Request to save InviteUser : {}", inviteUser);
        return inviteUserPrimaryRepository.save(inviteUser);
    }

    public InviteUser update(InviteUser inviteUser) {
        log.info("Request to update InviteUser : {}", inviteUser);
        return inviteUserPrimaryRepository.save(inviteUser);
    }

    public Optional<InviteUser> findOne(Long id) {
        log.info("Request to get InviteUser : {}", id);
        return inviteUserPrimaryRepository.findById(id);
    }

    @Transactional
    public Optional<User> completeCreateAccount(String fullName, String newPassword, String key) {
        log.info("Completing create account for user {}", key);
        InviteUser inviteUser = inviteUserPrimaryRepository.findOneByActivationKey(key)
                .filter(user -> user.getActivationDate().isAfter(ZonedDateTime.now().minus(1, ChronoUnit.DAYS)))
                .orElse(null);
        User user = new User();
        if (inviteUser.getCreatedId() != null) {
            user.setId(inviteUser.getCreatedId());
        }
        user.setActivated(true);
        user.setFullName(fullName);
        user.setLogin(inviteUser.getLogin());
        user.setEmail(inviteUser.getEmail());
        user.setLangKey(inviteUser.getLangKey());
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCreatedBy(inviteUser.getCreatedBy());
        user.setCreatedDate(ZonedDateTime.now());
        user.setLastModifiedBy(inviteUser.getCreatedBy());
        user.setLastModifiedDate(ZonedDateTime.now());
        user.setInvitedBy(inviteUser.getCreatedBy());
        String[] authorities = inviteUser.getAuthorities().split(",");
        Set<Authority> auths = new HashSet<>();
        for (String auth : authorities) {
            Authority authority = new Authority();
            authority.setName(auth);
            auths.add(authority);
        }
        user.setAuthorities(auths);
        User saveUser = userRepository.save(user);
        if (saveUser != null && saveUser.getAuthorities().contains(new Authority(AuthoritiesConstants.BROKER))) {
            Broker broker = brokerRepository.findById(saveUser.getId()).orElse(null);
            if (broker != null) {
                List<ChatRoom> chatRooms = chatRoomRepository.findByBrokerId(broker.getId());
                List<CreatedChatRoom> createdChatRooms = createdChatRoomRepository
                        .findAllByBrokerId(broker.getId());
                if (!fullName.equals(broker.getFullname())) {
                    for (ChatRoom chatRoom : chatRooms) {
                        chatRoom.setGroupOwner(fullName);
                    }
                    for (CreatedChatRoom createdChatRoom : createdChatRooms) {
                        createdChatRoom.setGroupOwner(fullName);
                    }
                }
                if (!user.getLogin().equals(broker.getUsername())) {
                    for (ChatRoom chatRoom : chatRooms) {
                        chatRoom.setBrokerName(user.getLogin());
                        chatRoom.setCreatedBy(user.getLogin());
                    }
                    for (CreatedChatRoom createdChatRoom : createdChatRooms) {
                        createdChatRoom.setBrokerName(user.getLogin());
                        createdChatRoom.setCreatedBy(user.getLogin());
                    }
                }
                chatRoomRepository.saveAll(chatRooms);
                createdChatRoomRepository.saveAll(createdChatRooms);
            }
            broker = BrokerMapper.toBroker(saveUser, broker);
            brokerRepository.save(broker);
        }
        if (saveUser != null && saveUser.getAuthorities().contains(new Authority(AuthoritiesConstants.MARKET_LEADER))) {
            CopyMarketLeaderDetails copyMarketLeaderDetails = new CopyMarketLeaderDetails();
            copyMarketLeaderDetails.setCreatedAt(ZonedDateTime.now());
            copyMarketLeaderDetails.setUpdatedAt(ZonedDateTime.now());
            copyMarketLeaderDetails.setType("COPY_TRADING");
            copyMarketLeaderDetails.setMlUserId(saveUser);
            copyMarketLeaderDetails.setKey("BE_MARKET_LEADER_DATE");
            copyMarketLeaderDetails.setLabel("MARKET_LEADER_SUMMARY_INFO");
            copyMarketLeaderDetails.setValue(ZonedDateTime.now().format(
                    DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)));
            copyMarketLeaderDetailsRepository.save(copyMarketLeaderDetails);
        }
        inviteUser.setActivationKey(null);
        inviteUser.setActivationDate(null);
        inviteUser.setStatus(InviteStatusEnum.ACCOUNT_CREATED);
        inviteUser.setUpdatedAt(ZonedDateTime.now());
        inviteUser.setCreatedId(saveUser.getId());
        inviteUserPrimaryRepository.save(inviteUser);
        return Optional.of(saveUser);
    }
}
