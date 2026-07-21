const fs = require('fs');

function extractFunctions(sourcePath, names) {
  const src = fs.readFileSync(sourcePath, 'utf8');
  return names.map(name => {
    const marker = `function ${name}(`;
    const start = src.indexOf(marker);
    if (start === -1) throw new Error(`function ${name} not found in ${sourcePath}`);
    const braceStart = src.indexOf('{', start);
    if (braceStart === -1) throw new Error(`no body found for ${name}`);
    let depth = 0;
    let end = -1;
    for (let i = braceStart; i < src.length; i++) {
      if (src[i] === '{') depth++;
      else if (src[i] === '}') {
        depth--;
        if (depth === 0) { end = i + 1; break; }
      }
    }
    if (end === -1) throw new Error(`unbalanced braces extracting ${name}`);
    return src.slice(start, end);
  }).join('\n\n');
}

module.exports = { extractFunctions };
