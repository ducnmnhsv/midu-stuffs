export interface ISymbolHistoryEvents {
  _id?: string;
  stock: string;
  eventTitle: string;
  eventDate: Date;
  language: 'EN' | 'VI';
  createdAt: Date;
  updatedAt: Date;
}
