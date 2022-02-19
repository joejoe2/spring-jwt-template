# spring-jwt-template

[![codecov](https://codecov.io/gh/joejoe2/spring-jwt-template/branch/main/graph/badge.svg?token=24IMFJ0D50)](https://codecov.io/gh/joejoe2/spring-jwt-template)
 
## Description

This is a template to help you to get started with jwt-based spring boot backend.

Including:
- `basic models` for user, access token, and refresh token
- `login api` issue access token and refresh token
- `refresh api` exchange new tokens via refresh token
- `register api` with email verification
- `logout api` revoke access token via redis blacklist
- `change password api`
- `admin api` change role and getUserList
- validation on `@RequestBody`
- validation on service layer

we will use open-ssl to generate the private and public key for jwt.

## Get started

1. clone the repo and run `mvn install` or `./mvnw install`


2. set up a postgresql server on localhost:5432 with database `spring-test`


3. set up a redis server on localhost:6379


4. install open-ssl and run ./jwtRSA256.sh 


5. cd to `./src/resources/`, then copy `application-dev.properties` and `application-dev.yml` to `application.properties` and `application.yml`


6. edit `application.properties` depends on your need
    - `db related settings`
     ```
    # connect to database `spring-test` on localhost:5432
    spring.datasource.url=jdbc:postgresql://localhost:5432/spring-test
    
    # set username and password to connect to your database
    spring.datasource.username=postgres
    spring.datasource.password=pa55ward
    
    spring.datasource.driver-class-name=org.postgresql.Driver
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
    spring.jpa.properties.hibernate.hbm2ddl.auto=update
    ```
    - `jwt related settings`
    ```
    # change this to your desired issuer in jwt
    jwt.issuer=joejoe2.com
    
    # specify lifetime of access and refresh token in seconds
    jwt.access.token.lifetime=900
    jwt.refresh.token.lifetime=1800
    ```
    - `default admin account`
    ```
    # username can only contain a-z, A-Z, and 0-9 
    # max length is 32
    default.admin.username=admin
   
    # password can only contain a-z, A-Z, and 0-9
    # min length is 8, max length is 32
    default.admin.password=pa55ward
    
    # change to your email
    default.admin.email=admin@email.com
    ```
    - `mail sender` (need to send verification code to newly registered user)
    ```
    # this is a example if you use the gmail as smtp server to send eamil
    spring.mail.host=smtp.gmail.com
    spring.mail.port=587
    spring.mail.username=test@gmail.com
    spring.mail.password=pa55ward
    spring.mail.properties.mail.smtp.auth=true
    spring.mail.properties.mail.smtp.starttls.enable=true
    ```


7. copy the contents of `private.key` and `public.key` (generated at project root in step 4.) into `application.yml`
    ```
   jwt:
     secret:
       privateKey: |
         -----BEGIN PRIVATE KEY-----
         ... your PRIVATE KEY ...
         -----END PRIVATE KEY-----
       publicKey: |
         -----BEGIN PUBLIC KEY-----
         ... your PUBLIC KEY ...
         -----END PUBLIC KEY-----
    ```
8. now you can start to develop your own project

## Notice

We use RSA private key to sign tokens and public key 
to verify tokens (described in above). So you can use the public key to 
parse and verify the tokens out of this application (could use 
this application as an AuthService).

## Testing

`mvn test` or `./mvnw test`

## Deploy

1. install docker and docker-compose
2. edit `./env/application.env` (just like application.properties) but you need to set `jwt.secret.privateKey`
   and `jwt.secret.publicKey` like application.yml
3. edit `./nginx/nginx-certbot.env` (just set value at first line)
4. edit `./nginx/user_conf.d/server.conf` (just change server_name to your own FQDN)
5. make sure that `POSTGRES_PASSWORD` and `POSTGRES_DB` in `./docker-compose.yml` is same with settings in `./env/application.env`
6. `docker-compose up` or `docker-compose up -d`

## ToDo

1. add test for controllers
2. add example frontend
