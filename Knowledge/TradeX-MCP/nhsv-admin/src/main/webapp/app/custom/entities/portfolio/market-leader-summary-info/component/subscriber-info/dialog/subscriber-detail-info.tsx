import React from 'react';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap';
import { SubscriberDetailInfoProps } from 'app/custom/model/subscriber-detail-info-props';
import '../../../../portfolio.scss';
import SubscriberOrderList from 'app/custom/entities/portfolio/market-leader-summary-info/component/subscriber-info/dialog/list/subscriber-order-list';

const SubscriberDetailInfoComponent: React.FC<SubscriberDetailInfoProps> = ({
  isShowDetailDialog,
  showSubscriberDetail,
  subscriberData,
}) => {

  return (
    <div className="container-fluid p-0">
      <div className="row">
        <Modal isOpen={isShowDetailDialog} toggle={showSubscriberDetail} centered={true} size={'xl'}>
          <ModalHeader toggle={showSubscriberDetail}>
            <span className="p-font-25">Copy Trading Orders</span>
          </ModalHeader>
          <ModalBody>
            <SubscriberOrderList subscriberData={subscriberData} />
          </ModalBody>
          <ModalFooter>
            <Button color="secondary" onClick={showSubscriberDetail}>
              Close
            </Button>
          </ModalFooter>
        </Modal>
      </div>
    </div>
  );
};

export default SubscriberDetailInfoComponent;
