export function versionCompare(v1: string, v2: string, options?: {
  lexicographical?: boolean;
  zeroExtend?: boolean;
}): number {
  const lexicographical: boolean = options != null && options.lexicographical != null;
  const zeroExtend: boolean = options  != null && options.zeroExtend != null;
  let v1parts: string[] | number[] = v1.split('.');
  let v2parts: string[] | number[] = v2.split('.');

  const isValidPart = (x: string) => {
    return (lexicographical ? /^\d+[A-Za-z]*$/ : /^\d+$/).test(x);
  };

  if (!v1parts.every(isValidPart) || !v2parts.every(isValidPart)) {
    return NaN;
  }

  if (zeroExtend) {
    while (v1parts.length < v2parts.length) {
      v1parts.push("0");
    }
    while (v2parts.length < v1parts.length) {
      v2parts.push("0");
    }
  }

  if (!lexicographical) {
    v1parts = v1parts.map(Number);
    v2parts = v2parts.map(Number);
  }

  for (let i = 0; i < v1parts.length; ++i) {
    if (v2parts.length === i) {
      return 1;
    }

    if (v1parts[i] === v2parts[i]) {
      continue;
    } else if (v1parts[i] > v2parts[i]) {
      return 1;
    } else {
      return -1;
    }
  }

  if (v1parts.length !== v2parts.length) {
    return -1;
  }

  return 0;
}
