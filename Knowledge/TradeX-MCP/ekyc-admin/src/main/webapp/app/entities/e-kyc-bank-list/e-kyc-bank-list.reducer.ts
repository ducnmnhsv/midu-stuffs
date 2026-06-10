import axios from 'axios';
import { ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IEKycBankList, defaultValue } from 'app/shared/model/e-kyc-bank-list.model';

export const ACTION_TYPES = {
  FETCH_EKYCBANKLIST_LIST: 'eKycBankList/FETCH_EKYCBANKLIST_LIST',
  FETCH_EKYCBANKLIST: 'eKycBankList/FETCH_EKYCBANKLIST',
  CREATE_EKYCBANKLIST: 'eKycBankList/CREATE_EKYCBANKLIST',
  UPDATE_EKYCBANKLIST: 'eKycBankList/UPDATE_EKYCBANKLIST',
  PARTIAL_UPDATE_EKYCBANKLIST: 'eKycBankList/PARTIAL_UPDATE_EKYCBANKLIST',
  DELETE_EKYCBANKLIST: 'eKycBankList/DELETE_EKYCBANKLIST',
  RESET: 'eKycBankList/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IEKycBankList>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type EKycBankListState = Readonly<typeof initialState>;

// Reducer

export default (state: EKycBankListState = initialState, action): EKycBankListState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.FETCH_EKYCBANKLIST_LIST):
    case REQUEST(ACTION_TYPES.FETCH_EKYCBANKLIST):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_EKYCBANKLIST):
    case REQUEST(ACTION_TYPES.UPDATE_EKYCBANKLIST):
    case REQUEST(ACTION_TYPES.DELETE_EKYCBANKLIST):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_EKYCBANKLIST):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.FETCH_EKYCBANKLIST_LIST):
    case FAILURE(ACTION_TYPES.FETCH_EKYCBANKLIST):
    case FAILURE(ACTION_TYPES.CREATE_EKYCBANKLIST):
    case FAILURE(ACTION_TYPES.UPDATE_EKYCBANKLIST):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_EKYCBANKLIST):
    case FAILURE(ACTION_TYPES.DELETE_EKYCBANKLIST):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.FETCH_EKYCBANKLIST_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_EKYCBANKLIST):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_EKYCBANKLIST):
    case SUCCESS(ACTION_TYPES.UPDATE_EKYCBANKLIST):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_EKYCBANKLIST):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_EKYCBANKLIST):
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

const apiUrl = 'api/e-kyc-bank-lists';

// Actions

export const getEntities: ICrudGetAllAction<IEKycBankList> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_EKYCBANKLIST_LIST,
  payload: axios.get<IEKycBankList>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<IEKycBankList> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_EKYCBANKLIST,
    payload: axios.get<IEKycBankList>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IEKycBankList> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_EKYCBANKLIST,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IEKycBankList> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_EKYCBANKLIST,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<IEKycBankList> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_EKYCBANKLIST,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IEKycBankList> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_EKYCBANKLIST,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
