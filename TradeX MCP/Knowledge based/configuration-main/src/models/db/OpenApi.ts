import {
  Entity,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  PrimaryColumn,
} from "typeorm";

@Entity("t_open_api")
export default class OpenApi {
  @PrimaryColumn()
  public id: number;

  @Column({ name: "operation_id" })
  public operationId: string;

  @Column({ type: "json" })
  public tags: any[];

  @Column({ name: "uri_pattern" })
  public uriPattern: string;

  @Column()
  public summary: string;

  @Column()
  public description: string;

  @Column({ type: "json" })
  public security: any[];

  @Column({ type: "json" })
  public parameters: any[];

  @Column({ name: "request_body", type: "json" })
  public requestBody: any;

  @Column({ type: "json" })
  public responses: any;

  @CreateDateColumn({ name: "created_at" })
  public createdAt: Date;

  @UpdateDateColumn({ name: "updated_at" })
  public updatedAt: Date;
}
