import { Container } from 'typedi';
import { IRightHistoryStrategy } from '../IRightHistoryStrategy';
import { RightHistoryOtherService } from '../strategies/RightHistoryOtherService';
import { RightHistoryType } from '../types/RightHistoryType';
import { RightHistoryIssueService } from '../strategies/RightHistoryIssueService';
import { RightHistoryDividendService } from '../strategies/RightHistoryDividendService';
import { RightHistoryBondService } from '../strategies/RightHistoryBondService';
import { RightHistoryBonusSharesService } from '../strategies/RightHistoryBonusSharesService';
import { RightHistoryConversionService } from '../strategies/RightHistoryConversionService';
import { RightHistoryBondInterestService } from '../strategies/RightHistoryBondInterestService';

export class RightHistoryStrategyFactory {
  private static strategyMap = new Map<RightHistoryType, new () => IRightHistoryStrategy>([
    [RightHistoryType.OTHER, RightHistoryOtherService],
    [RightHistoryType.ISSUE, RightHistoryIssueService],
    [RightHistoryType.DIVIDEND, RightHistoryDividendService],
    [RightHistoryType.BONUS_SHARES, RightHistoryBonusSharesService],
    [RightHistoryType.BOND, RightHistoryBondService],
    [RightHistoryType.CONVERSION, RightHistoryConversionService],
    [RightHistoryType.BOND_INTEREST, RightHistoryBondInterestService],
  ]);

  static getStrategy(type: RightHistoryType): IRightHistoryStrategy {
    const StrategyClass = this.strategyMap.get(type);
    if (!StrategyClass) {
      throw new Error(`No strategy found for type: ${type}`);
    }

    // Use Container.get for dependency injection
    return Container.get(StrategyClass);
  }
}
