import RefreshToken from "../models/db/RefreshToken";
import {Errors} from "tradex-common";
import {Connection} from "../db/async";
import {Query} from "../models/db/BaseModel";

const queryByToken: Query<RefreshToken> = new Query<RefreshToken>(new RefreshToken())
  .where((model: RefreshToken) => model.token, "=?");

const queryById: Query<RefreshToken> = new Query<RefreshToken>(new RefreshToken())
.where((model: RefreshToken) => model.id, "=?");

export async function deleteRefreshToken(token: string, con: Connection): Promise<any> {
  const results: any[] = await con.queryResult(queryByToken.select(), [token]);
  if (results && results.length > 0) {
    const rt: RefreshToken = new RefreshToken(results[0]);
    await con.queryResult(queryByToken.delete(), [token]);
    if (rt.parentId.get()) {
      await con.queryResult(queryByToken.delete(), [rt.parentId.get()]);
    }
  }
}

export async function deleteRefreshTokenById(token: string, con: Connection): Promise<any> {
  const results: any[] = await con.queryResult(queryById.select(), [token]);
  if (results && results.length > 0) {
    const rt: RefreshToken = new RefreshToken(results[0]);
    await con.queryResult(queryById.delete(), [token]);
    if (rt.parentId.get()) {
      await con.queryResult(queryById.delete(), [rt.parentId.get()]);
    }
  }
}

export async function getRefreshToken(token: string, con: Connection): Promise<RefreshToken> {
  const query: Query<RefreshToken> = new Query<RefreshToken>(new RefreshToken())
    .where((model: RefreshToken) => model.token, "=?");
  const results: any[] = await con.queryResult(query.select(), [token]);
  if (results.length === 0) {
    throw new Errors.ObjectNotFoundError();
  }
  return new RefreshToken(results[0]);
}

export async function getRefreshTokenById(id: number, con: Connection): Promise<RefreshToken> {
  const query: Query<RefreshToken> = new Query<RefreshToken>(new RefreshToken())
    .where((model: RefreshToken) => model.id, "=?");
  const results: any[] = await con.queryResult(query.select(), [id]);
  if (results.length === 0) {
    throw new Errors.ObjectNotFoundError();
  }
  return new RefreshToken(results[0]);
}
