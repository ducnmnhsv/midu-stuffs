import { Entity, PrimaryGeneratedColumn, Column } from "typeorm";
import { DataViewTypeEnum } from "../../constants/DataViewTypeEnum";
import { DataViewStatusEnum } from "../../constants/DataViewStatusEnum";

@Entity("t_data_view")
export default class DataView {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public code: string;

  @Column({ name: "view_name" })
  public viewName: string;

  @Column("enum", { enum: DataViewTypeEnum, name: "type" })
  public dataViewType: DataViewTypeEnum;

  @Column({ name: "is_translated" })
  public isTranslated: boolean;

  @Column("enum", { enum: DataViewStatusEnum, name: "status" })
  public status: DataViewStatusEnum;

  @Column({ type: "json", name: "filter_fields" })
  public filterFields: string[];
}
