import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { Cursor } from 'mongodb';
import { COLLECTIONS_NAME } from '../constants';
import { ISymbol } from '../models/db/ISymbol';

@Service()
export class SymbolRepository {
  public findAll(): Cursor<ISymbol> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL).find({}).sort({ _id: 1 });
  }
}
