import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToMany,
  ManyToOne,
  JoinColumn,
} from "typeorm";
import LangTranslate from "./LangTranslate";
import LangNamespace from "./LangNamespace";

@Entity("t_lang_key")
export default class LangKey {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public key: string;

  @Column({ name: "namespace_id" })
  public namespaceId: number;

  @ManyToOne(
    (objType: any) => LangNamespace,
    (langNamespace: LangNamespace) => langNamespace.langKeys,
  )
  @JoinColumn({
    name: "namespace_id",
    referencedColumnName: "id",
  })
  public langNamespace: LangNamespace;

  @OneToMany(
    (objType: any) => LangTranslate,
    (langTranslate: LangTranslate) => langTranslate.langKey,
  )
  public langTranslates: LangTranslate[];
}
