#!/usr/bin/env bash

GREEN="\033[0;32m"
BLUE="\033[0;34m"
NC="\033[0m" 


# ADMIN_JWT and USER_JWT are created only containing "role" and have a long expiration date (15/02) to serve the testing purposes
ADMIN_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDczMjcwNDEsImV4cCI6MTczOTY0MDY0MSwiYXVkIjoiIiwic3ViIjoiIiwicm9sZSI6ImFkbWluIn0.057sRk479Vipy6KFqy_vrPxRNGzWV3TLb2nbgvNMR60"
USER_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDcxNTQ2NzgsImV4cCI6MTczOTY0MTA3OCwiYXVkIjoiIiwic3ViIjoiIiwiUm9sZSI6InVzZXIifQ.0iky-9QTxMaq2njnh25oYY67ByARunrlrFSyK4mLzY0"
INVALID_JWT="iJodHRwYmluMiIsImlhdCI6MTcwNjc5OTY5NywiZXhwIjoxNzA3NjYzNjk3LCJyb2xlIjoiYWRtaW4iLCJwZXJtaXNzaW9ucyI6WyJyZWFkIiwid3JpdGUiXX0.KYrKIX9dobREn479XkExxq42aFKEkP0S6NsopQc1jCY"

echo "${GREEN}3. Authorization ${NC}" 
echo "${BLUE}Making a request without Authorization headers. Gateway should reject it.${NC}"
curl --location "http://localhost:8080/users"

echo "${BLUE}\nMaking a request with an incorret Authorization header. Gateway should reject it.${NC}"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $INVALID_JWT"
sleep 2

echo "\n"
echo "${GREEN}4. Authentication ${NC}" 
echo "${BLUE}Gateway accepts only POST requests with admin role in JWT.${NC}"
echo "${BLUE}It will reject a POST request made by a user.${NC}"
curl --location --request POST "http://localhost:8080/users/echo" \
--header "Authorization: bearer $USER_JWT"

echo "${BLUE}But, it will accept it if it is made by admin.${NC}"
curl --location --request POST "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"
