import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row, Table } from 'reactstrap';
import { byteSize, Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntities } from './e-kyc-creator-status.reducer';
import { IEKycCreatorStatus } from 'app/shared/model/e-kyc-creator-status.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycCreatorStatusProps extends StateProps, DispatchProps, RouteComponentProps<{ url: string }> {}

export const EKycCreatorStatus = (props: IEKycCreatorStatusProps) => {
  useEffect(() => {
    props.getEntities();
  }, []);

  const handleSyncList = () => {
    props.getEntities();
  };

  const { eKycCreatorStatusList, match, loading } = props;
  return (
    <div>
      <h2 id="e-kyc-creator-status-heading" data-cy="EKycCreatorStatusHeading">
        <Translate contentKey="eKycAdminApp.eKycCreatorStatus.home.title">E Kyc Creator Statuses</Translate>
        <div className="d-flex justify-content-end">
          <Button className="mr-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="eKycAdminApp.eKycCreatorStatus.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="eKycAdminApp.eKycCreatorStatus.home.createLabel">Create new E Kyc Creator Status</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {eKycCreatorStatusList && eKycCreatorStatusList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.status">Status</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.reason">Reason</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.updatedAt">Updated At</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.updatedBy">Updated By</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.fullResult">Full Result</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycCreatorStatus.eKyc">E Kyc</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {eKycCreatorStatusList.map((eKycCreatorStatus, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${eKycCreatorStatus.id}`} color="link" size="sm">
                      {eKycCreatorStatus.id}
                    </Button>
                  </td>
                  <td>{eKycCreatorStatus.status}</td>
                  <td>{eKycCreatorStatus.reason}</td>
                  <td>
                    {eKycCreatorStatus.updatedAt ? (
                      <TextFormat type="date" value={eKycCreatorStatus.updatedAt} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{eKycCreatorStatus.updatedBy}</td>
                  <td>{eKycCreatorStatus.fullResult}</td>
                  <td>
                    {eKycCreatorStatus.eKyc ? <Link to={`e-kyc/${eKycCreatorStatus.eKyc.id}`}>{eKycCreatorStatus.eKyc.id}</Link> : ''}
                  </td>
                  <td className="text-right">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`${match.url}/${eKycCreatorStatus.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`${match.url}/${eKycCreatorStatus.id}/edit`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`${match.url}/${eKycCreatorStatus.id}/delete`}
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
              <Translate contentKey="eKycAdminApp.eKycCreatorStatus.home.notFound">No E Kyc Creator Statuses found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

const mapStateToProps = ({ eKycCreatorStatus }: IRootState) => ({
  eKycCreatorStatusList: eKycCreatorStatus.entities,
  loading: eKycCreatorStatus.loading,
});

const mapDispatchToProps = {
  getEntities,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycCreatorStatus);
