package com.difisoft.nhsv.admin.repository.mongodb;

import com.difisoft.market.common.repository.SymbolDailyRepository;
import com.difisoft.market.model.v2.db.SymbolDaily;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CopySymbolDailyRepository extends SymbolDailyRepository {
    @Query(value = "{'code': ?0, '$expr': {'$eq': [{'$dateToString': {'format': '%Y%m%d', 'date': '$date'}}, ?1]}}")
    Optional<SymbolDaily> findByStockCodeAndDateString(String stockCode, String dateString);
}
