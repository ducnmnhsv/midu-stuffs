import { Inject, Service } from "typedi";
import config from "../config";
import { Errors, Kafka, ServiceRegistration } from "tradex-common";
import LangResourceService from "../services/LangResourceService";
import AmazonWebService from "../services/AmazonWebService";
import FaqService from "../services/FaqService";
import CommonService from "../services/CommonService";
import TemplateResourceService from "../services/TemplateResourceService";
import HolidayService from "../services/HolidayService";
import InterestInfoService from "../services/InterestInfoService";
import MenuService from "../services/admin/MenuService";
import DataViewService from "../services/admin/DataViewService";
import AdminAmazonWebService from "../services/admin/AdminAmazonWebService";
import LanguageService from "../services/admin/LanguageService";
import ScopeService from "../services/admin/ScopeService";
import ClientService from "../services/admin/ClientService";
import LoginMethodService from "../services/admin/LoginMethodService";
import ScopeGroupService from "../services/admin/ScopeGroupService";
import JSONService from "../services/admin/JSONService";
import SyncDataService from "../services/SyncDataService";
import OpenApiService from "../services/OpenApiService";

@Service()
export default class RequestHandler {
  @Inject()
  private langResourceService: LangResourceService;

  @Inject()
  private templateResourceService: TemplateResourceService;

  @Inject()
  private amazonWebService: AmazonWebService;

  @Inject()
  private faqService: FaqService;

  @Inject()
  private holidayService: HolidayService;

  @Inject()
  private interestInfoService: InterestInfoService;

  @Inject()
  private menuService: MenuService;

  @Inject()
  private commonService: CommonService;

  @Inject()
  private dataViewService: DataViewService;

  @Inject()
  private adminAmazonWebService: AdminAmazonWebService;

  @Inject()
  private languageService: LanguageService;

  @Inject()
  private scopeService: ScopeService;

  @Inject()
  private scopeGroupService: ScopeGroupService;

  @Inject()
  private clientService: ClientService;

  @Inject()
  private loginMethodService: LoginMethodService;

  @Inject()
  private jsonService: JSONService;

  @Inject()
  private openApiService: OpenApiService;

  @Inject()
  private syncDataService: SyncDataService;

  private apiMap: {
    [k: string]: (data: any, message?: Kafka.IMessage) => Promise<any>;
  }; // tslint:disable-line

  public init() {
    ServiceRegistration.create(Kafka.getInstance(), {
      nodeId: config.nodeId,
      serviceName: config.clusterId,
    });

    const handle: Kafka.MessageHandler = new Kafka.MessageHandler();
    new Kafka.StreamHandler(
      config,
      config.kafkaConsumerOptions,
      [config.clusterId],
      (message: any) => handle.handle(message, this.handleRequest),
      config.kafkaTopicOptions,
    );
  }

  private handleRequest: Kafka.Handle = (message: Kafka.IMessage) => {
    this.initApiMap();
    if (message == null || message.data == null) {
      return Promise.reject(new Errors.SystemError());
    }
    const func: (data: any, msg: Kafka.IMessage) => Promise<any> =
      this.apiMap[message.uri];
    if (func != null) {
      return func(message.data, message);
    } else {
      return false;
    }
  };

