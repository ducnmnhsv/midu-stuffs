import axios from 'axios';
import { ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IEKycAdditionalInfo, defaultValue } from 'app/shared/model/e-kyc-additional-info.model';

export const ACTION_TYPES = {
  FETCH_EKYCADDITIONALINFO_LIST: 'eKycAdditionalInfo/FETCH_EKYCADDITIONALINFO_LIST',
  FETCH_EKYCADDITIONALINFO: 'eKycAdditionalInfo/FETCH_EKYCADDITIONALINFO',
  CREATE_EKYCADDITIONALINFO: 'eKycAdditionalInfo/CREATE_EKYCADDITIONALINFO',
  UPDATE_EKYCADDITIONALINFO: 'eKycAdditionalInfo/UPDATE_EKYCADDITIONALINFO',
  PARTIAL_UPDATE_EKYCADDITIONALINFO: 'eKycAdditionalInfo/PARTIAL_UPDATE_EKYCADDITIONALINFO',
  DELETE_EKYCADDITIONALINFO: 'eKycAdditionalInfo/DELETE_EKYCADDITIONALINFO',
  RESET: 'eKycAdditionalInfo/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IEKycAdditionalInfo>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type EKycAdditionalInfoState = Readonly<typeof initialState>;

// Reducer

export default (state: EKycAdditionalInfoState = initialState, action): EKycAdditionalInfoState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.FETCH_EKYCADDITIONALINFO_LIST):
    case REQUEST(ACTION_TYPES.FETCH_EKYCADDITIONALINFO):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_EKYCADDITIONALINFO):
    case REQUEST(ACTION_TYPES.UPDATE_EKYCADDITIONALINFO):
    case REQUEST(ACTION_TYPES.DELETE_EKYCADDITIONALINFO):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_EKYCADDITIONALINFO):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.FETCH_EKYCADDITIONALINFO_LIST):
    case FAILURE(ACTION_TYPES.FETCH_EKYCADDITIONALINFO):
    case FAILURE(ACTION_TYPES.CREATE_EKYCADDITIONALINFO):
    case FAILURE(ACTION_TYPES.UPDATE_EKYCADDITIONALINFO):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_EKYCADDITIONALINFO):
    case FAILURE(ACTION_TYPES.DELETE_EKYCADDITIONALINFO):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.FETCH_EKYCADDITIONALINFO_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_EKYCADDITIONALINFO):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_EKYCADDITIONALINFO):
    case SUCCESS(ACTION_TYPES.UPDATE_EKYCADDITIONALINFO):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_EKYCADDITIONALINFO):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_EKYCADDITIONALINFO):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {},
      };
    case ACTION_TYPES.RESET:
      return {
        ...initialState,
      };
    default:
      return state;
  }
};

const apiUrl = 'api/e-kyc-additional-infos';

// Actions

export const getEntities: ICrudGetAllAction<IEKycAdditionalInfo> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_EKYCADDITIONALINFO_LIST,
  payload: axios.get<IEKycAdditionalInfo>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<IEKycAdditionalInfo> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_EKYCADDITIONALINFO,
    payload: axios.get<IEKycAdditionalInfo>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IEKycAdditionalInfo> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_EKYCADDITIONALINFO,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IEKycAdditionalInfo> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_EKYCADDITIONALINFO,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<IEKycAdditionalInfo> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_EKYCADDITIONALINFO,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IEKycAdditionalInfo> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_EKYCADDITIONALINFO,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
