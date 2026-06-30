/**
 * symbolQuoteResponse schema
 */
export interface SymbolQuoteResponse {
	/**
	 * time (yyyyMMddhhmmss)
	 */
	t?: string;
	/**
	 * open price
	 */
	o?: number;
	/**
	 * high price
	 */
	h?: number;
	/**
	 * low price
	 */
	l?: number;
	/**
	 * close price
	 */
	c?: number;
	/**
	 * change
	 */
	ch?: number;
	/**
	 * rate
	 */
	ra?: number;
	/**
	 * trading volume
	 */
	vo?: number;
	/**
	 * trading value
	 */
	va?: number;
	/**
	 * matching volume
	 */
	mv?: number;
	/**
	 * sequence
	 */
	se?: number;
	/**
	 * matched by
	 */
	mb?: string;
	/**
	 * ceiling floor equal
	 */
	cf?: string;
	/**
	 * total offer volume
	 */
	asv?: number;
	/**
	 * total bid volume
	 */
	abv?: number;
	[k: string]: any;
}