import axios from 'axios';
import { ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction, ICrudSearchAction } from 'react-jhipster';

import { cleanEntity, mapIdList } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IEKyc, defaultValue } from 'app/shared/model/custom-e-kyc.model';

export const ACTION_TYPES = {
  FETCH_EKYC_LIST: 'eKyc/FETCH_EKYC_LIST',
  FETCH_EKYC: 'eKyc/FETCH_EKYC',
  CREATE_EKYC: 'eKyc/CREATE_EKYC',
  UPDATE_EKYC: 'eKyc/UPDATE_EKYC',
  PARTIAL_UPDATE_EKYC: 'eKyc/PARTIAL_UPDATE_EKYC',
  DELETE_EKYC: 'eKyc/DELETE_EKYC',
  RESET: 'eKyc/RESET',
  UPDATE_PARAMSTRING: 'eKyc/UPDATE_PARAMSTRING',
  APPROVE_EKYC: 'eKyc/APPROVE_EKYC',
  REJECT_EKYC: 'eKyc/REJECT_EKYC',
  SET_SELECTED_ID: 'eKyc/SET_SELECTED_ID',
  WAITING_CONFIRM_EKYC: 'eKyc/WAITING_CONFIRM_EKYC',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IEKyc>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
  paramString: '',
};

export type EKycState = Readonly<typeof initialState>;

// Reducer
export default (state: EKycState = initialState, action): EKycState => {
  switch (action.type) {
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
    case REQUEST(ACTION_TYPES.WAITING_CONFIRM_EKYC):
    case REQUEST(ACTION_TYPES.REJECT_EKYC):
    case REQUEST(ACTION_TYPES.APPROVE_EKYC):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.FETCH_EKYC_LIST):
    case FAILURE(ACTION_TYPES.FETCH_EKYC):
    case FAILURE(ACTION_TYPES.CREATE_EKYC):
    case FAILURE(ACTION_TYPES.UPDATE_EKYC):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_EKYC):
    case FAILURE(ACTION_TYPES.DELETE_EKYC):
    case FAILURE(ACTION_TYPES.WAITING_CONFIRM_EKYC):
    case FAILURE(ACTION_TYPES.REJECT_EKYC):
    case FAILURE(ACTION_TYPES.APPROVE_EKYC):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
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
    case SUCCESS(ACTION_TYPES.REJECT_EKYC):
    case SUCCESS(ACTION_TYPES.APPROVE_EKYC):
    case SUCCESS(ACTION_TYPES.WAITING_CONFIRM_EKYC):
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
const apiAction = '/api/v1/ekyc-admin/ekyc/';

// Actions

export const getEntities: ICrudGetAllAction<IEKyc> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_EKYC_LIST,
  payload: axios.get<IEKyc>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntitiesFilter: ICrudGetAction<IEKyc> = (paramString: string | null) => ({
  type: ACTION_TYPES.FETCH_EKYC_LIST,
  payload: axios.get<IEKyc>(`/api/v1/ekyc-admin/e-kycs?cacheBuster=${new Date().getTime()}${paramString}`),
});

export const getEntity: ICrudGetAction<IEKyc> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_EKYC,
    payload: axios.get<IEKyc>(requestUrl),
  };
};

export const setEntitySelectedID = (id: string) => ({
  type: ACTION_TYPES.SET_SELECTED_ID,
  payload: id,
});

export const createEntity: ICrudPutAction<IEKyc> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_EKYC,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const approveEntity: ICrudPutAction<IEKyc> = entityArr => async dispatch => {
  try {
    const result = await dispatch({
      type: ACTION_TYPES.APPROVE_EKYC,
      payload: axios.put(`${apiAction}approve`, entityArr.idList),
    });
    dispatch(getEntitiesFilter(entityArr.paramString));
    return result;
  } catch (error) {
    console.log(error);
  }
};

export const approveAndCreateAccount: ICrudPutAction<IEKyc> = entityArr => async dispatch => {
  try {
    const result = await dispatch({
      type: ACTION_TYPES.APPROVE_EKYC,
      payload: axios.put(`${apiAction}approveAndCreate`, entityArr.idList),
    });
    dispatch(getEntitiesFilter(entityArr.paramString));
    return result;
  } catch (error) {
    console.log(error);
  }
};

export const rejectEntity: ICrudPutAction<IEKyc> = entityArr => async dispatch => {
  try {
    const result = await dispatch({
      type: ACTION_TYPES.REJECT_EKYC,
      payload: axios.put(`${apiAction}reject`, entityArr.idList),
    });
    dispatch(getEntitiesFilter(entityArr.paramString));
    return result;
  } catch (error) {
    console.log(error);
  }
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

export const waitingConfirmEntity: ICrudPutAction<IEKyc> = entityArr => async dispatch => {
  try {
    const result = await dispatch({
      type: ACTION_TYPES.WAITING_CONFIRM_EKYC,
      payload: axios.put(`${apiAction}waiting-confirmation`),
    });
    dispatch(getEntitiesFilter(entityArr.paramString));
    return result;
  } catch (error) {
    console.log(error);
  }
}
