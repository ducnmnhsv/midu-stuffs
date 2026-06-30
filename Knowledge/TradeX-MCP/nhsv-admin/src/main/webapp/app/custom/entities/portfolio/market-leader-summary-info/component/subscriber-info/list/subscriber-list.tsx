import React, { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { format } from 'date-fns';
import '../subscriber-info.scss';
import '../../../../portfolio.scss';
import { utcToZonedTime } from 'date-fns-tz';
import { APP_DATE_TIME_FORMAT, APP_TIMESTAMP_FORMAT, APT_ZONE_UTC } from "app/config/constants";
import { ICopySubscriber } from 'app/shared/model/copy-subscriber.model';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowsRotate, faEye } from '@fortawesome/free-solid-svg-icons';
import SubscriberDetailInfo from 'app/custom/entities/portfolio/market-leader-summary-info/component/subscriber-info/dialog/subscriber-detail-info';
import { getSortState, JhiItemCount, JhiPagination, TextFormat } from "react-jhipster";
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';
import { getSubscriberOfMLAccount } from 'app/custom/entities/portfolio/handle/api/portfolio-api';
import { IAccount } from 'app/custom/model/account.model';
import TableListEmpty from 'app/custom/entities/commons/table-list-empty';
import { Button } from "reactstrap";

const SubscriberListComponent = props => {
  const accountInfo: IAccount = props.accountInfo;
  const [isShowDetailDialog, setShowDetailDialog] = useState(false);
  const [subscriberData, setSubscriberData] = useState<ICopySubscriber>({});
  const mlSubscribers: ICopySubscriber[] = useAppSelector(state => state.portfolioReducer.entity?.mlSubscribers);
  const loading = useAppSelector(state => state.portfolioReducer.entity.loadingSubscribers);
  const totalItems = useAppSelector(state => state.portfolioReducer.entity.subscriberTotalItems);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const showSubscriberDetail = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>, id: number) => {
    e.preventDefault();
    setSubscriberData(mlSubscribers.filter(x => x.id === id)[0]);
    toggleModal();
  };

  const toggleModal = () => {
    localStorage.removeItem('subOrderFilterFromDate');
    localStorage.removeItem('subOrderFilterToDate');
    localStorage.removeItem('subOrderFilterRoles');
    localStorage.removeItem('subOrderFilterSellBuyType');
    localStorage.removeItem('subOrderFilterStockCode');
    setShowDetailDialog(!isShowDetailDialog);
  };

  const dispatch = useAppDispatch();

  const reloadSubscriberInfo = () => {
    fetchSubscribers();
  };

  useEffect(() => {
    reloadSubscriberInfo();
  }, [dispatch, paginationState.activePage, paginationState.order, paginationState.sort]);

  const makeRequestParam = () => {
    const paramArray = [];
    let paramString = ``;
    paramArray.push(`mlID=${accountInfo?.id}`);
    paramArray.push(`page=${paginationState.activePage - 1}`);
    paramArray.push(
      `size=${paginationState.itemsPerPage && paginationState.itemsPerPage === 0 ? ITEMS_PER_PAGE : paginationState.itemsPerPage}`
    );
    paramArray.push(`sort=${paginationState.sort},${paginationState.order}`);
    if (paramArray.length > 0) {
      for (let i = 0; i < paramArray.length; i++) {
        if (i === 0) {
          paramString += `?${paramArray[i]}`;
        } else {
          paramString += `&${paramArray[i]}`;
        }
      }
    }
    return paramString;
  };

  const fetchSubscribers = () => {
    const requestParams = makeRequestParam();
    dispatch(getSubscriberOfMLAccount(requestParams));
  };

  const handlePagination = currentPage => {
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });
  };

  return (
    <div className="container-fluid">
      <div className="row">
        <div className="col-12 d-flex justify-content-end align-items-center p-0">
          <Button color="info" onClick={reloadSubscriberInfo} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
        </div>
      </div>
      <div className="row p-m-30">
        <div className="col-12 p-0">
          <table className="table table-hover table-bordered table-responsive align-middle">
            <thead className="table-light">
              <tr>
                <th className="p-width-percent-10 text-center" scope="col">
                  ID
                </th>
                <th className="p-width-percent-30 text-center" scope="col">
                  Account Number
                </th>
                <th className="p-width-percent-20 text-center" scope="col">
                  Sub Number
                </th>
                <th className="p-width-percent-30 text-center" scope="col">
                  Subscribed Time
                </th>
                <th className="p-width-percent-10 text-center" scope="col">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {mlSubscribers && Array.isArray(mlSubscribers) && mlSubscribers.length > 0 ? (
                mlSubscribers?.map((sub, i) => (
                  <tr key={`entity-${i}`} data-cy="entityTable">
                    <td className="p-width-percent-10 text-center">{sub?.id}</td>
                    <td className="p-width-percent-30 text-center">{sub?.accountNumber}</td>
                    <td className="p-width-percent-20 text-center">{sub?.subNumber}</td>
                    <td className="p-width-percent-30 text-center">
                      {sub.createdAt ? <TextFormat type="date" value={sub.createdAt} format={APP_TIMESTAMP_FORMAT} /> : null}
                    </td>
                    <td className="p-width-percent-10 text-center">
                      <button type="button" className="btn btn-sm btn-info" onClick={event => showSubscriberDetail(event, sub?.id)}>
                        <FontAwesomeIcon icon={faEye} className="p-icon-l" /> View
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <TableListEmpty title={'Subscribers'} />
              )}
            </tbody>
          </table>
        </div>
      </div>
      <div className="row">
        <div className="col-12">
          {totalItems ? (
            <div className={mlSubscribers && mlSubscribers.length > 0 ? 'p-pagination' : 'd-none'}>
              <div className="justify-content-center d-flex">
                <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
              </div>
              <div className="justify-content-center d-flex">
                <JhiPagination
                  activePage={paginationState.activePage}
                  onSelect={handlePagination}
                  maxButtons={5}
                  itemsPerPage={paginationState.itemsPerPage}
                  totalItems={totalItems}
                />
              </div>
            </div>
          ) : (
            ''
          )}
        </div>
      </div>
      <div className="row">
        <SubscriberDetailInfo isShowDetailDialog={isShowDetailDialog} showSubscriberDetail={toggleModal} subscriberData={subscriberData} />
      </div>
    </div>
  );
};

export default SubscriberListComponent;
