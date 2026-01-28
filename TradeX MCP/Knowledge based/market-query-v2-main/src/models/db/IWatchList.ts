export interface ISymbolWatchList {
  symbol: string;
  name?: string;
}

export interface IWatchList {
  _id?: string;
  username?: string;
  watchlistName?: string;
  order?: number;
  symbols?: string[];
  createdAt?: Date;
  updatedAt?: Date;
  deletedAt?: Date;
}
