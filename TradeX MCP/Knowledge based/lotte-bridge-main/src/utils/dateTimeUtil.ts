export function getCurrentTime() {
  const now = new Date();
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');

  return hours + minutes + seconds;
}

export const checkDate = (date: string): string => {
  if (!date || date.trim() === '' || date.replace('00000000', '') === '') {
    return null;
  } else {
    return date;
  }
};
