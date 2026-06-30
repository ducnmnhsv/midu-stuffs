import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IChatRoom } from 'app/shared/model/chat-room.model';
import { getEntities as getChatRooms } from 'app/entities/chat-room/chat-room.reducer';
import { ISocialLink } from 'app/shared/model/social-link.model';
import { getEntity, updateEntity, createEntity, reset } from './social-link.reducer';

export const SocialLinkUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const chatRooms = useAppSelector(state => state.chatRoom.entities);
  const socialLinkEntity = useAppSelector(state => state.socialLink.entity);
  const loading = useAppSelector(state => state.socialLink.loading);
  const updating = useAppSelector(state => state.socialLink.updating);
  const updateSuccess = useAppSelector(state => state.socialLink.updateSuccess);

  const handleClose = () => {
    navigate('/social-link');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getChatRooms({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...socialLinkEntity,
      ...values,
      chatRoom: chatRooms.find(it => it.id.toString() === values.chatRoom.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...socialLinkEntity,
          chatRoom: socialLinkEntity?.chatRoom?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.socialLink.home.createOrEditLabel" data-cy="SocialLinkCreateUpdateHeading">
            Create or edit a Social Link
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="social-link-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField label="Type" id="social-link-type" name="type" data-cy="type" type="text" />
              <ValidatedField label="Link" id="social-link-link" name="link" data-cy="link" type="text" />
              <ValidatedField id="social-link-chatRoom" name="chatRoom" data-cy="chatRoom" label="Chat Room" type="select">
                <option value="" key="0" />
                {chatRooms
                  ? chatRooms.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/social-link" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default SocialLinkUpdate;
