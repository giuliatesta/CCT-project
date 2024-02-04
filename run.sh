#!/usr/bin/env bash

echo 'RUN'

export DD='ciao'

echo "${DD} "

docker compose up 

# routing and round robin (+ curl)
curl -v  -XGET 'http://localhost:8080/api/v1/profile' \
  --cookie "okta_access_token=invalid"  \
  -H 'Content-type: application/json'


docker compose up user1 user2 resource1 resource2 # (no users3)

curl 

#fault recovery
docker compose down user1



docker compose up users3 #aggiungerlo 


# while di curl 
# per rate limiting

