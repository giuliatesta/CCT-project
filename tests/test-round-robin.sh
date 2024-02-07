#!/usr/bin/env bash

GREEN="\033[0;32m"
BLUE="\033[0;34m"
NC="\033[0m" 


# ADMIN_JWT and USER_JWT are created only containing "role" and have a long expiration date (15/02) to serve the testing purposes
ADMIN_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDczMjcwNDEsImV4cCI6MTczOTY0MDY0MSwiYXVkIjoiIiwic3ViIjoiIiwicm9sZSI6ImFkbWluIn0.057sRk479Vipy6KFqy_vrPxRNGzWV3TLb2nbgvNMR60"
USER_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDcxNTQ2NzgsImV4cCI6MTczOTY0MTA3OCwiYXVkIjoiIiwic3ViIjoiIiwiUm9sZSI6InVzZXIifQ.0iky-9QTxMaq2njnh25oYY67ByARunrlrFSyK4mLzY0"
INVALID_JWT="iJodHRwYmluMiIsImlhdCI6MTcwNjc5OTY5NywiZXhwIjoxNzA3NjYzNjk3LCJyb2xlIjoiYWRtaW4iLCJwZXJtaXNzaW9ucyI6WyJyZWFkIiwid3JpdGUiXX0.KYrKIX9dobREn479XkExxq42aFKEkP0S6NsopQc1jCY"

# The available hosts for /users are http://users1:80,http://users2:80. Since I have implemented round robin for choosing the host, 
# I make three requests at the same microservice. I expect that the first time, the microservice will be called using users2, then users1 and once again users2. 
echo "${GREEN}2. Round Robin${NC}"
echo "${BLUE}A microservice is called three times. Gateway uses all available hosts using round robin.${NC}" 
echo "${BLUE}NB: since round robin for two hosts means alternating, the output will be users1, users2, users1 or users2, users1, users2.${NC}" 
echo "${BLUE}First call: http://localhost:8080/users -> http://users2:80/json${NC}" 
curl --location "http://localhost:8080/users" \
--header "Authorization: bearer $ADMIN_JWT"
echo "${BLUE}Second call: http://localhost:8080/users/get -> http://users1:80/get${NC}"
curl --location "http://localhost:8080/users/get" \
--header "Authorization: bearer $ADMIN_JWT"
echo "${BLUE}Third call: http://localhost:8080/users/echo -> http://users2:80/anything${NC}"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"