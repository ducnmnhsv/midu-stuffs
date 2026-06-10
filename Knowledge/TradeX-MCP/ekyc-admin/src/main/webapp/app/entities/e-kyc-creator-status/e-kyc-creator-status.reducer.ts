import axios from 'axios';
import { ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IEKycCreatorStatus, defaultValue } from 'app/shared/model/e-kyc-creator-status.model';

export const ACTION_TYPES = {
  FETCH_EKYCCREATORSTATUS_LIST: 'eKycCreatorStatus/FETCH_EKYCCREATORSTATUS_LIST',
  FETCH_EKYCCREATORSTATUS: 'eKycCreatorStatus/FETCH_EKYCCREATORSTATUS',
  CREATE_EKYCCREATORSTATUS: 'eKycCreatorStatus/CREATE_EKYCCREATORSTATUS',
  UPDATE_EKYCCREATORSTATUS: 'eKycCreatorStatus/UPDATE_EKYCCREATORSTATUS',
  PARTIAL_UPDATE_EKYCCREATORSTATUS: 'eKycCreatorStatus/PARTIAL_UPDATE_EKYCCREATORSTATUS',
  DELETE_EKYCCREATORSTATUS: 'eKycCreatorStatus/DELETE_EKYCCREATORSTATUS',
  SET_BLOB: 'eKycCreatorStatus/SET_BLOB',
  RESET: 'eKycCreatorStatus/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IEKycCreatorStatus>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type EKycCreatorStatusState = Readonly<typeof initialState>;

// Reducer

export default (state: EKycCreatorStatusState = initialState, action): EKycCreatorStatusState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.FETCH_EKYCCREATORSTATUS_LIST):
    case REQUEST(ACTION_TYPES.FETCH_EKYCCREATORSTATUS):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_EKYCCREATORSTATUS):
    case REQUEST(ACTION_TYPES.UPDATE_EKYCCREATORSTATUS):
    case REQUEST(ACTION_TYPES.DELETE_EKYCCREATORSTATUS):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_EKYCCREATORSTATUS):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.FETCH_EKYCCREATORSTATUS_LIST):
    case FAILURE(ACTION_TYPES.FETCH_EKYCCREATORSTATUS):
    case FAILURE(ACTION_TYPES.CREATE_EKYCCREATORSTATUS):
    case FAILURE(ACTION_TYPES.UPDATE_EKYCCREATORSTATUS):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_EKYCCREATORSTATUS):
    case FAILURE(ACTION_TYPES.DELETE_EKYCCREATORSTATUS):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.FETCH_EKYCCREATORSTATUS_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_EKYCCREATORSTATUS):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_EKYCCREATORSTATUS):
    case SUCCESS(ACTION_TYPES.UPDATE_EKYCCREATORSTATUS):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_EKYCCREATORSTATUS):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_EKYCCREATORSTATUS):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {},
      };
    case ACTION_TYPES.SET_BLOB: {
      const { name, data, contentType } = action.payload;
      return {
        ...state,
        entity: {
          ...state.entity,
          [name]: data,
          [name + 'ContentType']: contentType,
        },
      };
    }
    case ACTION_TYPES.RESET:
      return {
        ...initialState,
      };
    default:
      return state;
  }
};

const apiUrl = 'api/e-kyc-creator-statuses';

// Actions

export const getEntities: ICrudGetAllAction<IEKycCreatorStatus> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_EKYCCREATORSTATUS_LIST,
  payload: axios.get<IEKycCreatorStatus>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<IEKycCreatorStatus> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_EKYCCREATORSTATUS,
    payload: axios.get<IEKycCreatorStatus>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IEKycCreatorStatus> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_EKYCCREATORSTATUS,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IEKycCreatorStatus> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_EKYCCREATORSTATUS,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<IEKycCreatorStatus> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_EKYCCREATORSTATUS,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IEKycCreatorStatus> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_EKYCCREATORSTATUS,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const setBlob = (name, data, contentType?) => ({
  type: ACTION_TYPES.SET_BLOB,
  payload: {
    name,
    data,
    contentType,
  },
});

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