  private initApiMap() {
    if (this.apiMap == null) {
      this.apiMap = {
        "/api/v1/system/client": (data: any) =>
          this.clientService.queryClientForUpdate(data),
        "/api/v1/system/loginMethod": (data: any) =>
          this.loginMethodService.queryLoginMethodForUpdate(data),
        "/api/v1/system/scope": (data: any) =>
          this.scopeService.queryScopeForUpdate(data),
        "/api/v1/system/scopeGroup": (data: any) =>
          this.scopeGroupService.queryScopeGroupForUpdate(data),
        "/api/v1/admin/common/dataview": (data: any) =>
          this.dataViewService.getDataByView(data),
        "/api/v1/admin/locale/resource": (data: any) =>
          this.languageService.getAllResources(data),
        "/api/v1/admin/locale/{namespaceId}/key": (data: any) =>
          this.languageService.getAllKeysByNamespace(data),
        "/api/v1/admin/locale/{namespaceId}/key/add": (data: any) =>
          this.languageService.addNewKey(data),
        "/api/v1/admin/locale/{namespaceId}/key/delete": (data: any) =>
          this.languageService.deleteKey(data),
        "/api/v1/admin/locale/{keyId}/{lang}": (data: any) =>
          this.languageService.updateLangByKey(data),
        "/api/v1/admin/locale/{namespaceId}/upload": (data: any) =>
          this.languageService.uploadToAWS(data),
        "/api/v1/admin/menus": (data: any) =>
          this.menuService.findMenuByRoleIds(data),
        "/api/v1/admin/aws": (data: any) =>
          this.adminAmazonWebService.getSignedDataToUploadInternal(data),
        "/api/v1/admin/scope": (data: any) =>
          this.scopeService.getAllScopes(data),
        "/api/v1/admin/scope/add": (data: any) =>
          this.scopeService.saveNewScope(data),
        "/api/v1/admin/scope/{scopeId}/update": (data: any) =>
          this.scopeService.updateScope(data),
        "/api/v1/admin/scope/{scopeId}/delete": (data: any) =>
          this.scopeService.deleteScope(data),
        "/api/v1/admin/scopeGroup": (data: any) =>
          this.scopeGroupService.getAllScopeGroups(data),
        "/api/v1/admin/scopeGroup/add": (data: any) =>
          this.scopeGroupService.addNewScopeGroup(data),
        "/api/v1/admin/scopeGroup/{scopeGroupId}/update": (data: any) =>
          this.scopeGroupService.updateScopeGroup(data),
        "/api/v1/admin/scopeGroup/{scopeGroupId}/delete": (data: any) =>
          this.scopeGroupService.deleteScopeGroup(data),
        "/api/v1/locale/internal": (data: any) =>
          this.langResourceService.getAllResourcesForInternal(data),
        "/api/v1/locale": (data: any) =>
          this.langResourceService.getAllResources(data),
        "/api/v1/aws": (data: any) =>
          this.amazonWebService.getSignedDataToUploadPublic(data),
        "/api/v1/faq/{msName}": (data: any) =>
          this.faqService.getFaqsOfService(data),
        "/api/v1/faq/{faqId}/review/{isUseful}": (data: any) =>
          this.faqService.reviewFaq(data),
        "/api/v1/common/services": (data: any) =>
          this.commonService.getAllServices(data),
        "/api/v1/template": (data: any) =>
          this.templateResourceService.getAllResources(data),
        "/api/v1/holidays": (data: any) =>
          this.holidayService.findAllHoliday(data),
        "/api/v1/interestInfo": () =>
          this.interestInfoService.findAllInterestInfo(),
        "/api/v1/client": (data: any) => this.clientService.findAllClient(data),
        "put:/api/v1/client/{id}/changeSecret": (data: any) =>
          this.clientService.changeClientSecret(data),
        "/api/v1/client/add": (data: any) => this.clientService.addClient(data),
        "/api/v1/client/{id}": (data: any) =>
          this.clientService.findClientById(data),
        "/api/v1/client/{id}/update": (data: any) =>
          this.clientService.updateClient(data),
        "/api/v1/client/{id}/delete": (data: any) =>
          this.clientService.deleteClient(data),
        "/api/v1/loginMethod": (data: any) =>
          this.loginMethodService.findAllLoginMethod(data),
        "/api/v1/loginMethod/add": (data: any) =>
          this.loginMethodService.addNewLoginMethod(data),
        "/api/v1/loginMethod/{id}/update": (data: any) =>
          this.loginMethodService.updateLoginMethod(data),
        "/api/v1/loginMethod/{id}/delete": (data: any) =>
          this.loginMethodService.deleteLoginMethod(data),
        "/api/v1/loginMethod/{id}": (data: any) =>
          this.loginMethodService.findLoginMethodById(data),
        "/api/v1/export/dbJsonFile": () => this.jsonService.exportJsonMap(),
        "/api/v1/import/dbJsonFile": () => this.jsonService.importJsonMap(),
        "/api/v1/openApi/list": (data: any) =>
          this.openApiService.queryOpenApiList(data),
        "/api/v1/openApi/save": (data: any) =>
          this.openApiService.updateOpenApi(data),
        "/api/v1/openApi/delete": (data: any) =>
          this.openApiService.deleteOpenApiList(data),
        "/api/v1/openApi/client": (data: any) =>
          this.openApiService.queryOpenApiFileByClient(data),
        //sync data between domain and tradex
        "/api/v1/sync/holiday": () => this.syncDataService.syncHoliday(),
        "/api/v1/sync/interestInfo": () =>
          this.syncDataService.syncInterestInfo(),
        "/api/v1/sync/locale/internal": (data: any) =>
          this.syncDataService.syncResourcesForInternal(data),
        "/api/v1/sync/locale": (data: any) =>
          this.syncDataService.syncLangResource(data),
        "/api/v1/sync/admin/locale/resource": (data: any) =>
          this.syncDataService.syncAdminResource(data),
        "/api/v1/sync/admin/locale/{namespaceId}/key": (data: any) =>
          this.syncDataService.syncKeysByNamespace(data),
      };
    }
  }
}
