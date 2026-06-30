import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
} from "typeorm";
import LangNamespace from "./LangNamespace";

@Entity("t_lang_resource_file")
export default class LangResourceFile {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "namespace_id" })
  public namespaceId: number;

  @Column()
  public url: string;

  @Column()
  public lang: string;

  @ManyToOne(
    (objType: any) => LangNamespace,
    (langNamespace: LangNamespace) => langNamespace.langResourceFiles,
  )
  @JoinColumn({
    name: "namespace_id",
    referencedColumnName: "id",
  })
  public langNamespace: LangNamespace;
}
