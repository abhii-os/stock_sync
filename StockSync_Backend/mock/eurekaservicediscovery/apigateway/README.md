### curl commands  from API gateway

##### Register  new  user

```declarative
curl --location 'http://localhost:8080/api/v1/userservice/register' \
--header 'accept: */*' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4iLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc2MTg5NTc1MiwiZXhwIjoxNzYxODk2NjUyLCJpc3MiOiJnZW5jX2NvaG9ydCIsImF1ZCI6WyJHZW5DIl19.aj9prZ1q60ISU1iNwvdERvOgAt67YoyGv4ICR6bMQfc' \
--header 'Content-Type: application/json' \
--data-raw '{
  "username": "gencuser2",
  "password": "dev123",
  "email": "gencuser2@cts.com",
  "firstName": "gencuser1",
  "lastName": "DECohort",
  "phone": "9867198672",
  "roleType": "ROLE_DEV"
}'
```
##### Login for existing  user
```declarative
curl --location 'http://localhost:8080/api/v1/userservice/login' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data '{
  "username": "admin",
  "password": "admin123"
}'
```

###  for Roles  
```declarative
curl --location 'http://localhost:8080/api/v1/userservice/roles' \
--header 'accept: */*' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4iLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc2MTMwMzEyNCwiZXhwIjoxNzYxMzA0MDI0LCJpc3MiOiJnZW5jX2NvaG9ydCIsImF1ZCI6WyJHZW5DIl19.9XQXGDrytGZm0lLF0YY3u-Xw9wwDuvMSIJZjBtwCyL4'
```

### APIGATEWAY API  for  mock product  API
```
curl --location 'http://localhost:8080/api/v1/product-service/products' \
--header 'accept: */*' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4iLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc2MTMwNTU0NCwiZXhwIjoxNzYxMzA2NDQ0LCJpc3MiOiJnZW5jX2NvaG9ydCIsImF1ZCI6WyJHZW5DIl19.M0HeXjKSCeuTag_9icvesHa6DaVGQboZraejN-a5Y8o'
```

###  curl for greet
```
curl --location 'http://localhost:8080/api/v1/product-service/greet' \
--header 'accept: */*' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4iLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc2MTMwNTU0NCwiZXhwIjoxNzYxMzA2NDQ0LCJpc3MiOiJnZW5jX2NvaG9ydCIsImF1ZCI6WyJHZW5DIl19.M0HeXjKSCeuTag_9icvesHa6DaVGQboZraejN-a5Y8o'
```