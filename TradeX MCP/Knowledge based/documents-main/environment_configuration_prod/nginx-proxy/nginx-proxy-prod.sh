#!/usr/bin/env bash
resouce_dir=services/deployment-resources/nginx-proxy-prod
if [[ -d $resouce_dir ]]; then
  mkdir -p /data/ssl
  mkdir -p /etc/nginx/sites-available/
  mkdir -p /etc/nginx/sites-enabled/
  rm -Rf /etc/nginx/sites-available/*
  rm -Rf /etc/nginx/sites-enabled/*
  rm -f /etc/nginx/nginx.conf

  cp $resouce_dir/bundle.crt /data/ssl
  cp $resouce_dir/private.key /data/ssl
  cp $resouce_dir/certreq.csr /data/ssl
  cp $resouce_dir/nginx.conf /etc/nginx/
  cp $resouce_dir/proxy.conf /etc/nginx/sites-available/default

  sed -i "s|#{localIp}|${localIp}|g" /etc/nginx/sites-available/default
  sed -i "s|#{TRADEX_ENV_NODE1}|${TRADEX_ENV_NODE1}|g" /etc/nginx/sites-available/default
  sed -i "s|#{TRADEX_ENV_NODE2}|${TRADEX_ENV_NODE2}|g" /etc/nginx/sites-available/default
  sed -i "s|#{TRADEX_ENV_NODE3}|${TRADEX_ENV_NODE3}|g" /etc/nginx/sites-available/default
  sed -i "s|#{TRADEX_ENV_NSHV_ADMIN_PORT}|${TRADEX_ENV_NSHV_ADMIN_PORT}|g" /etc/nginx/sites-available/default
  sed -i "s|#{TRADEX_ENV_EKYC_ADMIN_PORT}|${TRADEX_ENV_EKYC_ADMIN_PORT}|g" /etc/nginx/sites-available/default
  cp /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default
fi