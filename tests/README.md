# Testing

Before starting the tests (any one of them), the services needs to be started. You can use

```
docker compose up -d --build 
docker compose logs gateway
```

Then, just run the file containing in the name the functionality you want to test. 

```
sh test-*.sh
```