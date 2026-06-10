package com.techx.tradex.realtime.services;

import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.IndexStockListRepository;
import com.difisoft.market.model.v2.db.IndexList;
import com.difisoft.market.model.v2.db.IndexStockList;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.exceptions.GeneralException;
import com.techx.tradex.realtime.constants.Constants;
import com.techx.tradex.realtime.constants.enums.IndexRankType;
import com.techx.tradex.realtime.model.dto.IndexRateDTO;
import com.techx.tradex.realtime.model.request.IndexRankRequest;
import com.techx.tradex.realtime.model.response.IndexRankResponse;
import com.techx.tradex.realtime.utils.MarketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexStockService {

    private final IndexStockListRepository indexStockListRepository;
    private final MarketRedisDao redisRepository;

    public IndexRankResponse getIndexRanks(IndexRankRequest request) {
        IndexRankType indexRankType = IndexRankType.valueOf(request.getType() != null ? request.getType() : "TRADING_VOLUME");
        List<String> stockList = getStockList(request.getIndex());
        List<IndexRankResponse.IndexRank> indexRanks = new ArrayList<>();

        switch (indexRankType) {
            case RATE_DESC:
                indexRanks = rankIndexes(getIndexRateDescDTOS(stockList), request);
                break;
            case RATE_ASC:
                indexRanks = rankIndexes(getIndexRateAscDTOS(stockList), request);
                break;
            default:
                break;
        }

        IndexRankResponse response = new IndexRankResponse();
        response.setIndexRanks(indexRanks);
        return response;
    }

    private List<IndexRateDTO> getIndexRateDescDTOS(List<String> stockList) {
        return getRateDTOS(
                stockList
                , Comparator.comparingDouble(IndexRateDTO::getRate)
                        .thenComparing(IndexRateDTO::getTradingValue)
                        .reversed()
        );
    }

    private List<IndexRateDTO> getIndexRateAscDTOS(List<String> stockList) {
        return getRateDTOS(stockList, Comparator.comparingDouble(IndexRateDTO::getRate).thenComparing(IndexRateDTO::getTradingValue));
    }

    private List<IndexRateDTO> getRateDTOS(List<String> stockList, Comparator<IndexRateDTO> comparator) {
        return stockList.parallelStream()
                .map(s -> {
                    IndexRateDTO indexRateDTO = new IndexRateDTO();
                    SymbolInfo symbolInfo = redisRepository.getSymbolInfo(s);
                    if (symbolInfo != null && MarketUtil.compareToZero(symbolInfo.getReferencePrice()) != 0) {
                        indexRateDTO.setRate(Objects.nonNull(symbolInfo.getRate()) ? symbolInfo.getRate() : BigDecimal.ZERO.doubleValue());
                        indexRateDTO.setTradingValue(Objects.nonNull(symbolInfo.getRate()) ? symbolInfo.getTradingValue() : BigDecimal.ZERO.doubleValue());
                    } else {
                        indexRateDTO.setRate(BigDecimal.ZERO.doubleValue());
                        indexRateDTO.setTradingValue(BigDecimal.ZERO.doubleValue());
                    }
                    indexRateDTO.setStockCode(s);
                    return indexRateDTO;
                }).sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<IndexRankResponse.IndexRank> rankIndexes(List<IndexRateDTO> indexDTOS, IndexRankRequest request) {
        int fetchCount = request.getPageSize() == null ? indexDTOS.size() : request.getPageSize();
        int offset = request.getPageNumber() == null ? Constants.DEFAULT_OFFSET : request.getPageNumber() * fetchCount;
        int end = Math.min(offset + fetchCount, indexDTOS.size());
        return IntStream.rangeClosed(offset + 1, end)
                .mapToObj(i -> {
                    IndexRateDTO indexDTO = indexDTOS.get(i - 1);
                    IndexRankResponse.IndexRank indexRank = new IndexRankResponse.IndexRank();
                    indexRank.setStockCode(indexDTO.getStockCode());
                    indexRank.setRate(indexDTO.getRate());
                    indexRank.setRank(i);
                    return indexRank;
                }).collect(Collectors.toList());
    }

    private List<String> getStockList(String index) {
        Optional<IndexStockList> optionalIndexStockList = indexStockListRepository.findById(index);
        if (!optionalIndexStockList.isPresent()) {
            throw new GeneralException(Constants.INDEX_NOT_FOUND);
        }
        List<String> stockList = optionalIndexStockList.get().getStockList();
        return stockList != null ? stockList : new ArrayList<>();
    }

    public void updateIndexList(IndexStockList item) {
        if(item.getIndexCode().equals("VNINDEX")){
            item.setIndexCode("VN");
        }
        if(item.getIndexCode().equals("HNXINDEX")){
            item.setIndexCode("HNX");
        }
        item.setUpdatedAt(new Date());
        indexStockListRepository.save(item);
    }
}
