import { Models } from "tradex-common";

export default interface IFaqReviewRequest extends Models.IDataRequest {
  faqId: number;
  isUseful: boolean;
}
