import * as crypto from "crypto";

const ALGORITHM: string = "RSA-SHA256";
const SIGN_FORMAT: crypto.HexBase64Latin1Encoding = "hex";

export function sign(privateKey: string, data: string): string {
  const signer: crypto.Signer = crypto.createSign(ALGORITHM);
  signer.write(data);
  signer.end();
  return signer.sign(privateKey, SIGN_FORMAT);
}

export function verifySign(publicKey: string, data: string, signtext: string): boolean {
  const verifier: crypto.Verify = crypto.createVerify(ALGORITHM);
  verifier.update(data);
  return verifier.verify(publicKey, signtext, SIGN_FORMAT);
}
