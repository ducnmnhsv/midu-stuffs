export interface ISearchEvent {
  fromDate?: string | null;
  toDate?: string | null;
}

export const defaultValue: Readonly<ISearchEvent> = {};
