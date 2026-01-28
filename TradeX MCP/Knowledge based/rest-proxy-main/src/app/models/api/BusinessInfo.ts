/**
 * @swagger
 * components:
 *   schemas:
 *     BusinessInfoResponse:
 *       type: object
 *       description: the list of data
 *       oneOf:
 *         - $ref: '#/components/schemas/BusinessInfoBankResponse'
 *         - $ref: '#/components/schemas/BusinessInfoCompanyResponse'
 *         - $ref: '#/components/schemas/BusinessInfoInsuranceResponse'
 *         - $ref: '#/components/schemas/BusinessInfoSecuritiesResponse'
 *     BusinessInfoBankResponse:
 *       type: object
 *       properties:
 *         type:
 *           type: string
 *           description: the type of company
 *           enum:
 *             - BANK
 *         code:
 *           type: string
 *           description: code of bank
 *         quarter:
 *           type: number
 *           format: int32
 *           description: quarter of year
 *           example: 1-4
 *         year:
 *           type: number
 *           format: int32
 *         netInterestIncome:
 *           type: number
 *           format: double
 *         incomeFromServiceActivities:
 *           type: number
 *           format: double
 *         operatingProfitBeforeProvisionForCreditLoss:
 *           type: number
 *           format: double
 *         eps:
 *           type: number
 *           format: double
 *         cash:
 *           type: number
 *           format: double
 *         depositAtSbvAndOtherCreditInstitutions:
 *           type: number
 *           format: double
 *         loansToCustomers:
 *           type: number
 *           format: double
 *         securityDealingAndInvestment:
 *           type: number
 *           format: double
 *         jointVentureAndLongTermInvestment:
 *           type: number
 *           format: double
 *         fixedAsset:
 *           type: number
 *           format: double
 *         totalAssets:
 *           type: number
 *           format: double
 *         totalEquity:
 *           type: number
 *           format: double
 *         depositsAndLoansAtStateBankAndOtherCreditInstitutions:
 *           type: number
 *           format: double
 *         customerDeposits:
 *           type: number
 *           format: double
 *         valuablePapersInsuance:
 *           type: number
 *           format: double
 *         otherDebts:
 *           type: number
 *           format: double
 *         capitalOfCreditInstitutions:
 *           type: number
 *           format: double
 *         quartersAndOthers:
 *           type: number
 *           format: double
 *         totalLiabilitiesAndEquity:
 *           type: number
 *           format: double
 *         netCashFlowFromOperatingActivities:
 *           type: number
 *           format: double
 *         netCashFlowFromInvestingActivities:
 *           type: number
 *           format: double
 *         netCashFlowFromFinancingActivities:
 *           type: number
 *           format: double
 *         netIncreaseInCashAndCashEquivalents:
 *           type: number
 *           format: double
 *         cashAndCashEquivalentsAtTheEndOfPeriod:
 *           type: number
 *           format: double
 *         dividend:
 *           type: number
 *           format: double
 *         numberOfShares:
 *           type: number
 *           format: int32
 *         price:
 *           type: number
 *           format: double
 *         marketCapital:
 *           type: number
 *           format: double
 *         per:
 *           type: number
 *           format: double
 *         pbr:
 *           type: number
 *           format: double
 *         dividendYield:
 *           type: number
 *           format: double
 *         roe:
 *           type: number
 *           format: double
 *     BusinessInfoCompanyResponse:
 *       type: object
 *       properties:
 *         type:
 *           type: string
 *           description: the type of company
 *           enum:
 *             - COMPANY
 *         code:
 *           type: string
 *           description: code of company
 *         quarter:
 *           type: number
 *           format: int32
 *           description: quarter of year. Cannot get if quarter equal "5"
 *           example: 1-4. Cannot get if quarter equal 5
 *         year:
 *           type: number
 *           format: int32
 *         revenue:
 *           type: number
 *           format: double
 *         grossProfit:
 *           type: number
 *           format: double
 *         netProfitForAYear:
 *           type: number
 *           format: double
 *         eps:
 *           type: number
 *           format: double
 *         cash:
 *           type: number
 *           format: double
 *         inventories:
 *           type: number
 *           format: double
 *         accountReceivable:
 *           type: number
 *           format: double
 *         fixedAssets:
 *           type: number
 *           format: double
 *         otherAssets:
 *           type: number
 *           format: double
 *         totalAssets:
 *           type: number
 *           format: double
 *         equity:
 *           type: number
 *           format: double
 *         currentLiabilities:
 *           type: number
 *           format: double
 *         longTermLiabilities:
 *           type: number
 *           format: double
 *         otherLongTermPayable:
 *           type: number
 *           format: double
 *         equityOfShareHolders:
 *           type: number
 *           format: double
 *         charterCapital:
 *           type: number
 *           format: double
 *         totalResources:
 *           type: number
 *           format: double
 *         netCashFromOperatingActivities:
 *           type: number
 *           format: double
 *         netCashFromInvestingActivities:
 *           type: number
 *           format: double
 *         netCashFromFinancingActivities:
 *           type: number
 *           format: double
 *         netCashFlowDuringPeriod:
 *           type: number
 *           format: double
 *         cashAndCashEquivalentsAtTheEndOfYear:
 *           type: number
 *           format: double
 *         dividend:
 *           type: number
 *           format: double
 *         numberOfShares:
 *           type: number
 *           format: int32
 *         price:
 *           type: number
 *           format: double
 *         marketCapital:
 *           type: number
 *           format: double
 *         per:
 *           type: number
 *           format: double
 *         pbr:
 *           type: number
 *           format: double
 *         dividendYield:
 *           type: number
 *           format: double
 *         roe:
 *           type: number
 *           format: double
 *     BusinessInfoInsuranceResponse:
 *       type: object
 *       properties:
 *         type:
 *           type: string
 *           description: the type of company
 *           enum:
 *             - INSURANCE
 *         code:
 *           type: string
 *           description: code of company
 *         quarter:
 *           type: number
 *           format: int32
 *           description: quarter of year. Cannot get if quarter equal "5"
 *           example: 1-4. Cannot get if quarter equal 5
 *         year:
 *           type: number
 *           format: int32
 *         revenue:
 *           type: number
 *           format: double
 *         grossProfit:
 *           type: number
 *           format: double
 *         netProfitAfterTax:
 *           type: number
 *           format: double
 *         eps:
 *           type: number
 *           format: double
 *         cashAndCashEquivalents:
 *           type: number
 *           format: double
 *         inventories:
 *           type: number
 *           format: double
 *         accountReceivable:
 *           type: number
 *           format: double
 *         fixedAssets:
 *           type: number
 *           format: double
 *         otherLongTermAssets:
 *           type: number
 *           format: double
 *         totalAssets:
 *           type: number
 *           format: double
 *         equity:
 *           type: number
 *           format: double
 *         currentLiabilities:
 *           type: number
 *           format: double
 *         longTermLiabilities:
 *           type: number
 *           format: double
 *         otherLongTermPayables:
 *           type: number
 *           format: double
 *         equityOfShareholders:
 *           type: number
 *           format: double
 *         charterCapital:
 *           type: number
 *           format: double
 *         totalResources:
 *           type: number
 *           format: double
 *         netCashFromOperatingActivities:
 *           type: number
 *           format: double
 *         netCashFromInvestingActivities:
 *           type: number
 *           format: double
 *         netCashFromFinancingActivities:
 *           type: number
 *           format: double
 *         netCashFlowDuringPeriod:
 *           type: number
 *           format: double
 *         cashAndCashEquivalentsEnding:
 *           type: number
 *           format: double
 *         dividend:
 *           type: number
 *           format: double
 *         numberOfShares:
 *           type: number
 *           format: int32
 *         price:
 *           type: number
 *           format: double
 *         marketCapital:
 *           type: number
 *           format: double
 *         per:
 *           type: number
 *           format: double
 *         pbr:
 *           type: number
 *           format: double
 *         dividendYield:
 *           type: number
 *           format: double
 *         roe:
 *           type: number
 *           format: double
 *     BusinessInfoSecuritiesResponse:
 *       type: object
 *       properties:
 *         type:
 *           type: string
 *           description: the type of company
 *           enum:
 *             - SECURITIES
 *         code:
 *           type: string
 *           description: code of company
 *         quarter:
 *           type: number
 *           format: int32
 *           description: quarter of year. Cannot get if quarter equal "5"
 *           example: 1-4. Cannot get if quarter equal 5
 *         year:
 *           type: number
 *           format: int32
 *         revenue:
 *           type: number
 *           format: double
 *         grossProfit:
 *           type: number
 *           format: double
 *         netProfitForTheYear:
 *           type: number
 *           format: double
 *         eps:
 *           type: number
 *           format: double
 *         shortTermAssets:
 *           type: number
 *           format: double
 *         longTermAssets:
 *           type: number
 *           format: double
 *         totalAssets:
 *           type: number
 *           format: double
 *         equity:
 *           type: number
 *           format: double
 *         currentLiabilities:
 *           type: number
 *           format: double
 *         otherLongTermPayables:
 *           type: number
 *           format: double
 *         equityOfShareholders:
 *           type: number
 *           format: double
 *         totalResources:
 *           type: number
 *           format: double
 *         netCashFromOperatingActivities:
 *           type: number
 *           format: double
 *         netCashFromInvestingActivities:
 *           type: number
 *           format: double
 *         netCashFromFinancingActivities:
 *           type: number
 *           format: double
 *         netCashFlowDuringPeriod:
 *           type: number
 *           format: double
 *         cashAndCashEquivalentsAtTheEndOfYear:
 *           type: number
 *           format: double
 *         dividend:
 *           type: number
 *           format: double
 *         numberOfShares:
 *           type: number
 *           format: int32
 *         price:
 *           type: number
 *           format: double
 *         marketCapital:
 *           type: number
 *           format: double
 *         per:
 *           type: number
 *           format: double
 *         pbr:
 *           type: number
 *           format: double
 *         dividendYield:
 *           type: number
 *           format: double
 *         roe:
 *           type: number
 *           format: double
 */
