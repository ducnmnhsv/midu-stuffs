export enum RightHistoryType {
  OTHER = 'other',
  ISSUE = 'issue',
  DIVIDEND = 'dividend',
  BONUS_SHARES = 'bonusShares',
  BOND = 'bond',
  CONVERSION = 'conversion',
  BOND_INTEREST = 'bondInterest',
}

export function parseRightTypeFromUri(uri: string): RightHistoryType {
  // Extract the last segment from URI
  const segments = uri.split('/');
  const lastSegment = segments[segments.length - 1];

  // Map URI segments to enum values
  const uriMapping: { [key: string]: RightHistoryType } = {
    other: RightHistoryType.OTHER,
    issue: RightHistoryType.ISSUE,
    dividend: RightHistoryType.DIVIDEND,
    bonusShares: RightHistoryType.BONUS_SHARES,
    bond: RightHistoryType.BOND,
    conversion: RightHistoryType.CONVERSION,
    bondInterest: RightHistoryType.BOND_INTEREST,
  };
  return uriMapping[lastSegment];
}
