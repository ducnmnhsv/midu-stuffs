import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router-dom';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity, deleteEntity } from './ttl-issue-place-code-map.reducer';

export interface ITtlIssuePlaceCodeMapDeleteDialogProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const TtlIssuePlaceCodeMapDeleteDialog = (props: ITtlIssuePlaceCodeMapDeleteDialogProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const handleClose = () => {
    props.history.push('/ttl-issue-place-code-map' + props.location.search);
  };

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const confirmDelete = () => {
    props.deleteEntity(props.ttlIssuePlaceCodeMapEntity.id);
  };

  const { ttlIssuePlaceCodeMapEntity } = props;
  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="ttlIssuePlaceCodeMapDeleteDialogHeading">
        <Translate contentKey="entity.delete.title">Confirm delete operation</Translate>
      </ModalHeader>
      <ModalBody id="eKycAdminApp.ttlIssuePlaceCodeMap.delete.question">
        <Translate contentKey="eKycAdminApp.ttlIssuePlaceCodeMap.delete.question" interpolate={{ id: ttlIssuePlaceCodeMapEntity.id }}>
          Are you sure you want to delete this TtlIssuePlaceCodeMap?
        </Translate>
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp;
          <Translate contentKey="entity.action.cancel">Cancel</Translate>
        </Button>
        <Button id="jhi-confirm-delete-ttlIssuePlaceCodeMap" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp;
          <Translate contentKey="entity.action.delete">Delete</Translate>
        </Button>
      </ModalFooter>
    </Modal>
  );
};

const mapStateToProps = ({ ttlIssuePlaceCodeMap }: IRootState) => ({
  ttlIssuePlaceCodeMapEntity: ttlIssuePlaceCodeMap.entity,
  updateSuccess: ttlIssuePlaceCodeMap.updateSuccess,
});

const mapDispatchToProps = { getEntity, deleteEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(TtlIssuePlaceCodeMapDeleteDialog);
