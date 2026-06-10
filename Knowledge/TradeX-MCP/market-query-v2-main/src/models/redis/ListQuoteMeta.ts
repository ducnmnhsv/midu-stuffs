import Base from "./Base";
import QuotePartition from "./QuotePartition";

export default class ListQuoteMeta extends Base {
  public partitions: QuotePartition[] = [];


  public encode(): string {
    return this.partitions.map(it => it.encode()).join(";");
  }

  public decode(redisData: string) {
    const parts: string[] = redisData.split(';');
    parts.forEach(part => {
      const partition: QuotePartition = new QuotePartition();
      partition.decode(part);
      this.partitions.push(partition);
    });
  }
}