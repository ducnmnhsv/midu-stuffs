const fs = require('fs');
const os = require('os');
const path = require('path');
const { extractFunctions } = require('./extract-helpers');

function loadHelpers(names) {
  const jsxPath = path.join(__dirname, '..', 'ProjectDashboard.jsx');
  const body = extractFunctions(jsxPath, names);
  const wrapped = `${body}\nmodule.exports = { ${names.join(', ')} };`;
  const tmpFile = path.join(os.tmpdir(), `ptd-helpers-${process.pid}-${Date.now()}-${Math.random().toString(36).slice(2)}.js`);
  fs.writeFileSync(tmpFile, wrapped);
  try {
    delete require.cache[require.resolve(tmpFile)];
    return require(tmpFile);
  } finally {
    fs.unlinkSync(tmpFile);
  }
}

module.exports = { loadHelpers };
