import axios from 'axios';
import { ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IEKycExt, defaultValue } from 'app/shared/model/e-kyc-ext.model';

export const ACTION_TYPES = {
  FETCH_EKYCEXT_LIST: 'eKycExt/FETCH_EKYCEXT_LIST',
  FETCH_EKYCEXT: 'eKycExt/FETCH_EKYCEXT',
  CREATE_EKYCEXT: 'eKycExt/CREATE_EKYCEXT',
  UPDATE_EKYCEXT: 'eKycExt/UPDATE_EKYCEXT',
  PARTIAL_UPDATE_EKYCEXT: 'eKycExt/PARTIAL_UPDATE_EKYCEXT',
  DELETE_EKYCEXT: 'eKycExt/DELETE_EKYCEXT',
  SET_BLOB: 'eKycExt/SET_BLOB',
  RESET: 'eKycExt/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IEKycExt>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type EKycExtState = Readonly<typeof initialState>;

// Reducer

export default (state: EKycExtState = initialState, action): EKycExtState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.FETCH_EKYCEXT_LIST):
    case REQUEST(ACTION_TYPES.FETCH_EKYCEXT):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_EKYCEXT):
    case REQUEST(ACTION_TYPES.UPDATE_EKYCEXT):
    case REQUEST(ACTION_TYPES.DELETE_EKYCEXT):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_EKYCEXT):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.FETCH_EKYCEXT_LIST):
    case FAILURE(ACTION_TYPES.FETCH_EKYCEXT):
    case FAILURE(ACTION_TYPES.CREATE_EKYCEXT):
    case FAILURE(ACTION_TYPES.UPDATE_EKYCEXT):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_EKYCEXT):
    case FAILURE(ACTION_TYPES.DELETE_EKYCEXT):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.FETCH_EKYCEXT_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_EKYCEXT):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_EKYCEXT):
    case SUCCESS(ACTION_TYPES.UPDATE_EKYCEXT):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_EKYCEXT):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_EKYCEXT):
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

const apiUrl = 'api/e-kyc-exts';

// Actions

export const getEntities: ICrudGetAllAction<IEKycExt> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_EKYCEXT_LIST,
  payload: axios.get<IEKycExt>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<IEKycExt> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_EKYCEXT,
    payload: axios.get<IEKycExt>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IEKycExt> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_EKYCEXT,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IEKycExt> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_EKYCEXT,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<IEKycExt> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_EKYCEXT,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IEKycExt> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_EKYCEXT,
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
