import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
} from "typeorm";
import LangResource from "./LangResource";

@Entity("t_lang_resource_version")
export default class LangResourceVersion {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public lang: string;

  @Column()
  public version: string;

  @Column({ name: "resource_id" })
  public resourceId: number;

  @ManyToOne(
    (objType: any) => LangResource,
    (langResource: LangResource) => langResource.langResourceVersions,
  )
  @JoinColumn({
    name: "resource_id",
    referencedColumnName: "id",
  })
  public langResource: LangResource;
}
