import { AccountChangeBrokerRequest, ChangeBrokerStatus } from '../models/db/AccountChangeBrokerRequest';
import { Service } from 'typedi';
import { EntityRepository, Repository } from 'typeorm';

@Service()
@EntityRepository(AccountChangeBrokerRequest)
export class AccountChangeBrokerRequestRepository extends Repository<AccountChangeBrokerRequest> {
  async findPendingByAccountNo(accountNo: string): Promise<AccountChangeBrokerRequest | undefined> {
    return this.findOne({
      where: {
        accountNo,
        status: ChangeBrokerStatus.PENDING,
      },
    });
  }

  async findAllPending(): Promise<AccountChangeBrokerRequest[]> {
    return this.find({
      where: {
        status: ChangeBrokerStatus.PENDING,
      },
      order: {
        createdAt: 'ASC',
      },
    });
  }

  async findByCoreSeqNo(coreSeqNo: string): Promise<AccountChangeBrokerRequest | undefined> {
    return this.findOne({
      where: {
        coreSeqNo,
      },
    });
  }

  async findHistoryByAccountNo(
    accountNo: string,
    status?: ChangeBrokerStatus,
    fromDate?: Date,
    toDate?: Date,
    fetchCount?: number,
    nextKey?: string
  ): Promise<AccountChangeBrokerRequest[]> {
    const queryBuilder = this.createQueryBuilder('request')
      .where('request.accountNo = :accountNo', { accountNo });

    if (status) {
      queryBuilder.andWhere('request.status = :status', { status });
    }

    if (fromDate) {
      queryBuilder.andWhere('request.createdAt >= :fromDate', { fromDate });
    }

    if (toDate) {
      queryBuilder.andWhere('request.createdAt <= :toDate', { toDate });
    }

    if (nextKey) {
      queryBuilder.andWhere('request.id < :nextKey', { nextKey: Number(nextKey) });
    }

    const limit = fetchCount && fetchCount > 0 ? fetchCount : 20;

    return queryBuilder
      .orderBy('request.id', 'DESC')
      .take(limit + 1)
      .getMany();
  }
}

