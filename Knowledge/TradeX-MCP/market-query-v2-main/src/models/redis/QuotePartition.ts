import Base from "./Base";

export default class QuotePartition extends Base {
  public partition: number;
  public fromVolume: number;
  public toVolume: number;
  public totalItems: number;


  public encode(): string {
    return `${this.partition}|${this.fromVolume}|${this.toVolume}|${this.totalItems}`;
  }
  
  public decode(redisData: string) {
    const parts: string[] = redisData.split('|');
    if (parts.length != 4) {
      throw new Error("invalid quote partition:" + redisData);
    }
    try {
      this.partition = parseInt(parts[0], 10);
      this.fromVolume = parseInt(parts[1], 10);
      this.toVolume = parseInt(parts[2], 10);
      this.totalItems = parseInt(parts[3], 10);
    } catch (e: any) {
      throw new Error("invalid quote partition:" + redisData + "--" + e?.message);
    }
  }
}