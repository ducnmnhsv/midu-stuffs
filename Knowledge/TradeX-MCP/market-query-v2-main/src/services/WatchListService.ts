import { Inject, Service } from 'typedi';
import { IWatchListRequest } from '../models/request/IWatchListRequest';
import { IBaseWatchListResponse } from '../models/response/IBaseWatchListResponse';
import { IWatchListResponse } from '../models/response/IWatchListResponse';
import { WatchListRepository } from '../repositories/WatchListRepository';
import { IWatchList } from '../models/db/IWatchList';
import { InsertOneWriteOpResult } from 'mongodb';
import { Utils, Errors, Models, Logger } from 'tradex-common';
import { IWatchListSymbolRequest } from '../models/request/IWatchListSymbolRequest';
import { MAX_SYMBOL_WATCHLIST, MAX_WATCHLIST } from '../constants';
import { IAddSymbolWatchListRequest } from '../models/request/IAddSymbolWatchListRequest';
import { IRemoveSymbolWatchListRequest } from '../models/request/IRemoveSymbolWatchListRequest';
import RedisService from './RedisService';
import { ISymbolInfo } from '../models/db/ISymbolInfo';
import { IUpdateOrderWatchListRequest } from '../models/request/IUpdateOrderWatchListRequest';
import { IWatchListIncludeSymbolRequest } from '../models/request/IWatchListIncludeSymbolRequest';

const { validate } = Utils;
const { GeneralError, InvalidParameterError, InvalidFieldValueError } = Errors;

@Service()
export default class WatchListService {
  @Inject()
  private watchListRepository: WatchListRepository;
  @Inject()
  private redisService: RedisService;

  // eslint-disable-next-line no-unused-vars
  public async createWatchList(request: IWatchListRequest, msgId: string): Promise<IBaseWatchListResponse> {
    const invalidParams = new InvalidParameterError();
    validate(request.watchlistName, 'watchlistName').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const now: Date = new Date();
    const username: string = request.headers.token.userData.username;
    const watchlistName: string = request.watchlistName.replace(/\s\s/g, ' ').trim();
    if (watchlistName.length === 0) {
      throw new InvalidFieldValueError('watchlistName', request.watchlistName);
    }
    const existName: IWatchList = await this.watchListRepository.findOneBy({
      username: username,
      watchlistName: watchlistName,
      deletedAt: null,
    });
    if (existName != null) {
      throw new GeneralError('WATCHLIST_NAME_EXIST');
    }
    const count: number = await this.watchListRepository.findBy({ username: username, deletedAt: null }).count();
    if (count >= MAX_WATCHLIST) {
      throw new GeneralError('MAX_20_WATCHLISTS');
    }
    const watchList: IWatchList = {
      username: username,
      watchlistName: watchlistName,
      symbols: [],
      order: count + 1,
      createdAt: now,
      updatedAt: now,
    };
    const result: InsertOneWriteOpResult<any> = await this.watchListRepository.save(watchList);
    return {
      message: 'WATCHLIST_CREATE_SUCCESS',
      watchlistId: result.insertedId,
    };
  }

  // eslint-disable-next-line no-unused-vars
  public async editWatchList(request: IWatchListRequest, msgId: string): Promise<IBaseWatchListResponse> {
    const invalidParams = new InvalidParameterError();
    validate(request.watchlistName, 'watchlistName').setRequire().throwValid(invalidParams);
    validate(request.watchlistId, 'watchlistId').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const username: string = request.headers.token.userData.username;
    const now: Date = new Date();
    const watchlistName: string = request.watchlistName.replace(/\s\s/g, ' ').trim();
    if (watchlistName.length === 0) {
      throw new InvalidFieldValueError('watchlistName', request.watchlistName);
    }
    const existName: IWatchList = await this.watchListRepository.findOneBy({
      username: username,
      watchlistName: watchlistName,
      deletedAt: null,
    });
    if (existName != null && existName._id !== request.watchlistId) {
      throw new GeneralError('WATCHLIST_NAME_EXIST');
    }
    const watchList: IWatchList = await this.watchListRepository.findOneBy({
      username: username,
      _id: request.watchlistId,
      deletedAt: null,
    });
    if (watchList == null) {
      throw new GeneralError('WATCHLIST_INVALID');
    }
    await this.watchListRepository.update(
      {
        _id: watchList._id,
      },
      {
        $set: {
          watchlistName: watchlistName,
          updatedAt: now,
        },
      },
    );

    return {
      message: 'WATCHLIST_MODIFY_SUCCESS',
      watchlistId: watchList._id,
    };
  }

