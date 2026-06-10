import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router-dom';
import { getEntity } from './e-kyc.reducer';
import { Translate, TextFormat } from 'react-jhipster';
import { IRootState } from 'app/shared/reducers';
import { Modal, ModalHeader, ModalBody, Row, Col, Table, Nav, TabContent, TabPane, NavItem, NavLink, Button } from 'reactstrap';
import config, {APP_LOCAL_DATE_FORMAT, APP_TIMESTAMP_FORMAT} from 'app/config/constants';
import axios from 'axios';
import { IEKycBankList } from 'app/shared/model/e-kyc-bank-list.model';
import { IEKycAdditionalInfo } from 'app/shared/model/e-kyc-additional-info.model';
import { IPublicCoop } from 'app/shared/model/public-coop.model';
import { IBlockholder } from 'app/shared/model/blockholder.model';
import classnames from 'classnames';
import { IEContractInfo } from 'app/shared/model/e-contract-info.model';
import { IEContract } from 'app/shared/model/e-contract.model';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faExclamationCircle, faDownload } from '@fortawesome/free-solid-svg-icons';

export interface IEKycDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const EKycPopupDetail = (props: IEKycDetailProps) => {
  const [creatorStatus, setCreatorStatus] = useState(null);
  const [bankList, setBankList] = useState<IEKycBankList[]>([]);
  const [eKycAdditionalInfo, setEKycAdditionalInfo] = useState<IEKycAdditionalInfo>({});
  const [publicCoop, setPublicCoop] = useState<IPublicCoop[]>([]);
  const [blockholder, setBlockholder] = useState<IBlockholder[]>([]);
  const [eContractInfo, setEContractInfo] = useState<IEContractInfo>({});
  const [eContract, setEContract] = useState<IEContract>({});
  const [activeTab, setActiveTab] = useState('1');
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    props.getEntity(props.match.params.id);
    if (config.domain === 'nhsv') {
      fetchNhsv();
    } else {
      fetchCreatorStatus();
    }
  }, []);

  const handleClose = () => {
    props.history.push('/');
  };

  const toggle = tab => {
    if (activeTab !== tab) setActiveTab(tab);
  };

  const fetchCreatorStatus = async () => {
    await axios
      .get(`/api/e-kyc-creator-statuses`)
      .then(res => {
        const entityCreatorStatus = res.data.find(ele => ele.ekyc.id === Number(props.match.params.id)) || {};
        setCreatorStatus(entityCreatorStatus);
      })
      .catch(err => {
        console.log(err);
      });
  };

  const fetchBankList = async () => {
    try {
      const res = await axios.get(`/api/v1/ekyc-admin/ekyc/e-kyc-bank-lists/${props.match.params.id}`);
      setBankList(res.data);
    } catch (err) {
      console.log(err);
    }
  };

  const fetchEKycAdditionalInfo = async () => {
    try {
      const res = await axios.get(`/api/v1/ekyc-admin/ekyc/e-kyc-additional-infos/${props.match.params.id}`, {
        headers: {
          'Cache-Control': 'no-cache',
          Accept: 'application/json;charset=UTF-8',
        },
      });
      setEKycAdditionalInfo(res.data);
      await Promise.all([fetchPublicCoop(res.data.id), fetchBlockholder(res.data.id)]);
    } catch (err) {
      console.log(err);
    }
  };

  const fetchPublicCoop = async id => {
    try {
      const res = await axios.get(`/api/v1/ekyc-admin/ekyc/public-coop/${id}`);
      setPublicCoop(res.data);
    } catch (err) {
      console.log(err);
    }
  };

  const fetchBlockholder = async id => {
    try {
      const res = await axios.get(`/api/v1/ekyc-admin/ekyc/blockholder/${id}`);
      setBlockholder(res.data);
    } catch (err) {
      console.log(err);
    }
  };

  const fetchEContractInfo = async () => {
    try {
      const res = await axios.get(`/api/v1/ekyc-admin/ekyc/e-contract-info/${props.match.params.id}`);
      setEContractInfo(res.data);
      setEContract(res.data.econtract)
    } catch (err) {
      console.log(err);
    }
  };

  const fetchNhsv = () => {
    fetchBankList();
    fetchEKycAdditionalInfo();
    fetchEContractInfo();
  };

  const classOfMatchingRate = value => {
    let name = '';
    const valueOfMatchingRate = Number(value);
    if (valueOfMatchingRate > 90) {
      name = 'high';
    } else if (valueOfMatchingRate > 80 && valueOfMatchingRate <= 90) {
      name = 'little-high';
    } else if (valueOfMatchingRate > 70 && valueOfMatchingRate <= 80) {
      name = 'medium';
    } else {
      name = 'low';
    }
    return name;
  };

  const downloadFile = async (id) => {
    setDownloading(true);
    try {
      const res= await axios.get(`/api/v1/ekyc-admin/ekyc/e-contract/download/${id}`);
      const data = res.data;
      const filename = data.contractFileName;
      const linkSource = `data:application/pdf;base64,${data.contractFileContent}`;
      const downloadLink = document.createElement('a');
      downloadLink.href = linkSource;
      downloadLink.download = `${filename}.pdf`;
      downloadLink.click();
    }
    finally {
      setDownloading(false);
    }
  };

  const renderReferral = (referral, partnerName) => {
    switch (referral) {
      case '1': return `Nhân viên/CTV - ${partnerName}.`;
      case '2': return `Người quen - ${partnerName}.`;
      case '3': return `Quảng cáo - ${partnerName}.`;
      default: return `Khác - ${partnerName}.`;
    }
  }

  const { eKycEntity } = props;

  const contentModalBodyNhsv = () => {
    return (
      <div className="detail-popup">
        <Nav tabs className="mb-4">
          <NavItem>
            <NavLink
              className={classnames({ active: activeTab === '1' })}
              onClick={() => {
                toggle('1');
              }}
            >
              <Translate contentKey="eKycAdminApp.customEKyc.nhsv.personalInfo">Personal Info</Translate>
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink
              className={classnames({ active: activeTab === '2' })}
              onClick={() => {
                toggle('2');
              }}
            >
              <Translate contentKey="eKycAdminApp.customEKyc.nhsv.additionalInfo">Additional Info</Translate>
            </NavLink>
          </NavItem>
          <NavItem title={eKycEntity.accountNumber == null ? "Contract is not available because this EKYC has not been granted with a Account Number" : null}>
            <NavLink
              className={classnames({ active: activeTab === '3' })}
              onClick={() => {
                toggle('3');
              }}
              disabled={eKycEntity.accountNumber == null}
            >
              <Translate contentKey="eKycAdminApp.customEKyc.nhsv.contractInfo">Contract Info</Translate>
              &nbsp;
              {eKycEntity.accountNumber == null && (<FontAwesomeIcon icon={faExclamationCircle} className="p-icon-l"/>)}
            </NavLink>
          </NavItem>
        </Nav>
        <TabContent activeTab={activeTab}>
          <TabPane tabId="1">
            <Row>
              <Col className="info" md="6">
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.eKycId">eKYC-ID</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.eKycId}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.name">Name</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.fullName}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.id">ID</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.identifierId}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.gender">Gender</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.gender}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.dob">Date of birth</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>
                      {eKycEntity.birthDay ? <TextFormat type="date" value={eKycEntity.birthDay} format={APP_LOCAL_DATE_FORMAT} /> : null}
                    </span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.placeOfIssued">Place of Issued</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.issuePlace}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.matchingRate">Matching rate</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span className={`matchingRate-${classOfMatchingRate(eKycEntity.matchingRate?.toFixed(2))}`}>
                      {eKycEntity.matchingRate?.toFixed(2).toString() || '_'}%
                    </span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.status">Status</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span className={`status-${(eKycEntity?.status || '_').toLowerCase()}`}>{eKycEntity?.status || '_'}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.accountNumber">Account Number</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.accountNumber}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.phoneNumber">Phone Number</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.phoneNo}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.email">Email</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.email}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.contactAddress">Contact Address</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.contactAddress}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.taxNumber">Tax Number</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.taxNumber}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.bankList">Bank List</Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    {bankList.map((bank, index) => (
                      <div key={index}>
                        {index !== 0 && <br />}
                        <span className="d-block">
                          <b>{bank.bankName}</b> - {bank.ownerName}
                        </span>
                        <span className="d-block">{bank.bankAccNo}</span>
                        <span className="d-block">CN {bank.branchId}</span>
                      </div>
                    ))}
                  </Col>
                </Row>
              </Col>
              <Col md="6" className="info-image">
                <Row>
                  <Col md="6" className="text-center mb-4">
                    <p>
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.frontOfYourDocument">Front of your document</Translate>
                    </p>
                    <img width="100%" src={eKycEntity.frontImageUrl} />
                  </Col>
                  <Col md="6" className="text-center mb-4">
                    <p>
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.backOfYourDocument">Back of your document</Translate>
                    </p>
                    <img width="100%" src={eKycEntity.backImageUrl} />
                  </Col>
                </Row>
              </Col>
            </Row>
          </TabPane>
          <TabPane tabId="2">
            <Row className="mb-4">
              <Col className="info" md="4">
                <Translate contentKey="eKycAdminApp.customEKyc.nhsv.accountOpeningPerferences">Account Opening Perferences</Translate>
                <span>:</span>
              </Col>
              <Col className="info" md="8">
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.maginIncluded">Margin Included</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{eKycEntity.isMargin ? 'Yes' : 'No'}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.onlineTrading">Online Trading</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{eKycEntity.onlineTrading ? 'Yes' : 'No'}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.authenticationMethod">Authentication Method</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{eKycEntity.authenMethod?.toUpperCase()}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.otpReceiveMethod">OTP Receive Method</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{eKycEntity.otpReceiveMethod?.toUpperCase()}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.cashInAdvancedIncluded">Cash in advance Included</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{eKycEntity.advancedCashIncluded ? 'Yes' : 'No'}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.smsMethod">SMS Method</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{eKycEntity.smsMethod?.toUpperCase()}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.notificationViaEmail">Notification via Email</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{eKycEntity.emailNotification ? 'Yes' : 'No'}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.referral">Referral</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{renderReferral(eKycEntity.referral, eKycEntity.partnerName)}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.customerSupport">Customer Support</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{eKycEntity.customerSupport ? `Yes - ${eKycEntity.csPartnerId}.` : 'No'}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col className="info" sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.fatca">FATCA</Translate>
                    <span>:</span>
                  </Col>
                  <Col className="info" sm="6">
                    <span>{eKycEntity.fatca ? 'Yes' : 'No'}</span>
                  </Col>
                </Row>
              </Col>
            </Row>
            <Row className="mb-4">
              <Col className="info" md="4">
                <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwnerInfo">Beneficiairy Owner Info</Translate>
                <span>:</span>
              </Col>
              <Col className="info" md="8">
                <details
                  onToggle={e => {
                    const details = e.target as HTMLDetailsElement;
                    const summary = details.querySelector('summary');
                    if (details.open) {
                      summary.textContent = 'Hide';
                    } else {
                      summary.textContent = 'Show';
                    }
                  }}
                >
                  <summary>Show</summary>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.fullName"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.fullName}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.dob"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.birthDay}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.nationality"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.nationality}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.identifierID"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.identifierId}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.issueDate"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.issueDate}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.issuePlace"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.issuePlace}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.permanentAddress"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.permanentAddress}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.contactAddress"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.contactAddress}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.occupation"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.occupation}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.position"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.position}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.phoneNumber"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.phoneNumber}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.visaNo"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.visaNo}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.visaIssuedPlace"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.visaIssuePlace}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.beneficiairyOwner.foreignResidence"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.foreignResidence}</span>
                    </Col>
                  </Row>
                </details>
              </Col>
            </Row>
            <Row className="mb-4">
              <Col className="info" md="4">
                <Translate contentKey="eKycAdminApp.customEKyc.nhsv.investmentExperienceInfo">Investment Experience Info</Translate>
                <span>:</span>
              </Col>
              <Col className="info" md="8">
                <details
                  onToggle={e => {
                    const details = e.target as HTMLDetailsElement;
                    const summary = details.querySelector('summary');
                    if (details.open) {
                      summary.textContent = 'Hide';
                    } else {
                      summary.textContent = 'Show';
                    }
                  }}
                >
                  <summary>Show</summary>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.investmentExperience.investmentGoal"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.investmentGoal}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.investmentExperience.riskTakingLevel"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.risk}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.investmentExperience.experienced"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycAdditionalInfo.experienced ? 'Yes' : 'No'}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.investmentExperience.publicCoop"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      {publicCoop.map((item, index) => (
                        <span className="d-block" key={index}>
                          {item.companyName} ({item.stock}) - {item.position}
                        </span>
                      ))}
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.investmentExperience.blockholder"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      {blockholder.map((item, index) => (
                        <span className="d-block" key={index}>
                          {item.companyName} ({item.stock}) - {item.position}
                        </span>
                      ))}
                    </Col>
                  </Row>
                </details>
              </Col>
            </Row>
            <Row className="mb-4">
              <Col className="info" md="4">
                <Translate contentKey="eKycAdminApp.customEKyc.nhsv.vntpEKycLogInfo"></Translate>
                <span>:</span>
              </Col>
              <Col className="info" md="8">
                <details
                  onToggle={e => {
                    const details = e.target as HTMLDetailsElement;
                    const summary = details.querySelector('summary');
                    if (details.open) {
                      summary.textContent = 'Hide';
                    } else {
                      summary.textContent = 'Show';
                    }
                  }}
                >
                  <summary>Show</summary>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.vntpEKycLog.ocr"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycEntity.ocrLogId || '-'}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.vntpEKycLog.cardLiveness"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycEntity.cardLivenessLogId || '-'}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.vntpEKycLog.cardRear"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycEntity.cardRearLogId || '-'}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.vntpEKycLog.compare"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycEntity.compareLogId || '-'}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.vntpEKycLog.faceLiveness"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycEntity.faceLivenessLogId || '-'}</span>
                    </Col>
                  </Row>
                  <Row className="mb-4">
                    <Col className="info" sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.vntpEKycLog.faceMask"></Translate>
                      <span>:</span>
                    </Col>
                    <Col className="info" sm="6">
                      <span>{eKycEntity.faceMaskLogId || '-'}</span>
                    </Col>
                  </Row>
                </details>
              </Col>
            </Row>
          </TabPane>
          <TabPane tabId="3">
            <Row>
              <Col className="info" md="6">
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.contractNumber"></Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eKycEntity.contractNo}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.contractDate"></Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span><TextFormat type="date" value={eContract.createdAt} format={APP_TIMESTAMP_FORMAT} /></span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.refID"></Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eContract.refId}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.envelopeID"></Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eContract.envelopeId}</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.customerSignatureStatus"></Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eContractInfo.customerSignatueStatus == null ? 'processing' : eContractInfo.customerSignatueStatus }</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.securitiesSignatureStatus"></Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eContractInfo.securitiesSignatureStatus == null ? 'processing' : eContractInfo.securitiesSignatureStatus }</span>
                  </Col>
                </Row>
                <Row className="mb-4">
                  <Col sm="6">
                    <Translate contentKey="eKycAdminApp.customEKyc.nhsv.contractStatus"></Translate>
                    <span>:</span>
                  </Col>
                  <Col sm="6">
                    <span>{eContractInfo.contractStatus == null ? 'processing' : eContractInfo.contractStatus }</span>
                  </Col>
                </Row>
                {eContractInfo.contractStatus === 'completed' && (
                  <Row className="mb-4">
                    <Col sm="6">
                      <Translate contentKey="eKycAdminApp.customEKyc.nhsv.contractFile"></Translate>
                      <span>:</span>
                    </Col>
                    <Col sm="6">
                      <Button
                        size="sm"
                        color="info"
                        onClick={() => downloadFile(eContractInfo.id)}
                        disabled={downloading}
                      >
                        <FontAwesomeIcon icon={faDownload} className="p-icon-l" spin={downloading} />
                        &nbsp;
                        Available here
                      </Button>
                    </Col>
                  </Row>
                )}
              </Col>
              <Col className="info" md="6">
                <Row className="mb-4">
                  {eContractInfo.customerSignatueStatus === 'signed' && (
                    <Col md="6" className="text-center mb-4">
                      <p>
                        <Translate contentKey="eKycAdminApp.customEKyc.nhsv.signature">Signature</Translate>
                      </p>
                      <img width="100%" src={`data:image/png;base64,${eContractInfo.signFileContent}`} />
                    </Col>
                  )}
                </Row>
              </Col>
            </Row>
          </TabPane>
        </TabContent>
      </div>
    );
  };

  const contentModalBodyDefault = () => {
    return (
      <Row className="detail-popup">
        <Col className="info" md="5">
          <Table borderless>
            <tbody>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.name">Name</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.fullName}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.dob">DOB</Translate>
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
                  <Translate contentKey="eKycAdminApp.customEKyc.idNumber">ID</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.identifierId}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.dateOfIssued">Date of Issued</Translate>
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
                  <Translate contentKey="eKycAdminApp.customEKyc.placeOfIssued">Place of Issued</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.issuePlace}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.email">Email</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.email}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.phoneNumber">Phone Number</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.phoneNo}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.address">Address</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.address}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.bankAccount">Bank account</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.bankAccount || '_'}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.bank">Bank</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.bankName || '_'}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.bankBranch">Bank branch</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.branch || '_'}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.creatorStatus">Core creator status</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span className={`status-${(creatorStatus?.status || '_').toLowerCase()}`}>{creatorStatus?.status || '_'}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.reason">Reason</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{creatorStatus?.reason || '_'}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.fullResult">Full result</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{creatorStatus?.fullResult || '_'}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.matchingRate">Matching rate</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span className={`matchingRate-${classOfMatchingRate(eKycEntity.matchingRate?.toFixed(2))}`}>
                    {eKycEntity.matchingRate?.toFixed(2).toString() || '_'}%
                  </span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.kisBranch">KIS branch</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.referrerBranch || '_'}</span>
                </td>
              </tr>
              <tr>
                <td className="label">
                  <Translate contentKey="eKycAdminApp.customEKyc.agency">Agency</Translate>
                  <span>:</span>
                </td>
                <td>
                  <span>{eKycEntity.referrerIdName || '_'}</span>
                </td>
              </tr>
            </tbody>
          </Table>
        </Col>
        <Col md="7" className="info-image">
          <Row>
            <Col md="6" className="text-center mb-4">
              <p>
                <Translate contentKey="eKycAdminApp.customEKyc.frontOfYourDocument">Front of your document</Translate>
              </p>
              <img width="100%" src={eKycEntity.frontImageUrl} />
            </Col>
            <Col md="6" className="text-center mb-4">
              <p>
                <Translate contentKey="eKycAdminApp.customEKyc.backOfYourDocument">Back of your document</Translate>
              </p>
              <img width="100%" src={eKycEntity.backImageUrl} />
            </Col>
            <Col md="6" className="text-center mb-4">
              <p>
                <Translate contentKey="eKycAdminApp.customEKyc.signature">Signature</Translate>
              </p>
              <img width="100%" src={eKycEntity.signatureImageUrl} />
            </Col>
            <Col md="6" className="text-center mb-4">
              <p>
                <Translate contentKey="eKycAdminApp.customEKyc.tradingCode">Trading Code</Translate>
              </p>
              <img width="100%" src={eKycEntity.tradingCodeImageUrl} />
            </Col>
          </Row>
        </Col>
      </Row>
    );
  };

  const contentModalBody = () => {
    switch (config.domain) {
      case 'nhsv':
        return contentModalBodyNhsv();
      default:
        return contentModalBodyDefault();
    }
  };

  const content = contentModalBody();

  return (
    <Modal isOpen toggle={handleClose} size="xl">
      <ModalHeader toggle={handleClose} data-cy="entityDetailsHeading">
        <Translate contentKey="eKycAdminApp.customEKyc.detailedInformation">Detailed Information</Translate>
      </ModalHeader>
      <ModalBody>{content}</ModalBody>
    </Modal>
  );
};

const mapStateToProps = ({ eKyc }: IRootState) => ({
  eKycEntity: eKyc.entity
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKycPopupDetail);
