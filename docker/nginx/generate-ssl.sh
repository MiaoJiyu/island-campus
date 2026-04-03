#!/bin/bash
# 生成自签名SSL证书（开发环境使用，生产环境请替换为Let's Encrypt）
mkdir -p /etc/nginx/ssl
openssl req -x509 -nodes -days 3650 \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=IslandCampus/CN=localhost" \
    -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/key.pem \
    -out /etc/nginx/ssl/cert.pem
chmod 600 /etc/nginx/ssl/key.pem
echo "SSL certificates generated at /etc/nginx/ssl/"
