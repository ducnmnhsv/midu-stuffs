package com.difisoft.nhsv.admin.web.rest;

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
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderDetailsCustomRepository;
import com.difisoft.nhsv.admin.repository.UserRepository;
import com.difisoft.nhsv.admin.repository.primary.ChatRoomPrimaryRepository;
import com.difisoft.nhsv.admin.repository.primary.CreatedChatRoomPrimaryRepository;
import com.difisoft.nhsv.admin.repository.primary.InviteUserPrimaryRepository;
import com.difisoft.nhsv.admin.security.AuthoritiesConstants;
import com.difisoft.nhsv.admin.security.SecurityUtils;
import com.difisoft.nhsv.admin.service.UserQueryService;
import com.difisoft.nhsv.admin.service.UserService;
import com.difisoft.nhsv.admin.service.criteria.UserCriteria;
import com.difisoft.nhsv.admin.service.dto.AdminUserDTO;
import com.difisoft.nhsv.admin.service.dto.ImageDTO;
import com.difisoft.nhsv.admin.service.mapper.BrokerMapper;
import com.difisoft.nhsv.admin.service.mapper.UserMapper;
import com.difisoft.nhsv.admin.utils.Util;
import com.difisoft.nhsv.admin.web.rest.errors.EmailAlreadyUsedException;
import com.difisoft.nhsv.admin.web.rest.errors.LoginAlreadyUsedException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.StringFilter;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/admin")
public class UserResource {
    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserService userService;

    private final UserRepository userRepository;

    private final UserQueryService userQueryService;

    private final InviteUserPrimaryRepository inviteUserRepository;

    private final BrokerRepository brokerRepository;

    private final ChatRoomPrimaryRepository chatRoomRepository;

    private final CreatedChatRoomPrimaryRepository createdChatRoomRepository;

    private final CopyMarketLeaderDetailsCustomRepository copyMarketLeaderDetailsRepository;

    public UserResource(UserService userService, UserRepository userRepository,
                        UserQueryService userQueryService, InviteUserPrimaryRepository inviteUserRepository,
                        BrokerRepository brokerRepository, ChatRoomPrimaryRepository chatRoomRepository,
                        CreatedChatRoomPrimaryRepository createdChatRoomRepository,
                        CopyMarketLeaderDetailsCustomRepository copyMarketLeaderDetailsRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userQueryService = userQueryService;
        this.inviteUserRepository = inviteUserRepository;
        this.brokerRepository = brokerRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.createdChatRoomRepository = createdChatRoomRepository;
        this.copyMarketLeaderDetailsRepository = copyMarketLeaderDetailsRepository;
    }

