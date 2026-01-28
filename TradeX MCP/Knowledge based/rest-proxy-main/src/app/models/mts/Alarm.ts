/**
 * @swagger
 * components:
 *   schemas:
 *     AlarmSettingRequest:
 *       type: object
 *       properties:
 *         code:
 *           type: string
 *           description: the stock/index code of the alarm setting
 *         currentValue:
 *           type: number
 *           format: double
 *           description: the current value of the symbol at the current moment
 *         value:
 *           type: number
 *           format: double
 *           description: the touch value of the alarm setting
 *         type:
 *           type: string
 *           enum:
 *             - STOCK
 *             - INDEX
 *             - FUTURES
 *           description: index or stock alarm
 *         option:
 *           type: string
 *           enum:
 *             - ONCE
 *             - MULTIPLE
 *           description: notify once then clear or continuously notify
 *         notificationMethod:
 *           type: string
 *           enum:
 *             - PUSH_NOTIFICATION
 *             - SMS
 *             - EMAIL
 *           description: the method to receive notification
 *     AlarmSettingUpdateRequest:
 *       type: object
 *       properties:
 *         currentValue:
 *           type: number
 *           format: double
 *           description: the current value of the symbol at the current moment
 *         value:
 *           type: number
 *           format: double
 *           description: the touch value of the alarm setting
 *         option:
 *           type: string
 *           enum:
 *             - ONCE
 *             - MULTIPLE
 *           description: notify once then clear or continuously notify
 *         notificationMethod:
 *           type: string
 *           enum:
 *             - PUSH_NOTIFICATION
 *             - SMS
 *             - EMAIL
 *           description: the method to receive notification
 *     AlarmSettingResponse:
 *       type: object
 *       properties:
 *         id:
 *           type: integer
 *           description: the id of the alarm setting
 *         code:
 *           type: string
 *           description: the stock/index code of the alarm setting
 *         value:
 *           type: number
 *           format: double
 *           description: the touch value of the alarm setting
 *         type:
 *           type: string
 *           enum:
 *             - STOCK
 *             - INDEX
 *             - FUTURES
 *           description: index or stock alarm
 *         option:
 *           type: string
 *           enum:
 *             - ONCE
 *             - MULTIPLE
 *           description: notify once then clear or continuously notify
 *         notificationMethod:
 *           type: string
 *           enum:
 *             - PUSH_NOTIFICATION
 *             - SMS
 *             - EMAIL
 *           description: the method to receive notification
 */
