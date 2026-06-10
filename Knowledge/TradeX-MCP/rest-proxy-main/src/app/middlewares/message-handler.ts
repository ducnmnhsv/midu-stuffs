import { Models, Utils, Kafka, Logger, Errors } from 'tradex-common';
import { RequestHandler, Request, Response } from 'express';
import { jwtVerify } from '../../promisify';
import config from '../../config';
import { OpenAPIV3 } from 'openapi-types';
import { Matcher, Scope } from '../../models/IScope';
import { Container } from 'typedi';
import ScopeService from '../../services/ScopeService';
import { IRequestContext } from '../../models/IRequestContext';

const scopeService = Container.get(ScopeService);

const i18n = Utils.getI18nInstance();

const TOKEN_PREFIX = 'jwt ';

const FPT_PREFIX = 'Basic ';

let messageId = 0;
const prefix = `${new Date().getTime()}-${config.clientId}`;

function getMessageId(): string {
  messageId++;
  return `${prefix}-${messageId}`;
}

interface Body extends Models.IDataRequest {
  [s: string]: any; // tslint:disable-line
}

export const messageHandler: RequestHandler = (
  req: Request,
  res: Response,
  next: Function
) => {
  let messageId = getMessageId();
  const rid = req.headers.rid;
  if (rid != null) {
    messageId = `${messageId}-${rid}`;
  }
  const lang: string = def(
    first(req.headers['accept-language']),
    'vi'
  );
  const ctx: IRequestContext = {
    messageId,
    req, 
    res,
    lang,
    rid: rid != null ? `${rid}` : null,
  }
  if (
    !req.path.startsWith('/api') ||
    req.path.startsWith('/api/v1/clearCookie')
  ) {
    next();
  } else {
    requestHandler(ctx)
      .then()
      .catch((e: Error) => handleError(e, ctx));
  }
};

async function requestHandler(
  ctx: IRequestContext,
): Promise<null> {
  const uri = `/${ctx.req.method.toLowerCase()}${ctx.req.path}`;
  if (config.enableDebug) {
    Logger.info(messageId, 'request', uri);
  }

  if (config.enableEncryptPassword === true) {
    const fieldEncryptArr = config.encryptPassword[uri];
    if (fieldEncryptArr != null) {
      const body = ctx.req.body;
      fieldEncryptArr.forEach((field: string) => {
        if (body[field] != null && typeof body[field] === 'string') {
          body[field] = Utils.rsaEncrypt(body[field], config.rsa.publicKey);
        }
      });
    }
  }
  const requestId = ctx.rid;
  if (ctx.req.body == null) {
    ctx.req.body = {
      rid: requestId,
    };
  } else {
    ctx.req.body.rid = requestId;
  }

  if ((uri === '/post/api/v1/login' || uri.startsWith('/post/api/v1/login/') || uri === '/post/api/v1/user/SocialLogin') && uri !== '/post/api/v1/login/sec/verifyOTP') {
    if (config.isMaintain && config.domain === 'kbfinance') {
      if (!config.allowUser.includes(ctx.req.body.username)) {
        returnCode(ctx, 500, 'SERVER_UNDER_MAINTAINANCE');
        return null;
      }
    }
    return doSendRequest(
      null,
      forwardAAA(`${ctx.req.method.toLowerCase()}:${ctx.req.path}`),
      ctx.req.body,
      ctx,
    );
  } else if (uri === '/post/api/v1/loginCA' || uri === '/post/api/v1/refreshToken' || uri === '/post/api/v1/revokeToken') {
    return doSendRequest(
      null,
      forwardAAA(`${ctx.req.method.toLowerCase()}:${ctx.req.path}`),
      ctx.req.body,
      ctx,
    );
  } else {
    if (config.apiEncryptList.includes(uri)) {
      if (ctx.req.body.pin != null) {
        ctx.req.body.pin = config.enableEncryptPassword
          ? Utils.rsaEncrypt(ctx.req.body.pin, config.rsa.publicKey)
          : ctx.req.body.pin;
      }
      if (ctx.req.body.password != null) {
        if (
          config.enableEncryptPassword === true &&
          ![
            'biometric',
            'access_facebook',
            'access_google',
            'access_apple',
          ].includes(ctx.req.body.grant_type)
        ) {
          ctx.req.body.password = Utils.rsaEncrypt(
            ctx.req.body.password,
            config.rsa.publicKey
          );
        }
      }
    }

    // find matching, public scopes
    const [scope, matcher] = scopeService.findScope(uri, true);
    if (scope != null) {
      if (config.enableDebug) {
        Logger.info(messageId, 'public scope', scope, uri);
      }
      if (config.fptCallBackApi.includes(uri)) {
        let accessToken = ctx.req.headers.authorization;
        if (accessToken == null || !accessToken.startsWith(FPT_PREFIX)) {
          Logger.warn(messageId, 'no prefix in authorization header', uri);
          returnCode(ctx, 401, 'UNAUTHORIZED');
          return null;
        } else if (accessToken.length === 0) {
          Logger.warn(messageId, 'access token length 0', uri);
          returnCode(ctx, 401, 'UNAUTHORIZED');
          return null;
        } else {
          accessToken = accessToken.substr(FPT_PREFIX.length).trim();
          if (accessToken !== config.fptAuthToken) {
            Logger.warn(messageId, 'unmatch fpt authen token', uri);
            returnCode(ctx, 401, 'UNAUTHORIZED');
            return null;
          }
        }
      }
      return sendRequest(
        null,
        uri,
        scope,
        matcher!,
        null,
        ctx,
      );
    }
    return checkToken(uri, ctx);
  }
}

