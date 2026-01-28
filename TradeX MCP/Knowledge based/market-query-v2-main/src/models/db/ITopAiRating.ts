export interface ITopAiRating {
  _id: string;
  date: Date;
  code: string;
  techScore: number;
  valuationScore: number;
  gsScore: number;
  overall: number;
  price: number;
  change: number;
}
