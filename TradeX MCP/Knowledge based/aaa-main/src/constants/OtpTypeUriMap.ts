export const OTP_TYPE_URI_MAP = {
  mas: {
    MATRIX_CARD: '/api/v1/services/eqt/authCardMatrix',
    SMART_OTP: '/api/v2/bridge/verifyOtp',
    HARDWARE_OTP: '/api/v2/bridge/verifyOtp',
    SMS_OTP: '/api/v2/bridge/verifySMSOTP',
    TOPIC: {
      MATRIX_CARD: 'mas-rest-bridge',
      SMART_OTP: 'mas-ws-bridge',
      HARDWARE_OTP: 'mas-ws-bridge',
      SMS_OTP: 'mas-ws-bridge'
    }
  },
  kis: {
    MATRIX_CARD: '/api/v1/auth/matrix/verifyKisCard',
    SMART_OTP: '/api/v1/otp/verify',
    TOPIC: {
      MATRIX_CARD: 'mas-rest-bridge',
      SMART_OTP: 'mas-ws-bridge',
    }
  }
};
