export const checkStringTrim = (text: string): string => {
  if (!text || text.trim() === '') {
    return null;
  } else {
    return text;
  }
};
