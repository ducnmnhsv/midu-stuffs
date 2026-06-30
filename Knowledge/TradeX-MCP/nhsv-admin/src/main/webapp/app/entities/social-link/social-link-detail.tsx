import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './social-link.reducer';

export const SocialLinkDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const socialLinkEntity = useAppSelector(state => state.socialLink.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="socialLinkDetailsHeading">Social Link</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{socialLinkEntity.id}</dd>
          <dt>
            <span id="type">Type</span>
          </dt>
          <dd>{socialLinkEntity.type}</dd>
          <dt>
            <span id="link">Link</span>
          </dt>
          <dd>{socialLinkEntity.link}</dd>
          <dt>Chat Room</dt>
          <dd>{socialLinkEntity.chatRoom ? socialLinkEntity.chatRoom.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/social-link" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/social-link/${socialLinkEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default SocialLinkDetail;
