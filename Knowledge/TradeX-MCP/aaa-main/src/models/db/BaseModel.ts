/* tslint:disable */
import FieldModel from "./FieldModel";

abstract class BaseModel<T> {
  private fields: FieldModel<any>[] = [];

  protected constructor(private row?: any) {
    this.fields.forEach((f: FieldModel<any>) => {
      f.initDefault();
    });
  }

  public field<E>(fieldName: string, rowData: () => any, defaultValue?: E): FieldModel<E> {
    const f: FieldModel<E> = new FieldModel<E>(fieldName, rowData, defaultValue);
    this.fields.push(f);
    return f;
  }

  public getRow: () => any = () => {
    if (!this.row) {
      this.row = {};
    }
    return this.row;
  };

  public cloneFields(): FieldModel<any>[] {
    return this.fields.map((f: FieldModel<any>) => f.clone());
  }

  public abstract getTableName(): string;

  public abstract clone(): T;
}

export class QueryData {
  constructor(public query: string, public data: any) {

  }
}

class Query<T extends BaseModel<T>> {
  public model: T;
  private readonly fields: FieldModel<any>[];
  private readonly joins: Join<T, any, any>[] = [];
  private readonly orders: Order<any>[] = [];
  private readonly alias: string = "main";
  private condition: ICondition;
  private aliasIndex: number = 0;

  constructor(model: T) {
    this.fields = model.cloneFields();
    this.model = model.clone();
  }

  public getAlias(): string {
    return this.alias;
  }

  public getNextAlias(): number {
    return this.aliasIndex++;
  }

  public join<A extends BaseModel<A>>(to: A, fromField: (fromModel: T) => FieldModel<any>, toField: (to: A) => FieldModel<any>): Join<T, T, A> {
    const j: Join<T, T, A> = new Join(this, this.model, to, fromField, toField, this.alias);
    this.joins.push(j);
    return j;
  }

  public where(getField: (model: T) => FieldModel<any>, condition: string): Query<T> {
    this.whereCondition(new FieldCondition(getField(this.model), condition, this.alias));
    return this;
  }

  public whereCondition(condition: FieldCondition): Query<T> {
    this.condition = condition;
    return this;
  }

  public whereAnd(getField: (model: T) => FieldModel<any>, condition: string): AndCondition {
    return this.whereAndCondition(new FieldCondition(getField(this.model), condition, this.alias));
  }

  public whereAndCondition(condition: ICondition): AndCondition {
    const cond: AndCondition = new AndCondition();
    this.condition = cond;
    cond.addCondition(condition);
    return cond;
  }

  public whereOr(condition: ICondition): OrCondition {
    const cond: OrCondition = new OrCondition();
    this.condition = cond;
    cond.addCondition(condition);
    return cond;
  }

  public order(getField: (model: T) => FieldModel<any>
    , asc: boolean = true): Query<T> {
    return this.orderField(getField(this.model), asc, this.alias);
  }

  public orderField(field: FieldModel<any>
    , asc: boolean = true
    , alias: string = this.alias): Query<T> {
    this.orders.push(new Order(field, alias, asc));
    return this;
  }

  public selectFields(getFields: (model: T) => FieldModel<any>[]): string {
    return this.select(getFields(this.model));
  }

  public select(fieldArray?: FieldModel<any>[]): string {
    const fields = (!fieldArray || fieldArray.length === 0) ? this.fields : fieldArray;
    let result: string = null;
    if (this.joins.length === 0) {
      result = `select ${fields.map((f: FieldModel<any>) => `main.${f.getFieldName()} as ${f.getFieldName()}`).join(", ")} from ${this.model.getTableName()} as ${this.alias}  ${this.condition ? `where ${this.condition.build()}` : ""}`;
    } else {
      result = `select ${fields.map((f: FieldModel<any>) => `main.${f.getFieldName()} as ${f.getFieldName()}`).join(", ")} 
      from ${this.model.getTableName()} as ${this.alias}  
      ${this.joins.map((j: Join<T, any, any>) => j.build()).join(" ")} 
      ${this.condition ? `where ${this.condition.build()}` : ""}`;
    }

    if (this.orders.length > 0) {
      result += ` order by ${this.orders.map((order: Order<any>) => order.build()).join(",")}`;
    }

    console.info(result);// tslint:disable-line
    return result;
  }

  public insert(): string {
    const result: string = `insert into ${this.model.getTableName()} Set ?`;
    console.info(result);// tslint:disable-line
    return result;
  }

  public insertByFields(fields: FieldModel<any>[], records: T[]): [string, any[][]] {
    const fs: FieldModel<any>[] = (fields != null && fields.length > 0) ? fields : this.model.cloneFields();
    const params: any[][] = [];
    records.forEach(() => params.push([]));
    const fieldNames: string = fs.map((field: FieldModel<any>) => {
      records.forEach((record: T, index: number) => params[index].push(record.getRow()[field.getFieldName()]));
      return `\`${field.getFieldName()}\``;
    }).join(", ");
    let result: string = `insert into \`${this.model.getTableName()}\` (${fieldNames}) VALUES ?`;
    console.info(params.length, result);// tslint:disable-line
    return [result, [params]];
  }

  public insertData(realDataModel?: T): any {
    const data = {};
    const fields = realDataModel ? realDataModel.cloneFields() : this.fields;
    fields.forEach((f: FieldModel<any>) => {
      if (this.filterFieldForInsert(f)) {
        data[f.getFieldName()] = f.get();
      }
    });
    return data;
  }

