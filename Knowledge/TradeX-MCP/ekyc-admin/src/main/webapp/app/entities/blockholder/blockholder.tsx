import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row, Table } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntities } from './blockholder.reducer';
import { IBlockholder } from 'app/shared/model/blockholder.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IBlockholderProps extends StateProps, DispatchProps, RouteComponentProps<{ url: string }> {}

export const Blockholder = (props: IBlockholderProps) => {
  useEffect(() => {
    props.getEntities();
  }, []);

  const handleSyncList = () => {
    props.getEntities();
  };

  const { blockholderList, match, loading } = props;
  return (
    <div>
      <h2 id="blockholder-heading" data-cy="BlockholderHeading">
        <Translate contentKey="eKycAdminApp.blockholder.home.title">Blockholders</Translate>
        <div className="d-flex justify-content-end">
          <Button className="mr-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="eKycAdminApp.blockholder.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="eKycAdminApp.blockholder.home.createLabel">Create new Blockholder</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {blockholderList && blockholderList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="eKycAdminApp.blockholder.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.blockholder.companyName">Company Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.blockholder.stock">Stock</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.blockholder.position">Position</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.blockholder.eKycAdditionalInfo">E Kyc Additional Info</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {blockholderList.map((blockholder, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${blockholder.id}`} color="link" size="sm">
                      {blockholder.id}
                    </Button>
                  </td>
                  <td>{blockholder.companyName}</td>
                  <td>{blockholder.stock}</td>
                  <td>{blockholder.position}</td>
                  <td>
                    {blockholder.eKycAdditionalInfo ? (
                      <Link to={`e-kyc-additional-info/${blockholder.eKycAdditionalInfo.id}`}>{blockholder.eKycAdditionalInfo.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-right">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`${match.url}/${blockholder.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${blockholder.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${blockholder.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
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
              <Translate contentKey="eKycAdminApp.blockholder.home.notFound">No Blockholders found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

const mapStateToProps = ({ blockholder }: IRootState) => ({
  blockholderList: blockholder.entities,
  loading: blockholder.loading,
});

const mapDispatchToProps = {
  getEntities,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(Blockholder);
