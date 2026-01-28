import { COLLECTIONS_NAME } from './../constants/index';
import { ISymbolHistoryEvents } from './../models/db/ISymbolHistoryEvents';
import { Service } from 'typedi';
import { FilterQuery, Cursor } from 'mongodb';
import { getDb } from '../utils/dbConnection';

@Service()
export class SymbolHistoryEventsRepository {
  public findBy(query: FilterQuery<ISymbolHistoryEvents>, sort: any = {}): Cursor<ISymbolHistoryEvents> {
    return getDb().collection(COLLECTIONS_NAME.SYMBOL_HISTORY_EVENTS).find(query).sort(sort);
  }
}
