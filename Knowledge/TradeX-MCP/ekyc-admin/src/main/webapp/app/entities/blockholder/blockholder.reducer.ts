import axios from 'axios';
import { ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IBlockholder, defaultValue } from 'app/shared/model/blockholder.model';

export const ACTION_TYPES = {
  FETCH_BLOCKHOLDER_LIST: 'blockholder/FETCH_BLOCKHOLDER_LIST',
  FETCH_BLOCKHOLDER: 'blockholder/FETCH_BLOCKHOLDER',
  CREATE_BLOCKHOLDER: 'blockholder/CREATE_BLOCKHOLDER',
  UPDATE_BLOCKHOLDER: 'blockholder/UPDATE_BLOCKHOLDER',
  PARTIAL_UPDATE_BLOCKHOLDER: 'blockholder/PARTIAL_UPDATE_BLOCKHOLDER',
  DELETE_BLOCKHOLDER: 'blockholder/DELETE_BLOCKHOLDER',
  RESET: 'blockholder/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IBlockholder>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type BlockholderState = Readonly<typeof initialState>;

// Reducer

export default (state: BlockholderState = initialState, action): BlockholderState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.FETCH_BLOCKHOLDER_LIST):
    case REQUEST(ACTION_TYPES.FETCH_BLOCKHOLDER):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_BLOCKHOLDER):
    case REQUEST(ACTION_TYPES.UPDATE_BLOCKHOLDER):
    case REQUEST(ACTION_TYPES.DELETE_BLOCKHOLDER):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_BLOCKHOLDER):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.FETCH_BLOCKHOLDER_LIST):
    case FAILURE(ACTION_TYPES.FETCH_BLOCKHOLDER):
    case FAILURE(ACTION_TYPES.CREATE_BLOCKHOLDER):
    case FAILURE(ACTION_TYPES.UPDATE_BLOCKHOLDER):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_BLOCKHOLDER):
    case FAILURE(ACTION_TYPES.DELETE_BLOCKHOLDER):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.FETCH_BLOCKHOLDER_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_BLOCKHOLDER):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_BLOCKHOLDER):
    case SUCCESS(ACTION_TYPES.UPDATE_BLOCKHOLDER):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_BLOCKHOLDER):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_BLOCKHOLDER):
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

const apiUrl = 'api/blockholders';

// Actions

export const getEntities: ICrudGetAllAction<IBlockholder> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_BLOCKHOLDER_LIST,
  payload: axios.get<IBlockholder>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<IBlockholder> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_BLOCKHOLDER,
    payload: axios.get<IBlockholder>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IBlockholder> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_BLOCKHOLDER,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IBlockholder> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_BLOCKHOLDER,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<IBlockholder> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_BLOCKHOLDER,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IBlockholder> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_BLOCKHOLDER,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
