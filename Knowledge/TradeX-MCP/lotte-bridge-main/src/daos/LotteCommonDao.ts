import { Service } from 'typedi';
import { Errors, Logger } from 'tradex-common';
import { IAPI } from '../models/IAPI';
import config from '../config';
import { getUrl } from '../utils/lotte';
import { IContext } from '../models/IContext';
import { ILotteCommonResponse } from '../models/response/lotte/ILotteCommonResponse';
import { IParam } from '../models/request/lotte/ILotteRequest';
import axios, { AxiosRequestConfig, AxiosResponse, Method } from 'axios';
import r2curl from 'r2curl';
import { LOTTE_LANG_CODE } from '../constants/enum';

@Service()
export default class LotteCommonDao {
  private async getHeader(headers?: IParam): Promise<IParam> {
    const hd: IParam = {
      ...{
        'Content-Type': 'application/json',
        'Cache-Control': 'no-cache',
        'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36',
        Accept: 'application/json',
        apiKey: config.lotte.apiKey,
      },
      ...headers,
    };
    return hd;
  }

  async post<T extends ILotteCommonResponse>(
    api: IAPI,
    headers?: IParam,
    body?: object | string,
    ctx?: IContext
  ): Promise<T> {
    return this.sendRequest<T>('POST', api, headers, body, null, null, ctx);
  }

  async get<T extends ILotteCommonResponse>(
    api: IAPI,
    headers?: IParam,
    body?: object | string,
    pathParams?: IParam,
    queryParams?: IParam,
    ctx?: IContext
  ): Promise<T> {
    return this.sendRequest<T>('GET', api, headers, body, pathParams, queryParams, ctx);
  }

  async put<T extends ILotteCommonResponse>(
    api: IAPI,
    headers?: IParam,
    body?: object | string,
    pathParams?: IParam,
    ctx?: IContext
  ): Promise<T> {
    return this.sendRequest<T>('PUT', api, headers, body, pathParams, null, ctx);
  }

  async delete<T extends ILotteCommonResponse>(
    api: IAPI,
    headers?: IParam,
    body?: object | string,
    pathParams?: IParam,
    queryParams?: IParam,
    ctx?: IContext
  ): Promise<T> {
    return this.sendRequest<T>('DELETE', api, headers, body, pathParams, queryParams, ctx);
  }

  async sendRequest<T>(
    method: Method,
    api: IAPI,
    headers?: IParam,
    body?: object | string,
    pathParams?: IParam,
    queryParams?: IParam,
    ctx: IContext = {} as IContext
  ): Promise<T> {
    if (ctx != null && ctx.orgMsg != null && ctx.orgMsg.data != null) {
      if (body != null && typeof body === 'object' && ctx.orgMsg.data.headers != null) {
        const language = ctx.orgMsg.data.headers['accept-language'];
        body = {
          ...body,
          lang_code:
            language == null || LOTTE_LANG_CODE[language] == null ? config.defaultLanguage : LOTTE_LANG_CODE[language],
        };
        if (body['cli_mac_addr'] == null) {
          body['cli_mac_addr'] = ctx.orgMsg.data['deviceUniqueId'];
        }
      }
      const publicIp = ctx.orgMsg.data.sourceIp;
      if (publicIp != null) {
        headers = {
          ...headers,
          'X-Forwarded-For': publicIp,
        };
      }
    }
    const hd = await this.getHeader(headers);
    const url = this.getUrl(api, pathParams);
    const startTime: [number, number] = process.hrtime();
    const options: AxiosRequestConfig = {
      url,
      method,
      headers: hd,
      data: body,
      withCredentials: true,
    };
    // Axios will serialize options.params and add it to the query string https://masteringjs.io/tutorials/axios/get-query-params
    if (queryParams != null) {
      options.params = queryParams;
    }
    if (url.indexOf('https') !== -1) {
      const https = require('https');
      const agent = new https.Agent({
        rejectUnauthorized: false,
      });
      options.httpsAgent = agent;
    }
    delete axios.defaults.headers['Content-Length'];
    try {
      Logger.info(
        ctx.id,
        r2curl(options, {
          quote: 'single',
          forceBody: true,
        })
      );
      const response: AxiosResponse = await axios(options);
      const diff: [number, number] = process.hrtime(startTime);
      Logger.warn(ctx.id, url, `took ${diff[0]}.${diff[1]} seconds`, `status ${response.status}`);
      if (response.status === 200) {
        const res: T = await this.logResponseAndReturnJson(response, url, ctx.id);
        return res;
      } else {
        await this.logResponse(response, url, ctx.id);
        return this.handleError(response);
      }
    } catch (error) {
      if (error.isAxiosError) {
        Logger.error(`${ctx.id} response ${url} ${error.message} ${error.response}`, error);
        const response: AxiosResponse = error.response;
        if (response != null) {
          await this.logResponse(error.response, url, ctx.id);
          return this.handleError(error.response);
        } else {
          throw new Errors.GeneralError(error.message);
        }
      }
      throw new Errors.GeneralError(error);
    }
  }

  private async handleError<T>(response: AxiosResponse): Promise<T> {
    throw new Errors.GeneralError(response.data.error_desc);
  }

  async logResponseAndReturnJson<T>(response: AxiosResponse, url: string, msgId: string): Promise<T> {
    const res: T = response.data;
    const resStr: string = JSON.stringify(res);
    if (resStr.length > config.queryLogLength) {
      Logger.info(msgId, 'response', url, resStr.substring(0, config.queryLogSub), '...', resStr.length);
    } else {
      Logger.info(msgId, 'response', url, resStr);
    }
    return res;
  }

  async logResponse(response: AxiosResponse, url: string, msgId: string): Promise<void> {
    const res = response.data;
    const resStr: string = JSON.stringify(res);
    if (resStr.length > config.queryLogLength) {
      Logger.info(msgId, 'response', url, resStr.substring(0, config.queryLogSub), '...', resStr.length);
    } else {
      Logger.info(msgId, 'response', url, resStr);
    }
    return;
  }

  private getUrl(api: IAPI, pathParams?: IParam): string {
    return getUrl(this.getApiUrl(api, pathParams));
  }

  private getApiUrl(api: IAPI, urlData?: IParam): IAPI {
    if (urlData == null) {
      return api;
    } else {
      let data = api.api;
      const keys: string[] = Object.keys(urlData);
      for (let i = 0; i < keys.length; i++) {
        const key: string = keys[i];
        data = data.replace(`{${key}}`, urlData[key]);
      }
      const uri: IAPI = {
        api: data,
        category: api.category,
      };
      return uri;
    }
  }
}
