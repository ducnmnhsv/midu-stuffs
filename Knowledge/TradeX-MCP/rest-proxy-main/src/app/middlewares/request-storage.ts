import { v4 as uuid } from 'uuid';
import { Request, Response } from 'express';

function storeRequest(req: Request, res: Response, next: Function) {
  const id = uuid();
  (req as any)['session_id'] = id; // tslint:disable-line
  req.app.locals.requestStack[id] = {
    req: req,
    res: res,
    next: next,
  };
  next();
}

export { storeRequest };
