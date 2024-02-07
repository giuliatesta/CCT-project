#!/usr/bin/env bash

GREEN="\033[0;32m"
BLUE="\033[0;34m"
NC="\033[0m" 


# ADMIN_JWT and USER_JWT are created only containing "role" and have a long expiration date (15/02) to serve the testing purposes
ADMIN_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDczMjcwNDEsImV4cCI6MTczOTY0MDY0MSwiYXVkIjoiIiwic3ViIjoiIiwicm9sZSI6ImFkbWluIn0.057sRk479Vipy6KFqy_vrPxRNGzWV3TLb2nbgvNMR60"
USER_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDcxNTQ2NzgsImV4cCI6MTczOTY0MTA3OCwiYXVkIjoiIiwic3ViIjoiIiwiUm9sZSI6InVzZXIifQ.0iky-9QTxMaq2njnh25oYY67ByARunrlrFSyK4mLzY0"
INVALID_JWT="iJodHRwYmluMiIsImlhdCI6MTcwNjc5OTY5NywiZXhwIjoxNzA3NjYzNjk3LCJyb2xlIjoiYWRtaW4iLCJwZXJtaXNzaW9ucyI6WyJyZWFkIiwid3JpdGUiXX0.KYrKIX9dobREn479XkExxq42aFKEkP0S6NsopQc1jCY"
echo "${GREEN}5. Rate limiting ${NC}" 
echo "${BLUE}The gateway implements the token bucket algorithm, with a capacity equals to 10 (for testing purposes)${NC}"
echo "\n${BLUE}${NC}"

counter=0
while [ $counter -lt 15 ]
do
    curl --location "http://localhost:8080/resources" \
--header "Authorization: bearer $ADMIN_JWT"
    ((counter++))
done


