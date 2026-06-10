import axios from 'axios';
import { ICrudSearchAction, ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IEKyc, defaultValue } from 'app/shared/model/e-kyc.model';

export const ACTION_TYPES = {
  SEARCH_EKYCS: 'eKyc/SEARCH_EKYCS',
  FETCH_EKYC_LIST: 'eKyc/FETCH_EKYC_LIST',
  FETCH_EKYC: 'eKyc/FETCH_EKYC',
  CREATE_EKYC: 'eKyc/CREATE_EKYC',
  UPDATE_EKYC: 'eKyc/UPDATE_EKYC',
  PARTIAL_UPDATE_EKYC: 'eKyc/PARTIAL_UPDATE_EKYC',
  DELETE_EKYC: 'eKyc/DELETE_EKYC',
  RESET: 'eKyc/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IEKyc>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type EKycState = Readonly<typeof initialState>;

// Reducer

export default (state: EKycState = initialState, action): EKycState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.SEARCH_EKYCS):
    case REQUEST(ACTION_TYPES.FETCH_EKYC_LIST):
    case REQUEST(ACTION_TYPES.FETCH_EKYC):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_EKYC):
    case REQUEST(ACTION_TYPES.UPDATE_EKYC):
    case REQUEST(ACTION_TYPES.DELETE_EKYC):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_EKYC):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.SEARCH_EKYCS):
    case FAILURE(ACTION_TYPES.FETCH_EKYC_LIST):
    case FAILURE(ACTION_TYPES.FETCH_EKYC):
    case FAILURE(ACTION_TYPES.CREATE_EKYC):
    case FAILURE(ACTION_TYPES.UPDATE_EKYC):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_EKYC):
    case FAILURE(ACTION_TYPES.DELETE_EKYC):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.SEARCH_EKYCS):
    case SUCCESS(ACTION_TYPES.FETCH_EKYC_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_EKYC):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_EKYC):
    case SUCCESS(ACTION_TYPES.UPDATE_EKYC):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_EKYC):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_EKYC):
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

const apiUrl = 'api/e-kycs';
const apiSearchUrl = 'api/_search/e-kycs';

// Actions

export const getSearchEntities: ICrudSearchAction<IEKyc> = (query, page, size, sort) => ({
  type: ACTION_TYPES.SEARCH_EKYCS,
  payload: axios.get<IEKyc>(`${apiSearchUrl}?query=${query}`),
});

export const getEntities: ICrudGetAllAction<IEKyc> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_EKYC_LIST,
  payload: axios.get<IEKyc>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<IEKyc> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_EKYC,
    payload: axios.get<IEKyc>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IEKyc> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_EKYC,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IEKyc> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_EKYC,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<IEKyc> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_EKYC,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IEKyc> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_EKYC,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