async function checkToken(
  uri: string,
  ctx: IRequestContext,
): Promise<null> {
  let accessToken = ctx.req.headers.authorization;
  if (accessToken == null || !accessToken.startsWith(TOKEN_PREFIX)) {
    Logger.warn(messageId, 'no prefix in authorization header', uri);
    returnCode(ctx, 401, 'UNAUTHORIZED');
    return null;
  }
  if (accessToken.length === 0) {
    Logger.warn(messageId, 'access token length 0', uri);
    returnCode(ctx, 401, 'UNAUTHORIZED');
    return null;
  }
  accessToken = accessToken.substr(TOKEN_PREFIX.length).trim();
  const jwtConf = config.getJwt();
  let payload: Models.IAccessToken | null = null;
  try {
    payload = await jwtVerify(accessToken, jwtConf.publicKey, {
      algorithms: ['RS256'],
    });
  } catch (e) {
    Logger.warn(messageId, 'cannot decode access token', uri, e);
    returnCode(ctx, 401, 'UNAUTHORIZED');
    return null;
  }
  const scopeGroupIds: number[] = payload!.sgIds;
  const refreshTokenId = payload!.rId;
  Logger.info(messageId, refreshTokenId, 'request', uri);
  if (config.isMaintain && config.domain === 'kbfinance') {
    if (!config.allowUser.includes(payload!.ud?.username)) {
      returnCode(ctx, 500, 'SERVER_UNDER_MAINTAINANCE');
      return null;
    }
  }
  const [scope, matcher] = scopeService.findScope(uri, false, scopeGroupIds);
  if (scope == null) {
    Logger.warn(messageId, refreshTokenId, 'not found any private scope', uri);
    returnCode(ctx, 404, 'URI_NOT_FOUND');
    return null;
  }
  if (scope != null && scope.forwardData.tokenType === 'VERIFIED') {
    const otpToken = ctx.req.headers.otptoken as string;
    if (otpToken == null) {
      returnCode(ctx, 401, 'OTP_TOKEN_IS_REQUIRED');
      return null;
    }
    let otpTokenPayload;
    try {
      otpTokenPayload = await jwtVerify(otpToken, jwtConf.publicKey, {
        algorithms: ['RS256'],
      });
    } catch (e) {
      returnCode(ctx, 401, 'OTP_TOKEN_IS_EXPIRED');
      return null;
    }
    if (refreshTokenId !== otpTokenPayload.rId) {
      returnCode(ctx, 401, 'OTP_TOKEN_IS_EXPIRED');
      return null;
    }
  }

  const token: Models.IToken = Utils.convertToken(payload!);
  if (config.enableDebug) {
    Logger.info(
      messageId,
      refreshTokenId,
      'send request with scope',
      scope,
      uri
    );
  }
  return sendRequest(
    refreshTokenId,
    uri,
    scope,
    matcher!,
    token,
    ctx,
  );
}

function checkIfValidIPV6(str: string): boolean {
  // Regular expression to check if string is a IPv6 address
  const regexExp = /(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))/gi;

  return regexExp.test(str);
}

