import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn, UpdateDateColumn } from 'typeorm';

export enum ChangeBrokerStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

@Entity('t_account_change_broker_request')
export class AccountChangeBrokerRequest {
  @PrimaryGeneratedColumn({ type: 'bigint' })
  id: number;

  @Column({ name: 'core_seq_no', type: 'varchar', length: 20, nullable: true, unique: true })
  coreSeqNo: string;

  @Column({ name: 'account_no', type: 'varchar', length: 20 })
  accountNo: string;

  @Column({ name: 'customer_name', type: 'varchar', length: 100, nullable: true })
  customerName: string;

  @Column({ name: 'old_broker_id', type: 'varchar', length: 20, nullable: true })
  oldBrokerId: string;

  @Column({ name: 'old_broker_name', type: 'varchar', length: 100, nullable: true })
  oldBrokerName: string;

  @Column({ name: 'new_broker_id', type: 'varchar', length: 20 })
  newBrokerId: string;

  @Column({ name: 'new_broker_name', type: 'varchar', length: 100, nullable: true })
  newBrokerName: string;

  @Column({ name: 'reason', type: 'varchar', length: 50, nullable: true })
  reason: string;

  @Column({ name: 'user_note', type: 'text', nullable: true })
  userNote: string;

  @Column({ name: 'status', type: 'varchar', length: 20, default: ChangeBrokerStatus.PENDING })
  status: ChangeBrokerStatus;

  @Column({ name: 'reject_reason', type: 'text', nullable: true })
  rejectReason: string;

  @CreateDateColumn({ name: 'created_at', type: 'datetime' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at', type: 'datetime' })
  updatedAt: Date;

  @Column({ name: 'expired_at', type: 'datetime', nullable: true })
  expiredAt: Date;
}

