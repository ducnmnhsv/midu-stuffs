import { Inject, Service } from 'typedi';
import config from '../config';
import { Errors, Kafka, Utils } from 'tradex-common';
import SymbolService from '../services/SymbolService';
import MarketSessionStatusService from '../services/MarketSessionStatusService';
import EtfService from '../services/EtfService';
import PutThroughService from '../services/PutThroughService';
import FixService from '../services/FixService';
import FeedService from '../services/FeedService';
import ChartService from '../services/ChartService';
import TopAiRatingService from '../services/TopAiRatingService';
import MarketInfoService from '../services/MarketInfoService';
import CrawlDataService from '../services/CrawlDataService';
import WatchListService from '../services/WatchListService';

@Service()
export default class RequestHandler {
  @Inject()
  private symbolService: SymbolService;
  @Inject()
  private marketSessionStatus: MarketSessionStatusService;
  @Inject()
  private etfService: EtfService;
  @Inject()
  private putThroughService: PutThroughService;
  @Inject()
  private fixService: FixService;
  @Inject()
  private feedService: FeedService;
  @Inject()
  private chartService: ChartService;
  @Inject()
  private topAiRatingService: TopAiRatingService;
  @Inject()
  private marketInfoService: MarketInfoService;
  @Inject()
  private crawlDataService: CrawlDataService;
  @Inject()
  private watchListService: WatchListService;

  public init() {
    const handle: Kafka.MessageHandler = new Kafka.MessageHandler();
    new Kafka.StreamHandler(
      config,
      config.kafkaConsumerOptions,
      config.requestHandlerTopics,
      (message: any) => handle.handle(message, this.handleRequest),
      config.kafkaTopicOptions,
    );
  }

