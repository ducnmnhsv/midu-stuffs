import { Entity, PrimaryGeneratedColumn, Column } from "typeorm";

@Entity("t_template_resource")
export default class TemplateResource {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public name: string;

  @Column({ name: "ms_name" })
  public msName: string;

  @Column()
  public description: string;

  @Column()
  public version: string;

  @Column({ name: "is_latest" })
  public isLatest: boolean;

  @Column()
  public url: string;

  @Column()
  public lang: string;
}
