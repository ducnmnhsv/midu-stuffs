function filter(openapi) {
  let newPaths = {};
  let uris = Object.keys(openapi.paths);
  uris.forEach(uri => {
    let uriData = openapi.paths[uri];
    let newUriData = {};
    let methods = Object.keys(uriData);
    methods.forEach(method => {
      let methodData = uriData[method];
      if (methodData.tags != null && (methodData.tags.includes("Tuxedo") || methodData.tags.includes("Winway"))) {
        console.log(uri, ":", method);
        newUriData[method] = methodData;
      }
    });
    if (Object.keys(newUriData).length > 0) {
      newPaths[uri] = newUriData;
    }
  });

  return {
    ...openapi,
    paths: newPaths,
  }
}
