import { Kafka, Logger, Utils } from 'tradex-common';
import { ChangeBrokerStatus } from '../models/db/AccountChangeBrokerRequest';
import { IEmailConfiguration } from '../models/IEmailConfiguration';

const VIETNAM_TIMEZONE_OFFSET = 7 * 60;

/**
 * Convert date to Vietnam timezone (UTC+7)
 */
export function toVietnamTimezone(date: Date): Date {
  const utcTime = date.getTime() + date.getTimezoneOffset() * 60000;
  return new Date(utcTime + VIETNAM_TIMEZONE_OFFSET * 60000);
}

/**
 * Format date to display string in Vietnam timezone
 * @param format - Default: 'DD/MM/YYYY - HH:mm:ss'
 */
export function formatDateVietnam(date: Date, format: string = 'DD/MM/YYYY - HH:mm:ss'): string {
  const vnDate = toVietnamTimezone(date);
  return Utils.formatDateToDisplay(vnDate, format);
}

export interface ISendEmailParams {
  txId: string;
  logId: string;
  toList: string[];
  subject: string;
  templateName: string;
  templateData: Record<string, unknown>;
  locale?: string;
  logMessage?: string;
}

export interface ISendSmsParams {
  txId: string;
  logId: string;
  phoneNumber: string;
  templateName: string;
  templateData: Record<string, unknown>;
  locale?: string;
  logMessage?: string;
}

/**
 * Send email notification via Kafka
 */
export function sendEmailNotification(params: ISendEmailParams): void {
  const { txId, logId, toList, subject, templateName, templateData, locale = 'vi', logMessage } = params;

  const emailConfig: IEmailConfiguration = {
    toList,
    subject,
  };

  Kafka.getInstance().sendMessage(txId, 'notification', '', {
    method: 'EMAIL',
    template: {
      [templateName]: templateData,
    },
    configuration: JSON.stringify(emailConfig),
    domain: 'nhsv',
    locale,
  });

  if (logMessage) {
    Logger.info(logId, logMessage, { toList, templateName });
  }
}


export function formatPhoneNumber(phoneNumber: string): string {
  if (!phoneNumber) return phoneNumber;
  
  let formatted = phoneNumber.trim();
  
  if (formatted.startsWith('+')) {
    formatted = formatted.substring(1);
  }
  
  if (formatted.startsWith('0')) {
    formatted = `84${formatted.substring(1)}`;
  }
  
  return formatted;
}

/**
 * Send SMS notification via Kafka
 */
export function sendSmsNotification(params: ISendSmsParams): void {
  const { txId, logId, phoneNumber, templateName, templateData, locale = 'vi', logMessage } = params;

  const formattedPhone = formatPhoneNumber(phoneNumber);
  const smsConfig = {
    phoneNumber: formattedPhone,
  };

  Kafka.getInstance().sendMessage(txId, 'notification', '', {
    method: 'SMS',
    template: {
      [templateName]: templateData,
    },
    configuration: JSON.stringify(smsConfig),
    domain: 'nhsv',
    locale,
  });

  if (logMessage) {
    Logger.info(logId, logMessage, { phoneNumber: formattedPhone, templateName });
  }
}

/**
 * Map Lotte broker_status to TradeX ChangeBrokerStatus
 * @param brokerStatus - Lotte status: N (Pending), A (Approved), R (Rejected)
 */
export function mapBrokerStatusToTradeX(brokerStatus: string): ChangeBrokerStatus {
  switch (brokerStatus) {
    case 'N':
      return ChangeBrokerStatus.PENDING;
    case 'A':
      return ChangeBrokerStatus.APPROVED;
    case 'R':
      return ChangeBrokerStatus.REJECTED;
    default:
      return ChangeBrokerStatus.PENDING;
  }
}

/**
 * Map Lotte broker_status to TradeX ChangeBrokerStatus
 * @param brokerStatus - Lotte status: N (Pending), A (Approved), R (Rejected)
 */
export function mapBosStatusToTradeX(bosStatus: string): ChangeBrokerStatus {
  switch (bosStatus) {
    case 'N':
      return ChangeBrokerStatus.PENDING;
    case 'Y':
      return ChangeBrokerStatus.APPROVED;
    case 'R':
      return ChangeBrokerStatus.REJECTED;
    default:
      return ChangeBrokerStatus.PENDING;
  }
}

/**
 * Map TradeX status to Lotte broker_status
 * @param status - TradeX status: PENDING, APPROVED, REJECTED, ALL
 */
export function mapTradeXStatusToLotte(status?: string): string {
  if (!status) return '%';
  switch (status.toUpperCase()) {
    case 'PENDING':
      return 'N';
    case 'APPROVED':
      return 'A';
    case 'REJECTED':
      return 'R';
    default:
      return '%';
  }
}

/**
 * Parse broker string format "brokerId.brokerName" to separate parts
 */
export function parseBrokerInfo(brokerString: string): { brokerId: string; brokerName: string } {
  if (!brokerString) {
    return { brokerId: '', brokerName: '' };
  }
  const dotIndex = brokerString.indexOf('.');
  if (dotIndex === -1) {
    return { brokerId: brokerString, brokerName: '' };
  }
  return {
    brokerId: brokerString.substring(0, dotIndex),
    brokerName: brokerString.substring(dotIndex + 1),
  };
}


export function calculateExpiredDate(businessDays: number): Date {
  const date = new Date();
  let daysAdded = 0;
  while (daysAdded < businessDays) {
    date.setDate(date.getDate() + 1);
    const dayOfWeek = date.getDay();
    if (dayOfWeek !== 0 && dayOfWeek !== 6) {
      daysAdded++;
    }
  }
  return date;
}