  private handleRequest: Kafka.Handle = (message: Kafka.IMessage) => {
    if (message == null || message.data == null) {
      return Promise.reject(new Errors.SystemError());
    } else {
      if (message.uri === '/api/v2/market/symbol') {
        return true;
      }
      if (message.uri === '/api/v2/market/symbol/latest') {
        return this.symbolService.querySymbolLatestNormal(message.data);
      }
      if (message.uri === '/api/v2/market/symbol/oddlotLatest') {
        return this.symbolService.querySymbolLatestOddLot(message.data);
      }
      if (message.uri === '/api/v2/market/priceBoard') {
        return this.symbolService.queryPriceBoard(message.data);
      }
      if (message.uri === '/api/v2/market/symbol/staticInfo') {
        return this.symbolService.querySymbolStaticInfo(message.data);
      }
      if (message.uri === '/api/v2/market/symbol/{symbol}/quote') {
        return this.symbolService.querySymbolQuote(message.data);
      }
      if (message.uri === '/api/v2/market/symbolQuote/{symbol}') {
        return this.symbolService.queryQuoteData(message.data);
      }
      if (message.uri === '/api/v2/market/symbol/{symbol}/period/{periodType}') {
        return this.symbolService.querySymbolPeriod(message.data);
      }
      if (message.uri === '/api/v2/market/symbol/{symbol}/ticks') {
        return this.symbolService.querySymbolQuoteTick(message.data);
      }
      if (message.uri === '/api/v2/market/symbol/{symbol}/minutes') {
        return this.symbolService.querySymbolQuoteMinutes(message.data);
      }
      if (message.uri === '/api/v2/market/symbol/{symbol}/statistic') {
        return this.symbolService.querySymbolStatistics(message.data);
      }
      if (message.uri === '/api/v2/market/symbol/{symbol}/minuteChart') {
        return this.symbolService.queryMinuteChart(message.data);
      }
      if (message.uri === '/api/v2/market/sessionStatus') {
        return this.marketSessionStatus.queryMarketSessionStatus(message.data);
      }
      if (message.uri === '/api/v2/market/etf/{symbolCode}/nav/daily') {
        return this.etfService.queryEtfNavDaily(message.data);
      }
      if (message.uri === '/api/v2/market/etf/{symbolCode}/index/daily') {
        return this.etfService.queryEtfIndexDaily(message.data);
      }
      if (message.uri === '/api/v2/market/symbol/{symbolCode}/foreigner') {
        return this.symbolService.querySymbolForeignerDaily(message.data);
      }
      if (message.uri === '/api/v2/market/ranking/{symbolType}/trade') {
        return this.symbolService.querySymbolRankingTrade(message.data);
      } else if (message.uri === '/api/v2/market/putthrough/advertise') {
        return this.putThroughService.queryPutThroughAdvertise(message.data);
      } else if (message.uri === '/api/v2/market/putthrough/deal') {
        return this.putThroughService.queryPutThroughDeal(message.data);
      } else if (message.uri === '/api/v2/market/stock/ranking/upDown') {
        return this.symbolService.queryStockRankingUpDown(message.data);
      } else if (message.uri === '/api/v2/market/stock/ranking/top') {
        return this.symbolService.queryStockRankingTop(message.data);
      } else if (message.uri === '/api/v2/market/topForeignerTrading') {
        return this.symbolService.queryTopForeignerTrading(message.data);
      } else if (message.uri === '/api/v2/market/indexStockList/{indexCode}') {
        return this.symbolService.queryIndexStockList(message.data);
      } else if (message.uri === '/api/v2/market/dailyReturns') {
        return this.symbolService.querySymbolDailyReturns(message.data);
      } else if (message.uri === '/api/v2/market/symbol/tickSizeMatch') {
        return this.symbolService.querySymbolTickSizeMatch(message.data);
      } else if (message.uri === '/api/v2/market/symbol/foreignerSummary') {
        return this.symbolService.queryForeignerSummary(message.data);
      } else if (message.uri === '/api/v2/market/putthrough/dealTotal') {
        return this.symbolService.queryPtDealTotal(message.data);
      } else if (message.uri === '/api/v2/market/stock/ranking/period') {
        return this.symbolService.queryStockRankingPeriod(message.data);
      } else if (message.uri === '/api/v2/market/index/list') {
        return this.symbolService.queryIndexList(message.data);
      } else if (message.uri === '/api/v2/market/symbol/{symbol}/right') {
        return this.symbolService.querySymbolRight(message.data);
      } else if (message.uri === '/api/v2/market/ranking/foreigner') {
        return this.symbolService.queryForeignerRanking(message.data);
      } else if (message.uri === '/api/v1/equity/account/notification') {
        return this.symbolService.queryAccountNotification(message.data);
      }

      // Fix
      else if (message.uri === '/api/v2/fix/securitiesList') {
        return this.fixService.queryFixSymbolList(message.data);
      }
      // trading view
      else if (message.uri === '/api/v2/tradingview/config') {
        return this.feedService.queryConfig();
      } else if (message.uri === '/api/v2/tradingview/symbols') {
        return this.feedService.querySymbolInfo(message.data);
      } else if (message.uri === '/api/v2/tradingview/search') {
        return this.feedService.querySymbolSearch(message.data);
      } else if (message.uri === '/api/v2/tradingview/history') {
        return this.feedService.queryTradingViewHistory(message.data);
      } else if (message.uri === '/api/v2/tradingview/marks') {
        return this.feedService.querySymbolHistoryEvents(message.data);
      }
      // Save load charts
      else if (message.uri === '/api/v2/tradingview/charts/save') {
        if (!Utils.isEmpty(message.data.chart)) {
          return this.chartService.updateChart(message.data);
        } else {
          return this.chartService.saveChart(message.data);
        }
      } else if (message.uri === '/api/v2/tradingview/charts/load') {
        if (!Utils.isEmpty(message.data.chart)) {
          return this.chartService.loadChart(message.data);
        } else {
          return this.chartService.listChart(message.data);
        }
      } else if (message.uri === '/api/v2/tradingview/charts/delete') {
        return this.chartService.deleteChart(message.data);
      } else if (message.uri === '/api/v2/market/liquidity') {
        return this.chartService.queryMarketLiquidity(message.data);
      }
      // init symbol daily returns
      else if (message.uri === '/api/v2/market/dailyReturns/init') {
        return this.symbolService.initSymbolDailyReturns(message.data);
      } else if (message.uri === '/api/v2/market/topAiRating') {
        return this.topAiRatingService.queryTopAiRating(message.data);
      } else if (message.uri === '/api/v2/market/lastTradingDate') {
        return this.marketInfoService.getLastTradingDate();
      } else if (message.uri === '/api/v2/market/currentDividendEvent') {
        return this.marketInfoService.getCurrentDividendList();
      } else if (message.uri === '/api/v2/market/crawl/daily') {
        return this.crawlDataService.crawlChartData(message.data);
      } else if (message.uri === '/api/v2/market/dividend/updatePrice') {
        return this.crawlDataService.crawlChartData(message.data);
      } else if (message.uri === 'post:/api/v1/favorite/watchlist') {
        return this.watchListService.createWatchList(message.data, `${message.transactionId}`);
      } else if (message.uri === 'put:/api/v1/favorite/watchlist') {
        return this.watchListService.editWatchList(message.data, `${message.transactionId}`);
      } else if (message.uri === 'get:/api/v1/favorite/watchlist') {
        return this.watchListService.getWatchList(message.data, `${message.transactionId}`);
      } else if (message.uri === 'delete:/api/v1/favorite/watchlist') {
        return this.watchListService.deleteWatchList(message.data, `${message.transactionId}`);
      } else if (message.uri === 'get:/api/v1/favorite/symbol') {
        return this.watchListService.getWatchListSymbols(message.data, `${message.transactionId}`);
      } else if (message.uri === 'post:/api/v1/favorite/symbol') {
        return this.watchListService.addSymbolToWatchList(message.data, `${message.transactionId}`);
      } else if (message.uri === 'delete:/api/v1/favorite/symbol') {
        return this.watchListService.removeSymbolFromWatchList(message.data, `${message.transactionId}`);
      } else if (message.uri === 'put:/api/v1/favorite/watchlist/order') {
        return this.watchListService.updateOrderSymbolWatchList(message.data, `${message.transactionId}`);
      } else if (message.uri === 'get:/api/v1/favorite/symbol/include') {
        return this.watchListService.getWatchListIncludeSymbol(message.data, `${message.transactionId}`);
      } else if (message.uri === 'get:/api/v2/market/vnindexReturn') {
        return this.symbolService.getDailyAccumulativeVNIndex(message.data, `${message.transactionId}`);
      }
    }
    return false;
  };
}
