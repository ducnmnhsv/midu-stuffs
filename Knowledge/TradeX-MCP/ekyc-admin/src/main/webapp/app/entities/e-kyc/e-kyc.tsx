import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, InputGroup, Col, Row, Table } from 'reactstrap';
import { AvForm, AvGroup, AvInput } from 'availity-reactstrap-validation';
import { Translate, translate, ICrudSearchAction, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getSearchEntities, getEntities } from './e-kyc.reducer';
import { IEKyc } from 'app/shared/model/e-kyc.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycProps extends StateProps, DispatchProps, RouteComponentProps<{ url: string }> {}

export const EKyc = (props: IEKycProps) => {
  const [search, setSearch] = useState('');

  useEffect(() => {
    props.getEntities();
  }, []);

  const startSearching = () => {
    if (search) {
      props.getSearchEntities(search);
    }
  };

  const clear = () => {
    setSearch('');
    props.getEntities();
  };

  const handleSearch = event => setSearch(event.target.value);

  const handleSyncList = () => {
    props.getEntities();
  };

  const { eKycList, match, loading } = props;
  return (
    <div>
      <h2 id="e-kyc-heading" data-cy="EKycHeading">
        <Translate contentKey="eKycAdminApp.eKyc.home.title">E Kycs</Translate>
        <div className="d-flex justify-content-end">
          <Button className="mr-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="eKycAdminApp.eKyc.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="eKycAdminApp.eKyc.home.createLabel">Create new E Kyc</Translate>
          </Link>
        </div>
      </h2>
      <Row>
        <Col sm="12">
          <AvForm onSubmit={startSearching}>
            <AvGroup>
              <InputGroup>
                <AvInput
                  type="text"
                  name="search"
                  value={search}
                  onChange={handleSearch}
                  placeholder={translate('eKycAdminApp.eKyc.home.search')}
                />
                <Button className="input-group-addon">
                  <FontAwesomeIcon icon="search" />
                </Button>
                <Button type="reset" className="input-group-addon" onClick={clear}>
                  <FontAwesomeIcon icon="trash" />
                </Button>
              </InputGroup>
            </AvGroup>
          </AvForm>
        </Col>
      </Row>
      <div className="table-responsive">
        {eKycList && eKycList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.identifierId">Identifier Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.fullName">Full Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.phoneNo">Phone No</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.gender">Gender</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.type">Type</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.birthDay">Birth Day</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.expiredDate">Expired Date</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.issueDate">Issue Date</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.issuePlace">Issue Place</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.address">Address</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.occupation">Occupation</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.homeTown">Home Town</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.permanentProvince">Permanent Province</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.permanentDistrict">Permanent District</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.permanentAddress">Permanent Address</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.contactProvince">Contact Province</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.contactDistrict">Contact District</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.contactAddress">Contact Address</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.email">Email</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.referrerIdName">Referrer Id Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.referrerBranch">Referrer Branch</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.bankAccount">Bank Account</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.accountName">Account Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.bankName">Bank Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.branch">Branch</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.nationality">Nationality</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.status">Status</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.frontImageUrl">Front Image Url</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.backImageUrl">Back Image Url</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.portraitImageUrl">Portrait Image Url</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.signatureImageUrl">Signature Image Url</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.tradingCodeImageUrl">Trading Code Image Url</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.isMargin">Is Margin</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.matchingRate">Matching Rate</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.updatedAt">Updated At</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.createdAt">Created At</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.branchId">Branch Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.channelId">Channel Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.eKycId">E Kyc Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.taxNumber">Tax Number</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.onlineTrading">Online Trading</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.authenMethod">Authen Method</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.otpReceiveMethod">Otp Receive Method</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.advancedCashIncluded">Advanced Cash Included</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.smsMethod">Sms Method</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.emailNotification">Email Notification</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.referral">Referral</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.partnerId">Partner Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.partnerName">Partner Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.customerSupport">Customer Support</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.csPartnerId">Cs Partner Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.csName">Cs Name</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.contractId">Contract Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.contractStatus">Contract Status</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.fatca">Fatca</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.contractNo">Contract No</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.accountNumber">Account Number</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.ocrLogId">Ocr Log Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.cardLivenessLogId">Card Liveness Log Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.cardRearLogId">Card Rear Log Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.compareLogId">Compare Log Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.faceLivenessLogId">Face Liveness Log Id</Translate>
                </th>
                <th>
                  <Translate contentKey="eKycAdminApp.eKyc.faceMaskLogId">Face Mask Log Id</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {eKycList.map((eKyc, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${eKyc.id}`} color="link" size="sm">
                      {eKyc.id}
                    </Button>
                  </td>
                  <td>{eKyc.identifierId}</td>
                  <td>{eKyc.fullName}</td>
                  <td>{eKyc.phoneNo}</td>
                  <td>{eKyc.gender}</td>
                  <td>
                    <Translate contentKey={`eKycAdminApp.EkycType.${eKyc.type}`} />
                  </td>
                  <td>{eKyc.birthDay ? <TextFormat type="date" value={eKyc.birthDay} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{eKyc.expiredDate ? <TextFormat type="date" value={eKyc.expiredDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{eKyc.issueDate ? <TextFormat type="date" value={eKyc.issueDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
                  <td>{eKyc.issuePlace}</td>
                  <td>{eKyc.address}</td>
                  <td>{eKyc.occupation}</td>
                  <td>{eKyc.homeTown}</td>
                  <td>{eKyc.permanentProvince}</td>
                  <td>{eKyc.permanentDistrict}</td>
                  <td>{eKyc.permanentAddress}</td>
                  <td>{eKyc.contactProvince}</td>
                  <td>{eKyc.contactDistrict}</td>
                  <td>{eKyc.contactAddress}</td>
                  <td>{eKyc.email}</td>
                  <td>{eKyc.referrerIdName}</td>
                  <td>{eKyc.referrerBranch}</td>
                  <td>{eKyc.bankAccount}</td>
                  <td>{eKyc.accountName}</td>
                  <td>{eKyc.bankName}</td>
                  <td>{eKyc.branch}</td>
                  <td>{eKyc.nationality}</td>
                  <td>
                    <Translate contentKey={`eKycAdminApp.Status.${eKyc.status}`} />
                  </td>
                  <td>{eKyc.frontImageUrl}</td>
                  <td>{eKyc.backImageUrl}</td>
                  <td>{eKyc.portraitImageUrl}</td>
                  <td>{eKyc.signatureImageUrl}</td>
                  <td>{eKyc.tradingCodeImageUrl}</td>
                  <td>{eKyc.isMargin ? 'true' : 'false'}</td>
                  <td>{eKyc.matchingRate}</td>
                  <td>{eKyc.updatedAt ? <TextFormat type="date" value={eKyc.updatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{eKyc.createdAt ? <TextFormat type="date" value={eKyc.createdAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{eKyc.branchId}</td>
                  <td>{eKyc.channelId}</td>
                  <td>{eKyc.eKycId}</td>
                  <td>{eKyc.taxNumber}</td>
                  <td>{eKyc.onlineTrading ? 'true' : 'false'}</td>
                  <td>{eKyc.authenMethod}</td>
                  <td>{eKyc.otpReceiveMethod}</td>
                  <td>{eKyc.advancedCashIncluded ? 'true' : 'false'}</td>
                  <td>{eKyc.smsMethod}</td>
                  <td>{eKyc.emailNotification ? 'true' : 'false'}</td>
                  <td>{eKyc.referral}</td>
                  <td>{eKyc.partnerId}</td>
                  <td>{eKyc.partnerName}</td>
                  <td>{eKyc.customerSupport ? 'true' : 'false'}</td>
                  <td>{eKyc.csPartnerId}</td>
                  <td>{eKyc.csName}</td>
                  <td>{eKyc.contractId}</td>
                  <td>{eKyc.contractStatus}</td>
                  <td>{eKyc.fatca ? 'true' : 'false'}</td>
                  <td>{eKyc.contractNo}</td>
                  <td>{eKyc.accountNumber}</td>
                  <td>{eKyc.ocrLogId}</td>
                  <td>{eKyc.cardLivenessLogId}</td>
                  <td>{eKyc.cardRearLogId}</td>
                  <td>{eKyc.compareLogId}</td>
                  <td>{eKyc.faceLivenessLogId}</td>
                  <td>{eKyc.faceMaskLogId}</td>
                  <td className="text-right">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`${match.url}/${eKyc.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${eKyc.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${eKyc.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
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
              <Translate contentKey="eKycAdminApp.eKyc.home.notFound">No E Kycs found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

const mapStateToProps = ({ eKyc }: IRootState) => ({
  eKycList: eKyc.entities,
  loading: eKyc.loading,
});

const mapDispatchToProps = {
  getSearchEntities,
  getEntities,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKyc);
