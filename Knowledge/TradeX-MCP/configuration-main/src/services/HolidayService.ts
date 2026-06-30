import { Service } from "typedi";
import { TradexModelsConfiguration } from "tradex-models-ts";
import { HolidayRepository } from "../repositories/HolidayRepository";
import Holiday, { parseToHolidayResponse } from "../models/db/Holiday";

/* eslint-disable @typescript-eslint/no-unused-vars */
@Service()
export default class HolidayService {
  public async findAllHoliday(
    request: TradexModelsConfiguration.HolidayListRequest,
  ): Promise<TradexModelsConfiguration.HolidayListResponse> {
    /* eslint-enable @typescript-eslint/no-unused-vars */
    const holidays: Holiday[] = await HolidayRepository.find();
    return holidays.map(parseToHolidayResponse);
  }
}
