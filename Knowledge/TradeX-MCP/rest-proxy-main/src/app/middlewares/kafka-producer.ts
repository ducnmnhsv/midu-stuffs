import { Logger, Kafka, Utils } from 'tradex-common';
import config from '../../config';
import { parseURI } from './uri-parser';
import { v4 as uuid } from 'uuid';
import { Request, Response } from 'express';

function sendToKafka(req: Request, res: Response, uri: string) {
  const i18n = Utils.getI18nInstance();

  try {
    req.body = { ...req.body, ...req.params };
    req.body = { ...req.body, ...req.query };

    const startTime: [number, number] = process.hrtime();
    config.fieldType.integer.forEach((field: string) => {
      if (req.body[field] !== null) {
        req.body[field] = Math.floor(req.body[field]);
      }
    });

    config.fieldType.boolean.forEach((field: string) => {
      if (req.body[field] !== null) {
        req.body[field] = /true/i.test(req.body[field]);
      }
    });

    config.fieldType.array.forEach((field: string) => {
      if (req.body[field] !== null) {
        if (typeof req.body[field] === 'string') {
          req.body[field] = [req.body[field]];
        }
      }
    });

    parseURI(req, res);

    let diff: [number, number] = process.hrtime(startTime);
    Logger.info(`parse uri took ${diff[0]} seconds and ${diff[1]} nanoseconds`);
    Kafka.getInstance()
      .sendRequest(
        uuid(),
        config.kakfa.requestTopic,
        `${(req as any).parsedURI}${uri}`, // tslint:disable-line
        req.body
      )
      .subscribe((message: Kafka.IMessage) => {
        diff = process.hrtime(startTime);
        Logger.info(
          `query service took ${diff[0]} seconds and ${diff[1]} nanoseconds`
        );
        try {
          Logger.info('Response:', message);

          const response = message.data;

          if (response.status != null) {
            const errorResponse = Utils.translateErrorMessage(
              response.status,
              req.body['headers']['accept-language']
            );
            diff = process.hrtime(startTime);
            Logger.info(
              `after parse response took ${diff[0]} seconds and ${diff[1]} nanoseconds`
            );
            if (config.responseCode[errorResponse.code] != null) {
              res
                .status(config.responseCode[errorResponse.code])
                .send(errorResponse);
            } else {
              res.status(400).send(errorResponse);
            }
          } else {
            diff = process.hrtime(startTime);
            Logger.info(
              `after parse response took ${diff[0]} seconds and ${diff[1]} nanoseconds`
            );
            res.send(response.data);
          }
          return false;
        } catch (error) {
          return false;
        }
      });
  } catch (err) {
    Logger.error('A problem occurred when sending request');
    Logger.error(err);

    res.status(500).send({
      code: 'SENDING_REQUEST_ERROR',
      message: config.enableTranslation ? i18n.t('SENDING_REQUEST_ERROR', {
        lng: req.body['headers']['accept-language'],
      }) : undefined,
    });
  }
}

export { sendToKafka };
