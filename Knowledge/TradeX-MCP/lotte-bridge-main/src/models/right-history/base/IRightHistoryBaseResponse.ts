export interface IRightHistoryBaseResponse<T extends IRightHistoryBaseItem> {
  items: T[];
  nextKey: string | null;
}

export interface IRightHistoryBaseItem {}
