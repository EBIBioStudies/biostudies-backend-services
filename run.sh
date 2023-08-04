docker run -p 21:21 -p 30000:30000 \
  --platform linux/amd64 \
  -e FTP_USER_NAME="ftpUser" \
  -e FTP_USER_PASS="ftpPassword" \
  -e ADDED_FLAGS="--tls=1" \
  -e FTP_PASSIVE_PORTS="30000:30000" \
  -e PUBLICHOST="0.0.0.0" \
  -e FTP_USER_HOME="/home/ftpUser" \
  -e TLS_CN="localhost" \
  -e TLS_ORG="YourOrg" \
  -e TLS_C="DE" \
  stilliard/pure-ftpd


#docker run -p 21:21 -p 30000-30009:30000-30009 \
#  --platform linux/amd64 \
#  -e FTP_USER_NAME="ftpUser" \
#  -e FTP_USER_PASS="ftpPassword" \
#  -e FTP_PASSIVE_PORTS="30000:30009" \
#  -e ADDED_FLAGS="--tls=2" \
#  -e PUBLICHOST="0.0.0.0" \
#  -e TLS_CN="localhost" \
#  -e TLS_ORG="YourOrg" \
#  -e TLS_C="DE" \
#  stilliard/pure-ftpd
