package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.User;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
@Primary
public interface UserCustomRepository extends UserRepository {

    @Query(value = "select u from User u JOIN u.authorities a where (coalesce(:username, null ) is null or u.login like lower(concat('%', :username,'%'))) and a.name = :authName and u.activated = :activated ")
    Page<User> findAllMarketLeader(@Param("username") String username, @Param("authName") String authName, @Param("activated") boolean activated, Pageable pageable);

    @Query(value = "select u from User u JOIN u.authorities a where a.name = :authName and u.activated = :activated ")
    List<User> findAllUserByAuthorityTypeAndStatus(@Param("authName") String authName, @Param("activated") boolean activated);

    @Query(value = "select u from User u JOIN u.authorities a where u.id in :mlUserIds and a.name = :authName and u.activated = :activated ")
    List<User> findAllByIdsAndTypeAndStatus(@Param("mlUserIds") List<Long> mlUserIds, @Param("authName") String authName, @Param("activated") boolean activated);
}
