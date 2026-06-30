import FaqGroup from "../db/FaqGroup";
import { parse as parseFaq, FaqResponse } from "./FaqResponse";

const parse = (faqGroup: FaqGroup): FaqGroupResponse => {
  const response: FaqGroupResponse = new FaqGroupResponse();
  if (faqGroup != null) {
    response.name = faqGroup.name;
    response.msName = faqGroup.msName;
    response.lang = faqGroup.lang;
    if (faqGroup.faqs != null && faqGroup.faqs.length > 0) {
      for (let index = 0; index < faqGroup.faqs.length; index++) {
        response.faqs.push(parseFaq(faqGroup.faqs[index]));
      }
    }
  }

  return response;
};

class FaqGroupResponse {
  public name: string;
  public msName: string;
  public lang: string;
  public faqs: FaqResponse[] = [];
}

export { parse, FaqGroupResponse };
