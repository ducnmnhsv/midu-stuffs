import { FilterQuery, UpdateQuery } from 'mongodb';
import { Errors, Logger, Utils } from 'tradex-common';
import { Inject, Service } from 'typedi';
import { IUpdatePriceAfterDividendRequest } from '../models/request/IUpdatePriceAfterDividendRequest';
import { ISymbolDaily } from '../models/db/ISymbolDaily';
import { SymbolDailyRepository } from '../repositories/SymbolDailyRepository';
import { SymbolQuoteMinutesRepository } from '../repositories/SymbolQuoteMinutesRepository';

@Service()
export default class DividendService {
  @Inject()
  private readonly symbolDailyRepository: SymbolDailyRepository;
  @Inject()
  private readonly symbolQuoteMinuteRepository: SymbolQuoteMinutesRepository;
  // @Inject()
  // private readonly symbolHistoryEventsRepo: SymbolHistoryEventsRepository;

  public async updateDividendPrice(request: IUpdatePriceAfterDividendRequest) {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.code, 'code').setRequire().throwValid(invalidParams);
    Utils.validate(request.date, 'date').setRequire().throwValid(invalidParams);
    Utils.validate(request.open, 'open').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const startOfDateFilter: Date = Utils.convertStringToDate(request.date);
    const endOfDateFilter: Date = Utils.getEndOfDate(startOfDateFilter);

    const dailyInfoFilter: FilterQuery<ISymbolDaily> = {
      code: request.code,
      date: {
        $gte: startOfDateFilter,
        $lte: endOfDateFilter,
      },
    };

    const dailyInfos: ISymbolDaily[] = await this.symbolDailyRepository.findBy(dailyInfoFilter, 1).toArray();
    if (dailyInfos == null || dailyInfos.length === 0) {
      Logger.error('fail to found daily record');
      throw new Errors.ObjectNotFoundError();
    }

    const dailyInfo: ISymbolDaily = dailyInfos[0];
    const rate: number = request.open / dailyInfo.open;

    const filterUpdate: FilterQuery<ISymbolDaily> = {
      code: request.code,
      date: {
        $lte: endOfDateFilter,
      },
    };

    const updateQuery: UpdateQuery<ISymbolDaily> = {
      $mul: {
        open: rate,
        high: rate,
        low: rate,
        close: rate,
        last: rate,
        change: rate,
      },
    };

    this.symbolDailyRepository.updateMany(filterUpdate, updateQuery).catch((err: Error) => {
      throw err;
    });

    this.symbolQuoteMinuteRepository.updateMany(filterUpdate, updateQuery).catch((err: Error) => {
      throw err;
    });
  }
}
