import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  JoinColumn,
  ManyToOne,
} from "typeorm";
import FaqGroup from "./FaqGroup";

@Entity("t_faq")
export default class Faq {
  @PrimaryGeneratedColumn()
  public id: number;

  @Column()
  public question: string;

  @Column()
  public answer: string;

  @Column({ name: "group_id" })
  public groupId: number;

  @ManyToOne((objType: any) => FaqGroup, (faqGroup: FaqGroup) => faqGroup.faqs)
  @JoinColumn({
    name: "group_id",
    referencedColumnName: "id",
  })
  public faqGroup: FaqGroup;
}
