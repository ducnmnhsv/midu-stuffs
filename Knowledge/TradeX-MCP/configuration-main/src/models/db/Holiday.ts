import { Column, Entity, PrimaryGeneratedColumn } from "typeorm";
import { Utils } from "tradex-common";
import { TradexModelsConfiguration } from "tradex-models-ts";

@Entity("t_holiday")
export default class Holiday {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column("date")
  public date: Date;

  @Column()
  public description: string;
}

export function parseToHolidayResponse(
  holiday: Holiday,
): TradexModelsConfiguration.HolidayListResponse {
  return {
    id: holiday.id,
    date: Utils.formatDateToDisplay(holiday.date),
    description: holiday.description,
  };
}
