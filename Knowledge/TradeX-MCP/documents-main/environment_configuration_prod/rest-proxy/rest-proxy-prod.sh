#!/usr/bin/env bash


export TRADEX_ENV_ENABLE_ENCRYPT_PASSWORD=true
export TRADEX_ENV_DOMAIN=tradex

export TRADEX_WORKING_DIR=/app
mkdir -p $TRADEX_WORKING_DIR/keys/aaa/tradex/
export PORT=3001
if [[ $TRADEX_ENV_INSTANCE_ID != "" ]]; then
  export PORT=$((3000 + $TRADEX_ENV_INSTANCE_ID))
fi
tmp=$(cat << EndOfMessage

  conf.enableEncryptPassword = true;
  conf.enableDebug = false;
  conf.key = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbnYiOiJwcm9kIiwiaWF0IjoxNzQ4OTEzODUzLCJleHAiOjE3NTQwOTc4NTMsImlzcyI6IkRpZmlzb2Z0Iiwic3ViIjoicmVzdC1wcm94eSJ9.MmjucLf6tKUE_4zug4BnWl4d-Q389JRL7fyc9TiSsHr0RlFpfBQczNmfcgHeo9XxNQcOcXk2TugLr-STQBj3TzEYQvVGo90slvYH1btklochKew_clwV0DALWNpMioAZBZZ83kvfB5R3vDCQVftSyt9M7hwPG_fob91CuY7ss03NDUqU25uOMUejELrAZeu-UpHyigOFMkTo85X6XW-4mT1h2GV_azQxH8PvwlCe6w9Vs4By_LMNR7ha_vbYSrpoc24KhBuWSYCMb-Fbg-pcMdEPAO2w2jLEK4cJa7_uf5fxCNllHQMaGbZ2TH7fJGdTz1HBzKuEDvShbWbDp22rpA";
  conf.port = $PORT;
  conf.encryptPassword = {
    "/post/api/v1/login": ["password"],
    "/put/api/v1/equity/account/changePassword": ["oldPassword", "newPassword"],
    "/put/api/v1/lotte/equity/account/changePassword": ["oldPassword", "newPassword"],
    "/put/api/v1/lotte/equity/account/changePin": ["oldPassword","newPassword"],
    "/post/api/v1/lotte/account/resetPassword": ["password"],
    "/post/api/v1/account/resetPassword": ["password"],
    "/put/api/v1/equity/account/changePin": ["oldPassword","newPassword"]
  };
  conf.forwards = [
    {
      "pattern": "/nhsv-api/",
      "type": "http",
      "target": "http://172.33.30.51:3001",
      "pathRewrite": {
        "^/nhsv-api/": "/"
      },
      "changeOrigin": true,
      "secure": false
    }
  ];

EndOfMessage
)

echo "$tmp" > $TRADEX_WORKING_DIR/env.js

echo "$TRADEX_ENV_JWT_PUBLIC_KEY" > $TRADEX_WORKING_DIR/keys/aaa/tradex/jwt-public.key
echo "$TRADEX_ENV_PASSWORD_RSA_PUBLIC_KEY" > $TRADEX_WORKING_DIR/keys/aaa/tradex/rsa-public.key