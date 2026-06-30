package com.difisoft.nhsv.admin.repository.primary;

import com.difisoft.nhsv.admin.domain.InviteUser;
import com.difisoft.nhsv.admin.domain.enumeration.InviteStatusEnum;
import com.difisoft.nhsv.admin.repository.InviteUserRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteUserPrimaryRepository extends InviteUserRepository {
    Optional<InviteUser> findByEmailIgnoreCase(String email);

    Optional<InviteUser> findOneByActivationKey(String key);

    List<InviteUser> findAllByStatus(InviteStatusEnum pending);

    Optional<InviteUser> findByLogin(String login);
}
