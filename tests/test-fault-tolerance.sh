#!/usr/bin/env bash

GREEN="\033[0;32m"
BLUE="\033[0;34m"
NC="\033[0m" 


# ADMIN_JWT and USER_JWT are created only containing "role" and have a long expiration date (15/02) to serve the testing purposes
ADMIN_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDczMjcwNDEsImV4cCI6MTczOTY0MDY0MSwiYXVkIjoiIiwic3ViIjoiIiwicm9sZSI6ImFkbWluIn0.057sRk479Vipy6KFqy_vrPxRNGzWV3TLb2nbgvNMR60"
USER_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDcxNTQ2NzgsImV4cCI6MTczOTY0MTA3OCwiYXVkIjoiIiwic3ViIjoiIiwiUm9sZSI6InVzZXIifQ.0iky-9QTxMaq2njnh25oYY67ByARunrlrFSyK4mLzY0"
INVALID_JWT="iJodHRwYmluMiIsImlhdCI6MTcwNjc5OTY5NywiZXhwIjoxNzA3NjYzNjk3LCJyb2xlIjoiYWRtaW4iLCJwZXJtaXNzaW9ucyI6WyJyZWFkIiwid3JpdGUiXX0.KYrKIX9dobREn479XkExxq42aFKEkP0S6NsopQc1jCY"

echo "${GREEN}5. Fault tolerance ${NC}" 
echo "\n${BLUE}Shutting down service users1.${NC}"
docker compose down users1
sleep 2

echo "${BLUE}If an host fails, the gateway will put it in time out.${NC}"
echo "${BLUE}If an host is in time out, it's ignored and gateway tries to use it again after 30 seconds${NC}" #  short time interval to serve the testing purpose

echo "${BLUE}Calling microservice users. It will respond users2, even if it's users1 turn, but it's still down. The gateway tries 5 times to make the call to users1 and then switches to users2.${NC}"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"
echo "${BLUE}All the request to users will be evaluated by users2. After 30 seconds, the gateway tries to use again users1.${NC}"

countdown() {
  local start_time=$SECONDS
  local duration=$1

  while [ $duration -gt 0 ]; do
    local elapsed_time=$((SECONDS - start_time))
    tput cuu1  # Move cursor up one line
    tput el    # Clear the line
    echo -ne "${BLUE}Waiting for $duration seconds...${NC}"
    sleep 1
    ((duration--))
  done

  printf "\n"
}
# Display countdown with elapsed time while waiting for 30 seconds
countdown 30

echo "${BLUE}Users1 still down, the gateway tries users1 but users2 will respond.${NC}"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"

echo "${BLUE}Restarting service users1...${NC}"
docker compose up -d users1
sleep 5
docker compose logs users1

echo "${BLUE}The gateway tries users1 again and this time works.${NC}"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"
