import { AccountBankInfo } from '../models/db/AccountBankInfo';
import { Service } from 'typedi';
import { EntityRepository, Repository } from 'typeorm';

@Service()
@EntityRepository(AccountBankInfo)
export class AccountBankInfoRepository extends Repository<AccountBankInfo> {}
