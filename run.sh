#!/usr/bin/env bash

GREEN="\033[0;32m"
BLUE="\033[0;34m"
NC="\033[0m" 


# ADMIN_JWT and INVALID_JWT are created only containing "role" and have a long expiration date (15/02) to serve the testing purpose
ADMIN_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDcxNTQ2NzgsImV4cCI6MTczOTY0MTA3OCwiYXVkIjoiIiwic3ViIjoiIiwiUm9sZSI6ImFkbWluIn0.g0h8BpsTv4HQy81sYWwOTgiOmqnqXjGXSiU2hNtVp10"
USER_JWT="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDcxNTQ2NzgsImV4cCI6MTczOTY0MTA3OCwiYXVkIjoiIiwic3ViIjoiIiwiUm9sZSI6InVzZXIifQ.0iky-9QTxMaq2njnh25oYY67ByARunrlrFSyK4mLzY0"
INVALID_JWT="iJodHRwYmluMiIsImlhdCI6MTcwNjc5OTY5NywiZXhwIjoxNzA3NjYzNjk3LCJyb2xlIjoiYWRtaW4iLCJwZXJtaXNzaW9ucyI6WyJyZWFkIiwid3JpdGUiXX0.KYrKIX9dobREn479XkExxq42aFKEkP0S6NsopQc1jCY"
# TODO check compose up and logs

echo "Running test script"
docker compose up -d

while [[ $(docker-compose ps -q | wc -l) -lt 5 ]]; do
  echo "Waiting for services to start..."
  sleep 5
done
echo "${BLUE}Services running${NC}"

# docker-compose logs gateway   # to show the logs of the gateway
gnome-terminal -- bash -c "docker-compose logs -f gateway"


# The available microservices are users and resources. The gateway maps the request to the correct services
echo "\n"
echo "${GREEN}1. Routing${NC}"
echo "${BLUE}The gateway maps the request to the correct backend microservice${NC}"
echo "${BLUE}First call: http://localhost:8080/users -> http://users1:80/json${NC}" 
curl --location "http://localhost:8080/users" \
--header "Authorization: bearer $ADMIN_JWT"
echo "${BLUE}Second call: http://localhost:8080/resources -> http://resources1:80/xml${NC}"
curl --location "http://localhost:8080/resources" \
--header "Authorization: bearer $ADMIN_JWT"

# The available hosts for /users are http://users1:80,http://users2:80. Since I have implemented round robin for choosing the host, 
# I make three requests at the same microservice. I expect that the first time, the microservice will be called using users2, then users1 and once again users2. 
echo "\n"
echo "${GREEN}2. Round Robin${NC}"
echo "${BLUE}A microservice is called three times. Gateway uses all available hosts using round robin.${NC}" 
echo "${BLUE}First call: http://localhost:8080/users -> http://users2:80/json${NC}" 
curl --location "http://localhost:8080/users" \
--header "Authorization: bearer $ADMIN_JWT"
echo "${BLUE}Second call: http://localhost:8080/users/get -> http://users1:80/get${NC}"
curl --location "http://localhost:8080/users/get" \
--header "Authorization: bearer $ADMIN_JWT"
echo "${BLUE}Third call: http://localhost:8080/users/echo -> http://users2:80/anything${NC}"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"

echo "\n"
echo "${GREEN}3. Authorization ${NC}" 
echo "${BLUE}Making a request without Authorization headers. Gateway should reject it.${NC}"
curl --location "http://localhost:8080/users"

echo "${BLUE}\nMaking a request with an incorret Authorization header. Gateway should reject it.${NC}"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $INVALID_JWT"

echo "\n"
echo "${GREEN}3. Authentication ${NC}" 
echo "${BLUE}Gateway accepts only POST requests with admin role in JWT.${NC}"
echo "${BLUE}It will reject a POST request made by a user.${NC}"
curl --location --request POST "http://localhost:8080/users/echo" \
--header "Authorization: bearer $USER_JWT"

echo "${BLUE}But, it will accept it if it is made by admin.${NC}"
curl --location --request POST "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"

echo "\n"
echo "${GREEN}3. Fault tolerance ${NC}" 
echo "${BLUE}If an host fails, the gateway will put it in time out.${NC}"
echo "${BLUE}If an host is in time out, it's ignored and gateway tries to use it again after 30 seconds${NC}" #  short time interval to serve the testing purpose
echo "\n${BLUE}Shutting down service users1.${NC}"

docker compose down users1
while [[ $(docker-compose ps -q | wc -l) -lt 1 ]]; do
  echo "Waiting for service to stop..."
  sleep 2
done
echo "${BLUE}Service stopped.${NC}"
echo "${BLUE}Calling microservice users. It will respond users2, since users1 is down.${NC}"
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

echo "${BLUE}Users1 still down, so users2 will respond.${NC}"
curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"

docker compose up users1
while [[ $(docker-compose ps -q | wc -l) -lt 1 ]]; do
  echo "Waiting for service to start..."
  sleep 5
done
echo "${BLUE}Service running again${NC}"

curl --location "http://localhost:8080/users/echo" \
--header "Authorization: bearer $ADMIN_JWT"


# still TODO
# rate limiting 
