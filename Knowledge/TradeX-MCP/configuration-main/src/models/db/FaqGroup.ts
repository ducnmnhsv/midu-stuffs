import { Entity, PrimaryGeneratedColumn, Column, OneToMany } from "typeorm";
import Faq from "./Faq";

@Entity("t_faq_group")
export default class FaqGroup {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public name: string;

  @Column()
  public lang: string;

  @Column({ name: "ms_name" })
  public msName: string;

  @OneToMany((objType: any) => Faq, (faq: Faq) => faq.faqGroup)
  public faqs: Faq[];
}
