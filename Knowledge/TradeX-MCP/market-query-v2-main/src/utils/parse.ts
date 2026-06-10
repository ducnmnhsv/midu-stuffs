import { BaseRequest } from 'tradex-models-common';
import { Errors } from 'tradex-common';
import * as Ajv from 'ajv';
import { INVALID_PARAMETER } from '../constants';
import { ISymbolQuoteMinutes } from '../models/db/ISymbolQuoteMinutes';
import * as moment from 'moment';
import { ISymbolDaily } from '../models/db/ISymbolDaily';
import { ISymbolQuote } from '../models/db/ISymbolQuote';

export const validateRequest = (request: BaseRequest, validatorFunc: CallableFunction) => {
  const validator: Ajv.ValidateFunction = validatorFunc();
  if (!validator(request)) {
    throw new Errors.GeneralError(INVALID_PARAMETER, validator.errors);
  }
};

export const getKeySymbolQuoteTick = (item: ISymbolQuote, tickUnit: number): string => {
  return `${item.code}_${Math.trunc(item.sequence / tickUnit)}`;
};

export const getKeySymbolQuoteMinute = (item: ISymbolQuoteMinutes, minuteUnit: number): string => {
  return `${item.code}_${Math.trunc(item.date.getTime() / minuteUnit / 60 / 1000)}`;
};

export const getWeekKey = (item: ISymbolDaily): string => {
  // return `${item.code}_${moment(item.date).year()}_${moment(item.date).week()}`;
  return `${item.code}_${moment(item.date).isoWeekYear()}_${moment(item.date).isoWeek()}`;
};

export const getMonthKey = (item: ISymbolDaily): string => {
  return `${item.code}_${moment(item.date).year()}_${moment(item.date).month()}`;
};

export const getSixMonthKey = (item: ISymbolDaily): string => {
  return `${item.code}_${moment(item.date).year()}_${Math.trunc(moment(item.date).month() / 6)}`;
};

export const checkDate = (date: string): string => {
  return date.trim() === '' || date.replace('00000000', '') === '' ? null : date;
};
