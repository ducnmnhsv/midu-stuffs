package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.file.FileService;
import com.difisoft.nhsv.admin.config.ApplicationProperties;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.domain.ChatRoom;
import com.difisoft.nhsv.admin.domain.CreatedChatRoom;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.domain.enumeration.ActionEnum;
import com.difisoft.nhsv.admin.domain.enumeration.GetAllProfileEnum;
import com.difisoft.nhsv.admin.domain.enumeration.StatusEnum;
import com.difisoft.nhsv.admin.domain.request.GetAllChatRoomRequest;
import com.difisoft.nhsv.admin.domain.request.GetAllProfileRequest;
import com.difisoft.nhsv.admin.domain.request.GetChatRoomRequest;
import com.difisoft.nhsv.admin.domain.request.GetProfileRequest;
import com.difisoft.nhsv.admin.domain.response.GetAllChatRoomResponse;
import com.difisoft.nhsv.admin.domain.response.GetAllProfileResponse;
import com.difisoft.nhsv.admin.domain.response.GetChatRoomResponse;
import com.difisoft.nhsv.admin.domain.response.GetProfileResponse;
import com.difisoft.nhsv.admin.repository.UserRepository;
import com.difisoft.nhsv.admin.repository.primary.BrokerPrimaryRepository;
import com.difisoft.nhsv.admin.repository.primary.ChatRoomPrimaryRepository;
import com.difisoft.nhsv.admin.repository.primary.CreatedChatRoomPrimaryRepository;
import com.difisoft.nhsv.admin.security.SecurityUtils;
import com.difisoft.nhsv.admin.service.criteria.BrokerCriteria;
import com.difisoft.nhsv.admin.service.dto.ChatRoomImageDTO;
import com.difisoft.nhsv.admin.service.mapper.CreateChatRoomMapper;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;

import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.StringFilter;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service Implementation for managing {@link ChatRoom}.
 */
@Service
@Transactional
@Primary
public class ChatRoomServiceImp {

    private final Logger log = LoggerFactory.getLogger(ChatRoomServiceImp.class);

    private final BrokerPrimaryRepository brokerRepository;

    private final UserRepository userRepository;

    private final CreatedChatRoomPrimaryRepository createdChatRoomRepository;

    private final MailService mailService;

    private final ChatRoomPrimaryRepository chatRoomRepository;

    private final BrokerQueryService brokerQueryService;

    private final FileService fileService;

    private final ApplicationProperties appConf;

    public ChatRoomServiceImp(
        BrokerPrimaryRepository brokerRepository,
        UserRepository userRepository,
        CreatedChatRoomPrimaryRepository createdChatRoomRepository,
        MailService mailService,
        ChatRoomPrimaryRepository chatRoomRepository,
        BrokerQueryService brokerQueryService,
        FileService fileService,
        ApplicationProperties appConf) {
        this.brokerRepository = brokerRepository;
        this.userRepository = userRepository;
        this.createdChatRoomRepository = createdChatRoomRepository;
        this.mailService = mailService;
        this.chatRoomRepository = chatRoomRepository;
        this.brokerQueryService = brokerQueryService;
        this.fileService = fileService;
        this.appConf = appConf;
    }

