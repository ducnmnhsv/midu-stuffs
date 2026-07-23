const fs = require('fs');

function extractFunctions(sourcePath, names) {
  const src = fs.readFileSync(sourcePath, 'utf8');
  return names.map(name => {
    // Try to extract as a function first
    let marker = `function ${name}(`;
    let start = src.indexOf(marker);
    
    if (start !== -1) {
      // It's a function
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
    }
    
    // Try to extract as a const
    marker = `const ${name} =`;
    start = src.indexOf(marker);
    if (start !== -1) {
      // It's a const; find the terminating semicolon
      let end = src.indexOf(';', start);
      if (end === -1) throw new Error(`no semicolon found for const ${name}`);
      return src.slice(start, end + 1);
    }
    
    throw new Error(`function or const ${name} not found in ${sourcePath}`);
  }).join('\n\n');
}

module.exports = { extractFunctions };
