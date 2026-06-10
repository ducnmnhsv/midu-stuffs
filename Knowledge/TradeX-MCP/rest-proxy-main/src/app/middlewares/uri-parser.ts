import config from '../../config';
import { Request, Response } from 'express';

function parseURI(req: Request, res: Response) {
  (req as any).parsedURI = `${req.method.toLowerCase()}:${config.basePath}`; // tslint:disable-line
}

export { parseURI };