    public boolean checkGroupName(String groupName, Long id, String brokerName) {
        List<ChatRoom> existingChatRoom = chatRoomRepository.findByGroupNameAndBrokerName(groupName, brokerName);
        if (!existingChatRoom.isEmpty()) {
            for (ChatRoom chatRoom : existingChatRoom) {
                if (chatRoom.getAction().equals(ActionEnum.UPDATE) &&
                    (chatRoom.getStatus().equals(StatusEnum.APPROVED)
                        || chatRoom.getStatus().equals(StatusEnum.PENDING))) {
                    if (chatRoom.getId().equals(id)) {
                        return true;
                    } else {
                        throw new BadRequestAlertException(Constants.GROUP_NAME_EXISTS, "ChatRoom", "groupnameexists");
                    }
                }
                if (chatRoom.getAction().equals(ActionEnum.DELETE) &&
                    (chatRoom.getStatus().equals(StatusEnum.REJECTED)
                        || chatRoom.getStatus().equals(StatusEnum.PENDING))) {
                    if (chatRoom.getId().equals(id)) {
                        return true;
                    } else {
                        throw new BadRequestAlertException(Constants.GROUP_NAME_EXISTS, "ChatRoom", "groupnameexists");
                    }
                }
                if (chatRoom.getAction().equals(ActionEnum.CREATE) && chatRoom.getStatus().equals(StatusEnum.PENDING)) {
                    if (chatRoom.getId().equals(id)) {
                        return true;
                    } else {
                        throw new BadRequestAlertException(Constants.GROUP_NAME_EXISTS, "ChatRoom", "groupnameexists");
                    }
                }
            }
        }
        List<CreatedChatRoom> existingCreatedChatRoom = createdChatRoomRepository
            .findByGroupNameAndBrokerName(groupName, brokerName);
        if (!existingCreatedChatRoom.isEmpty()) {
            for (CreatedChatRoom createdChatRoom : existingCreatedChatRoom) {
                if (createdChatRoom.getStatus().equals(StatusEnum.APPROVED)) {
                    if (createdChatRoom.getId().equals(id)) {
                        return true;
                    } else {
                        throw new BadRequestAlertException(Constants.GROUP_NAME_EXISTS, "ChatRoom", "groupnameexists");
                    }
                }
            }
        }
        return true;
    }

