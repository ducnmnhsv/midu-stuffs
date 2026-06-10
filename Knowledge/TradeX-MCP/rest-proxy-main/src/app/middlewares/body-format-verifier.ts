import { Request, Response } from 'express';

function verifyFormat(req: Request, res: Response, next: Function) {
  if (Array.isArray(req.body)) {
    const items = req.body;
    req.body = {};
    req.body.items = items;
  }

  next();
}

export { verifyFormat };
