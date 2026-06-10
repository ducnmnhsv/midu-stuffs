import axios from 'axios';
import { ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IPublicCoop, defaultValue } from 'app/shared/model/public-coop.model';

export const ACTION_TYPES = {
  FETCH_PUBLICCOOP_LIST: 'publicCoop/FETCH_PUBLICCOOP_LIST',
  FETCH_PUBLICCOOP: 'publicCoop/FETCH_PUBLICCOOP',
  CREATE_PUBLICCOOP: 'publicCoop/CREATE_PUBLICCOOP',
  UPDATE_PUBLICCOOP: 'publicCoop/UPDATE_PUBLICCOOP',
  PARTIAL_UPDATE_PUBLICCOOP: 'publicCoop/PARTIAL_UPDATE_PUBLICCOOP',
  DELETE_PUBLICCOOP: 'publicCoop/DELETE_PUBLICCOOP',
  RESET: 'publicCoop/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IPublicCoop>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type PublicCoopState = Readonly<typeof initialState>;

// Reducer

export default (state: PublicCoopState = initialState, action): PublicCoopState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.FETCH_PUBLICCOOP_LIST):
    case REQUEST(ACTION_TYPES.FETCH_PUBLICCOOP):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_PUBLICCOOP):
    case REQUEST(ACTION_TYPES.UPDATE_PUBLICCOOP):
    case REQUEST(ACTION_TYPES.DELETE_PUBLICCOOP):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_PUBLICCOOP):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.FETCH_PUBLICCOOP_LIST):
    case FAILURE(ACTION_TYPES.FETCH_PUBLICCOOP):
    case FAILURE(ACTION_TYPES.CREATE_PUBLICCOOP):
    case FAILURE(ACTION_TYPES.UPDATE_PUBLICCOOP):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_PUBLICCOOP):
    case FAILURE(ACTION_TYPES.DELETE_PUBLICCOOP):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.FETCH_PUBLICCOOP_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_PUBLICCOOP):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_PUBLICCOOP):
    case SUCCESS(ACTION_TYPES.UPDATE_PUBLICCOOP):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_PUBLICCOOP):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_PUBLICCOOP):
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

const apiUrl = 'api/public-coops';

// Actions

export const getEntities: ICrudGetAllAction<IPublicCoop> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_PUBLICCOOP_LIST,
  payload: axios.get<IPublicCoop>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<IPublicCoop> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_PUBLICCOOP,
    payload: axios.get<IPublicCoop>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IPublicCoop> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_PUBLICCOOP,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IPublicCoop> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_PUBLICCOOP,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<IPublicCoop> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_PUBLICCOOP,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IPublicCoop> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_PUBLICCOOP,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