  public update(realDataModel?: T): QueryData {
    const result: string = `update ${this.model.getTableName()} as ${this.alias} 
      set ${this.fields.filter(this.filterFieldForUpdate).map((field: FieldModel<any>) => `${field.getFieldName(this.alias)} = ?`).join(", ")}
      ${this.condition ? `where ${this.condition.build()}` : ""}
    `;
    const data = this.updateData(realDataModel);
    console.info(result, data);// tslint:disable-line
    return new QueryData(result, data);
  }

  public updateData(realDataModel?: T): any {
    const data = {};
    const fields = realDataModel ? realDataModel.cloneFields() : this.fields;
    fields.filter(this.filterFieldForUpdate).forEach((field: FieldModel<any>) => data[field.getFieldName()] = field.get());
    return data;
  }

  public insertUpdateOnDuplicate(realDataModel?: T): QueryData {
    const result: string = `insert into ${this.model.getTableName()} Set ? ON DUPLICATE KEY UPDATE ?`;
    const insertData = this.insertData(realDataModel);
    const updateData = this.updateData(realDataModel);
    const data = [insertData, updateData];
    console.info(result, data);// tslint:disable-line
    return new QueryData(result, data);
  }

  public delete(): string {
    const result: string = `delete from ${this.alias} using ${this.model.getTableName()} as ${this.alias} ${this.condition ? `where ${this.condition.build()}` : ""}`;
    console.info(result);// tslint:disable-line
    return result;
  }

  private filterFieldForUpdate(field: FieldModel<any>): boolean {
    return !field.id;
  }

  private filterFieldForInsert(field: FieldModel<any>): boolean {
    return !field.autoGenerated;
  }
}

class Join<M extends BaseModel<M>, F extends BaseModel<F>, T extends BaseModel<T>> {
  private fromField: FieldModel<any>;
  private toField: FieldModel<any>;
  private readonly alias: string;
  private joinType: string = "inner join";

  constructor(private query: Query<M>
    , private readonly fromModel: F
    , private to: T
    , fieldFrom: (fromModel: F) => FieldModel<any>
    , toField: (to: T) => FieldModel<any>
    , private fromAlias?: string,
  ) {
    this.fromField = fieldFrom(this.fromModel);
    this.toField = toField(this.to);
    this.alias = `alias_${this.query.getNextAlias()}`;
  }

  public getAlias(): string {
    return this.alias;
  }

  public left(): Join<M, F, T> {
    const self = <Join<M, F, T>>this;
    self.joinType = "left join";
    return self;
  }

  public right(): Join<M, F, T> {
    const self = <Join<M, F, T>>this;
    self.joinType = "right join";
    return self;
  }

  public full(): Join<M, F, T> {
    const self = <Join<M, F, T>>this;
    self.joinType = "full join";
    return self;
  }

  public join<A extends BaseModel<A>>(anotherTo: any, fromField: (to: any) => FieldModel<any>, toField: (anotherTo: any) => FieldModel<any>): Join<M, T, A> {
    return new Join(this.query, this.to, anotherTo, fromField, toField, this.alias);
  }

  public fieldCondition(getField: (to: T) => FieldModel<any>, condition: string): FieldCondition {
    return new FieldCondition(getField(this.to), condition, this.alias);
  }

  public build(): string {
    return ` ${this.joinType} ${this.to.getTableName()} as ${this.alias} on ${this.fromAlias}.${this.fromField.getFieldName()} = ${this.alias}.${this.toField.getFieldName()}`;
  }

  public order(getField: (model: T) => FieldModel<any>, asc: boolean = true): Join<M, F, T> {
    this.query.orderField(getField(this.to), asc, this.alias);
    return this;
  }
}

class Order<T extends BaseModel<T>> {
  constructor(
    private readonly field: FieldModel<any>,
    private readonly alias: string,
    private readonly asc: boolean = true,
  ) {
  }

  public build(): string {
    return ` (${this.alias ? this.alias + "." : ""}${this.field.getFieldName()}) ${this.asc ? "asc" : "desc"}`;
  }
}

declare interface ICondition {
  build(): string;
}

class FieldCondition implements ICondition {
  constructor(private field: FieldModel<any>, private condition: string, private alias: string) {
  }

  public build(): string {
    return ` (${this.alias ? `${this.alias}.` : ""}${this.field.getFieldName()} ${this.condition}) `;
  }
}

class AndCondition implements ICondition {
  protected conditions: ICondition[] = [];

  public add(field: FieldModel<any>, condition: string, alias: string): AndCondition {
    return this.addCondition(new FieldCondition(field, condition, alias));
  }

  public addCondition(condition: ICondition): AndCondition {
    this.conditions.push(condition);
    return this;
  }

  public build(): string {
    return ` (${this.conditions.map((con: ICondition) => con.build()).join(" and ")}) `;
  }
}

class OrCondition extends AndCondition {
  public build(): string {
    return ` (${this.conditions.map((con: ICondition) => con.build()).join(" or ")}) `;
  }
}

abstract class DefaultBaseModel<T> extends BaseModel<T> {
  public createdBy: FieldModel<number> = new FieldModel<number>("created_by", this.getRow);
  public updatedBy: FieldModel<number> = new FieldModel<number>("updated_by", this.getRow);
  public createdAt: FieldModel<Date> = new FieldModel<Date>("created_at", this.getRow);
  public updatedAt: FieldModel<Date> = new FieldModel<Date>("updated_at", this.getRow);

  protected constructor(row: any) {
    super(row);
  }
}

export {
  BaseModel,
  Query,
  AndCondition,
  ICondition,
  FieldCondition,
  OrCondition,
  DefaultBaseModel,
  Order,
};