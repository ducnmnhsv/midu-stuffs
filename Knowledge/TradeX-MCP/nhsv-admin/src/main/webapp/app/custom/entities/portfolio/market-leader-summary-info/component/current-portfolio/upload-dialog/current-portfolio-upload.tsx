import React, { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import '../../../../portfolio.scss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faUpload } from '@fortawesome/free-solid-svg-icons';
import CurrentPortfolioUploadItem from 'app/custom/entities/portfolio/market-leader-summary-info/component/current-portfolio/upload-dialog/current-portfolio-upload-item';
import { Alert, Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap';
import { ICopyPortfolioUploadRequest } from 'app/custom/model/copy-portfolio-upload-request';
import { ICopyPortfolioDetail } from 'app/custom/model/copy-portfolio-detail.model';
import { postUploadCurrentPortfolio } from 'app/custom/entities/portfolio/handle/api/portfolio-api';
import lodash from 'lodash';
import { v4 as uuid } from 'uuid';

const CurrentPortfolioUploadComponent = props => {
  const toggleUpload = props.toggleUpload;
  const accountInfo = props.accountInfo;
  const isShowUploadDialog = props.isShowUploadDialog;
  const reloadCurrentPortfolio = props.reloadCurrentPortfolio;
  const dispatch = useAppDispatch();
  const [items, setItems] = useState([]);
  const [uploadPortfolioFormControl, setUploadPortfolioFormControl] = useState({});
  const [isItemEmpty, setItemEmpty] = useState(false);
  const [isTotalValid, setTotalValid] = useState(true);
  const [isUploadAction, setUploadAction] = useState(false);
  const [totalWeight, setTotalWeight] = useState(0);
  const [visible, setVisible] = useState(false);
  const loading = useAppSelector(state => state.portfolioReducer.loading);
  const onDismiss = () => setVisible(false);

  const addItem = () => {
    setVisible(false);
    const uuidRandom = uuid();
    const stockCodeControlName = `stockCodeControlName___${uuidRandom}`;
    const stockWeightControlName = `stockWeightControlName___${uuidRandom}`;
    const key = `${stockCodeControlName}_${stockWeightControlName}`;
    const itemFormControl = { ...uploadPortfolioFormControl };
    itemFormControl[stockCodeControlName] = '';
    itemFormControl[stockWeightControlName] = '';
    setUploadPortfolioFormControl({ ...itemFormControl });
    setItems(prevItems => {
      return [
        ...prevItems,
        {
          stockCodeControlName: stockCodeControlName,
          stockWeightControlName: stockWeightControlName,
          key: key,
        },
      ];
    });
  };

  const onFormControlChange = (e: React.ChangeEvent<HTMLInputElement>, controlName) => {
    setUploadPortfolioFormControl(prevFormControlName => {
      const value = e.target.value;
      return {
        ...prevFormControlName,
        [controlName]: value,
      };
    });
  };

  const onUploadPortfolio = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    e.preventDefault();
    setUploadAction(true);
    if (items.length === 0) {
      setItemEmpty(true);
      setVisible(true);
      return;
    }
    if (validateUploadForm()) {
      const request: ICopyPortfolioUploadRequest = {};
      request.mlUserId = accountInfo.id;

      const formItemGroup: ICopyPortfolioDetail = Object.entries(uploadPortfolioFormControl).reduce(
        (result: ICopyPortfolioDetail, [key, value]) => {
          console.log(`$${key}_________${value}`);
          const keyGroup = key.split('___')[1];
          if (!result[keyGroup]) {
            result[keyGroup] = {};
          }
          if (key.includes('stockCode')) {
            result[keyGroup].symbol = value;
          }
          if (key.includes('stockWeight')) {
            result[keyGroup].weight = value;
          }
          return result;
        },
        {}
      );

      const items: ICopyPortfolioDetail[] = [];
      for (const [key, value] of Object.entries(formItemGroup)) {
        items.push(value);
      }
      request.items = items;
      console.log(items);
      if (request && request.mlUserId && request.items.length > 0) {
        dispatch(
          postUploadCurrentPortfolio({
            request: request,
            onReloadCurrentPortfolioList: reloadCurrentPortfolio,
            onCloseModal: onCloseModal,
          })
        );
      }
    }
  };

  const validateUploadForm = () => {
    let weightValueIsEmpty = false;
    let isSymbolValueValid = true;
    const total = Object.entries(uploadPortfolioFormControl).reduce((result, [key, value]) => {
      if (key.includes('stockWeight') && lodash.isEmpty(value)) {
        weightValueIsEmpty = true;
      }
      if (key.includes('stockCode') && lodash.isEmpty(value)) {
        isSymbolValueValid = false;
      }
      return result + (key.includes('stockWeight') ? Number(value) : 0);
    }, 0);
    const isTotalValid = total === 100;
    if (!weightValueIsEmpty) {
      setTotalValid(isTotalValid);
    }
    setVisible(!isTotalValid);
    return isSymbolValueValid && isTotalValid;
  };

  const onRemoveItem = (
    e: React.MouseEvent<HTMLButtonElement, MouseEvent>,
    stockCodeControlName: string,
    stockWeightControlName: string
  ) => {
    e.preventDefault();
    setVisible(false);
    setUploadPortfolioFormControl(current => {
      const copy = { ...current };
      delete copy[stockCodeControlName];
      delete copy[stockWeightControlName];
      return { ...copy };
    });
    const itemKey = `${stockCodeControlName}_${stockWeightControlName}`;
    setItems(prevElements =>
      prevElements.filter(element => {
        return element['key'] && element['key'] !== itemKey;
      })
    );
  };

  const onCloseModal = () => {
    setItems([...[]]);
    setUploadPortfolioFormControl({ ...{} });
    setUploadAction(false);
    setItemEmpty(false);
    setTotalValid(true);
    setVisible(false);
    toggleUpload();
  };

  useEffect(() => {
    setItemEmpty(items.length === 0);
  }, [items]);

  useEffect(() => {
    const total = Object.entries(uploadPortfolioFormControl).reduce((result, [key, value]) => {
      return result + (key.includes('stockWeight') ? Number(value) : 0);
    }, 0);
    setTotalWeight(total);
    if (total === 100) {
      setTotalValid(total === 100);
    }
  }, [uploadPortfolioFormControl]);

  return (
    <div className="container-fluid p-0">
      <div className="row">
        <div className="col-12">
          <Modal
            isOpen={isShowUploadDialog}
            toggle={onCloseModal}
            size={'lg'}
            centered={true}
            backdrop={'static'}
            data-bs-config={{ backdrop: true }}
          >
            <ModalHeader toggle={onCloseModal}>
              <span className="p-font-25">Upload portfolio</span>
            </ModalHeader>
            <ModalBody>
              <div className="container-fluid">
                <div className="row">
                  <div className="col-12">
                    <p className="p-0">
                      Total weight ={' '}
                      <strong className="text-info">
                        {totalWeight} {totalWeight ? '%' : ''}
                      </strong>
                    </p>
                  </div>
                </div>
                {isItemEmpty || !isTotalValid ? (
                  <div className="row">
                    <div className="col-12">
                      <Alert color="info" isOpen={visible} toggle={onDismiss} className="text-center">
                        {isItemEmpty ? 'Please add more item' : isTotalValid ? '' : 'The total weight must be: 100%'}
                      </Alert>
                    </div>
                  </div>
                ) : (
                  ''
                )}
                <div className="row upload-item-detail">
                  {items.map((data, index) => (
                    <CurrentPortfolioUploadItem
                      onFormControlChange={onFormControlChange}
                      uploadPortfolioFormControl={uploadPortfolioFormControl}
                      stockCodeControlName={data.stockCodeControlName}
                      stockWeightControlName={data.stockWeightControlName}
                      onRemoveItem={onRemoveItem}
                      isUploadAction={isUploadAction}
                      key={data.key}
                    />
                  ))}
                </div>
                <div className="row">
                  <div className="col-12 d-flex justify-content-center align-items-center">
                    <button type="button" className="btn btn-primary btn-sm" onClick={addItem}>
                      <FontAwesomeIcon icon={faPlus} className="p-icon" /> Add items
                    </button>
                  </div>
                </div>
              </div>
            </ModalBody>
            <ModalFooter>
              <Button color="primary" disabled={loading} onClick={event => onUploadPortfolio(event)}>
                <FontAwesomeIcon icon={loading ? 'sync' : faUpload} className="p-icon" spin={loading} /> Upload
              </Button>{' '}
              <Button color="secondary" onClick={onCloseModal}>
                Cancel
              </Button>
            </ModalFooter>
          </Modal>
        </div>
      </div>
    </div>
  );
};

export default CurrentPortfolioUploadComponent;
