import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row, Table } from 'reactstrap';
import { byteSize, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntities } from './e-kyc-ext.reducer';
import { IEKycExt } from 'app/shared/model/e-kyc-ext.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycExtProps extends StateProps, DispatchProps, RouteComponentProps<{ url: string }> {}

export const EKycExt = (props: IEKycExtProps) => {
  useEffect(() => {
    props.getEntities();
  }, []);

  const handleSyncList = () => {
    props.getEntities();
  };

  const { eKycExtList, match, loading } = props;
  return (
    <div>
      <h2 id="e-kyc-ext-heading" data-cy="EKycExtHeading">
        <Translate contentKey="eKycAdminApp.eKycExt.home.title">E Kyc Exts</Translate>
        <div className="d-flex justify-content-end">
          <Button className="mr-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="eKycAdminApp.eKycExt.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="eKycAdminApp.eKycExt.home.createLabel">Create new E Kyc Ext</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {eKycExtList && eKycExtList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycExt.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycExt.logId">Log Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycExt.rawData">Raw Data</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycExt.eKyc">E Kyc</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {eKycExtList.map((eKycExt, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${eKycExt.id}`} color="link" size="sm">
                      {eKycExt.id}
                    </Button>
                  </td>
                  <td>{eKycExt.logId}</td>
                  <td>{eKycExt.rawData}</td>
                  <td>{eKycExt.eKyc ? <Link to={`e-kyc/${eKycExt.eKyc.id}`}>{eKycExt.eKyc.id}</Link> : ''}</td>
                  <td className="text-right">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`${match.url}/${eKycExt.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${eKycExt.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${eKycExt.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
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
              <Translate contentKey="eKycAdminApp.eKycExt.home.notFound">No E Kyc Exts found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

const mapStateToProps = ({ eKycExt }: IRootState) => ({
  eKycExtList: eKycExt.entities,
  loading: eKycExt.loading,
});

const mapDispatchToProps = {
  getEntities,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycExt);
