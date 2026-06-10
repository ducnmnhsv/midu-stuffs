import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row, Table } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntities } from './e-kyc-bank-list.reducer';
import { IEKycBankList } from 'app/shared/model/e-kyc-bank-list.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycBankListProps extends StateProps, DispatchProps, RouteComponentProps<{ url: string }> {}

export const EKycBankList = (props: IEKycBankListProps) => {
  useEffect(() => {
    props.getEntities();
  }, []);

  const handleSyncList = () => {
    props.getEntities();
  };

  const { eKycBankListList, match, loading } = props;
  return (
    <div>
      <h2 id="e-kyc-bank-list-heading" data-cy="EKycBankListHeading">
        <Translate contentKey="eKycAdminApp.eKycBankList.home.title">E Kyc Bank Lists</Translate>
        <div className="d-flex justify-content-end">
          <Button className="mr-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="eKycAdminApp.eKycBankList.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="eKycAdminApp.eKycBankList.home.createLabel">Create new E Kyc Bank List</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {eKycBankListList && eKycBankListList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycBankList.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycBankList.bankId">Bank Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycBankList.bankName">Bank Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycBankList.bankAccNo">Bank Acc No</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycBankList.ownerName">Owner Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycBankList.branchId">Branch Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycBankList.eKyc">E Kyc</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {eKycBankListList.map((eKycBankList, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${eKycBankList.id}`} color="link" size="sm">
                      {eKycBankList.id}
                    </Button>
                  </td>
                  <td>{eKycBankList.bankId}</td>
                  <td>{eKycBankList.bankName}</td>
                  <td>{eKycBankList.bankAccNo}</td>
                  <td>{eKycBankList.ownerName}</td>
                  <td>{eKycBankList.branchId}</td>
                  <td>{eKycBankList.eKyc ? <Link to={`e-kyc/${eKycBankList.eKyc.id}`}>{eKycBankList.eKyc.id}</Link> : ''}</td>
                  <td className="text-right">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`${match.url}/${eKycBankList.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${eKycBankList.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`${match.url}/${eKycBankList.id}/delete`}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="eKycAdminApp.eKycBankList.home.notFound">No E Kyc Bank Lists found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

const mapStateToProps = ({ eKycBankList }: IRootState) => ({
  eKycBankListList: eKycBankList.entities,
  loading: eKycBankList.loading,
});

const mapDispatchToProps = {
  getEntities,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycBankList);
