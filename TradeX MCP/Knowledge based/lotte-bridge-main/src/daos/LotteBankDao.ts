import { Inject, Service } from 'typedi';
import LotteCommonDao from './LotteCommonDao';
import { IContext } from '../models/IContext';
import config from '../config';
import { IEkycLotteBankListResponse } from '../models/response/lotte/IEkycLotteBankListResponse';
import { ILotteEkycBankListRequest } from '../models/request/lotte/ILotteEkycBankListRequest';
import { ILotteEkycBanksBranchListRequest } from '../models/request/lotte/ILotteEkycBanksBranchListRequest';
import { ILotteCheckAccountExistRequest } from '../models/request/lotte/ILotteCheckAccountExistRequest';
import { ILotteCheckAccountExistResponse } from '../models/response/lotte/ILotteCheckAccountExistResponse';
import { ILotteEkycPartnerNameRequest } from '../models/request/lotte/ILotteEkycPartnerNameRequest';
import { IEkycLottePartnerNameResponse } from '../models/response/lotte/IEkycLottePartnerNameResponse';

@Service()
export class LotteBankDao {
  @Inject()
  private lotteCommonDao: LotteCommonDao;

  getBankList(request: ILotteEkycBankListRequest, ctx: IContext): Promise<IEkycLotteBankListResponse> {
    return this.lotteCommonDao.get<IEkycLotteBankListResponse>(
      config.lotte.apis.getBankBranchs,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  getBanksListBranch(request: ILotteEkycBanksBranchListRequest, ctx: IContext): Promise<IEkycLotteBankListResponse> {
    return this.lotteCommonDao.get<IEkycLotteBankListResponse>(
      config.lotte.apis.getBankBranchs,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  checkAccountExist(request: ILotteCheckAccountExistRequest, ctx: IContext): Promise<ILotteCheckAccountExistResponse> {
    return this.lotteCommonDao.post<ILotteCheckAccountExistResponse>(
      config.lotte.apis.checkAccountExist,
      null,
      request,
      ctx
    );
  }

  getPartnerName(request: ILotteEkycPartnerNameRequest, ctx: IContext): Promise<IEkycLottePartnerNameResponse> {
    return this.lotteCommonDao.get<IEkycLottePartnerNameResponse>(
      config.lotte.apis.getPartnerName,
      null,
      request,
      null,
      null,
      ctx
    );
  }

  checkAccountOpeningStatus(
    request: ILotteCheckAccountExistRequest,
    ctx: IContext
  ): Promise<ILotteCheckAccountExistResponse> {
    return this.lotteCommonDao.get<ILotteCheckAccountExistResponse>(
      config.lotte.apis.checkAccountOpeningStatus,
      null,
      request,
      null,
      null,
      ctx
    );
  }
}
