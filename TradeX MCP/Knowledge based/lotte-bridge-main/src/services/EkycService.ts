import { Inject, Service } from 'typedi';
import { LotteBankDao } from '../daos/LotteBankDao';
import { IContext } from '../models/IContext';
import { IEkycBankListResponse } from '../models/response/IEkycBankListResponse';
import { ILotteEkycBankListRequest } from '../models/request/lotte/ILotteEkycBankListRequest';
import {
  IEkycLotteBankListResponse,
  IEkycLotteBankListResponseData,
} from '../models/response/lotte/IEkycLotteBankListResponse';
import { ILotteEkycBanksBranchListRequest } from '../models/request/lotte/ILotteEkycBanksBranchListRequest';
import { ICheckAccountExistRequest } from '../models/request/ICheckAccountExistRequest';
import { ILotteCheckAccountExistRequest } from '../models/request/lotte/ILotteCheckAccountExistRequest';
import { ILotteCheckAccountExistResponse } from '../models/response/lotte/ILotteCheckAccountExistResponse';
import { ICheckAccountExistResponse } from '../models/response/ICheckAccountExistResponse';
import { parseMessages } from '../utils/lotte';
import { IBanksListBranchRequest } from '../models/request/IBanksListBranchRequest';
import { IEkycBranchListResponse } from '../models/response/IEkycBranchListResponse';
import { IEkycPartnerNameRequest } from '../models/request/IEkycPartnerNameRequest';
import { IEkycPartnerNameResponse } from '../models/response/IEkycPartnerNameResponse';
import { Errors, Kafka, Logger, Utils } from 'tradex-common';
import { ILotteEkycPartnerNameRequest } from '../models/request/lotte/ILotteEkycPartnerNameRequest';
import { IEkycLottePartnerNameResponse } from '../models/response/lotte/IEkycLottePartnerNameResponse';
import { Constants } from '../constants/Constants';
import { IInternalGetEKycRequest } from '../models/request/IInternalGetEKycRequest';
import { IInternalGetEKycResponse } from '../models/response/IInternalGetEKycResponse';

const { GeneralError, InvalidParameterError } = Errors;
const { validate } = Utils;

@Service()
export class EkycService {
  @Inject()
  private lotteBankDao: LotteBankDao;

  async getBankList(ctx: IContext): Promise<IEkycBankListResponse[]> {
    const lotteResquest: ILotteEkycBankListRequest = {
      code_tp: 'bank_cd_off',
    };
    const lotteRes: IEkycLotteBankListResponse = await this.lotteBankDao.getBankList(lotteResquest, ctx);
    const bankList: IEkycBankListResponse[] = lotteRes.data_list.map(
      (item: IEkycLotteBankListResponseData): IEkycBankListResponse => ({
        bankCode: item.code,
        bankName: item.code_nm,
      })
    );
    return bankList;
  }

  async getListBranch(ctx: IContext): Promise<IEkycBranchListResponse[]> {
    const lotteResquest: ILotteEkycBankListRequest = {
      code_tp: 'brch_cd',
    };
    const lotteRes: IEkycLotteBankListResponse = await this.lotteBankDao.getBankList(lotteResquest, ctx);
    const bankList: IEkycBranchListResponse[] = lotteRes.data_list.map(
      (item: IEkycLotteBankListResponseData): IEkycBranchListResponse => ({
        branchCode: item.code,
        branchName: item.code_nm,
      })
    );
    return bankList;
  }

  async getBanksListBranch(request: IBanksListBranchRequest, ctx: IContext): Promise<IEkycBranchListResponse[]> {
    const lotteResquest: ILotteEkycBanksBranchListRequest = {
      code_tp: 'bank_brch',
      bank_code: request.id,
    };
    const lotteRes: IEkycLotteBankListResponse = await this.lotteBankDao.getBanksListBranch(lotteResquest, ctx);
    const bankList: IEkycBranchListResponse[] = lotteRes.data_list.map(
      (item: IEkycLotteBankListResponseData): IEkycBranchListResponse => ({
        branchCode: item.code,
        branchName: item.code_nm,
      })
    );
    return bankList;
  }

  async checkNationalId(id: ICheckAccountExistRequest, ctx: IContext): Promise<ICheckAccountExistResponse> {
    const internalRequest: IInternalGetEKycRequest = {
      identifierId: id.identifierId,
      headers: ctx.orgMsg.data.headers,
    };
    const msg: Kafka.IMessage = await Kafka.getInstance().sendRequestAsync(
      ctx.txId,
      'ekyc-admin',
      'internal:/api/v1/ekycs/get',
      internalRequest
    );
    const { data } = msg;
    if (data.status) {
      throw new Errors.GeneralError(data.status);
    }
    const dataResponse: IInternalGetEKycResponse = data.data;
    if (dataResponse.status === 'WAITING_CONFIRMATION') {
      throw new Errors.GeneralError('EKYC_WAITING_CONFIRMATION');
    }
    const lotteResquest: ILotteCheckAccountExistRequest = {
      idno: id.identifierId,
    };
    const lotteRes: ILotteCheckAccountExistResponse = await this.lotteBankDao.checkAccountExist(lotteResquest, ctx);
    const { codes } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    if (lotteRes.error_code === '0000') {
      const bankList: ICheckAccountExistResponse = {
        exist: false,
      };
      return bankList;
    } else {
      throw new GeneralError(`${codes}`);
    }
  }

  async getPartnerName(request: IEkycPartnerNameRequest, ctx: IContext): Promise<IEkycPartnerNameResponse> {
    const error = new InvalidParameterError();
    validate(request.partnerId, 'partnerId')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const partnerNameRequest: ILotteEkycPartnerNameRequest = {
      emp_no: request.partnerId,
    };
    if (request.branchCode != null && request.branchCode.trim().length > 0) {
      partnerNameRequest.brch_cd = request.branchCode;
    }
    const lotteRes: IEkycLottePartnerNameResponse = await this.lotteBankDao.getPartnerName(partnerNameRequest, ctx);
    const { codes, messages } = parseMessages(lotteRes.error_desc, lotteRes.error_code);
    Logger.info(ctx.id, 'codes', codes, 'messages', messages);
    if (lotteRes.error_code === '0000') {
      if (lotteRes.data_list && lotteRes.data_list[0] && lotteRes.data_list[0].code_nm) {
        return {
          name: lotteRes.data_list[0].code_nm,
        };
      }
      throw new GeneralError(`${Constants.PARTNER_NOT_FOUND}`);
    } else {
      throw new GeneralError(`${codes}`);
    }
  }

  async checkAccountOpeningStatus(
    request: ICheckAccountExistRequest,
    ctx: IContext
  ): Promise<ICheckAccountExistResponse> {
    const error = new InvalidParameterError();
    validate(request.identifierId, 'identifierId')
      .setRequire()
      .throwValid(error);
    error.throwErr();

    const lotteResquest: ILotteCheckAccountExistRequest = {
      idno: request.identifierId,
    };
    const lotteRes: ILotteCheckAccountExistResponse = await this.lotteBankDao.checkAccountOpeningStatus(
      lotteResquest,
      ctx
    );

    if (lotteRes.error_code === '0000') {
      return {
        exist: true,
      };
    } else if (lotteRes.error_code === '1005') {
      return {
        exist: false,
      };
    } else {
      throw new GeneralError(`${Constants.CHECK_ACCOUNT_OPEN_STATUS}`);
    }
  }
}
