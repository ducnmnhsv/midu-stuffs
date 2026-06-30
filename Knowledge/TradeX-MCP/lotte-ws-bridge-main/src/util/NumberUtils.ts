/**
 * Chuyển giá trị (string/number/unknown) thành number bằng Number().
 * Nếu không chuyển được (NaN) thì trả về giá trị fallback truyền vào.
 *
 * @param value - Giá trị cần chuyển (thường là string từ API)
 * @param fallback - Giá trị dùng khi parse ra NaN
 * @returns number
 */
export function stringToNumberDefault(value: string | number | unknown, fallback: number|string): number|string {
  const num = Number(value);
  return Number.isNaN(num) ? fallback : num;
}
