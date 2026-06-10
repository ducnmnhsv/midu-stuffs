import * as moment from 'moment';
import { Utils } from 'tradex-common';
import { MARKET_TIMEZONE } from '../constants';
import config from '../config';

export const defaultBaseTime = (format: string = Utils.TIME_DISPLAY_FORMAT): string => {
  return moment(Utils.getEndOfDate(new Date())).format(format);
};

export const defaultBaseDate = (): Date => {
  const baseDate: Date = new Date();
  baseDate.setHours(baseDate.getHours() + MARKET_TIMEZONE);
  baseDate.setDate(baseDate.getDate() + 1); // default baseDate = tomorrow
  return Utils.getStartOfDate(baseDate);
};

export const isHoliday = (inputDate?: Date): boolean => {
  const date: Date = inputDate == null ? new Date() : inputDate;
  const dateStr = Utils.formatDateToDisplay(date);
  const holidaysStr: string[] = config.holidays;
  return holidaysStr.indexOf(dateStr) !== -1;
};
