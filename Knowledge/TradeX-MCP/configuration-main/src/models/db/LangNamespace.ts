import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
  OneToMany,
} from "typeorm";
import LangResource from "./LangResource";
import LangResourceFile from "./LangResourceFile";
import LangKey from "./LangKey";

@Entity("t_lang_namespace")
export default class LangNamespace {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public namespace: string;

  @Column()
  public description: string;

  @Column({ name: "resource_id" })
  public resourceId: number;

  @ManyToOne(
    (objType: any) => LangResource,
    (langResource: LangResource) => langResource.langNamespaces,
  )
  @JoinColumn({
    name: "resource_id",
    referencedColumnName: "id",
  })
  public langResource: LangResource;

  @OneToMany(
    (objType: any) => LangResourceFile,
    (langResourceFile: LangResourceFile) => langResourceFile.langNamespace,
  )
  public langResourceFiles: LangResourceFile[];

  @OneToMany(
    (objType: any) => LangKey,
    (langKey: LangKey) => langKey.langNamespace,
  )
  public langKeys: LangKey[];
}
