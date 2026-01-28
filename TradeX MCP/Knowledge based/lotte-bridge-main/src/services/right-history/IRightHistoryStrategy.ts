import { IContext } from '../../models/IContext';
import {
  IRightHistoryBaseItem,
  IRightHistoryBaseRequest,
  IRightHistoryBaseResponse,
} from '../../models/right-history/base';

// Base strategy interface for right history services
export interface IRightHistoryStrategy {
  // Process right history request
  processRightHistory(
    request: IRightHistoryBaseRequest,
    ctx: IContext
  ): Promise<IRightHistoryBaseResponse<IRightHistoryBaseItem>>;
}
