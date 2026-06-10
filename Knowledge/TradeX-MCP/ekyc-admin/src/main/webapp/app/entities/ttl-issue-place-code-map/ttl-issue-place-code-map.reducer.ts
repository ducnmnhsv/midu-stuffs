import axios from 'axios';
import { ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { ITtlIssuePlaceCodeMap, defaultValue } from 'app/shared/model/ttl-issue-place-code-map.model';

export const ACTION_TYPES = {
  FETCH_TTLISSUEPLACECODEMAP_LIST: 'ttlIssuePlaceCodeMap/FETCH_TTLISSUEPLACECODEMAP_LIST',
  FETCH_TTLISSUEPLACECODEMAP: 'ttlIssuePlaceCodeMap/FETCH_TTLISSUEPLACECODEMAP',
  CREATE_TTLISSUEPLACECODEMAP: 'ttlIssuePlaceCodeMap/CREATE_TTLISSUEPLACECODEMAP',
  UPDATE_TTLISSUEPLACECODEMAP: 'ttlIssuePlaceCodeMap/UPDATE_TTLISSUEPLACECODEMAP',
  PARTIAL_UPDATE_TTLISSUEPLACECODEMAP: 'ttlIssuePlaceCodeMap/PARTIAL_UPDATE_TTLISSUEPLACECODEMAP',
  DELETE_TTLISSUEPLACECODEMAP: 'ttlIssuePlaceCodeMap/DELETE_TTLISSUEPLACECODEMAP',
  RESET: 'ttlIssuePlaceCodeMap/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<ITtlIssuePlaceCodeMap>,
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

export type TtlIssuePlaceCodeMapState = Readonly<typeof initialState>;

// Reducer

export default (state: TtlIssuePlaceCodeMapState = initialState, action): TtlIssuePlaceCodeMapState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.FETCH_TTLISSUEPLACECODEMAP_LIST):
    case REQUEST(ACTION_TYPES.FETCH_TTLISSUEPLACECODEMAP):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_TTLISSUEPLACECODEMAP):
    case REQUEST(ACTION_TYPES.UPDATE_TTLISSUEPLACECODEMAP):
    case REQUEST(ACTION_TYPES.DELETE_TTLISSUEPLACECODEMAP):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_TTLISSUEPLACECODEMAP):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.FETCH_TTLISSUEPLACECODEMAP_LIST):
    case FAILURE(ACTION_TYPES.FETCH_TTLISSUEPLACECODEMAP):
    case FAILURE(ACTION_TYPES.CREATE_TTLISSUEPLACECODEMAP):
    case FAILURE(ACTION_TYPES.UPDATE_TTLISSUEPLACECODEMAP):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_TTLISSUEPLACECODEMAP):
    case FAILURE(ACTION_TYPES.DELETE_TTLISSUEPLACECODEMAP):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.FETCH_TTLISSUEPLACECODEMAP_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
        totalItems: parseInt(action.payload.headers['x-total-count'], 10),
      };
    case SUCCESS(ACTION_TYPES.FETCH_TTLISSUEPLACECODEMAP):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_TTLISSUEPLACECODEMAP):
    case SUCCESS(ACTION_TYPES.UPDATE_TTLISSUEPLACECODEMAP):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_TTLISSUEPLACECODEMAP):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_TTLISSUEPLACECODEMAP):
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

const apiUrl = 'api/ttl-issue-place-code-maps';

// Actions

export const getEntities: ICrudGetAllAction<ITtlIssuePlaceCodeMap> = (page, size, sort) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}` : ''}`;
  return {
    type: ACTION_TYPES.FETCH_TTLISSUEPLACECODEMAP_LIST,
    payload: axios.get<ITtlIssuePlaceCodeMap>(`${requestUrl}${sort ? '&' : '?'}cacheBuster=${new Date().getTime()}`),
  };
};

export const getEntity: ICrudGetAction<ITtlIssuePlaceCodeMap> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_TTLISSUEPLACECODEMAP,
    payload: axios.get<ITtlIssuePlaceCodeMap>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<ITtlIssuePlaceCodeMap> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_TTLISSUEPLACECODEMAP,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<ITtlIssuePlaceCodeMap> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_TTLISSUEPLACECODEMAP,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<ITtlIssuePlaceCodeMap> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_TTLISSUEPLACECODEMAP,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<ITtlIssuePlaceCodeMap> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_TTLISSUEPLACECODEMAP,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