async function sendRequest(
  refreshTokenId: number | null,
  uri: string,
  scope: Scope,
  matcher: Matcher,
  token: Models.IToken | null,
  ctx: IRequestContext,
): Promise<null> {
  const body: Body = ctx.req.body;
  Object.keys(ctx.req.query).forEach((queryParam: string) => {
    body[queryParam] = ctx.req.query[queryParam];
  });
  if (matcher != null) {
    if (matcher.paramNames != null) {
      for (let i = 0; i < matcher.paramNames.length; i++) {
        if (i < matcher.paramValues.length) {
          body[matcher.paramNames[i]] = matcher.paramValues[i];
          // will do: type of parameter
        } else {
          Logger.error(
            'lack of param',
            ctx.req.path,
            scope.processedPattern,
            scope.uriPattern,
            matcher
          );
        }
      }
    }
  }

  if (body.headers == null) {
    body.headers = {};
  }

  if (token != null) {
    body.headers.token = token;
  }

  body.headers['accept-language'] = Utils.getLanguageCode(ctx.lang);
  (body.headers as any)['user-agent'] = ctx.req.headers['user-agent'];

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

  const secDomain: string | undefined = first(ctx.req.headers.secdomain);
  let secToken: string | undefined = first(ctx.req.headers.sectoken);
  if (secDomain != null && secToken != null) {
    const jwtConf = config.getJwt();
    if (jwtConf != null) {
      if (secToken.startsWith(TOKEN_PREFIX)) {
        secToken = secToken.substr(TOKEN_PREFIX.length);
        if (secToken.length > 0) {
          let payload: Models.IAccessToken | undefined;
          try {
            payload = await jwtVerify(secToken, jwtConf.publicKey, {
              algorithms: ['RS256'],
            });
          } catch (e) {
            returnCode(ctx, 401, 'UNAUTHORIZED');
            return null;
          }
          body.headers.secToken = Utils.convertToken(payload!);
        }
      }
    }
  }
  processType(body, scope);

  const forwardResult: Models.IForwardUriResult = Utils.getForwardUri(
    uri,
    scope,
    token!,
    {}
  );
  return doSendRequest(
    refreshTokenId,
    forwardResult,
    body,
    ctx,
  );
}

export async function doSendRequest(
  refreshTokenId: number | null,
  forwardResult: Models.IForwardUriResult,
  body: Body,
  ctx: IRequestContext,
): Promise<null> {
  let time: [number, number] = process.hrtime();
  let responseMsg: Kafka.IMessage | undefined;
  ctx.txId = `${new Date().getTime()}-${ctx.messageId}`;
  const logMsg = `${ctx.txId} ${ctx.messageId} ${ctx.rid} rftId${refreshTokenId} forward request ${ctx.req.path} to ${forwardResult.topic}:${forwardResult.uri}`;
  try {
    responseMsg = await Kafka.getInstance().sendRequestAsync(
      ctx.txId,
      forwardResult.topic!,
      forwardResult.uri!,
      body,
      config.timeout
    );
  } catch (e) {
    time = process.hrtime(time);
    Logger.error(`${logMsg} took ${time[0]}.${time[1]} seconds with error`, e);
    throw e;
  }
  time = process.hrtime(time);
  Logger.warn(`${logMsg} took ${time[0]}.${time[1]} seconds`);
  let data;
  try {
    data = Kafka.getResponse(responseMsg);
  } catch (e) {
    Logger.error(`${ctx.txId} ${ctx.messageId} ${ctx.rid} rftId${refreshTokenId}`, "parsing response error", responseMsg);
    throw e;
  }
  ctx.res.status(200).send(data);
  return null;
}

export function returnCode(ctx: IRequestContext, status: number, code: string) {
  if (status < 200 || status >= 300) {
    Logger.warn(ctx.txId, ctx.messageId, ctx.rid, ctx.req.path, "response", status, code);
  }
  ctx.res.status(status).send({ code, message: config.enableTranslation ? i18n.t(code) : undefined });
}

function processParameterType(
  body: object,
  parameter: OpenAPIV3.ParameterObject
) {
  const name: string = parameter.name;
  const schema: OpenAPIV3.SchemaObject = parameter.schema as OpenAPIV3.SchemaObject;
  const value = (body as any)[name]; // tslint:disable-line

  if (value == null) {
    if (parameter.required) {
      throw new Errors.FieldRequiredError(name);
    }
  } else {
    (body as any)[name] = convertParameterType(name, value, schema);
  }
}

