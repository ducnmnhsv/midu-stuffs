package com.difisoft.nhsv.admin.repository.primary;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.repository.BrokerRepository;

@Repository
@Primary
public interface BrokerPrimaryRepository extends BrokerRepository {
    Page<Broker> findAllByStatus(boolean b, Pageable pageable);

    List<Broker> findAllByIsDynamic(boolean b);

    Optional<Broker> findByUsername(String brokerName);

    Optional<Broker> findByIdAndStatus(Long brokerId, boolean b);

    @Query("select b from Broker b where b.totalViewdChatRoom > 0")
    List<Broker> findAllByTotalViewdChatRoom(Sort sort);

    Optional<Broker> findByUsernameAndStatusIsFalse(String name);
}
