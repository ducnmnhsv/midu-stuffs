import { COLOR } from '../../constants';
import { ISymbolHistoryEvents } from './../db/ISymbolHistoryEvents';
export interface IQuerySymbolHistoryEventsResponse {
  time: number[];
  color: string[];
  text: string[];
  label?: string[];
  labelFontColor?: string[];
  minSize?: string[];
}

export const toQuerySymbolHistoryResponse = (request: ISymbolHistoryEvents[]): IQuerySymbolHistoryEventsResponse => {
  const response: IQuerySymbolHistoryEventsResponse = {
    time: [],
    color: [],
    text: [],
    label: [],
    labelFontColor: [],
    minSize: [],
  };
  request.forEach((item: ISymbolHistoryEvents) => {
    response.time.push(item.eventDate.getTime());
    response.color.push(COLOR.BLUE);
    response.text.push(item.eventTitle);
  });
  return response;
};
