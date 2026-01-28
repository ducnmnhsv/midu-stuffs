import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  JoinColumn,
  ManyToOne,
} from "typeorm";
import Faq from "./Faq";

@Entity("t_faq_review")
export default class FaqReview {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column({ name: "is_useful" })
  public isUseful: boolean;

  @Column({ name: "faq_id" })
  public faqId: number;

  @Column({ name: "created_by" })
  public userId: number;

  @ManyToOne((objType: any) => Faq)
  @JoinColumn({
    name: "faq_id",
    referencedColumnName: "id",
  })
  public faq: Faq;
}
