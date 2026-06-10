const TradexCommon = require('tradex-common');
const fs = require('fs');
const conf = require('./conf');

let scopeMap = {};
let scopeGroupMap = {};
let groupMap = {};
let loadedTime = null;
let unAuthenticatedScopes = [];
let dataFileName = conf.scopeCachedFile;

function saveData() {
  const data = {
    scopeMap: scopeMap,
    scopeGroupMap: {},
    groupMap: groupMap,
    loadedTime: loadedTime,
  };
  const scGrIds = Object.keys(scopeGroupMap);
  for (const element of scGrIds) {
    const scGrId = element;
    data.scopeGroupMap[scGrId] = scopeGroupMap[scGrId].map(scope => scope.id);
  }
  fs.writeFile(dataFileName, JSON.stringify(data, null), err => {
    if (err != null) {
      TradexCommon.Logger.error("fail to save scopes to file", err);
    }
  });
}

function loadGroupPage(groupMap, lastSequence, pageSize, next) {
  const fetchCount = pageSize == null ? 100 : pageSize;
  TradexCommon.Kafka.getInstance().sendRequestAsync("",
    conf.scopes.loadFrom.topic, conf.scopes.loadFrom.uris.scopeGroup, {
      fetchCount: fetchCount,
      lastSequence: lastSequence
    }).then(msg => {
    const scopeGroups = TradexCommon.Kafka.getResponse(msg);
    scopeGroups.forEach(scopeGroup => {
      groupMap[scopeGroup.id] = scopeGroup;
    });
    if (scopeGroups.length >= fetchCount) {
      loadGroupPage(groupMap, scopeGroups[scopeGroups.length - 1].id, fetchCount, next);
    } else {
      next();
    }
  });
}

function loadScopesPage(scMap, scGrMap, lastSequence, pageSize, next) {
  const fetchCount = pageSize == null ? 100 : pageSize;
  TradexCommon.Kafka.getInstance().sendRequestAsync("",
    conf.scopes.loadFrom.topic, conf.scopes.loadFrom.uris.scope, {
      fetchCount: fetchCount,
      lastSequence: lastSequence
    }).then(msg => {
    const scopes = TradexCommon.Kafka.getResponse(msg);
    scopes.forEach(scope => {
      scMap[scope.id] = scope;
      const scopeGroupIds = scope.scopeGroupIds;
      if (scopeGroupIds != null) {
        scopeGroupIds.forEach(sgId => {
          let scopeGroup = scGrMap[sgId];
          if (scopeGroup == null) {
            scopeGroup = [scope];
            scGrMap[sgId] = scopeGroup;
          } else {
            scopeGroup.push(scope);
          }
        });
      }
    });
    if (scopes.length >= fetchCount) {
      loadScopesPage(scMap, scGrMap, scopes[scopes.length - 1].id, fetchCount, next);
    } else {
      next();
    }
  });
}

function loadScopesFromConf(onFinish) {
  TradexCommon.Logger.info("loadScopesFromConf");
  const scMap = {};
  const scGrMap = {};
  const grMap = {};
  loadScopesPage(scMap, scGrMap, null, 100, () => {
    scopeMap = scMap;
    scopeGroupMap = scGrMap;
    TradexCommon.Logger.info("loadGroupPage");
    loadGroupPage(grMap, null, 100, () => {
      TradexCommon.Logger.info("load finished");
      loadedTime = Date.now();
      groupMap = grMap;
      saveData();
      loadUnAuthenticatedScopes();
      if (onFinish != null) {
        onFinish();
      }
    });
  });
}

function loadScopes(isLeader, onFinish) {
  console.log("load scope", isLeader);
  fs.exists(dataFileName, exists => {
    if (exists) {
      fs.readFile(dataFileName, "utf8", (err, data) => {
        if (err != null) {
          TradexCommon.Logger.error("cannot load scopeFile", dataFileName, err);
          if (isLeader) {
            loadScopesFromConf(onFinish);
          }
        } else {
          let scopeData = JSON.parse(data);
          scopeMap = scopeData.scopeMap;
          groupMap = scopeData.groupMap;
          loadedTime = scopeData.loadedTime;
          let scopeGroupMapData = scopeData.scopeGroupMap;
          let scopeGroupIds = Object.keys(scopeGroupMapData);
          for (const element of scopeGroupIds) {
            const scopeGroupId = element;
            const scopeIds = scopeGroupMapData[scopeGroupId];
            const scopes = [];
            scopeIds.forEach(id => scopes.push(scopeMap[id]));
            scopeGroupMapData[scopeGroupId] = scopes;
          }
          scopeGroupMap = scopeGroupMapData;
          TradexCommon.Logger.info("finish load scope from file");
          loadUnAuthenticatedScopes();
          if (Date.now() - loadedTime >= conf.scopes.maximumAliveTime) {
            if (isLeader) {
              loadScopesFromConf(onFinish);
            }
          }
        }
      });
    } else {
      TradexCommon.Logger.info("file does not exist. load from conf");
      if (isLeader) {
        loadScopesFromConf(onFinish);
      }
    }
  });
}

function loadUnAuthenticatedScopes() {
  unAuthenticatedScopes.length = 0;
  const scGrIds = Object.keys(groupMap);
  for (const element of scGrIds) {
    const group = groupMap[element];
    if (group.scopeGroupName === conf.scopes.specialScopes.unAuthenticated) {
      const scopes = scopeGroupMap[element];
      if (scopes != null) {
        unAuthenticatedScopes.push(...scopes);
      }
    }
  }
}

function getScopesByScopeGroups(scopeGroupIds) {
  const result = [];
  scopeGroupIds.forEach(scGrId => {
    const scopes = scopeGroupMap[scGrId];
    if (scopes != null) {
      result.push(...scopes);
    }
  });
  return result;
}

function getScopes(scopeGroupIds) {
  if (scopeGroupIds == null || scopeGroupIds.length === 0) {
    return unAuthenticatedScopes;
  } else {
    return getScopesByScopeGroups(scopeGroupIds).concat(unAuthenticatedScopes);
  }
}

module.exports = {
  loadScopes,
  loadScopesFromConf,
  getScopes,
  unAuthenticatedScopes,
};
