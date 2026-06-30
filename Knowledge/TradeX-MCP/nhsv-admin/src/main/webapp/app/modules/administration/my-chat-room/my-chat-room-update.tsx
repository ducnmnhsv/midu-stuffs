import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity, reset, updateEntity } from './my-chat-room.reducer';
import { getSession } from 'app/shared/reducers/authentication';

export const MyChatRoomUpdate = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();

  const chatRoom = useAppSelector(state => state.createdChatRoom.entity);
  const loading = useAppSelector(state => state.createdChatRoom.loading);
  const updating = useAppSelector(state => state.createdChatRoom.updating);
  const updateSuccess = useAppSelector(state => state.createdChatRoom.updateSuccess);

  const [additionalFields, setAdditionalFields] = useState([]);
  const [imageValue, setImageValue] = useState('');
  const [isSelectValid, setIsSelectValid] = useState(true);

  const handleAddFields = () => {
    setAdditionalFields(prevFields => [
      ...prevFields,
      { type: '', link: '' }
    ]);
    if (additionalFields.length === 0 && chatRoom.socialLinks.length === 0) {
      setIsSelectValid(false);
    } else {
      setIsSelectValid(true);
    }
  };

  const [selectedFile, setSelectedFile] = useState(null);

  const imageChange = (e) => {
    setSelectedFile(e.target.files[0]);
    const [file] = e.target.files;
    if (file) {
      setImageValue(URL.createObjectURL(file));
    }
  };

  const searchParams = new URLSearchParams(history.state.usr).toString();
  const handleClose = () => {
    navigate('/admin/my-chat-room', { state: searchParams });
  };

  useEffect(() => {
    dispatch(getSession());
    dispatch(getEntity(id));
  }, []);

  useEffect(() => {
    if (chatRoom.socialLinks) {
      const extractedFields = chatRoom.socialLinks.map(({ id, type, link }) => ({ id, type, link }));
      setAdditionalFields(extractedFields);
      setIsSelectValid(true);
    }
  }, [chatRoom.socialLinks]);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
      dispatch(reset());
    }
  }, [updateSuccess]);

  const saveEntity = (values: any) => {
    values.file = selectedFile;
    values.groupName = values.groupName.trim();
    const entity = {
      ...chatRoom,
      ...values,
    };
    entity.socialLinks = additionalFields;
    if (isSelectValid === true) {
      dispatch(updateEntity(entity));
    }
  };

  const additionalFieldsChange = (index, value, type) => {
    let values = [...additionalFields];
    values[index][type] = value;
    setAdditionalFields(values);
    additionalFields.forEach((item) => {
      if (item.type === '' || item.link === '') {
        setIsSelectValid(false);
      }
      else {
        setIsSelectValid(true);
      }
    })
  };

  const typeOptions = [
    "Zalo",
    "Telegram",
    "Skype",
    "KakaoTalk",
    "Viber",
    "Messenger",
    "WhatsApp",
  ];

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="nhsvAdminApp.socialLink.home.createOrEditLabel" data-cy="SocialLinkCreateUpdateHeading">
            Update Chat Room
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={chatRoom} onSubmit={saveEntity}>
              <ValidatedField name="id" required readOnly id="id" label="ID" disabled validate={{ required: true }} />
              <ValidatedField
                label="Group Name"
                id="chat-room-groupName"
                name="groupName"
                data-cy="GroupName"
                type="text"
                validate={{
                  required: {
                    value: true,
                    message: 'Group Name is required.',
                  }
                }} />
              <ValidatedField
                label="Introduction"
                id="chat-room-introduction"
                name="introduction"
                data-cy="Introduction"
                type="textarea"
                validate={{
                  required: {
                    value: true,
                    message: 'Introduction is required.',
                  }
                }}
              />
              <ValidatedField
                label="Broker Contact"
                id="chat-room-brokerContact"
                name="brokerContact"
                data-cy="Broker Contact"
                type="text"
                validate={{
                  required: {
                    value: true,
                    message: 'Broker Contact is required.',
                  }
                }}
              />
              <span>Social Link</span>
              <div>
                <a className='linkButton' onClick={handleAddFields}> Add Social Link</a>
              </div>
              <div className="md-3">
                <table>
                  <tbody>
                    {additionalFields &&
                      additionalFields.map((field, index) => (
                        <tr key={'tr' + index}>
                          <td key={'tdtype' + index}>
                            <label htmlFor={`type${index + 1}`} className='form-label'>Type</label>
                            <select
                              id={`type${index + 1}`}
                              name={`type${index + 1}`}
                              onChange={(e) => {
                                additionalFieldsChange(index, e.target.value, 'type');
                              }}
                              defaultValue={field.type}
                              className='form-select'
                              required
                            >
                              <option value="">Select a Type</option> {/* Add a default option */}
                              {typeOptions.map((option) => (
                                <option value={option}>
                                  {option}
                                </option>
                              ))}
                            </select>
                            {!additionalFields[index].link || additionalFields[index].link == '' ? <div className="invalid-feedback">‎</div> : null}
                          </td>
                          <td key={'tdlink' + index}>
                            <label htmlFor={`link${index + 1}`} className='form-label'>Link</label>
                            <input
                              key={'link' + index}
                              id={`link${index + 1}`}
                              name={`link${index + 1}`}
                              defaultValue={field.link}
                              className='form-control'
                              onChange={(e) => {
                                additionalFieldsChange(index, e.target.value, 'link');
                              }}
                              type="text"
                            />
                            {!additionalFields[index].link || additionalFields[index].link == '' ? <div className="invalid-feedback">This field is required.</div> : null}
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>

              <div>
                <Button onClick={handleClose} id="cancel-save" data-cy="entityCreateCancelButton" replace color="info">
                  <FontAwesomeIcon icon="arrow-left" />
                  &nbsp;
                  <span className="d-none d-md-inline">Back</span>
                </Button>
                <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                  <FontAwesomeIcon icon="save" />
                  &nbsp; Save
                </Button>
              </div>
            </ValidatedForm>
          )}
        </Col>
        <Col>
          <div className='upload-contener'>
            <img src={imageValue ? imageValue : chatRoom.photo} alt="" width={200} height={200} className='user-image' />
            <input id="image" type="file" name="photo" accept="image/*" onChange={imageChange} hidden />
            <div className='upload-contener'>
              <label htmlFor="image" className='upload'>Upload</label>
            </div>
          </div>
        </Col>
      </Row>
    </div>
  );
};

export default MyChatRoomUpdate;
