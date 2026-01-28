import { Column, CreateDateColumn, Entity, PrimaryColumn, UpdateDateColumn } from 'typeorm';

@Entity('t_header_token_user_data')
export class HeaderTokenUserData {
  @PrimaryColumn({ type: 'varchar' })
  accountNumber: string;

  @Column({ type: 'varchar' })
  userData: string;

  @CreateDateColumn({ type: 'timestamp' })
  createdAt: Date = new Date();

  @UpdateDateColumn({ type: 'timestamp' })
  updatedAt: Date = new Date();
}
