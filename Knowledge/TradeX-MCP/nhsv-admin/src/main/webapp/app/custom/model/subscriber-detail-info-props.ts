import { ICopySubscriber } from 'app/shared/model/copy-subscriber.model';

export interface SubscriberDetailInfoProps {
  isShowDetailDialog: boolean;
  showSubscriberDetail: () => void;
  subscriberData: ICopySubscriber;
}
