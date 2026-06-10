import {Models} from 'tradex-common';

export default interface IUpdateProfileRequest extends Models.IDataRequest {
  avatar?: string;
}
