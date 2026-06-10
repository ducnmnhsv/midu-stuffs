import * as express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import config from './config';
import { Kafka, Logger, Utils } from 'tradex-common';
import * as cors from 'cors';
import * as bodyParser from 'body-parser';
import { verifyFormat } from './app/middlewares/body-format-verifier';
const device = require('express-device');
import { messageHandler } from './app/middlewares/message-handler';
import { forward, IForwardConfig } from './app/middlewares/kafka-forward';
import { Request, Response } from 'express';
import { Container } from 'typedi';
import ScopeService from './services/ScopeService';
import { checkApiKey } from './app/middlewares/check-apikey';

const app: express.Application = express();


Kafka.create(config, {}, true, { 'auto.offset.reset': 'earliest' }, {}, () => Logger.info("requestor reaady!"));

export default async function initServer() {
  /** Cors */
  app.use(cors(config.cors));
  await initApis();
}

async function initApis() {
  /** Serve static file */
  app.use('/assets', express.static('assets'));

  /** Body Parser */
  app.use(bodyParser.urlencoded({ extended: true }));
  app.use(bodyParser.json());

  /** Verify Format */
  app.use(verifyFormat);

  /** Device Capture */
  app.use(device.capture());

  /** Handler message here */
  app.use(messageHandler);

  app.use("/reInitScope", checkApiKey); 

  app.get("/api/v1/clearCookie/:key",
    (req: Request, res: Response) => {
      const cookies = req.headers.cookie?.split('; ');
      if (cookies) {
        cookies.forEach(cookie => {
          const key = cookie.split('=')[0].trim();
          if (key === req.params.key) {
            res.setHeader('Set-Cookie', `${key}=deleted; path=/; Max-Age=0`);
          } 
        });
        res.status(200).json({message: "Clear Successfully!", status: 200});
      }
    });

    app.get("/currentTime",
      (req: Request, res: Response) => {
        res.status(200).json(Utils.formatDateToDisplay(new Date(), Utils.DATETIME_DISPLAY_FORMAT));
      });
   
 
  app.get("/reInitScope",
    async (req: Request, res: Response) => {
        const scopeService = Container.get(ScopeService);
        await scopeService.init();
        res.status(200).json({message: "re-init scope successfully!", status: 200});     
  });  
  config.forwards.forEach((it: IForwardConfig | any) => {
    let forwardFunction: express.RequestHandler | null = null;
    if (it.type === 'http') {
      const config = {...it};
      config.type = undefined;
      config.pattern = undefined;
      config.method = undefined;
      config.logger = Logger;
      config.on = {
        proxyReq: (proxyReq: Request, req: Request, res: Response) => {
          Logger.info("proxy request", proxyReq.path);
        },
        proxyRes: (proxyRes: Response, req: Request, res: Response) => {
          Logger.info("proxy request get response", proxyRes.status);
        },
        error: (err: unknown, req: Request, res: Response) => {
          Logger.error("proxy error", err);
        },
      };
      forwardFunction = createProxyMiddleware(config);
    } else if (it.type === 'kafka') {
      forwardFunction = (req: Request, res: Response, next: express.NextFunction) => forward(req, res, next, it as IForwardConfig);
    }
    if (forwardFunction != null) {
      if (it.method === "get") {
        app.get(it.pattern, forwardFunction);
      } else if (it.method === "post") {
        app.post(it.pattern, forwardFunction);
      } else if (it.method === "put") {
        app.put(it.pattern, forwardFunction);
      } else if (it.method === "delete") {
        app.delete(it.pattern, forwardFunction);
      } else if (it.method === "all") {
        app.all(it.pattern, forwardFunction);
      } else if (it.method === "patch") {
        app.patch(it.pattern, forwardFunction);
      } else {
        app.use(it.pattern, forwardFunction);
      }
    }
  });

  app.listen(config.port, () => {
    Logger.info('Server Start!');
    /** Init i18next */
    if (config.enableTranslation) {
      Utils.initI18nInternal('rest', ['message', 'field', 'tuxedo']);
    } else {
      Utils.initByOptions({});
    }
  });
}
