import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './invite-user.reducer';

export const InviteUserDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const inviteUserEntity = useAppSelector(state => state.inviteUser.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="inviteUserDetailsHeading">Invite User</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{inviteUserEntity.id}</dd>
          <dt>
            <span id="login">Login</span>
          </dt>
          <dd>{inviteUserEntity.login}</dd>
          <dt>
            <span id="email">Email</span>
          </dt>
          <dd>{inviteUserEntity.email}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{inviteUserEntity.status}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>
            {inviteUserEntity.createdAt ? <TextFormat value={inviteUserEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="updatedAt">Updated At</span>
          </dt>
          <dd>
            {inviteUserEntity.updatedAt ? <TextFormat value={inviteUserEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="createdId">Created Id</span>
          </dt>
          <dd>{inviteUserEntity.createdId}</dd>
          <dt>
            <span id="createdBy">Created By</span>
          </dt>
          <dd>{inviteUserEntity.createdBy}</dd>
          <dt>
            <span id="activationKey">Activation Key</span>
          </dt>
          <dd>{inviteUserEntity.activationKey}</dd>
          <dt>
            <span id="activationDate">Activation Date</span>
          </dt>
          <dd>
            {inviteUserEntity.activationDate ? (
              <TextFormat value={inviteUserEntity.activationDate} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="langKey">Lang Key</span>
          </dt>
          <dd>{inviteUserEntity.langKey}</dd>
          <dt>
            <span id="authorities">Authorities</span>
          </dt>
          <dd>{inviteUserEntity.authorities}</dd>
        </dl>
        <Button tag={Link} to="/invite-user" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/invite-user/${inviteUserEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default InviteUserDetail;
