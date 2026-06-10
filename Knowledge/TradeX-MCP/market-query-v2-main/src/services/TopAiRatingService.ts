import { Inject, Service } from 'typedi';
import { TopAiRatingRepository } from '../repositories/TopAiRatingRepository';
import { validateRequest } from '../utils/parse';
import { toTopAiRatingResponse } from '../utils/ResponseUtils';
import { DEFAULT_PAGE_SIZE, INVALID_PARAMETER } from '../constants';
import { ITopAiRating } from '../models/db/ITopAiRating';
import { TopAiRatingRequest, TopAiRatingResponse } from 'tradex-models-market';
import { topAiRatingRequestValidator } from 'tradex-models-market-validator';
import { Errors } from 'tradex-common';
@Service()
export default class TopAiRatingService {
  @Inject()
  private readonly topAiRatingRepository: TopAiRatingRepository;

  public async queryTopAiRating(request: TopAiRatingRequest): Promise<TopAiRatingResponse> {
    validateRequest(request, topAiRatingRequestValidator);

    const fetchCount: number = request.fetchCount == null ? DEFAULT_PAGE_SIZE : request.fetchCount;
    const sort = {
      overall: -1,
    };

    let finalList: ITopAiRating[] = [];
    if (request.lastOverAll != null && request.lastCode != null) {
      const filter = { overall: { $lt: request.lastOverAll } };
      const majorList = await this.topAiRatingRepository.findBy(filter, fetchCount, 0, sort).toArray();

      let minorList: ITopAiRating[] = await this.topAiRatingRepository
        .findBy({ overall: request.lastOverAll }, Number.MAX_SAFE_INTEGER, 0, sort)
        .toArray();

      const codeList: string[] = minorList.map((item: ITopAiRating) => item.code);
      const lastRecordIndex = codeList.indexOf(request.lastCode);

      minorList = minorList.splice(lastRecordIndex + 1, minorList.length);
      const totalList = minorList.concat(majorList);
      finalList = totalList.splice(0, fetchCount);
    } else if (request.lastOverAll == null && request.lastCode == null) {
      finalList = await this.topAiRatingRepository.findBy({}, fetchCount, 0, sort).toArray();
    } else {
      throw new Errors.GeneralError(INVALID_PARAMETER);
    }

    return toTopAiRatingResponse(finalList);
  }
}
