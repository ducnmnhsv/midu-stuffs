import { Inject, Service } from 'typedi';
import { EtfNavDailyRepository } from '../repositories/EtfNavDailyRepository';
import { EtfIndexDailyRepository } from '../repositories/EtfIndexDailyRepository';
import { DEFAULT_PAGE_SIZE, INVALID_PARAMETER } from '../constants';
import { Utils, Errors } from 'tradex-common';
import * as Ajv from 'ajv';
import { toEtfIndexDailyResponse, toEtfNavDailyResponse } from '../utils/ResponseUtils';
import { EtfNavDailyRequest, EtfNavDailyResponse, EtfIndexDailyRequest, EtfIndexDailyResponse } from 'tradex-models-market';
import { etfNavDailyRequestValidator, etfIndexDailyRequestValidator } from 'tradex-models-market-validator';

@Service()
export default class EtfService {
  @Inject()
  public etfNavDailyRepository: EtfNavDailyRepository;
  @Inject()
  public etfIndexDailyRepository: EtfIndexDailyRepository;

  public async queryEtfNavDaily(request: EtfNavDailyRequest): Promise<EtfNavDailyResponse[]> {
    const validator: Ajv.ValidateFunction = etfNavDailyRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const fetchCount = request.fetchCount == null ? DEFAULT_PAGE_SIZE : request.fetchCount;

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    const baseDate: Date = Utils.isEmpty(request.baseDate) ? tomorrow : Utils.convertStringToDate(request.baseDate, Utils.DATE_DISPLAY_FORMAT);

    return this.etfNavDailyRepository
      .findBy(
        {
          code: request.symbolCode,
          date: { $lt: baseDate },
        },
        fetchCount,
        { date: -1 },
      )
      .map(toEtfNavDailyResponse)
      .toArray();
  }

  public async queryEtfIndexDaily(request: EtfIndexDailyRequest): Promise<EtfIndexDailyResponse[]> {
    const validator: Ajv.ValidateFunction = etfIndexDailyRequestValidator();
    if (!validator(request)) {
      throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
    }
    const fetchCount = request.fetchCount == null ? DEFAULT_PAGE_SIZE : request.fetchCount;

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    const baseDate: Date = Utils.isEmpty(request.baseDate) ? tomorrow : Utils.convertStringToDate(request.baseDate, Utils.DATE_DISPLAY_FORMAT);

    return this.etfIndexDailyRepository
      .findBy(
        {
          code: request.symbolCode,
          date: { $lt: baseDate },
        },
        fetchCount,
        { date: -1 },
      )
      .map(toEtfIndexDailyResponse)
      .toArray();
  }
}