  // eslint-disable-next-line no-unused-vars
  public async getWatchList(request: Models.IDataRequest, msgId: string): Promise<IWatchListResponse[]> {
    const watchList: IWatchList[] = await this.watchListRepository
      .findBy({ username: request.headers.token.userData.username, deletedAt: null })
      .sort({ order: 1 })
      .toArray();

    return watchList.map((item: IWatchList) => ({
      watchlistId: item._id,
      watchlistName: item.watchlistName,
      numberOfStocks: item.symbols.length,
    }));
  }

  // eslint-disable-next-line no-unused-vars
  public async deleteWatchList(request: IWatchListRequest, msgId: string): Promise<IBaseWatchListResponse> {
    const invalidParams = new InvalidParameterError();
    validate(request.watchlistId, 'watchlistId').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const username: string = request.headers.token.userData.username;
    const now: Date = new Date();
    const watchList: IWatchList = await this.watchListRepository.findOneBy({
      username: username,
      _id: request.watchlistId,
    });
    if (watchList == null) {
      throw new GeneralError('WATCHLIST_INVALID');
    }
    if (watchList.deletedAt != null) {
      throw new GeneralError('WATCHLIST_HAS_BEEN_DELETED');
    }
    await this.watchListRepository.update(
      {
        _id: watchList._id,
      },
      {
        $set: {
          deletedAt: now,
          order: null,
        },
      },
    );
    await this.watchListRepository.updateMany(
      {
        username: username,
        order: { $gt: watchList.order },
        deletedAt: null,
      },
      {
        $inc: { order: -1 },
      },
    );
    return {
      message: 'WATCHLIST_DELETE_SUCCESS',
      watchlistId: request.watchlistId,
    };
  }

  // eslint-disable-next-line no-unused-vars
  public async getWatchListSymbols(request: IWatchListSymbolRequest, msgId: string): Promise<string[]> {
    const invalidParams = new InvalidParameterError();
    validate(request.watchlistId, 'watchlistId').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const username: string = request.headers.token.userData.username;
    const watchList: IWatchList = await this.watchListRepository.findOneBy({
      username: username,
      _id: request.watchlistId,
    });
    if (watchList == null) {
      throw new GeneralError('WATCHLIST_INVALID');
    }
    if (watchList.deletedAt != null) {
      throw new GeneralError('WATCHLIST_HAS_BEEN_DELETED');
    }
    return watchList.symbols.map((symbol: string) => symbol).reverse();
  }

  // eslint-disable-next-line no-unused-vars
  public async addSymbolToWatchList(request: IAddSymbolWatchListRequest, msgId: string): Promise<IBaseWatchListResponse> {
    const invalidParams = new InvalidParameterError();
    validate(request.watchlistId, 'watchlistId').setRequire().throwValid(invalidParams);
    validate(request.symbol, 'symbol').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const username: string = request.headers.token.userData.username;
    const watchList: IWatchList[] = await this.watchListRepository
      .findBy({
        username: username,
        _id: { $in: request.watchlistId },
      })
      .toArray();
    if (watchList == null || watchList.length < 1) {
      throw new GeneralError('WATCHLIST_INVALID');
    }
    let listPromise: Promise<any>[] = request.symbol.map(
      (symbol: string) =>
        new Promise<ISymbolInfo>((resolve, reject) => {
          this.redisService
            .getSymbolInfo(symbol)
            .then((symbolInfo: ISymbolInfo) => {
              if (symbolInfo == null || symbolInfo.referencePrice == null || Math.abs(symbolInfo.referencePrice) < 0.0000001) {
                reject(new GeneralError('STOCK_NOT_FOUND', [symbol]));
              }
              resolve(symbolInfo);
            })
            .catch(reject);
        }),
    );
    await Promise.all(listPromise);
    listPromise = watchList.map((item: IWatchList) => {
      const symbols: string[] = request.symbol.filter((symbol: string) => !item.symbols.includes(symbol));
      item.symbols.push(...symbols);
      if (item.symbols.length > MAX_SYMBOL_WATCHLIST) {
        throw new GeneralError('WATCHLIST_50_SYMBOL_MAX', [item._id]);
      }
      if (item.deletedAt != null) {
        throw new GeneralError('WATCHLIST_HAS_BEEN_DELETED', [item._id]);
      }
      return this.watchListRepository.update(
        {
          _id: item._id,
        },
        {
          $push: {
            symbols: { $each: symbols },
          },
        },
      );
    });
    await Promise.all(listPromise);
    return {
      message: 'ADD_SYMBOL_WATCHLIST_SUCCESS',
    };
  }

