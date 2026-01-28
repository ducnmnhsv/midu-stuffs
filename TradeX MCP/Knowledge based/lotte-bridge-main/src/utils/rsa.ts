import * as fs from 'fs';
import * as path from 'path';
import config from '../config';

const publicKeyAbsolutePath = path.resolve(config.rsa.publicKeyFile);
const privateKeyAbsolutePath = path.resolve(config.rsa.privateKeyFile);

const rsaPublicKey = config.enableEncryptPassword ? fs.readFileSync(publicKeyAbsolutePath, 'utf8') : '';
const rsaPrivateKey = config.enableEncryptPassword ? fs.readFileSync(privateKeyAbsolutePath, 'utf8') : '';

export { rsaPublicKey, rsaPrivateKey };
