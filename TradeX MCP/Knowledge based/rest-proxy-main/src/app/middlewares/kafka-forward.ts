import { Logger, Models, Utils } from 'tradex-common';
import { Request, Response } from 'express';
import config from '../../config';
import { def, doSendRequest, first, handleError, returnCode } from './message-handler';
import { IRequestContext } from '../../models/IRequestContext';
import { jwtVerify } from '../../promisify';

let messageId = 0;
const prefix = `${new Date().getTime()}-${config.clientId}`;
const TOKEN_PREFIX = 'jwt ';

interface Body extends Models.IDataRequest {
  [s: string]: any; // tslint:disable-line
}

function getMessageId(): string {
  messageId++;
  return `${prefix}-${messageId}`;
}

export interface IForwardConfig {
  topic: string;
  uri: string;
  secToken?: "decode" | "verify" | null; 
}

export const forward = (
  req: Request,
  res: Response,
  next: Function,
  forwardConfig: IForwardConfig,
) => {
  let messageId = getMessageId();
  const rid = req.headers.rid;
  if (rid != null) {
    messageId = `${messageId}-${rid}`;
  }
  const languageCode: string = def(
    first(req.headers['accept-language']),
    'vi'
  );
  const ctx: IRequestContext = {
    lang: languageCode,
    req, 
    res,
    messageId,
  };
  sendRequest(ctx, forwardConfig)
    .then()
    .catch((e: Error) => handleError(e, ctx));
};

function checkIfValidIPV6(str: string): boolean {
  // Regular expression to check if string is a IPv6 address
  const regexExp = /(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))/gi;

  return regexExp.test(str);
}

async function sendRequest(
  ctx: IRequestContext,
  forwardConfig: IForwardConfig,
): Promise<null> {
  const body: Body = ctx.req.body;
  Object.keys(ctx.req.query).forEach((queryParam: string) => {
    body[queryParam] = ctx.req.query[queryParam];
  });

  if (body.headers == null) {
    body.headers = ctx.req.headers as any as Models.IHeaders;
  }

  body.headers['accept-language'] = Utils.getLanguageCode(ctx.lang);
  if (forwardConfig.secToken != null) {
    let secTokenString: string = ctx.req.headers.sectoken as string;
    if (secTokenString != null) {
      const ignoreExpiration = forwardConfig.secToken !== "verify";
      if (secTokenString.startsWith(TOKEN_PREFIX)) {
        secTokenString = secTokenString.substr(TOKEN_PREFIX.length).trim();
      }
      const jwtConf = config.getJwt();
      let payload: Models.IAccessToken | null = null;
      try {
        payload = await jwtVerify(secTokenString, jwtConf.publicKey, {
          algorithms: ['RS256'],
          ignoreExpiration,
        });
        body.headers.secToken = Utils.convertToken(payload!);
      } catch (e) {
        Logger.warn(messageId, ctx.rid, 'cannot decode sec token', ctx.req.path, e);
        returnCode(ctx, 401, 'SEC_UNAUTHORIZED');
        return null;
      }
    }
  }

  const ip: string | undefined = first([
    first(ctx.req.headers['tx-source-ip']),
    first(ctx.req.headers['x-forwarded-for']),
    first(ctx.req.connection.remoteAddress),
  ]);
  if (ip != null) {
    if (!checkIfValidIPV6(ip)) {
      body.sourceIp = ip.replace(/^.*:/, '');
    }
    body.sourceIp = ip;
  }
  body.deviceType = (ctx.req as any).device.type; // tslint:disable-line

  return doSendRequest(
    null,
    forwardConfig,
    body,
    ctx,
  );
}
