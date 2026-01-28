package com.difisoft.marketcollector.repositories;

import com.difisoft.marketcollector.model.db.MappingMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class MarketRepository {
    private MongoTemplate mongoTemplate;
    private final int BATCH_SIZE = 1000;

    @Autowired
    public MarketRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    public <E extends MappingMongo> void updateOne(E data, Class<E> clazz) {
        if (data == null) {
            return;
        }
        updateInBulk(Collections.singletonList(data), clazz);
    }

    public <E extends MappingMongo> void updateInBulk(List<E> dataList, Class<E> clazz) {
        updateInBulkNonCheckClazz(dataList, clazz);
    }

    public <E extends MappingMongo> void updateInBulkNonCheckClazz(List<E> dataList, Class clazz) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, clazz);
        int count = 0;

        for (int i = 0; i < dataList.size(); i++) {
            MappingMongo mappingMongo = dataList.get(i);
            Map<String, Object> map = mappingMongo.toMapUpdate();
            Query query = new Query();
            Criteria criteria = Criteria.where("_id").is(mappingMongo.getId());
            query.addCriteria(criteria);
            Update update = new Update();
            map.forEach((key, value) -> update.set(key, value));
            bulkOps.upsert(query, update);
            count++;
            if (count == BATCH_SIZE) {
                bulkOps.execute();
                count = 0;
            }
        }
        if (count > 0) {
            bulkOps.execute();
        }
    }

}
