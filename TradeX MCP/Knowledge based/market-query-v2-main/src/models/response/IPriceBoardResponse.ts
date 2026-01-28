export default interface IPriceBoardResponse {
  s?: string[]; // symbol code
  t?: string[]; // type: "INDEX" | "STOCK" | "FUTURES" | "CW";
  o?: number[]; // open
  h?: number[]; // high
  l?: number[]; // low
  c?: number[]; // close
  a?: number[]; // average price
  ep?: number[]; // expected price
  exc?: number[]; // expected change
  exr?: number[]; // expected rate
  exv?: number[]; // expected volume
  ch?: number[]; // change
  ra?: number[]; // rate
  vo?: number[]; // trading volume
  va?: number[]; // trading value
  mv?: number[]; // match volume
  mb?: string[]; // match by "CEILING" | "FLOOR" | "";
  ss?: string[]; // session
  tb?: number[]; // total Bid Volume
  to?: number[]; // total Offer Volume
  bb?: IBid[][]; // best bid
  bo?: IAsk[][]; // best offer
  ic?: IIndexChange[]; // index change
  fr?: IForeigner[]; //foreigner
  be?: number[]; // break even
  pe?: number[]; // % premium
  oi?: number[]; // open interest
  ba?: number[]; // basis
  exp?: number[]; // exercise price
}

export interface IAsk {
  p?: number; // price
  v?: number; // volume
  c?: number; // volume change
}

export interface IBid {
  p?: number; // price
  v?: number; // volume
  c?: number; // volume change
}

export interface IIndexChange {
  ce?: number; // ceiling count
  fl?: number; // floor count
  up?: number; // up count
  dw?: number; // down count
  uc?: number; // unChange count
  tc?: number; // trade count
  utc?: number; // unTrade count
}

export interface IForeigner {
  bv?: number; // buy volume
  sv?: number; // sell volume
  tr?: number; // total room
  cr?: number; // current room
  nva?: number; // net buy value
  nvo?: number; // net buy volume
}