function convertParameterType(
  name: string,
  value: any,
  schema: OpenAPIV3.SchemaObject
): any {
  // tslint:disable-line
  if (schema.type === 'array') {
    if (value instanceof Array) {
      for (let i = 0; i < value.length; i++) {
        value[i] = convertParameterType(
          name,
          value[i],
          schema.items as OpenAPIV3.SchemaObject
        );
      }
      return value;
    } else if (typeof value === 'string') {
      let newValue = null;
      if (value.startsWith('[') && value.endsWith(']')) {
        try {
          newValue = JSON.parse(value);
        } catch (e) {
          throw new Errors.InvalidFieldValueError(name, value);
        }
      } else {
        newValue = value.split(',');
      }
      for (let i = 0; i < newValue.length; i++) {
        newValue[i] = convertParameterType(
          name,
          newValue[i],
          schema.items as OpenAPIV3.SchemaObject
        );
      }
      return newValue;
    } else {
      throw new Errors.InvalidFieldValueError(name, value);
    }
  } else if (schema.type === 'string') {
    if (typeof value === 'string') {
      return value;
    } else {
      try {
        return JSON.stringify(value);
      } catch (e) {
        throw new Errors.InvalidFieldValueError(name, value);
      }
    }
  } else if (schema.type === 'number' || schema.type === 'integer') {
    if (typeof value === 'number') {
      return value;
    } else if (typeof value === 'string') {
      const newValue = Number(value);
      if (isNaN(newValue)) {
        throw new Errors.InvalidFieldValueError(name, value);
      }
      return newValue;
    } else {
      throw new Errors.InvalidFieldValueError(name, value);
    }
  } else if (schema.type === 'boolean') {
    if (typeof value === 'boolean') {
      return value;
    } else if (typeof value === 'string') {
      if (value.toLowerCase() === 'false') {
        return false;
      } else if (value.toLowerCase() === 'true') {
        return true;
      } else {
        throw new Errors.InvalidFieldValueError(name, value);
      }
    } else {
      throw new Errors.InvalidFieldValueError(name, value);
    }
  } else if (schema.type === 'object') {
    if (typeof value === 'string') {
      try {
        return JSON.parse(value);
      } catch (e) {
        throw new Errors.InvalidFieldValueError(name, value);
      }
    } else if (typeof value === 'object') {
      return value;
    } else {
      throw new Errors.InvalidFieldValueError(name, value);
    }
  }
}

function processType(body: Models.IDataRequest, scope: Scope) {
  const operator:
    | OpenAPIV3.OperationObject
    | undefined = scopeService.getSchema(+scope.id);
  if (operator != null && operator.parameters) {
    operator.parameters.forEach(
      (parameter: OpenAPIV3.ReferenceObject | OpenAPIV3.ParameterObject) =>
        processParameterType(body, parameter as OpenAPIV3.ParameterObject)
    );
  }
}

export function handleError(e: Error, ctx: IRequestContext) {
  Logger.error(ctx.txId, ctx.messageId, ctx.rid, 'error on handler request', ctx.req.path, ctx.req.method, e);
  if (e instanceof Errors.GeneralError) {
    const code: string = e.code;
    const status: number = config.responseCode[code];
    if (status != null) {
      ctx.res.status(status).send(e.toStatus());
    } else {
      ctx.res.status(400).send(e.toStatus());
    }
  } else {
    ctx.res.status(500).send({
      code: 'INTERNAL_SERVER_ERROR',
      message: config.enableTranslation ? i18n.t('INTERNAL_SERVER_ERROR', { lng: ctx.lang }) : undefined,
    });
  }
}

export function def<T>(data: T | undefined, def: T): T {
  if (data == null) {
    return def;
  }
  return data;
}

export function first(
  s: string | Array<string | undefined> | undefined
): string | undefined {
  if (s == null) {
    return undefined;
  }
  if (typeof s === 'string') {
    return s;
  }
  if (s.length === 0) {
    return undefined;
  }
  return s.find((i: string | undefined) => i != null);
}

function forwardAAA(uri: string): Models.IForwardUriResult {
  return {
    topic: 'aaa',
    uri,
  };
}