    @PutMapping("/users")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + " \" , \"" + AuthoritiesConstants.SUPER_ADMIN
        + "\")")
    @Transactional
    public ResponseEntity<AdminUserDTO> updateUser(@RequestBody @Valid AdminUserDTO userDTO) {
        log.info("REST request to update User : {}", userDTO);
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new EmailAlreadyUsedException();
        }
        existingUser = userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new LoginAlreadyUsedException();
        }
        User user = userRepository.findById(userDTO.getId()).orElseThrow(() -> new RuntimeException("User could not be found"));
        Set<String> authorities = user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet());
        log.info("updateUser -- user: {}", Util.objectToStringJsonIgnoreError(user));
        Optional<AdminUserDTO> updatedUser = userService.updateUser(userDTO, user);
        log.info("updatedUser: {}", updatedUser.isPresent() ? Util.objectToStringJsonIgnoreError(updatedUser.get()) : updatedUser);
        if (updatedUser.isPresent()) {
            boolean wasBroker = authorities.contains(AuthoritiesConstants.BROKER);
            boolean isBroker = updatedUser.get().getAuthorities().contains(AuthoritiesConstants.BROKER);
            if (wasBroker && !isBroker)  {
                brokerRepository.findById(updatedUser.get().getId()).ifPresent(broker -> {
                    broker.setStatus(false);
                    brokerRepository.save(broker);
                });
            }
            if (isBroker) {
                Broker broker = brokerRepository.findById(updatedUser.get().getId()).orElse(null);
                if (broker != null && !userDTO.getFullName().equals(broker.getFullname())) {
                    List<ChatRoom> chatRooms = chatRoomRepository.findByBrokerId(broker.getId());
                    for (ChatRoom chatRoom : chatRooms) {
                        chatRoom.setGroupOwner(userDTO.getFullName());
                    }
                    chatRoomRepository.saveAll(chatRooms);
                    List<CreatedChatRoom> createdChatRooms = createdChatRoomRepository
                        .findAllByBrokerId(broker.getId());
                    for (CreatedChatRoom createdChatRoom : createdChatRooms) {
                        createdChatRoom.setGroupOwner(userDTO.getFullName());
                    }
                    createdChatRoomRepository.saveAll(createdChatRooms);
                }
                broker = BrokerMapper.toBroker(updatedUser.get(), broker);
                brokerRepository.save(broker);
            }
        }
        if (updatedUser.isPresent() && updatedUser.get().getAuthorities()
            .contains(AuthoritiesConstants.MARKET_LEADER)) {
            CopyMarketLeaderDetails copyMarketLeaderDetails = new CopyMarketLeaderDetails();
            copyMarketLeaderDetails.setCreatedAt(ZonedDateTime.now());
            copyMarketLeaderDetails.setUpdatedAt(ZonedDateTime.now());
            copyMarketLeaderDetails.setType("COPY_TRADING");
            copyMarketLeaderDetails.setMlUserId(UserMapper.userDTOToUser(userDTO));
            copyMarketLeaderDetails.setLabel("MARKET_LEADER_SUMMARY_INFO");
            copyMarketLeaderDetails.setKey("BE_MARKET_LEADER_DATE");
            copyMarketLeaderDetails.setValue(ZonedDateTime.now().format(
                DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_ddMMyyyy_hhmmss)));
            copyMarketLeaderDetailsRepository.save(copyMarketLeaderDetails);
        }
        if (existingUser.get().getAuthorities().contains(new Authority(AuthoritiesConstants.MARKET_LEADER)) &&
            updatedUser.isPresent()
            && !updatedUser.get().getAuthorities().contains(AuthoritiesConstants.MARKET_LEADER)) {
            copyMarketLeaderDetailsRepository
                .deleteByMlUserId(UserMapper.userDTOToUser(updatedUser.get()));
        }
        String message = "A user is updated with identifier " + userDTO.getLogin();
        HttpHeaders headers = new HttpHeaders();
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        headers.add("X-" + applicationName + "-alert", encodedMessage);
        return ResponseUtil.wrapOrNotFound(updatedUser, headers);
    }

        @PostMapping("/users/upload")
        @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\" , \"" + AuthoritiesConstants.SUPER_ADMIN
                        + "\" , \"" + AuthoritiesConstants.BROKER + "\" , \"" + AuthoritiesConstants.MARKET_LEADER + "\")")
        public void uploadUser(@ModelAttribute ImageDTO userDTO) {
                log.info("REST request to upload User image : {}", userDTO.getPhoto().getOriginalFilename());
                userService.updateImage(userDTO);
        }

    @PutMapping("/users/updateStatus")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + " \" , \"" + AuthoritiesConstants.SUPER_ADMIN
        + "\")")
    @Transactional
    public ResponseEntity<AdminUserDTO> updateStatusUser(@Valid @RequestBody AdminUserDTO userDTO) {
        log.info("REST request to update User : {}", userDTO);
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new EmailAlreadyUsedException();
        }
        existingUser = userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))) {
            throw new LoginAlreadyUsedException();
        }
        userDTO.setDeactivatedAt(ZonedDateTime.now());
        userDTO.setDeactivatedBy(SecurityUtils.getCurrentUserLogin().get());
        User user = userRepository.findById(userDTO.getId()).orElse(null);
        log.info("updateStatusUser -- user: {}", Util.objectToStringJsonIgnoreError(user));
        Optional<AdminUserDTO> updatedUser = userService.updateUser(userDTO, user);
        InviteUser inviteUser = inviteUserRepository.findByEmailIgnoreCase(userDTO.getEmail()).orElse(null);
        if (inviteUser != null) {
            inviteUser.setStatus(InviteStatusEnum.ACCOUNT_DEACTIVATED);
            inviteUserRepository.save(inviteUser);
        }
        if (updatedUser.isPresent() && updatedUser.get().getAuthorities()
            .contains(AuthoritiesConstants.BROKER)) {
            Broker broker = brokerRepository.findById(updatedUser.get().getId()).orElse(null);
            broker = BrokerMapper.toBroker(updatedUser.get(), broker);
            brokerRepository.save(broker);
        }
        if (updatedUser.isPresent() && updatedUser.get().getAuthorities()
            .contains(AuthoritiesConstants.MARKET_LEADER) && !userDTO.isActivated()) {
            copyMarketLeaderDetailsRepository
                .deleteByMlUserId(UserMapper.userDTOToUser(updatedUser.get()));
        }
        String message = "A user is deactivated with identifier " + userDTO.getLogin();
        HttpHeaders headers = new HttpHeaders();
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        headers.add("X-" + applicationName + "-alert", encodedMessage);
        return ResponseUtil.wrapOrNotFound(updatedUser, headers);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + " \" , \"" + AuthoritiesConstants.SUPER_ADMIN
        + "\")")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers(
        @RequestParam MultiValueMap<String, String> queryParams,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.info("REST request to get all User for an admin");
        UserCriteria userCriteria = new UserCriteria();
        if (queryParams.containsKey("status")) {
            boolean status = queryParams.get("status").get(0).equals("ACTIVATED") ? true
                : queryParams.get("status").get(0).equals("DEACTIVATED") ? false : null;
            BooleanFilter booleanFilter = new BooleanFilter();
            booleanFilter.setEquals(status);
            userCriteria.setStatus(booleanFilter);
        }
        if (queryParams.containsKey("roles")) {
            StringFilter stringFilter = new StringFilter();
            stringFilter.setEquals(queryParams.get("roles").get(0));
            userCriteria.setRoles(stringFilter);
        }
        if (queryParams.containsKey("fullName")) {
            StringFilter stringFilter = new StringFilter();
            stringFilter.setContains(queryParams.get("fullName").get(0));
            userCriteria.setFullName(stringFilter);
        }
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SUPER_ADMIN)) {
            StringFilter stringFilter = new StringFilter();
            List<String> authorities = new ArrayList<>();
            authorities.add(AuthoritiesConstants.BROKER);
            authorities.add(AuthoritiesConstants.MARKET_LEADER);
            stringFilter.setIn(authorities);
            userCriteria.setIsSuperAdmin(stringFilter);
        }
        Page<AdminUserDTO> page = userQueryService.findByCriteria(userCriteria, pageable)
            .map(AdminUserDTO::new);
        HttpHeaders headers = PaginationUtil
            .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/users/{login}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + " \" , \"" + AuthoritiesConstants.SUPER_ADMIN
        + "\")")
    public ResponseEntity<AdminUserDTO> getUser(
        @PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        log.info("REST request to get User : {}", login);
        return ResponseUtil.wrapOrNotFound(
            userService.getUserWithAuthoritiesByLogin(login).map(AdminUserDTO::new));
    }

    @DeleteMapping("/users/{login}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + " \" , \"" + AuthoritiesConstants.SUPER_ADMIN
        + "\")")
    public ResponseEntity<Void> deleteUser(@PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        log.info("REST request to delete User: {}", login);
        userService.deleteUser(login);
        String message = "A user is deactivate with identifier " + login;
        HttpHeaders headers = new HttpHeaders();
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        headers.add("X-" + applicationName + "-alert", encodedMessage);
        return ResponseEntity
            .noContent()
            .headers(headers)
            .build();
    }

}
