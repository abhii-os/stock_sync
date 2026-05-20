### Step 1 : unzip  the  file
### Step 2 : Switch to the extract  path
```
cd mockserver
```
### Step 3 : Install Dependencies by running the  following  command 
```
npm install
```
### Step 4 : go the root  folder and Start the Server
```
npm start
```
### Step 5 : Test the Endpoints
- Test the POST /api/login endpoint (successful login):
```
curl --location 'http://localhost:3000/api/v1/login' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data '{
  "username": "admin",
  "password": "admin123"
}'
```

- Test the POST /api/login endpoint (bad  request --400):
```
curl --location 'http://localhost:3000/api/v1/login' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data '{
  "username": "admin",
  "password": "user"
}'
```

- Test the GET /products endpoint:
```
curl --location 'http://localhost:3000/api/v1/products'
```

- Test the POST /products endpoint:
```
curl --location 'http://localhost:3000/api/v1/products' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data '{
        "id": 101,
        "model": "UPC00001",
        "name": "Samsung Mobile S25",
        "imageUrl": "https://images.unsplash.com/photo-1738830234395-a351829a1c7b?w=150"
    }'
```

- Test the PUT /products endpoint:
```
curl --location --request PUT 'http://localhost:3000/api/v1/products/101' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data '{
        "id": 101,
        "model": "UPC00001",
        "name": "Samsung Mobile S25 new update",
        "imageUrl": "https://images.unsplash.com/photo-1738830234395-a351829a1c7b?w=150"
    }'
```
- Test the DELETE /products endpoint:
```
curl --location --request DELETE 'http://localhost:3000/api/v1/products/101' \
--header 'accept: */*' \
--header 'Content-Type: application/json'
```

- Test the GET /users endpoint:
```
curl --location 'http://localhost:3000/users'
```

## To generate  zip file

```
git archive --format=zip --output=mockserver.zip master
```

