package com.techx.tradex.order.repositories;

import com.difisoft.model.constants.ProfitLossOrderStatusEnum;
import com.techx.tradex.order.model.db.BullBearOrder;
import com.techx.tradex.order.model.db.OcoOrder;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfitLossOrderRepository extends JpaRepository<ProfitLossOrder, Long>, JpaSpecificationExecutor<ProfitLossOrder> {
    ProfitLossOrder findByIdAndStatus(Long id, ProfitLossOrderStatusEnum status);

    ProfitLossOrder findByUsernameAndId(String username, Long id);

    List<ProfitLossOrder> findByStatus(ProfitLossOrderStatusEnum status);

    List<ProfitLossOrder> findByOcoOrder(OcoOrder ocoOrder);

    List<ProfitLossOrder> findByBullBearOrder(BullBearOrder ocoOrder);

}