  // eslint-disable-next-line no-unused-vars
  public async removeSymbolFromWatchList(request: IRemoveSymbolWatchListRequest, msgId: string): Promise<IBaseWatchListResponse> {
    const invalidParams = new InvalidParameterError();
    validate(request.watchlistId, 'watchlistId').setRequire().throwValid(invalidParams);
    validate(request.symbol, 'symbol').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const username = request.headers.token.userData.username;
    const symbol: string = request.symbol;
    const watchList: IWatchList[] = await this.watchListRepository
      .findBy({
        username: username,
        _id: { $in: request.watchlistId },
      })
      .toArray();
    if (watchList == null || watchList.length < 1) {
      throw new GeneralError('DELETE_SYMBOL_FAILED');
    }
    const listPromise: any[] = watchList.map((item: IWatchList) => {
      if (item.deletedAt != null) {
        throw new GeneralError('WATCHLIST_HAS_BEEN_DELETED', [item._id]);
      }
      return this.watchListRepository.update(
        {
          _id: item._id,
        },
        {
          $pull: {
            symbols: symbol,
          },
        },
      );
    });
    await Promise.all(listPromise);
    return {
      message: 'DELETE_SYMBOL_SUCCESS',
    };
  }

  public async updateOrderSymbolWatchList(request: IUpdateOrderWatchListRequest, msgId: string) {
    const invalidParams = new InvalidParameterError();
    validate(request.watchlistId, 'watchlistId').setRequire().throwValid(invalidParams);
    validate(request.orderNo, 'orderNo').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const username: string = request.headers.token.userData.username;
    const watchList: IWatchList = await this.watchListRepository.findOneBy({
      username: username,
      _id: request.watchlistId,
    });
    if (watchList == null) {
      throw new GeneralError('WATCHLIST_INVALID');
    }
    if (watchList.deletedAt != null) {
      throw new GeneralError('WATCHLIST_HAS_BEEN_DELETED');
    }
    try {
      const max: number = await this.watchListRepository.findBy({ username: username, deletedAt: null }).count();
      const orderNo: number = Math.min(Math.max(request.orderNo, 1), max);
      if (orderNo < watchList.order) {
        await this.watchListRepository.updateMany(
          {
            username: username,
            order: { $gte: orderNo, $lt: watchList.order },
            deletedAt: null,
          },
          {
            $inc: { order: 1 },
          },
        );
      } else {
        await this.watchListRepository.updateMany(
          {
            username: username,
            order: { $gt: watchList.order, $lte: orderNo },
            deletedAt: null,
          },
          {
            $inc: { order: -1 },
          },
        );
      }
      await this.watchListRepository.update(
        {
          _id: watchList._id,
        },
        {
          $set: { order: orderNo },
        },
      );
    } catch (e) {
      Logger.error(`${msgId} error`, e);
      throw new GeneralError('WATCHLIST_MODIFY_ORDER_FAILED');
    }
    return {
      message: 'WATCHLIST_MODIFY_ORDER_SUCCESS',
    };
  }

  // eslint-disable-next-line no-unused-vars
  public async getWatchListIncludeSymbol(request: IWatchListIncludeSymbolRequest, msgId: string): Promise<string[]> {
    const invalidParams = new InvalidParameterError();
    validate(request.symbol, 'symbol').setRequire().throwValid(invalidParams);
    invalidParams.throwErr();
    const username: string = request.headers.token.userData.username;
    const watchList: IWatchList[] = await this.watchListRepository
      .findBy({
        username: username,
        symbols: { $in: [request.symbol] },
      })
      .toArray();
    return watchList?.map((value: IWatchList) => value._id);
  }
}
