import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Col, Row, Table } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntities } from './e-kyc-additional-info.reducer';
import { IEKycAdditionalInfo } from 'app/shared/model/e-kyc-additional-info.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycAdditionalInfoProps extends StateProps, DispatchProps, RouteComponentProps<{ url: string }> {}

export const EKycAdditionalInfo = (props: IEKycAdditionalInfoProps) => {
  useEffect(() => {
    props.getEntities();
  }, []);

  const handleSyncList = () => {
    props.getEntities();
  };

  const { eKycAdditionalInfoList, match, loading } = props;
  return (
    <div>
      <h2 id="e-kyc-additional-info-heading" data-cy="EKycAdditionalInfoHeading">
        <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.home.title">E Kyc Additional Infos</Translate>
        <div className="d-flex justify-content-end">
          <Button className="mr-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.home.createLabel">Create new E Kyc Additional Info</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {eKycAdditionalInfoList && eKycAdditionalInfoList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.fullName">Full Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.birthDay">Birth Day</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.nationality">Nationality</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.identifierId">Identifier Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.issueDate">Issue Date</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.issuePlace">Issue Place</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.permanentAddress">Permanent Address</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.contactAddress">Contact Address</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.occupation">Occupation</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.position">Position</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.phoneNumber">Phone Number</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.visaNo">Visa No</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.visaIssuePlace">Visa Issue Place</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.foreignResidence">Foreign Residence</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.investmentGoal">Investment Goal</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.risk">Risk</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.experienced">Experienced</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.eKyc">E Kyc</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {eKycAdditionalInfoList.map((eKycAdditionalInfo, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${eKycAdditionalInfo.id}`} color="link" size="sm">
                      {eKycAdditionalInfo.id}
                    </Button>
                  </td>
                  <td>{eKycAdditionalInfo.fullName}</td>
                  <td>{eKycAdditionalInfo.birthDay}</td>
                  <td>{eKycAdditionalInfo.nationality}</td>
                  <td>{eKycAdditionalInfo.identifierId}</td>
                  <td>{eKycAdditionalInfo.issueDate}</td>
                  <td>{eKycAdditionalInfo.issuePlace}</td>
                  <td>{eKycAdditionalInfo.permanentAddress}</td>
                  <td>{eKycAdditionalInfo.contactAddress}</td>
                  <td>{eKycAdditionalInfo.occupation}</td>
                  <td>{eKycAdditionalInfo.position}</td>
                  <td>{eKycAdditionalInfo.phoneNumber}</td>
                  <td>{eKycAdditionalInfo.visaNo}</td>
                  <td>{eKycAdditionalInfo.visaIssuePlace}</td>
                  <td>{eKycAdditionalInfo.foreignResidence}</td>
                  <td>{eKycAdditionalInfo.investmentGoal}</td>
                  <td>{eKycAdditionalInfo.risk}</td>
                  <td>{eKycAdditionalInfo.experienced ? 'true' : 'false'}</td>
                  <td>
                    {eKycAdditionalInfo.eKyc ? <Link to={`e-kyc/${eKycAdditionalInfo.eKyc.id}`}>{eKycAdditionalInfo.eKyc.id}</Link> : ''}
                  </td>
                  <td className="text-right">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`${match.url}/${eKycAdditionalInfo.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`${match.url}/${eKycAdditionalInfo.id}/edit`}
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
                        to={`${match.url}/${eKycAdditionalInfo.id}/delete`}
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
              <Translate contentKey="eKycAdminApp.eKycAdditionalInfo.home.notFound">No E Kyc Additional Infos found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

const mapStateToProps = ({ eKycAdditionalInfo }: IRootState) => ({
  eKycAdditionalInfoList: eKycAdditionalInfo.entities,
  loading: eKycAdditionalInfo.loading,
});

const mapDispatchToProps = {
  getEntities,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycAdditionalInfo);
