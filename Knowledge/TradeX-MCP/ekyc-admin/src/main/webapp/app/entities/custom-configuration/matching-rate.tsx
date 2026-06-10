import React, {useCallback, useEffect, useState} from "react";
import {Button, Col, Form, FormGroup, Input, Label, Row} from "reactstrap";
import axios from "axios";
import List from "reactstrap/es/List";

export const MatchingRateConfiguration = () => {
  const [matchingRate, setMatchingRate] = useState(0);
  const [edit, setEdit] = useState(false);
  const [loading, setLoading] = useState(false);
  const [windowSize, setWindowSize] = useState({
    width: undefined,
    height: undefined,
  });

  useEffect(() => {
    function handleResize() {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    }
    window.addEventListener('resize', handleResize);
    handleResize();
    const getMatchingRate = () => {
      axios.get('/api/v1/ekyc-admin/configurations/matching-rate').then(response => {
        setMatchingRate(response.data);
      });
    }

    getMatchingRate();
  }, []);

  const saveEntity = useCallback(async (event) => {
    event.preventDefault();
    try {
      setLoading(true);
      const entity = {
        matchingRate: event.target.matchingRate.value
      };
      await axios.post('/api/v1/ekyc-admin/configurations/matching-rate', entity);
    }
    finally {
      setLoading(false);
      setEdit(false);
    }
  }, []);

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2>Matching Rate Configuration</h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          <Form className="form mb-2" onSubmit={saveEntity}  id="formMatchingRate" inline={windowSize.width > 400}>
            <FormGroup>
              <Label for="matchingRate" className="mr-2">eKYC Matching Rate Limitation is currently set as</Label>
              <Input type="number" id={"matchingRate"}
                     className="mr-2"
                     name="matchingRate"
                     readOnly={!edit}
                     disabled={!edit}
                     onChange={(event) => setMatchingRate(Number(event.target.value))}
                     value={matchingRate}
                     required={edit}
                     min={0}
              />
              {edit
                ? <Button color="link" type="submit" disabled={loading} form="formMatchingRate">Save</Button>
                : <Button color="link" type="button" onClick={(event) => {
                  event.preventDefault();
                  setEdit(true)
                }} disabled={loading}>Edit</Button>}
            </FormGroup>
          </Form>
          <List>
            <li>If the Matching Rate in the eKYC request belows this number the system would reject the request</li>
          </List>
        </Col>
      </Row>
    </div>
  );
}
