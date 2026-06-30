export interface IEmailConfiguration {
  toList: string[];
  subject: string;
  ccList?: string[];
  bccList?: string[];
  from?: string;
}
