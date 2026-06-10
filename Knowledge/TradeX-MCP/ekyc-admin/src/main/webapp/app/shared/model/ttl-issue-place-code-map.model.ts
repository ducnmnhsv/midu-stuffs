export interface ITtlIssuePlaceCodeMap {
  id?: number;
  code?: string;
  name?: string;
  enableRegex?: boolean | null;
}

export const defaultValue: Readonly<ITtlIssuePlaceCodeMap> = {
  enableRegex: false,
};
