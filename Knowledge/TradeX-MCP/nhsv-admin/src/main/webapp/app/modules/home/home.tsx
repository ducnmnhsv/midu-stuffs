import './home.scss';

import React, { useEffect } from 'react';

import { Row, Col, Alert } from 'reactstrap';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';

export const Home = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getSession());
  },[]);
  const account = useAppSelector(state => state.authentication.account);

  return (
    <Row>
      <Col md="3" className="pad">
        <span className="hipster rounded" />
      </Col>
      <Col md="9">
        <h2>Welcome to Admin Page!</h2>
        <p className="lead">This is your homepage</p>
        {account?.login ? (
          <div>
            <Alert color="success">You are logged in as user &quot;{account.login}&quot;.</Alert>
            <Alert color="success">You are logged in as roles &quot;{account.authorities.join(" And ")}&quot;.</Alert>
          </div>
        ) : null }
      </Col>
    </Row>
  );
};

export default Home;
