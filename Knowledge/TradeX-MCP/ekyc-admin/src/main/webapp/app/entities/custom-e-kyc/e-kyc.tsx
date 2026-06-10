import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Input, Label, Form, FormGroup, Modal, ModalBody, ModalFooter } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';
import {
  getEntitiesFilter,
  approveEntity,
  approveAndCreateAccount,
  rejectEntity,
  waitingConfirmEntity
} from './e-kyc.reducer';
import config, { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import axios from 'axios';

export interface IEKycProps extends StateProps, DispatchProps, RouteComponentProps<{ url: string }> {}

const documentType = ['CMND', 'CC', 'PASSPORT'];
const status = config.domain === 'nhsv' ? ['PENDING', 'WAITING_CONFIRMATION', 'APPROVED'] : ['PENDING', 'REJECT', 'APPROVED', 'AUTO_APPROVED'];

export const EKyc = (props: IEKycProps) => {
  const toDay = new Date();
  const currentDate = toDay.toJSON().slice(0, 10).replace(/-/g, '-');
  const fromDate = new Date(toDay.getTime() - 7 * 24 * 60 * 60 * 1000).toJSON().slice(0, 10).replace(/-/g, '-');
  const [windowSize, setWindowSize] = useState({
    width: undefined,
    height: undefined,
  });
  const [filterData, setFilterData] = useState({
    fromDate: localStorage.getItem('filterFromDate') !== null ? localStorage.getItem('filterFromDate') : fromDate,
    toDate: localStorage.getItem('filterToDate') !== null ? localStorage.getItem('filterToDate') : currentDate,
    documentType: localStorage.getItem('filterDocumentType') ? localStorage.getItem('filterDocumentType') : '',
    status: localStorage.getItem('filterStatus') ? localStorage.getItem('filterStatus') : '',
    searchName: localStorage.getItem('filterSearchName') ? localStorage.getItem('filterSearchName') : '',
  });
  const [paramQuery, setParamQuery] = useState('');
  const [checkedItems, setCheckedItems] = useState<number[]>([]);
  const [approvePopup, setApprovePopup] = useState(false);
  const [approveAndCreateAccPopup, setApproveAndCreateAccPopup] = useState(false);
  const [rejectPopup, setRejectPopup] = useState(false);
  const [waitingConfirmPopup, setWaitingConfirmPopup] = useState(false);
  const [creatorStatus, setCreatorStatus] = useState([]);
  const { eKycList, loading, location, match, updating } = props;
  const lengthPendingItems = eKycList.filter(item => item.status === 'PENDING').length;

  useEffect(() => {
    function handleResize() {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    }
    window.addEventListener('resize', handleResize);
    handleResize();
    const paramArray = [];
    let paramString = ``;

    if (filterData.fromDate) {
      paramArray.push('createdAt.greaterThanOrEqual=' + new Date(filterData.fromDate).toISOString());
    }
    if (filterData.toDate) {
      const nextDate = new Date(filterData.toDate);
      nextDate.setDate(nextDate.getDate() + 1);
      paramArray.push('createdAt.lessThanOrEqual=' + nextDate.toISOString());
    }
    if (filterData.documentType) {
      paramArray.push('type.equals=' + filterData.documentType);
    }
    if (filterData.status) {
      paramArray.push('status.equals=' + filterData.status);
    }
    if (filterData.searchName) {
      paramArray.push('fullName.contains=' + filterData.searchName);
    }
    if (paramArray.length > 0) {
      paramArray.forEach(element => {
        paramString += '&' + element;
      });
    }
    setParamQuery(paramString);
    props.getEntitiesFilter(paramString);
    return () => window.removeEventListener('resize', handleResize);
  }, [filterData]);

  useEffect(() => {
    fetchCreatorStatus();
  }, []);

  const fetchCreatorStatus = async () => {
    await axios
      .get(`/api/e-kyc-creator-statuses`)
      .then(res => {
        setCreatorStatus(res.data);
      })
      .catch(err => {
        console.log(err);
      });
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

  window.addEventListener('beforeunload', e => {
    e.preventDefault();
    if (window.localStorage.filterFromDate) {
      localStorage.removeItem('filterFromDate');
    }
    if (window.localStorage.filterToDate) {
      localStorage.removeItem('filterToDate');
    }
    if (window.localStorage.filterDucumentType) {
      localStorage.removeItem('filterDucumentType');
    }
    if (window.localStorage.filterStatus) {
      localStorage.removeItem('filterStatus');
    }
    if (window.localStorage.filterSearchName) {
      localStorage.removeItem('filterSearchName');
    }
    return '';
  });

  const resetFilter = () => {
    if (window.localStorage.filterFromDate) {
      localStorage.removeItem('filterFromDate');
    }
    if (window.localStorage.filterToDate) {
      localStorage.removeItem('filterToDate');
    }
    if (window.localStorage.filterDucumentType) {
      localStorage.removeItem('filterDucumentType');
    }
    if (window.localStorage.filterStatus) {
      localStorage.removeItem('filterStatus');
    }
    if (window.localStorage.filterSearchName) {
      localStorage.removeItem('filterSearchName');
    }
    setFilterData({
      fromDate: currentDate,
      toDate: currentDate,
      documentType: '',
      status: '',
      searchName: '',
    });
  };

  const onHandleChecked = e => {
    if (e.target.checked) {
      setCheckedItems([...checkedItems, parseInt(e.target.value, 10)]);
    } else {
      setCheckedItems(checkedItems.filter(item => item !== parseInt(e.target.value, 10)));
    }
  };

  const onCheckedAll = e => {
    if (e.target.checked) {
      const idList: number[] = [];
      eKycList
        .filter(item => item.status === 'PENDING')
        .forEach(element => {
          idList.push(element.id);
        });
      setCheckedItems(idList);
    } else {
      setCheckedItems([]);
    }
  };

  const onRejectID = () => {
    props.rejectEntity({ idList: checkedItems, paramString: paramQuery });
    setCheckedItems([]);
    setRejectPopup(false);
  };

  const onApproveID = () => {
    Promise.all([props.approveEntity({ idList: checkedItems, paramString: paramQuery })])
      .then(() => {
        fetchCreatorStatus();
      })
      .catch(err => console.log(err));
    setCheckedItems([]);
    setApprovePopup(false);
  };

  const onApproveAndCreateID = () => {
    Promise.all([props.approveAndCreateAccount({ idList: checkedItems, paramString: paramQuery })])
      .then(() => {
        fetchCreatorStatus();
      })
      .catch(err => console.log(err));
    setCheckedItems([]);
    setApproveAndCreateAccPopup(false);
  };

  const onWaitingConfirm = () => {
    Promise.all([props.waitingConfirmEntity({paramString: paramQuery})])
      .then(() => {
        fetchCreatorStatus();
      })
      .catch(err => console.log(err));
    setWaitingConfirmPopup(false);
  }

  const onRejectPopup = () => setRejectPopup(!rejectPopup);
  const onApprovePopup = () => setApprovePopup(!approvePopup);
  const onApproveAndCreateAccPopup = () => setApproveAndCreateAccPopup(!approveAndCreateAccPopup);
  const onWaitingConfirmPopup = () => setWaitingConfirmPopup(!waitingConfirmPopup);
  const onFromDateChange = e => {
    setFilterData({
      ...filterData,
      fromDate: e.target.value,
      toDate: new Date(e.target.value) > new Date(filterData.toDate) ? e.target.value : filterData.toDate,
    });
    localStorage.setItem('filterFromDate', e.target.value);
    localStorage.setItem('filterToDate', new Date(e.target.value) > new Date(filterData.toDate) ? e.target.value : filterData.toDate);
  };

  const onToDateChange = e => {
    setFilterData({
      ...filterData,
      toDate: e.target.value,
      fromDate: new Date(e.target.value) < new Date(filterData.fromDate) ? e.target.value : filterData.fromDate,
    });
    localStorage.setItem('filterFromDate', e.target.value);
    localStorage.setItem('filterToDate', new Date(e.target.value) > new Date(filterData.toDate) ? e.target.value : filterData.toDate);
  };

  const onDocumentTypeChange = e => {
    setFilterData({ ...filterData, documentType: e.target.value });
    localStorage.setItem('filterDocumentType', e.target.value);
  };

  const onStatusChange = e => {
    setFilterData({ ...filterData, status: e.target.value });
    localStorage.setItem('filterStatus', e.target.value);
  };

  const onSearchNameChange = e => {
    setFilterData({ ...filterData, searchName: e.target.value });
    localStorage.setItem('filterSearchName', e.target.value);
  };

  const handleSyncList = () => {
    getEntitiesFilter(paramQuery);
  };

  const filterCreatorStatus = id => {
    let data;
    if (creatorStatus.length > 0) {
      data = creatorStatus.find(ele => ele?.ekyc?.id === id);
    }
    return data?.status || '_';
  };

  const headTableNHSV = () => {
    return (
      <tr>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.no">No</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.eKycId">eKYC-ID</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.date">Date</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.type">Document Type</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.id">ID</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.name">Name</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.dob">Date of birth</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.dateOfIssued">Date of Issued</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.placeOfIssued">Place of Issued</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.permanentAddress">Address</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.phoneNumber">Phone Number</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.email">Email</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.matchingRate">Matching rate</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.status">Status</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.accountNumber">Account Number</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.nhsv.detailAction">Detail</Translate>
        </th>
      </tr>
    );
  };

  const bodyTableNHSV = () => {
    return eKycList.map((eKyc, i) => (
      <>
        <tr key={`entity-${i}`} data-cy="entityTable">
          <td>{i + 1}</td>
          <td>{eKyc.eKycId}</td>
          <td>{eKyc.createdAt ? <TextFormat type="date" value={eKyc.createdAt} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
          <td className="text-left">{eKyc.type === 'CC' ? 'CCCD' : eKyc.type}</td>
          <td>{eKyc.identifierId}</td>
          <td className="text-left">{eKyc.fullName}</td>
          <td>{eKyc.issueDate ? <TextFormat type="date" value={eKyc.birthDay} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
          <td>{eKyc.issueDate ? <TextFormat type="date" value={eKyc.issueDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
          <td className="text-left">{eKyc.issuePlace}</td>
          <td className="text-left">{eKyc.permanentAddress}</td>
          <td className="text-left">{eKyc.phoneNo}</td>
          <td className="text-left">{eKyc.email}</td>
          <td className={`matchingRate-${classOfMatchingRate(eKyc.matchingRate?.toFixed(2))}`}>
            {eKyc.matchingRate?.toFixed(2).toString() || '_'}%
          </td>
          <td className={`status-${eKyc.status.toLowerCase()}`}>{eKyc.status}</td>
          <td className="text-left">{eKyc.accountNumber}</td>
          <td className="text-center">
            <div className="btn-group flex-btn-group-container">
              <Button tag={Link} to={`${match.url}custom-e-kyc/${eKyc.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                <FontAwesomeIcon icon="eye" />{' '}
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.view">View</Translate>
                </span>
              </Button>
            </div>
          </td>
        </tr>
      </>
    ));
  };

  const headerTableDefault = () => {
    return (
      <tr>
        <th>
          <Input
            checked={lengthPendingItems > 0 && lengthPendingItems === checkedItems.length}
            disabled={lengthPendingItems === 0}
            style={{ position: 'inherit' }}
            className="ml-0"
            type="checkbox"
            onChange={onCheckedAll}
          />
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.no">No</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.date">Date</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.type">Document Type</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.idNumber">ID</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.name">Name</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.dateOfIssued">Date of Issued</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.placeOfIssued">Place of Issued</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.phoneNumber">Phone Number</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.address">Address</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.email">Email</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.status">Status</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.creatorStatus">Creator Status</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.matchingRate">Matching rate</Translate>
        </th>
        <th>
          <Translate contentKey="eKycAdminApp.customEKyc.detailAction">Detail</Translate>
        </th>
      </tr>
    );
  };

  const bodyTableDefault = () => {
    return eKycList.map((eKyc, i) => (
      <>
        <tr key={`entity-${i}`} data-cy="entityTable">
          <td>
            <Input
              style={eKyc.status === 'PENDING' ? { visibility: 'visible', position: 'unset' } : { visibility: 'hidden', position: 'unset' }}
              checked={checkedItems.indexOf(eKyc.id) >= 0}
              className="ml-0"
              type="checkbox"
              value={eKyc.id}
              onChange={onHandleChecked}
            />
          </td>
          <td>{i + 1}</td>
          <td>{eKyc.createdAt ? <TextFormat type="date" value={eKyc.createdAt} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
          <td className="text-left">{eKyc.type === 'CC' ? 'CCCD' : eKyc.type}</td>
          <td>{eKyc.identifierId}</td>
          <td className="text-left">{eKyc.fullName}</td>
          <td>{eKyc.issueDate ? <TextFormat type="date" value={eKyc.issueDate} format={APP_LOCAL_DATE_FORMAT} /> : null}</td>
          <td className="text-left">{eKyc.issuePlace}</td>
          <td className="text-left">{eKyc.phoneNo}</td>
          <td className="text-left">{eKyc.address}</td>
          <td className="text-left">{eKyc.email}</td>
          <td className={`status-${eKyc.status.toLowerCase()}`}>{eKyc.status}</td>
          <td className={`status-${filterCreatorStatus(eKyc.id).toLowerCase()}`}>{filterCreatorStatus(eKyc.id)}</td>
          <td className={`matchingRate-${classOfMatchingRate(eKyc.matchingRate?.toFixed(2))}`}>
            {eKyc.matchingRate?.toFixed(2).toString() || '_'}%
          </td>
          <td className="text-center">
            <div className="btn-group flex-btn-group-container">
              <Button tag={Link} to={`${match.url}custom-e-kyc/${eKyc.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                <FontAwesomeIcon icon="eye" />{' '}
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.view">View</Translate>
                </span>
              </Button>
            </div>
          </td>
        </tr>
      </>
    ));
  };

  const tableContent = () => {
    let headTable = null;
    let bodyTable = null;

    switch (config.domain) {
      case 'nhsv':
        headTable = headTableNHSV();
        bodyTable = bodyTableNHSV();
        break;
      default:
        headTable = headerTableDefault();
        bodyTable = bodyTableDefault();
        break;
    }

    return { headTable, bodyTable };
  };

  const { headTable, bodyTable } = tableContent();

  return (
    <>
      <div className="filter-group">
        <Form inline={windowSize.width > 400}>
          <FormGroup>
            <Label className="mr-2" for="fromDate">
              <Translate contentKey="eKycAdminApp.label.fromDate">From Date</Translate>
            </Label>
            <Input
              onChange={onFromDateChange}
              value={filterData.fromDate}
              max={currentDate}
              type="date"
              name="fromDate"
              id="fromDate"
              placeholder="date placeholder"
              bsSize="sm"
            />
          </FormGroup>

          <FormGroup>
            <Label className="mr-2" for="toDate">
              <Translate contentKey="eKycAdminApp.label.toDate">To date</Translate>
            </Label>
            <Input
              onChange={onToDateChange}
              value={filterData.toDate}
              max={currentDate}
              type="date"
              name="toDate"
              id="toDate"
              placeholder="date placeholder"
              bsSize="sm"
            />
          </FormGroup>

          {config.domain !== 'nhsv' && (
            <FormGroup>
              <Label className="mr-2" for="documentType">
                <Translate contentKey="eKycAdminApp.label.documentType">Document type</Translate>
              </Label>
              <Input
                onChange={onDocumentTypeChange}
                bsSize="sm"
                placeholder="Document type"
                type="select"
                name="documentType"
                id="documentType"
              >
                <option value="">All</option>
                {documentType.map((value, key) => (
                  <option key={key} value={value}>
                    {value === 'CC' ? 'CCCD' : value}
                  </option>
                ))}
              </Input>
            </FormGroup>
          )}

          <FormGroup>
            <Label className="mr-2" for="status">
              <Translate contentKey="eKycAdminApp.label.status">Status</Translate>
            </Label>
            <Input bsSize="sm" onChange={onStatusChange} placeholder="Status" type="select" name="status" id="status">
              <option value="">All</option>
              {status.map((value, key) => (
                <option key={key} value={value}>
                  {value}
                </option>
              ))}
            </Input>
          </FormGroup>

          <FormGroup>
            <Label className="mr-2" for="toDate">
              <Translate contentKey="eKycAdminApp.label.searchName">Search name</Translate>
            </Label>
            <Input onChange={onSearchNameChange} bsSize="sm" placeholder="Search name" type="search" name="searchName" id="searchName" />
          </FormGroup>
        </Form>
        <div style={{ padding: '10px' }}>
          <Button className="mr-2" type="reset" size="sm" color="secondary" onClick={resetFilter}>
            <FontAwesomeIcon icon="remove-format" />
            <Translate contentKey="eKycAdminApp.customEKyc.home.clear">Clear</Translate>
          </Button>
          <Button size="sm" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="eKycAdminApp.customEKyc.home.refreshListLabel">Refresh List</Translate>
          </Button>
        </div>
      </div>
      <div className="tableFixHead mt-4">
        {eKycList && eKycList.length > 0 ? (
          <table>
            <thead>{headTable}</thead>
            <tbody>{bodyTable}</tbody>
          </table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="eKycAdminApp.customEKyc.home.notFound">No data to show</Translate>
            </div>
          )
        )}
      </div>
      {config.domain === 'nhsv' ?
        <>
          <Modal isOpen={waitingConfirmPopup}>
            <ModalBody>Are you sure update PENDING requests?</ModalBody>
            <ModalFooter>
              <Button color="primary" onClick={onWaitingConfirm}>
                Yes
              </Button>{' '}
              <Button color="secondary" onClick={onWaitingConfirmPopup}>
                Cancel
              </Button>
            </ModalFooter>
          </Modal>
          <div className="d-flex justify-content-end mt-4">
            <Button disabled={updating || loading} onClick={onWaitingConfirmPopup} className="mr-1"
                    color="warning">
              Update PENDING Requests
            </Button>
          </div>
        </>
        : (
          <>
            <Modal isOpen={rejectPopup}>
              <ModalBody>Are you sure you reject these items?</ModalBody>
              <ModalFooter>
                <Button color="primary" onClick={onRejectID}>
                  Yes
                </Button>{' '}
                <Button color="secondary" onClick={onRejectPopup}>
                  Cancel
                </Button>
              </ModalFooter>
            </Modal>
            <Modal isOpen={approvePopup}>
              <ModalBody>Are you sure you approve these items?</ModalBody>
              <ModalFooter>
                <Button color="primary" onClick={onApproveID}>
                  Yes
                </Button>{' '}
                <Button color="secondary" onClick={onApprovePopup}>
                  Cancel
                </Button>
              </ModalFooter>
            </Modal>
            <Modal isOpen={approveAndCreateAccPopup}>
            <ModalBody>Are you sure you approve and create account these items?</ModalBody>
            <ModalFooter>
              <Button color="primary" onClick={onApproveAndCreateID}>
                Yes And Create Account
              </Button>{' '}
              <Button color="secondary" onClick={onApproveAndCreateAccPopup}>
                Cancel
              </Button>
            </ModalFooter>
          </Modal>
          <div className="d-flex justify-content-end mt-4">
            <Button disabled={updating || loading || checkedItems.length === 0} onClick={onRejectPopup} className="mr-1" color="danger">
              <Translate contentKey="eKycAdminApp.customEKyc.home.rejectButton">Reject</Translate>
            </Button>
            <Button disabled={updating || loading || checkedItems.length === 0} onClick={onApproveAndCreateAccPopup} className="ml-1" color="warning">
              <Translate contentKey="eKycAdminApp.customEKyc.home.approveAndCreateAccButton">Approve and create account</Translate>
            </Button>
            <Button disabled={updating || loading || checkedItems.length === 0} onClick={onApprovePopup} className="ml-1" color="success">
              <Translate contentKey="eKycAdminApp.customEKyc.home.approveButton">Approve</Translate>
            </Button>
          </div>
        </>
      )}
    </>
  );
};

const mapStateToProps = ({ eKyc }: IRootState) => ({
  eKycList: eKyc.entities,
  loading: eKyc.loading,
  updating: eKyc.updating
});

const mapDispatchToProps = {
  getEntitiesFilter,
  approveEntity,
  approveAndCreateAccount,
  rejectEntity,
  waitingConfirmEntity
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(EKyc);
