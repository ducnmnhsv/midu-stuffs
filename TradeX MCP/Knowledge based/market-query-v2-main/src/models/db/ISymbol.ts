export interface ISymbol {
  _id?: string;
  code?: string;
  exchange?: string;
  marketType?: string;
  name?: string;
  nameEn?: string;
  type?: string;

  // only for index
  isHighlight?: number;
  refCode?: string;

  // only for stock
  securitiesType?: string;
}
