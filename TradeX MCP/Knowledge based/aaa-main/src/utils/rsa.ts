import * as fs from 'fs';
import * as path from 'path';
import conf from '../conf';

const publicKeyAbsolutePath = path.resolve(conf.rsa.publicKey);
const privateKeyAbsolutePath = path.resolve(conf.rsa.privateKey);

const rsaPublicKey = fs.readFileSync(publicKeyAbsolutePath, 'utf8');
const rsaPrivateKey = fs.readFileSync(privateKeyAbsolutePath, 'utf8');

export { rsaPublicKey, rsaPrivateKey };
