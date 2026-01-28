import { Column, Entity, PrimaryGeneratedColumn } from "typeorm";
import { Utils } from "tradex-common";
import { TradexModelsConfiguration } from "tradex-models-ts";

@Entity("t_interest_info")
export default class InterestInfo {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column("date", { name: "start_date" })
  public startDate: Date;

  @Column("date", { name: "end_date" })
  public endDate: Date;

  @Column()
  public value: number;
}

export function parseInterestInfos(
  interestInfos: InterestInfo[],
): TradexModelsConfiguration.QueryInterestInfoResponse {
  return interestInfos.map((interestInfo: InterestInfo) => ({
    id: interestInfo.id,
    startDate: Utils.formatDateToDisplay(interestInfo.startDate),
    endDate: Utils.formatDateToDisplay(interestInfo.endDate),
    value: interestInfo.value,
  }));
}