    @Transactional
    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        String name = SecurityUtils.getCurrentUserLogin().get();
        this.checkGroupName(chatRoom.getGroupName(), null, name);
        User user = userRepository.findOneByLogin(name).orElse(null);
        chatRoom.setCreatedAt(ZonedDateTime.now());
        chatRoom.setUpdatedAt(ZonedDateTime.now());
        chatRoom.setCreatedBy(name);
        chatRoom.setBrokerName(name);
        if (user != null) {
            chatRoom.setGroupOwner(user.getFullName());
        }
        chatRoom.setGroupOwner(user.getFullName());
        chatRoom.setBrokerPhoto(user.getPhoto());
        chatRoom.action(ActionEnum.CREATE);
        chatRoom.status(StatusEnum.PENDING);
        ChatRoom res = chatRoomRepository.save(chatRoom);
        CreatedChatRoom createdChatRoom = createdChatRoomRepository.findById(res.getId()).orElse(null);
        Broker broker = brokerRepository.findByUsername(chatRoom.getBrokerName()).orElse(null);
        createdChatRoom = CreateChatRoomMapper.toCreatedChatRoom(chatRoom, createdChatRoom, broker);
        createdChatRoom.setStatus(StatusEnum.PENDING);
        createdChatRoomRepository.save(createdChatRoom);
        return res;
    }

    @Transactional
    public ChatRoom updateChatRoom(CreatedChatRoom createdChatRoom) {
        String name = SecurityUtils.getCurrentUserLogin().get();
        ChatRoom chatRoom = chatRoomRepository.findById(createdChatRoom.getId()).orElse(null);
        this.checkGroupName(createdChatRoom.getGroupName(), chatRoom.getId(), name);
        chatRoom.setGroupName(createdChatRoom.getGroupName());
        chatRoom.setGroupOwner(createdChatRoom.getGroupOwner());
        chatRoom.setIntroduction(createdChatRoom.getIntroduction());
        chatRoom.setBrokerContact(createdChatRoom.getBrokerContact());
        chatRoom.setPhoto(createdChatRoom.getPhoto());
        chatRoom.setSocialLinks(createdChatRoom.getSocialLinks());
        chatRoom.setUpdatedAt(ZonedDateTime.now());
        chatRoom.action(ActionEnum.UPDATE);
        chatRoom.status(StatusEnum.PENDING);
        return chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public ChatRoom approveCreateOrUpdate(ChatRoom chatRoom, String name) {
        chatRoom.setUpdatedAt(ZonedDateTime.now());
        chatRoom.status(StatusEnum.APPROVED);
        chatRoom.setApprovedAt(ZonedDateTime.now());
        chatRoom.setApprovedBy(name);
        chatRoom.setRejectReason(null);
        chatRoom.setRejectedAt(null);
        chatRoom.setRejectedBy(null);
        User user = userRepository.findOneByLogin(chatRoom.getCreatedBy()).orElse(null);
        if (user != null) {
            mailService.sendApproveMail(user, chatRoom);
        }
        CreatedChatRoom createdChatRoom = createdChatRoomRepository.findById(chatRoom.getId()).orElse(null);
        Broker broker = brokerRepository.findByUsername(chatRoom.getBrokerName()).orElse(null);
        createdChatRoom = CreateChatRoomMapper.toCreatedChatRoom(chatRoom, createdChatRoom, broker);
        createdChatRoomRepository.save(createdChatRoom);
        broker.setTotalChatRoom(broker.getTotalChatRoom() == null ? 1 : broker.getTotalChatRoom() + 1);
        brokerRepository.save(broker);
        ChatRoom res = chatRoomRepository.save(chatRoom);
        return res;
    }

    @Transactional
    public void approveDelete(ChatRoom chatRoom, String name) {
        chatRoom.setUpdatedAt(ZonedDateTime.now());
        chatRoom.status(StatusEnum.APPROVED);
        chatRoom.setApprovedAt(ZonedDateTime.now());
        chatRoom.setApprovedBy(name);
        chatRoom.setRejectReason(null);
        chatRoom.setRejectedAt(null);
        chatRoom.setRejectedBy(null);
        User user = userRepository.findOneByLogin(chatRoom.getCreatedBy()).orElse(null);
        if (user != null) {
            mailService.sendApproveMail(user, chatRoom);
        }
        Broker broker = brokerRepository.findByUsername(chatRoom.getBrokerName()).orElse(null);
        if (broker != null) {
            CreatedChatRoom createdChatRoom = createdChatRoomRepository.findById(chatRoom.getId()).orElse(null);
            broker.setTotalChatRoom(broker.getTotalChatRoom() - 1);
            Long totalview = createdChatRoom == null ? 0 : createdChatRoom.getTotalView();
            broker.setTotalViewdChatRoom(broker.getTotalViewdChatRoom() - totalview);
            brokerRepository.save(broker);
        }
        chatRoomRepository.save(chatRoom);
        createdChatRoomRepository.deleteById(chatRoom.getId());
    }

    @Transactional
    public ChatRoom rejectChatRoom(ChatRoom chatRoom, String name) {
        chatRoom.setUpdatedAt(ZonedDateTime.now());
        chatRoom.setStatus(StatusEnum.REJECTED);
        chatRoom.setRejectedAt(ZonedDateTime.now());
        chatRoom.setRejectedBy(name);
        chatRoom.setApprovedAt(null);
        chatRoom.setApprovedBy(null);
        User user = userRepository.findOneByLogin(chatRoom.getCreatedBy()).orElse(null);
        if (user != null) {
            mailService.sendRejectEmail(user, chatRoom);
        }
        CreatedChatRoom createdChatRoom = createdChatRoomRepository.findById(chatRoom.getId()).orElse(null);
        if (chatRoom.getAction().equals(ActionEnum.UPDATE)) {
            chatRoom.setGroupName(createdChatRoom.getGroupName());
            chatRoom.setIntroduction(createdChatRoom.getIntroduction());
            chatRoom.setGroupOwner(createdChatRoom.getGroupOwner());
            chatRoom.setBrokerName(createdChatRoom.getBrokerName());
            chatRoom.setIntroduction(createdChatRoom.getIntroduction());
            chatRoom.setPhoto(createdChatRoom.getPhoto());
            chatRoom.setSocialLinks(createdChatRoom.getSocialLinks());
        }
        ChatRoom res = chatRoomRepository.save(chatRoom);
        createdChatRoom.setUpdatedAt(ZonedDateTime.now());
        if (chatRoom.getAction().equals(ActionEnum.CREATE)) {
            createdChatRoom.setStatus(StatusEnum.REJECTED);
            createdChatRoom.setRejectedAt(ZonedDateTime.now());
            createdChatRoom.setRejectedBy(name);
            createdChatRoom.setRejectReason(chatRoom.getRejectReason());
            createdChatRoom.setApprovedAt(null);
            createdChatRoom.setApprovedBy(null);
        }
        createdChatRoomRepository.save(createdChatRoom);
        return res;
    }

    @Transactional
    @Scheduled(fixedDelay = 60000 * 30)
    public void doRankingJob() {
//        if (!appConf.isEnableJob()) {
//            return;
//        }
        try {
            log.info("Request to do ranking at : {}", ZonedDateTime.now());
            int count = 1;
            int rank = 1;
            Long previousTotalView = null;
            List<Broker> brokers = brokerRepository
                .findAll(Sort.by(Sort.Direction.DESC, "totalViewdChatRoom"));
            for (Broker broker : brokers) {
                if (broker.getTotalViewdChatRoom() == null || broker.getTotalViewdChatRoom() == 0) {
                    broker.setCurrentRank(null);
                    broker.setIsDynamic(false);
                } else {
                    if (!broker.getStatus()) {
                        broker.setCurrentRank(null);
                        broker.setIsDynamic(false);
                    } else {
                        if (count == 1) {
                            broker.setCurrentRank(rank);
                        } else {
                            if (!Objects.equals(broker.getTotalViewdChatRoom(), previousTotalView)) {
                                rank = count;
                            }
                        }
                        broker.setCurrentRank(rank);
                        previousTotalView = broker.getTotalViewdChatRoom();
                        count++;
                        broker.setIsDynamic(rank <= 5);
                    }
                }
            }
            brokerRepository.saveAll(brokers);
        } catch (Exception e) {
            log.error("doRankingJob error", e);
        }
    }

    public ChatRoom update(ChatRoom chatRoom) {
        log.info("Request to update ChatRoom : {}", chatRoom);
        return chatRoomRepository.save(chatRoom);
    }

    @Transactional(readOnly = true)
    public Optional<ChatRoom> findOne(Long id) {
        log.info("Request to get ChatRoom : {}", id);
        return chatRoomRepository.findById(id);
    }

    public GetProfileResponse getProfile(GetProfileRequest request, RequestContext<GetProfileRequest> ctx) {
        log.info("{} Request to get profile for brokerId : {}", ctx.getId(), request.getBrokerId());
        Long brokerId = request.getBrokerId();
        Broker broker = brokerRepository.findById(brokerId).orElse(null);
        GetProfileResponse response = new GetProfileResponse();
        if (broker == null) {
            log.info("Broker not found for brokerId : {}", request.getBrokerId());
            throw new GeneralException(Constants.ACCOUNT_NOT_FOUND);
        } else if (broker.getStatus()) {
            List<CreatedChatRoom> chatRooms = createdChatRoomRepository.findByBrokerIdAndStatus(brokerId,
                StatusEnum.APPROVED);
            response = GetProfileResponse.toGetProfileResponse(broker, chatRooms);
        }
        return response;
    }

    public GetAllProfileResponse getAllProfile(GetAllProfileRequest request, RequestContext<GetAllProfileRequest> ctx) {
        log.info("{} Request to get all profile : {}", ctx.getId(), request);
        if (request.getType() == null || request.getType().isEmpty()) {
            request.setType(GetAllProfileEnum.ALL.name());
        }
        GetAllProfileEnum type;
        try {
            type = GetAllProfileEnum.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(Constants.INVALID_TYPE);
        }
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize(),
            Sort.by(Sort.Direction.ASC, "currentRank", "fullname"));
        String keyword = request.getKeyword();
        BrokerCriteria criteria = new BrokerCriteria();
        BooleanFilter statusFilter = new BooleanFilter();
        statusFilter.setEquals(true);
        criteria.setStatus(statusFilter);
        if (keyword != null) {
            StringFilter filter = new StringFilter();
            filter.setContains(keyword);
            criteria.setFullname(filter);
        }
        switch (type) {
            case ALL:
                Page<Broker> brokers = brokerQueryService.findByCriteria(criteria, pageable);
                GetAllProfileResponse response = GetAllProfileResponse.toGetAllProfileResponse(brokers);
                return response;
            case DYNAMIC:
                BooleanFilter dynamicFilter = new BooleanFilter();
                dynamicFilter.setEquals(true);
                criteria.setIsDynamic(dynamicFilter);
                Page<Broker> brokersDynamic = brokerQueryService.findByCriteria(criteria, pageable);
                GetAllProfileResponse responseDynamic = GetAllProfileResponse
                    .toGetAllProfileResponse(brokersDynamic);
                return responseDynamic;
            default:
                throw new GeneralException(Constants.INVALID_TYPE);
        }
    }

    public GetAllChatRoomResponse getAllChatRoom(GetAllChatRoomRequest request,
                                                 RequestContext<GetAllChatRoomRequest> ctx) {
        log.info("{} Request to get all chat room : {}", ctx.getId(), request);
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdAt"));
        String keyword = request.getKeyword();
        GetAllChatRoomResponse response = new GetAllChatRoomResponse();
        Page<CreatedChatRoom> chatRooms = null;
        if (keyword != null) {
            keyword = "%" + keyword + "%";
            chatRooms = createdChatRoomRepository.findAllByStatusWithKeyWord(StatusEnum.APPROVED, keyword, pageable);
        } else {
            chatRooms = createdChatRoomRepository.findAllByStatus(StatusEnum.APPROVED, pageable);
        }
        response = GetAllChatRoomResponse.toGetAllChatRoomResponse(chatRooms);
        return response;
    }

    @Transactional
    public GetChatRoomResponse getChatRoom(GetChatRoomRequest request, RequestContext<GetChatRoomRequest> ctx) {
        log.info("{} Request to get chat room : {}", ctx.getId(), request);
        GetChatRoomResponse response = new GetChatRoomResponse();
        Optional<CreatedChatRoom> createChatRoom = createdChatRoomRepository.findById(request.getChatRoomId());
        if (!createChatRoom.isPresent()) {
            throw new GeneralException(Constants.CHAT_ROOM_NOT_FOUND);
        }
        if (!createChatRoom.get().getStatus().equals(StatusEnum.APPROVED)) {
            throw new GeneralException(Constants.CHAT_ROOM_NOT_FOUND);
        }
        if (brokerRepository.findByIdAndStatus(createChatRoom.get().getBrokerId(), false).isPresent()) {
            throw new GeneralException(Constants.CHAT_ROOM_OWNER_IS_DEACTIVATED);
        }
        Broker broker = brokerRepository.findByUsername(createChatRoom.get().getBrokerName()).orElse(null);
        response = GetChatRoomResponse.toGetChatRoomResponse(createChatRoom.get(), broker);
        createChatRoom.get().setTotalView(createChatRoom.get().getTotalView() + 1);
        createdChatRoomRepository.save(createChatRoom.get());
        broker.setTotalViewdChatRoom(broker.getTotalViewdChatRoom() + 1);
        brokerRepository.save(broker);
        return response;
    }

    private File convertMultipartFileToTempFile(MultipartFile multipartFile, String name, String path)
        throws IOException {
        File tempFile = File.createTempFile(name, path);
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    @Transactional
    public void uploadChatRoomImage(ChatRoomImageDTO chatRoomImageDTO, boolean isUpdate) {
        log.info("Request to upload chat room image : {}", chatRoomImageDTO);
        if (chatRoomImageDTO.getFile() != null) {
            String filePath = "chatRoom";
            String orginFilename = chatRoomImageDTO.getFile().getOriginalFilename();
            String path = orginFilename.substring(orginFilename.lastIndexOf("."), orginFilename.length());
            File file = null;
            String fileName = "" + chatRoomImageDTO.getId() + new Date().getTime();
            try {
                file = convertMultipartFileToTempFile(chatRoomImageDTO.getFile(), fileName, path);
            } catch (IOException e) {
                log.info(e.getMessage());
            }
            String realPath = filePath + "/" + fileName + path;
            String url = fileService.uploadFile(file, "nhsv-admin", realPath, false);
            if (isUpdate) {
                chatRoomRepository
                    .findById(chatRoomImageDTO.getId())
                    .ifPresent(chatRoom -> {
                        chatRoom.setPhoto(url);
                        chatRoomRepository.save(chatRoom);
                    });
            } else {
                createdChatRoomRepository.findById(chatRoomImageDTO.getId())
                    .ifPresent(createdChatRoom -> {
                        createdChatRoom.setPhoto(url);
                        createdChatRoomRepository.save(createdChatRoom);
                    });

                chatRoomRepository.findById(chatRoomImageDTO.getId())
                    .ifPresent(chatRoom -> {
                        chatRoom.setPhoto(url);
                        chatRoomRepository.save(chatRoom);
                    });
            }
        }
    }
}
