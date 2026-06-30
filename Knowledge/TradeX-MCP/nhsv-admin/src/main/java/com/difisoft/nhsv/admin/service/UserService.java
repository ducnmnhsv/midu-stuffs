package com.difisoft.nhsv.admin.service;

import com.difisoft.file.FileService;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.Authority;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.repository.AuthorityRepository;
import com.difisoft.nhsv.admin.repository.BrokerRepository;
import com.difisoft.nhsv.admin.repository.UserRepository;
import com.difisoft.nhsv.admin.security.AuthoritiesConstants;
import com.difisoft.nhsv.admin.security.SecurityUtils;
import com.difisoft.nhsv.admin.service.dto.AdminUserDTO;
import com.difisoft.nhsv.admin.service.dto.ImageDTO;
import com.difisoft.nhsv.admin.service.dto.UserDTO;
import com.difisoft.nhsv.admin.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import tech.jhipster.security.RandomUtil;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final FileService fileService;
    private final BrokerRepository brokerRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthorityRepository authorityRepository,
                       FileService fileService,
                       BrokerRepository brokerRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.fileService = fileService;
        this.brokerRepository = brokerRepository;
    }

    public Optional<User> activateRegistration(String key) {
        log.info("Activating user for activation key {}", key);
        return userRepository
            .findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                log.info("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.info("Reset user password for reset key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(ZonedDateTime.now().minus(1, ChronoUnit.DAYS)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository
            .findOneByEmailIgnoreCase(mail)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(ZonedDateTime.now());
                return user;
            });
    }

    public User createUser(AdminUserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFullName(userDTO.getFullName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setInvitedBy(SecurityUtils.getCurrentUserLogin().orElse(null));
        user.setActivated(true);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO
                .getAuthorities()
                .stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        log.info("Created Information for User: {}", user);
        return user;
    }

    private File convertMultipartFileToTempFile(MultipartFile multipartFile, String name, String path)
        throws IOException {
        File tempFile = File.createTempFile(name, path);
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO, User user) {
        if (Objects.isNull(user)) {
            return Optional.empty();
        }
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFullName(userDTO.getFullName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        user.setActivated(userDTO.isActivated());
        user.setLangKey(userDTO.getLangKey());
        user.setIntroduction(userDTO.getIntroduction());
        user.setDeactivatedAt(userDTO.getDeactivatedAt());
        user.setDeactivatedBy(userDTO.getDeactivatedBy());
        Set<Authority> managedAuthorities = user.getAuthorities();
        managedAuthorities.clear();
        userDTO
            .getAuthorities()
            .stream()
            .map(authorityRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(managedAuthorities::add);
        user.setAuthorities(managedAuthorities);
        log.info(userDTO
            .getAuthorities().toString());
        log.info("Changed Information for User: {}", user);
        return Optional.of(new AdminUserDTO(user));
    }

    public void deleteUser(String login) {
        userRepository
            .findOneByLogin(login)
            .ifPresent(user -> {
                user.setActivated(false);
                user.setDeactivatedAt(ZonedDateTime.now());
                user.setDeactivatedBy(SecurityUtils.getCurrentUserLogin().orElse(null));
                userRepository.save(user);
                log.info("Deactivate User: {}", user);
            });
    }

    public void updateUser(String login, String photo) {
        userRepository
                .findOneByLogin(login)
                .ifPresent(user -> {
                    user.setPhoto(photo);
                    userRepository.save(user);
                    log.info("Update User: {}", user);
                    List<String> authorities = user.getAuthorities().stream()
                        .map(Authority::getName).collect(Collectors.toList());
                    if (authorities.contains(AuthoritiesConstants.BROKER)) {
                        brokerRepository.findById(user.getId()).
                            ifPresent(broker -> {
                                broker.setPhoto(photo);
                                brokerRepository.save(broker);
                            });
                    }
                });
    }

    public void updateUser(String fullName, String email, String langKey, String introduction) {
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFullName(fullName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setIntroduction(introduction);
                log.info("Changed Information for User: {}", user);
            });
    }

    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                log.info("Changed password for User: {}", user);
            });
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(MultiValueMap<String, String> param, Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    public void updateImage(ImageDTO userDTO) {
        User user = userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).orElse(null);
        if (userDTO.getPhoto() != null && user != null) {
            String filePath = "userPhoto";
            String orginFilename = userDTO.getPhoto().getOriginalFilename();
            String path = orginFilename.substring(orginFilename.lastIndexOf("."), orginFilename.length());
            File file = null;
            String fileName = "" + user.getId() + new Date().getTime();
            try {
                file = convertMultipartFileToTempFile(userDTO.getPhoto(), fileName, path);
            } catch (IOException e) {
                log.info(e.getMessage());
            }
            String realPath = filePath + "/" + fileName + path;
            String url = fileService.uploadFile(file, "nhsv-admin", realPath, false);
            updateUser(userDTO.getLogin(), url);
        }
    }

}
