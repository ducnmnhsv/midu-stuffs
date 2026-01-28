import { Request, Response } from 'express';
import config from "../../config";

function checkApiKey(req: Request, res: Response, next: Function) {
    if(req.headers.apikey !== config.apiKey){ 
        res.status(403).json({ message: "Unauthorized", code: 403});     
    } else {
        next();    
    }
       
}

export { checkApiKey };
