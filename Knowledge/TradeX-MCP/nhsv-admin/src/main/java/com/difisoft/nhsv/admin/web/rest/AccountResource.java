package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.InviteUser;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.domain.request.UpdateIntroductionRequest;
import com.difisoft.nhsv.admin.repository.UserRepository;
import com.difisoft.nhsv.admin.repository.primary.InviteUserPrimaryRepository;
import com.difisoft.nhsv.admin.security.SecurityUtils;
import com.difisoft.nhsv.admin.service.InviteUserServiceImp;
import com.difisoft.nhsv.admin.service.MailService;
import com.difisoft.nhsv.admin.service.UserService;
import com.difisoft.nhsv.admin.service.dto.AdminUserDTO;
import com.difisoft.nhsv.admin.service.dto.PasswordChangeDTO;
import com.difisoft.nhsv.admin.web.rest.errors.*;
import com.difisoft.nhsv.admin.web.rest.vm.KeyAndPasswordVM;
import com.difisoft.nhsv.admin.web.rest.vm.ManagedUserVM;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;
    private final InviteUserPrimaryRepository inviteUserRepository;
    private final InviteUserServiceImp inviteUserService;

    public AccountResource(UserRepository userRepository, UserService userService, MailService mailService,
            InviteUserPrimaryRepository inviteUserRepository, InviteUserServiceImp inviteUserService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.inviteUserRepository = inviteUserRepository;
        this.inviteUserService = inviteUserService;
    }

    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.info("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    @GetMapping("/account")
    public AdminUserDTO getAccount() {
        return userService
                .getUserWithAuthorities()
                .map(AdminUserDTO::new)
                .orElseThrow(() -> new AccountResourceException("User could not be found"));
    }

    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody AdminUserDTO userDTO) {
        String userLogin = SecurityUtils
                .getCurrentUserLogin()
                .orElseThrow(() -> new AccountResourceException("Current user login not found"));
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
            throw new EmailAlreadyUsedException();
        }
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        userService.updateUser(
                userDTO.getFullName(),
                userDTO.getEmail(),
                userDTO.getLangKey(),
                userDTO.getIntroduction());
    }

    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
        mailService
                .sendChangePasswordMail(userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).get());
    }

    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String mail) {
        Optional<User> user = userService.requestPasswordReset(mail);
        if (user.isPresent()) {
            mailService.sendPasswordResetMail(user.get());
        } else {
            log.warn("Password reset requested for non existing mail");
            throw new AccountResourceException("Email address not registered");
        }
    }

    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user = userService.completePasswordReset(
                keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
    }

    @PostMapping(path = "/account/reset-password/create")
    public void finishCreateAccount(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user = inviteUserService.completeCreateAccount(keyAndPassword.getFullName(),
                keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (StringUtils.isEmpty(password) ||
                password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
                password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH);
    }

    @GetMapping(path = "/account/reset-password/getAccount")
    public void finishPasswordGetAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userRepository.findOneByResetKey(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
    }

    @GetMapping(path = "/account/reset-password/getKey")
    public void finishPasswordGetKey(@RequestParam(value = "key") String key) {
        Optional<InviteUser> user = inviteUserRepository.findOneByActivationKey(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this Activation key");
        }
    }

    @PutMapping(path = "/account/introduction")
    public ResponseEntity<User> updateIntroduction(@RequestBody UpdateIntroductionRequest userRequest) {
        Optional<User> userOptional = userRepository.findOneByLogin(userRequest.getLogin());
        if (!userOptional.isPresent()) {
            throw new AccountResourceException("No user was found");
        } else {
            User user = userOptional.get();
            user.setIntroduction(userRequest.getIntroduction());
            userRepository.save(user);
        }
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createAlert(applicationName, "Your introduction is edited successfully",
                        userOptional.get().getLogin()))
                .body(userOptional.get());
    }

}
