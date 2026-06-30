import React from 'react';
import '../../../../portfolio.scss';
import { APP_TIMESTAMP_FORMAT } from 'app/config/constants';
import TableListEmpty from 'app/custom/entities/commons/table-list-empty';
import lodash from 'lodash';
import { JhiItemCount, JhiPagination, TextFormat } from 'react-jhipster';
import { useAppSelector } from 'app/config/store';

const CurrentPortfolioListComponent = props => {
  const loading = props.loading;
  const copyPortfolioDetails = props.copyPortfolioDetails;
  const paginationState = props.paginationState;
  const handlePagination = props.handlePagination;
  const totalItems = useAppSelector(state => state.portfolioReducer.entity.portfolioDetailTotalItems);

  return (
    <div className="container-fluid">
      <div className="row">
        <div className="col-12 p-0">
          <table className="table table-hover table-bordered">
            <thead className="table-light">
              <tr>
                <th className="p-width-percent-10 text-center" scope="col">
                  ID
                </th>
                <th className="p-width-percent-30 text-center" scope="col">
                  Stock Code
                </th>
                <th className="p-width-percent-30 text-center" scope="col">
                  Stock Weight
                </th>
                <th className="p-width-percent-30 text-center" scope="col">
                  Updated Time
                </th>
              </tr>
            </thead>
            <tbody>
              {copyPortfolioDetails && Array.isArray(copyPortfolioDetails) && copyPortfolioDetails.length > 0 ? (
                copyPortfolioDetails?.map((pDetail, i) => (
                  <tr key={`entity-${i}`} data-cy="entityTable">
                    <td className="p-width-percent-10 text-center">{pDetail?.id}</td>
                    <td className="p-width-percent-30 text-center">{pDetail?.symbol}</td>
                    <td className="p-width-percent-30 text-center">{lodash.round(pDetail?.weight * 100, 2)} %</td>
                    <td className="p-width-percent-30 text-center">
                      {pDetail.createdAt ? <TextFormat type="date" value={pDetail.createdAt} format={APP_TIMESTAMP_FORMAT} /> : null}
                    </td>
                  </tr>
                ))
              ) : (
                <TableListEmpty title={'Current portfolio'} />
              )}
            </tbody>
          </table>
        </div>
      </div>
      <div className="row">
        <div className="col-12">
          {totalItems ? (
            <div className={copyPortfolioDetails && copyPortfolioDetails.length > 0 ? 'p-pagination' : 'd-none'}>
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
    </div>
  );
};

export default CurrentPortfolioListComponent;
