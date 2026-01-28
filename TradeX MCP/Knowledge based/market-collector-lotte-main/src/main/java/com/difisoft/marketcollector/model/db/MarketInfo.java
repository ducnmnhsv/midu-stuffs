package com.difisoft.marketcollector.model.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "c_market_info")
public class MarketInfo implements MappingMongo {
    @Id
    private String id;
    private List<DividendRate> eventList;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
