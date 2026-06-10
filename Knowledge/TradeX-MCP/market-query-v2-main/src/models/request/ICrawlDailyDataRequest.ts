export interface ICrawlDailyDataRequest {
  from?: string;
  to?: string;
  symbols?: string[];
  symbolCodeMap?: { [k: string]: string };
}
