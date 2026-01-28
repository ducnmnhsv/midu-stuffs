import { IMessage } from 'tradex-common/build/src/modules/kafka';

export interface IContext {
  id: string;
  txId: string;
  orgMsg: IMessage;
}
