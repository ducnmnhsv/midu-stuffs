# How it works

This service receives request from topic mas-rest-bridge, call ttl apis via http.

# How to start

1. **Modify your config in src/env.js. Example file:**

```
const MAS_API_CATEGORIES = require('./constants/MasApi').MAS_API_CATEGORIES;
console.log(MAS_API_CATEGORIES);
var configure = (conf) => {
  conf.clientId = '';
  conf.enableEncryptPassword = false;
  conf.db.connection.host = 'localhost';
  conf.logger.config.categories.default.appenders = ['console'];
  return conf;
};
module.exports = configure;

```

2. **Run**
   Make sure you have node, npm, nvm installed.

```
export TRADEX_ENV_DOMAIN="mas" #mas or kis, you choose. This is mandatory.
nvm use
npm run build-local
```

# Dev note

###### Where to start reading this repo?

in **[src/index.ts](src/index.ts)**, `requestHandler.init()` is called. in **[src/init/RequestHandler.ts](src/init/RequestHandler.ts)** `initApiMap()` is where things begin

###### This calls TTL apis, shouldn't it be TRADEX-TTL-REST-BRIDGE?

It should, but apparently it was misunderstood at first that apis are from Mas

###### A lot of files in src/kis and src/mas might look similar?

We completed dev for mas, it's done and stable. TTL team develop total differrent repo for kis, there is no need to modify module for mas.

###### How to reuse this repo for other secs, e.g. vcsc?

Copy *src/kis* to *src/vcsc* and work with it. Add handlers to **[src/init/RequestHandler.ts](src/init/RequestHandler.ts)**
