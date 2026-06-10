import { IDataRequest } from 'tradex-common/build/src/modules/models';

export interface IInternalGetEKycRequest extends IDataRequest {
  identifierId?: string;
}
