import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router-dom';
import { getEntity } from './e-kyc.reducer';
import { Translate, TextFormat } from 'react-jhipster';
import { IRootState } from 'app/shared/reducers';
import { Modal, ModalHeader, ModalBody, Row, Col, Table } from 'reactstrap';
import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IEKycDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycPopupDetail = (props: IEKycDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const handleClose = () => {
    props.history.push('/e-kyc');
  };

  const { eKycEntity } = props;
  return (
    <Modal isOpen toggle={handleClose} size="xl">
      <ModalHeader toggle={handleClose} data-cy="entityDetailsHeading">
        <Translate contentKey="eKycAdminApp.eKyc.detailedInformation">Detailed Information</Translate>
      </ModalHeader>
      <ModalBody>
        <Row className="detail-popup">
          <Col className="info" md="5">
            <Table borderless>
              <tbody>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.name">Name</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.fullName}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.dob">DOB</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>
                      {eKycEntity.birthDay ? <TextFormat type="date" value={eKycEntity.birthDay} format={APP_LOCAL_DATE_FORMAT} /> : null}
                    </span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.idNumber">ID</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.identifierId}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.dateOfIssued">Date of Issued</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>
                      {eKycEntity.issueDate ? <TextFormat type="date" value={eKycEntity.issueDate} format={APP_LOCAL_DATE_FORMAT} /> : null}
                    </span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.placeOfIssued">Place of Issued</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.issuePlace}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.email">Email</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.email}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.phoneNumber">Phone Number</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.phoneNo}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.address">Address</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.address}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.bankAccount">Bank account</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.bankAccount}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.bank">Bank</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.bankName}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.bankBranch">Bank branch</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.branch}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.matchingRate">Matching rate</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.matchingRate?.toFixed(2).toString()}%</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.kisBranch">KIS branch</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.referrerBranch}</span>
                  </td>
                </tr>
                <tr>
                  <td className="label">
                    <Translate contentKey="eKycAdminApp.eKyc.agency">Agency</Translate>
                    <span>:</span>
                  </td>
                  <td>
                    <span>{eKycEntity.referrerIdName}</span>
                  </td>
                </tr>
              </tbody>
            </Table>
          </Col>
          <Col md="7" className="info-image">
            <Row>
              <Col md="6" className="text-center mb-4">
                <p>
                  <Translate contentKey="eKycAdminApp.eKyc.frontOfYourDocument">Front of your document</Translate>
                </p>
                <img width="100%" src={eKycEntity.frontImageUrl} />
              </Col>
              <Col md="6" className="text-center mb-4">
                <p>
                  <Translate contentKey="eKycAdminApp.eKyc.backOfYourDocument">Back of your document</Translate>
                </p>
                <img width="100%" src={eKycEntity.backImageUrl} />
              </Col>
              <Col md="6" className="text-center mb-4">
                <p>
                  <Translate contentKey="eKycAdminApp.eKyc.signature">Signature</Translate>
                </p>
                <img width="100%" src={eKycEntity.signatureImageUrl} />
              </Col>
              <Col md="6" className="text-center mb-4">
                <p>
                  <Translate contentKey="eKycAdminApp.eKyc.tradingCode">Trading Code</Translate>
                </p>
                <img width="100%" src={eKycEntity.tradingCodeImageUrl} />
              </Col>
            </Row>
          </Col>
        </Row>
      </ModalBody>
    </Modal>
  );
};

const mapStateToProps = ({ eKyc }: IRootState) => ({
  eKycEntity: eKyc.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycPopupDetail);
