import * as util from 'util';
import * as fs from 'fs';
import {
  decode as jsonwebtokenDecode,
  verify as jsonwebtokenVerify,
  VerifyOptions,
  VerifyErrors,
  DecodeOptions,
} from 'jsonwebtoken';

export const fsStat = util.promisify(fs.stat);
export const fsReadFile = util.promisify(fs.readFile);
export const fsWriteFile = util.promisify(fs.writeFile);
export const jwtVerify = (
  token: string,
  secretOrPublicKey: string | Buffer,
  options?: VerifyOptions
) => {
  return new Promise(
    (resolve: (payload: any) => void, reject: (err: Error) => void) => {
      // tslint:disable-line
      jsonwebtokenVerify(
        token,
        secretOrPublicKey,
        options,
        (err: VerifyErrors, decoded: object | string) => {
          if (err != null) {
            reject(err);
          } else {
            resolve(decoded);
          }
        }
      );
    }
  );
};

export const decode = (
  token: string,
  options?: DecodeOptions
) => {
  return jsonwebtokenDecode(
    token,
    options,
  );
};
