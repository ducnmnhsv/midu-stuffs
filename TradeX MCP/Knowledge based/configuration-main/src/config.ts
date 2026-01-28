/* tslint:disable */
import { Logger } from "tradex-common";
import * as fs from "fs";
import { v4 as uuid } from "uuid";

const nodeId = uuid();
export const STORAGE_TYPES = {
  S3: "aws",
  MINIO: "minio",
};
const config = {
  domain: "tradex",
  defaultFetchCount: 100,
  db: {
    client: "mysql",
    connection: {
      host: "ls-7cb4c06adc779212e6cb3028d74965c3c413c80e.cq3lbezwn0da.ap-southeast-1.rds.amazonaws.com",
      port: 3306,
      user: "techxdev",
      password: "7,B,2^H+mTK.:(O)po3iKPI0N$.7D<ox",
      database: "tradex-configuration",
    },
  },
  logger: {
    config: {
      appenders: {
        application: { type: "console" },
        file: {
          type: "file",
          filename: "/logs/application.log",
          compression: true,
          maxLogSize: 10485760,
          backups: 10,
        },
      },
      categories: {
        default: { appenders: ["application", "file"], level: "info" },
      },
    },
  },
  log: {
    serviceName: "configuration-service",
    format: "FLAT", // 'FLAT' or 'JSON'
    transport: [],
  },
  clusterId: "configuration",
  clientId: `configuration-${nodeId}`,
  nodeId: nodeId,
  json: "dbExport.json",
  kafkaUrls: ["localhost:9092"],
  zkUrls: ["localhost:2181"],
  storageService: STORAGE_TYPES.S3, // STORAGE_TYPES.MINIO
  topic: {
    configurationSync: "configuration.sync",
    domainConnector: "domain-connector",
  },
  timerConfigurationSync: 60 * 60 * 1000, //1h sync interval
  countConfigurationSync: 10,
  uri: {
    configurationSync: "/api/v1/configurationSync",
    findAllHoliday: "/api/v1/holidays",
    findAllInterestInfo: "/api/v1/interestInfo",
    getAllResourcesForInternal: "/api/v1/locale/internal",
    getAllResources: "/api/v1/locale",
    getAllAdminResources: "/api/v1/admin/locale/resource",
    getAllKeysByNamespace: "/api/v1/admin/locale/{namespaceId}/key",
  },
  aws: {
    accessKeyId: "AKIAIOLXKEAYDTDCI2HA",
    secretAccessKey: "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",
    s3: {
      announcement: {
        region: "ap-southeast-1",
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "announcement/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "text/html",
      },
      analysisReport: {
        region: "ap-southeast-1",
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "analysis_report/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "application/pdf",
      },
      langResource: {
        region: "ap-southeast-1",
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "lang_resource/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "application/json",
      },
      public: {
        region: "ap-southeast-1",
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "avatar/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "image/",
      },
      dbExport: {
        region: "ap-southeast-1",
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "json_file/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "application/json/",
      },
    },
  },
  minio: {
    internal: {
      endPoint: "",
      accessKey: "AKIAIOLXKEAYDTDCI2HA",
      secretKey: "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",
      useSSL: undefined,
      port: undefined,
      region: undefined, //'us-east-1'|'us-west-1'|'us-west-2'|'eu-west-1'|'eu-central-1'|'ap-southeast-1'|'ap-northeast-1'|'ap-southeast-2'|'sa-east-1'|'cn-north-1'|string;
      transport: undefined,
      sessionToken: undefined,
      partSize: undefined, // number
    },
    external: {
      endPoint: "",
      accessKey: "AKIAIOLXKEAYDTDCI2HA",
      secretKey: "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",
      useSSL: undefined,
      port: undefined,
      region: undefined, //'us-east-1'|'us-west-1'|'us-west-2'|'eu-west-1'|'eu-central-1'|'ap-southeast-1'|'ap-northeast-1'|'ap-southeast-2'|'sa-east-1'|'cn-north-1'|string;
      transport: undefined,
      sessionToken: undefined,
      partSize: undefined, // number
    },
    urlRewriteTo: "http://prod3.dev.tradex.vn:9000",
    port: 9000,

    region: "ap-southeast-1",
    policies: {
      "public-read": `{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"AWS":["*"]},"Action":["s3:GetBucketLocation"],"Resource":["arn:aws:s3:::xxBucketNamexx"]},{"Effect":"Allow","Principal":{"AWS":["*"]},"Action":["s3:GetObject"],"Resource":["arn:aws:s3:::xxBucketNamexx/*"]}]}`,
    },
    buckets: {
      announcement: {
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "announcement/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "text/html",
      },
      analysisReport: {
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "analysis_report/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "application/pdf",
      },
      langResource: {
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "lang_resource/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "application/json",
      },
      public: {
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "avatar/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "image/",
      },
      dbExport: {
        acl: "public-read",
        bucket: "tradex-vn",
        expires: 300, //seconds
        pathToUpload: "json_file/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "application/json/",
      },
      ekyc: {
        region: "ap-southeast-1",
        acl: "public-read",
        bucket: "ekyc-images",
        expires: 300, //seconds
        pathToUpload: "avatar/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "image/",
        requireAuthenticate: false,
      },
      feedback: {
        region: "ap-southeast-1",
        acl: "public-read",
        bucket: "feedback-images",
        expires: 300, //seconds
        pathToUpload: "feedback/",
        minUpload: 0,
        maxUpload: 2097152, // 2 MiB;
        contentType: "image/",
        requireAuthenticate: false,
      },
    },
  },
  publicScopeGroup: 4,
  dbExportUrl:
    "https://tradex-vn.s3.ap-southeast-1.amazonaws.com/dbExport.json",
  assumeRole: {
    public: {
      DurationSeconds: 900,
      RoleArn: "arn:aws:iam::157907901550:role/TradeX_Customer",
    },
  },
  kafkaCommonOptions: {},
  kafkaConsumerOptions: {},
  kafkaProducerOptions: {},
  kafkaTopicOptions: {},
  swagger: {
    server: "localhost:3000",
    version: {
      v1: "/api/v1",
      v2: "/api/v2",
    },
    header: {
      openapi: "3.0.0",
      info: {
        title: "Rest API Specification",
        version: "1.0.0",
        description: "![API Flow](/assets/img/API_Flow.png)",
      },
      servers: [
        {
          url: `localhost:3000`,
          description: "TRADEX API Server",
        },
      ],
      components: {
        securitySchemes: {
          jwt: {
            type: "apiKey",
            in: "header",
            name: "Authorization",
          },
        },
      },
    },
  },
  s3: {
    region: "ap-southeast-1",
    bucketName: "tradex-vn",
    accessKey: "AKIAIOLXKEAYDTDCI2HA",
    privateKey: "Dl6k9qgN7IQ4THu7m3G/AO/VwtK4YyqOJak0cyzu",
  },
};

function initLogger() {
  try {
    Logger.create(config.logger.config, true);
  } catch (err) {
    console.error("fail to init logger");
    process.exit(1);
  }
}

try {
  const configFileStr = fs.readFileSync("env.js", "utf8");
  const vm = require("node:vm");
  const script = new vm.Script(configFileStr);
  script.runInNewContext({
    conf: config,
    config: config,
    process,
    console,
  });
  initLogger();
} catch (e) {
  initLogger();
  Logger.error("fail to load external configuration", e);
}

config.kafkaConsumerOptions = {
  ...(config.kafkaCommonOptions ? config.kafkaCommonOptions : {}),
  ...(config.kafkaConsumerOptions ? config.kafkaConsumerOptions : {}),
};
config.kafkaProducerOptions = {
  ...(config.kafkaCommonOptions ? config.kafkaCommonOptions : {}),
  ...(config.kafkaProducerOptions ? config.kafkaProducerOptions : {}),
};

export default config;
