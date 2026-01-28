import { Column, CreateDateColumn, Entity, PrimaryColumn, UpdateDateColumn } from 'typeorm';

@Entity('t_account_bank_info')
export class AccountBankInfo {
  @PrimaryColumn({ type: 'varchar' })
  username: string;

  @PrimaryColumn({ type: 'varchar' })
  subNumber: string;

  @PrimaryColumn({ type: 'varchar' })
  bankCode: string;

  @Column({ type: 'varchar' })
  bankName: string;

  @Column({ type: 'varchar', nullable: true })
  bankAccount: string;

  @CreateDateColumn({ type: 'datetime' })
  createdAt: Date = new Date();

  @UpdateDateColumn({ type: 'datetime' })
  updatedAt: Date = new Date();
}
