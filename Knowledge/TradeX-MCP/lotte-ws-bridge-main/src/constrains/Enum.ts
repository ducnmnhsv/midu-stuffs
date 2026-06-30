function createMapper<V>(map: Record<string, V>) {
  return (key: string, defaultValue?: V): V | undefined => map[key] ?? defaultValue;
}

export const getSellBuyType = createMapper({
  "1": 'BUY',
  "2": 'SELL',
});

export const getOrderOperation = createMapper({
  "1": 'NEW_ORDER',
  "2": 'MODIFY_ORDER',
  "3": 'CANCEL_ORDER',
});

export const getOrderType = createMapper({
  "0": 'LO',
  "2": 'ATO',
  "3": 'MAK',
  "4": 'MOK',
  "7": 'ATC',
  "9": 'MTL',
});

export const getOrderStatus = createMapper({
  "0": 'RECEIVED',
  "3": 'CONFIRMED',
  "4": 'FILLED',
  "5": 'PARTIALLY_FILLED',
  "R/X": 'REJECTED',
});
