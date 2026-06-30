import config from "./config";
import { DataSource, DataSourceOptions } from "typeorm";

import AdminRole from "./models/db/AdminRole";
import Client from "./models/db/Client";
import DataView from "./models/db/DataView";
import Faq from "./models/db/Faq";
import FaqGroup from "./models/db/FaqGroup";
import FaqReview from "./models/db/FaqReview";
import Holiday from "./models/db/Holiday";
import InterestInfo from "./models/db/InterestInfo";
import LangKey from "./models/db/LangKey";
import LangNamespace from "./models/db/LangNamespace";
import LangResource from "./models/db/LangResource";
import LangResourceFile from "./models/db/LangResourceFile";
import LangResourceVersion from "./models/db/LangResourceVersion";
import LangTranslate from "./models/db/LangTranslate";
import LoginMethod from "./models/db/LoginMethod";
import LoginMethodStep from "./models/db/LoginMethodStep";
import Menu from "./models/db/Menu";
import MenuGroup from "./models/db/MenuGroup";
import MenuRole from "./models/db/MenuRole";
import OpenApi from "./models/db/OpenApi";
import Scope from "./models/db/Scope";
import ScopeGroup from "./models/db/ScopeGroup";
import Service from "./models/db/Service";
import TemplateResource from "./models/db/TemplateResource";

const options: DataSourceOptions = {
  type: "mysql",
  host: config.db.connection.host,
  port: config.db.connection.port,
  username: config.db.connection.user,
  password: config.db.connection.password,
  database: config.db.connection.database,
  entities: [
    AdminRole,
    Client,
    DataView,
    Faq,
    FaqGroup,
    FaqReview,
    Holiday,
    InterestInfo,
    LangKey,
    LangNamespace,
    LangResource,
    LangResourceFile,
    LangResourceVersion,
    LangTranslate,
    LoginMethod,
    LoginMethodStep,
    Menu,
    MenuGroup,
    MenuRole,
    OpenApi,
    Scope,
    ScopeGroup,
    Service,
    TemplateResource,
  ],
  synchronize: false,
  logging: true,
  timezone: "Z",
};

export const AppDataSource = new DataSource(options);
