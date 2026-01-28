import { Service } from 'typedi';
import { getDb } from '../utils/dbConnection';
import { Cursor, FilterQuery } from 'mongodb';
import { COLLECTIONS_NAME, DEFAULT_OFFSET, DEFAULT_PAGE_SIZE } from '../constants';
import { ITopAiRating } from '../models/db/ITopAiRating';

@Service()
export class TopAiRatingRepository {
  public findBy(
    query: FilterQuery<ITopAiRating>,
    limit: number = DEFAULT_PAGE_SIZE,
    offset: number = DEFAULT_OFFSET,
    sort: any = {},
  ): Cursor<ITopAiRating> {
    return getDb().collection(COLLECTIONS_NAME.TOP_AI_RATING).find(query).sort(sort).skip(offset).limit(limit);
  }
}
