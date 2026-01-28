/**
 * @swagger
 * components:
 *   schemas:
 *     DerivativesCashTransferInfoResponse:
 *       type: object
 *       properties:
 *         depositAmount:
 *           type: number
 *           format: double
 *           description: the cash balance of account
 *         waitingAmount:
 *           type: number
 *           format: double
 *           description: the wait amount for withdraw of account
 *         transferableAmount:
 *           type: number
 *           format: double
 *           description: the deposit balance amount that is transferable
 *     DerivativesCashTransferRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number used to transfer cash
 *         subNumber:
 *           type: string
 *           description: the sub number used to transfer cash
 *         receivingAccountNumber:
 *           type: string
 *           description: the receiving account number
 *         amount:
 *           type: number
 *           format: double
 *           description: the transfer amount
 *         note:
 *           type: string
 *           description: the transfer note
 *         bankCode:
 *           type: string
 *           description: the bank code used to transfer, default is '9999'
 *     DerivativesCashTransferResponse:
 *       type: object
 *       properties:
 *         transactionDate:
 *           type: string
 *           description: the transaction date of the transfer, format is 'yyyyMMdd'
 *         outSequenceNumber:
 *           type: string
 *           description: the sequence number of the transaction for the transfering account
 *         outPreviousCashBalance:
 *           type: number
 *           format: double
 *           description: the previous cash balance amount of transfering account
 *         outCashBalance:
 *           type: number
 *           format: double
 *           description: the current cash balance amount of transfering account
 *         inSequenceNumber:
 *           type: string
 *           description: the sequence number of the transaction for the receiving account
 *         inPreviousCashBalance:
 *           type: number
 *           format: double
 *           description: the previous cash balance amount of receiving account
 *         inCashBalance:
 *           type: number
 *           format: double
 *           description: the current cash balance amount of receiving account
 *     DerivativesCashWithdrawInfoResponse:
 *       type: object
 *       properties:
 *         depositAmount:
 *           type: number
 *           format: double
 *           description: the cash balance of account
 *         totalBlockAmount:
 *           type: number
 *           format: double
 *           description: the total block amount of account
 *         waitingAmount:
 *           type: number
 *           format: double
 *           description: the wait amount for withdraw of account
 *         withdrawableAmount:
 *           type: number
 *           format: double
 *           description: the deposit balance amount that is withdrawable
 *         depositBlockAmount:
 *           type: number
 *           format: double
 *           description: Block amount for deposit
 *         fillingLossBlockAmount:
 *           type: number
 *           format: double
 *           description: Block amount for filling loss
 *         maturityPaymentBlockAmount:
 *           type: number
 *           format: double
 *           description: Block amount for maturity payment
 *     DerivativesCashWithdrawRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number used to withdraw cash
 *         amount:
 *           type: number
 *           format: double
 *           description: the withdraw amount
 *         note:
 *           type: string
 *           description: the withdraw note
 *         bankAccountNumber:
 *           type: string
 *           description: the bank account number to transfer cash
 *         beneficiaryBankAccountNumber:
 *           type: string
 *           description: the bank account number to receive cash
 *         beneficiaryBankAccountName:
 *           type: string
 *           description: the bank account name to receive cash
 *         beneficiaryBankBranch:
 *           type: string
 *           description: the bank branch to receive cash
 *     DerivativesCashWithdrawResponse:
 *       type: object
 *       properties:
 *         transactionDate:
 *           type: string
 *           description: the transaction date of the transfer, format is 'yyyyMMdd'
 *         sequenceNumber:
 *           type: string
 *           description: the sequence number of the transaction
 *         previousCashBalance:
 *           type: number
 *           format: double
 *           description: the previous cash balance amount
 *         cashBalance:
 *           type: number
 *           format: double
 *           description: the current cash balance amount
 *         fee:
 *           type: number
 *           format: double
 *           description: the fee of the transaction
 *         receivedCash:
 *           type: number
 *           format: double
 *           description: the received amount
 *     DerivativesTransferBankResponse:
 *       type: object
 *       properties:
 *         bankAccountNumber:
 *           type: string
 *           description: the bank account number
 *         bankAccountName:
 *           type: string
 *           description: the bank account name
 *     DerivativesTransferFeeResponse:
 *       type: object
 *       properties:
 *         feeAmount:
 *           type: number
 *           format: double
 *           description: the fee amount
 *         adjustedAmount:
 *           type: number
 *           format: double
 *           description: the adjusted amount
 *         receivedAmount:
 *           type: number
 *           format: double
 *           description: the received amount after adjustment
 *         feeType:
 *           type: string
 *           description: the fee type
 *     DerivativesIMDepositInfoResponse:
 *       type: object
 *       properties:
 *         depositAmount:
 *           type: number
 *           format: double
 *           description: the cash balance of account
 *         otherBlockAmount:
 *           type: number
 *           format: double
 *           description: the other block amount of account
 *         collateralAmount:
 *           type: number
 *           format: double
 *           description: the collateral amount of account
 *         withdrawBlockAmount:
 *           type: number
 *           format: double
 *           description: the block amount for withdraw of account
 *         depositBlockAmount:
 *           type: number
 *           format: double
 *           description: the block amount for deposit of account
 *         availableAmount:
 *           type: number
 *           format: double
 *           description: the available amount for deposit of account
 *         settleBlockAmount:
 *           type: number
 *           format: double
 *           description: the block amount for settlement of account
 *         maturityBlockAmount:
 *           type: number
 *           format: double
 *           description: the block amount for maturity payment of account
 *     DerivativesIMDepositRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number used to deposit IM
 *         amount:
 *           type: number
 *           format: double
 *           description: the deposit amount
 *         note:
 *           type: string
 *           description: the deposit note
 *         sourceBank:
 *           type: string
 *           description: the source bank to deposit IM
 *         destBank:
 *           type: string
 *           description: the destination bank to deposit IM
 *         feeAmount:
 *           type: number
 *           format: double
 *           description: the fee amount
 *         adjustedAmount:
 *           type: number
 *           format: double
 *           description: the adjusted amount
 *         receivedAmount:
 *           type: number
 *           format: double
 *           description: the received amount after adjustment
 *         feeType:
 *           type: string
 *           description: the fee type
 *     DerivativesIMWithdrawInfoResponse:
 *       type: object
 *       properties:
 *         depositAmount:
 *           type: number
 *           format: double
 *           description: the cash balance of account
 *         otherBlockAmount:
 *           type: number
 *           format: double
 *           description: the other block amount of account
 *         collateralAmount:
 *           type: number
 *           format: double
 *           description: the collateral amount of account
 *         withdrawBlockAmount:
 *           type: number
 *           format: double
 *           description: the block amount for withdraw of account
 *         depositBlockAmount:
 *           type: number
 *           format: double
 *           description: the block amount for deposit of account
 *         availableAmount:
 *           type: number
 *           format: double
 *           description: the available amount for deposit of account
 *         settleBlockAmount:
 *           type: number
 *           format: double
 *           description: the block amount for settlement of account
 *     DerivativesIMWithdrawRequest:
 *       type: object
 *       properties:
 *         accountNumber:
 *           type: string
 *           description: the account number used to withdraw IM
 *         amount:
 *           type: number
 *           format: double
 *           description: the wihthdraw amount
 *         note:
 *           type: string
 *           description: the withdraw note
 *         sourceBank:
 *           type: string
 *           description: the source bank to withdraw IM
 *         destBank:
 *           type: string
 *           description: the destination bank to withdraw IM
 *         feeAmount:
 *           type: string
 *           description: the fee amount
 *         adjustedAmount:
 *           type: string
 *           description: the adjusted amount
 *         receivedAmount:
 *           type: string
 *           description: the received amount after adjustment
 *         feeType:
 *           type: string
 *           description: the fee type
 *     DerivativesTransferIMResponse:
 *       type: object
 *       properties:
 *         sequenceNumber:
 *           type: string
 *           description: the sequence number of the transaction
 *         transactionDate:
 *           type: string
 *           description: the transaction date, format is 'yyyyMMdd'
 *         transactionType:
 *           type: string
 *           description: the type of the transaction
 *         amount:
 *           type: number
 *           format: double
 *           description: the transaction amount
 *         receivedAmount:
 *           type: number
 *           format: double
 *           description: the received amount after adjustment
 *         feeAmount:
 *           type: number
 *           format: double
 *           description: the fee amount
 *         note:
 *           type: string
 *           description: the withdraw note
 *         isCancel:
 *           type: boolean
 *           description: the transaction is cancelled or not
 *         sourceBank:
 *           type: string
 *           description: the source bank to withdraw IM
 *         destBank:
 *           type: string
 *           description: the destination bank to withdraw IM
 *         bankStatus:
 *           type: string
 *           description: the bank status of transaction
 *         bosStatus:
 *           type: string
 *           description: the BOS status of transaction
 *         vsdStatus:
 *           type: string
 *           description: the VSD status of transaction
 */
