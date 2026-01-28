import { Service } from "typedi";
import { Errors, Logger, Utils } from "tradex-common";
import DataView from "../../models/db/DataView";
import { ViewSelectResponse } from "../../models/response/admin/ViewSelectResponse";
import IDataViewRequest from "../../models/request/admin/IDataViewRequest";
import { DataViewRepository } from "../../repositories/DataViewRepository";
import { DataViewStatusEnum } from "../../constants/DataViewStatusEnum";
import { DATA_VIEW_CODE_NOT_EXISTED } from "../../constants/errors";
import { DataViewTypeEnum } from "../../constants/DataViewTypeEnum";
import IKeyValue from "../../models/request/IKeyValue";

@Service()
export default class DataViewService {
  private dataViews: DataView[] = [];

  constructor() {
    setTimeout(() => {
      this.getAllDataViews()
        .then((dataViews: DataView[]) => {
          this.dataViews = dataViews;
        })
        .catch((err: any) => {
          Logger.error(err);
        });
    });
  }

  public getAllDataViews(): Promise<DataView[]> {
    return DataViewRepository.find({
      where: {
        status: DataViewStatusEnum.ENABLED,
      },
    });
  }

  public async getDataByView(
    request: IDataViewRequest,
  ): Promise<ViewSelectResponse[]> {
    const invalidParams = new Errors.InvalidParameterError();
    Utils.validate(request.code, "code").setRequire().throwValid(invalidParams);
    invalidParams.throwErr();

    const dataView = this.dataViews.find(
      (value: DataView) => value.code === request.code,
    );
    if (dataView == null) {
      throw new Errors.GeneralError(
        DATA_VIEW_CODE_NOT_EXISTED,
        invalidParams.params,
      );
    }

    const args: IKeyValue[] = [];
    if (dataView.filterFields != null) {
      dataView.filterFields.forEach((field: string) => {
        if (request[field] != null) {
          args.push({
            key: field,
            value: request[field],
          });
        }
      });
    }

    if (dataView.dataViewType === DataViewTypeEnum.SELECT) {
      return DataViewRepository.getSelectDataByView(
        dataView.viewName,
        request.fetchCount,
        request.lastSequence,
        args,
      );
    } else {
      return [];
    }
  }
}
