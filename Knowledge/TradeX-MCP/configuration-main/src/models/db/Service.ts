import { Entity, PrimaryGeneratedColumn, Column } from "typeorm";

@Entity("t_service")
export default class Service {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "service_name" })
  public serviceName: string;

  @Column({ name: "service_code" })
  public serviceCode: string;

  @Column({ name: "support_phone" })
  public supportPhone: string;

  @Column({ name: "support_email" })
  public supportEmail: string;

  @Column({ name: "logo_url" })
  public logoUrl: string;
}
