import Faq from "../db/Faq";

const parse = (faq: Faq): FaqResponse => {
  const response: FaqResponse = new FaqResponse();
  if (faq != null) {
    response.question = faq.question;
    response.question = faq.answer;
  }

  return response;
};

class FaqResponse {
  public question: string;
  public answer: string;
}

export { parse, FaqResponse };
