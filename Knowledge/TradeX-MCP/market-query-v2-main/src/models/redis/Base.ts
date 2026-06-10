export default abstract class Base {
  public abstract encode(): string;

  public abstract decode(redisData: string);
}