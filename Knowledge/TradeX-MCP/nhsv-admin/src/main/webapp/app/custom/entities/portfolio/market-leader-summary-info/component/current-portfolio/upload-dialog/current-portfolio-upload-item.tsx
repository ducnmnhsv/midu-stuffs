import React from 'react';
import '../../../../portfolio.scss';
import { Button, Col, Form, FormGroup, FormText, Input, InputGroup, InputGroupText, Row } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faXmark } from '@fortawesome/free-solid-svg-icons';
import lodash from 'lodash';

const CurrentPortfolioUploadItemComponent = props => {
  const stockCodeControlName = props.stockCodeControlName;
  const stockWeightControlName = props.stockWeightControlName;
  const uploadPortfolioFormControl = props.uploadPortfolioFormControl;
  const onFormControlChange = props.onFormControlChange;
  const onRemoveItem = props.onRemoveItem;
  const isUploadAction = props.isUploadAction;


  return (
    <div>
      <Form>
        <Row className="p-mb-5">
          <Col md={5} className="">
            <FormGroup>
              <Input
                id={stockCodeControlName}
                onChange={event => onFormControlChange(event, stockCodeControlName)}
                name={stockCodeControlName}
                value={uploadPortfolioFormControl[stockCodeControlName]}
                placeholder="Symbol"
                required
                maxLength={3}
                valid={!lodash.isEmpty(uploadPortfolioFormControl[stockCodeControlName])}
                className={lodash.isEmpty(uploadPortfolioFormControl[stockCodeControlName]) && isUploadAction ? 'is-invalid' : ''}
                type="text"
              />
              {lodash.isEmpty(uploadPortfolioFormControl[stockCodeControlName]) && isUploadAction ? (
                <FormText className="text-danger">
                  <p className="text-danger">This field is required.</p>
                </FormText>
              ) : (
                ''
              )}
            </FormGroup>
          </Col>
          <Col md={5}>
            <FormGroup>
              <InputGroup>
                <Input
                  id={stockWeightControlName}
                  onChange={event => onFormControlChange(event, stockWeightControlName)}
                  name={stockWeightControlName}
                  value={uploadPortfolioFormControl[stockWeightControlName]}
                  placeholder="Weight"
                  required
                  valid={!lodash.isEmpty(uploadPortfolioFormControl[stockWeightControlName])}
                  className={lodash.isEmpty(uploadPortfolioFormControl[stockWeightControlName]) && isUploadAction ? 'is-invalid' : ''}
                  type="number"
                />
                <InputGroupText>%</InputGroupText>
              </InputGroup>
              {lodash.isEmpty(uploadPortfolioFormControl[stockWeightControlName]) && isUploadAction ? (
                <FormText>
                  <p className="text-danger">This field is required.</p>
                </FormText>
              ) : (
                ''
              )}
            </FormGroup>
          </Col>
          <Col md={2} className="">
            <Button color="danger" className="m-0" onClick={event => onRemoveItem(event, stockCodeControlName, stockWeightControlName)}>
              <FontAwesomeIcon icon={faXmark} className="p-icon" />
            </Button>
          </Col>
        </Row>
      </Form>
    </div>
  );
};

export default CurrentPortfolioUploadItemComponent;
