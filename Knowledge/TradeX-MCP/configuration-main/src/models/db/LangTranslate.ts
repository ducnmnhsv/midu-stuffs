import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
} from "typeorm";
import LangKey from "./LangKey";

@Entity("t_lang_translate")
export default class LangTranslate {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "key_id" })
  public keyId: number;

  @Column()
  public lang: string;

  @Column()
  public value: string;

  @ManyToOne(
    (objType: any) => LangKey,
    (langKey: LangKey) => langKey.langTranslates,
  )
  @JoinColumn({
    name: "key_id",
    referencedColumnName: "id",
  })
  public langKey: LangKey;
}
