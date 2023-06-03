# spring-jwt-template

[![workflow](https://github.com/joejoe2/spring-jwt-template/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/joejoe2/spring-jwt-template/actions/workflows/main.yml)
[![codecov](https://codecov.io/gh/joejoe2/spring-jwt-template/branch/main/graph/badge.svg?token=24IMFJ0D50)](https://codecov.io/gh/joejoe2/spring-jwt-template)

## Description

This is a template to help you to get started with jwt-based spring boot backend.

Besides, this project can be used as a ```Single-Sign On center (or authentication service) ```
directly.
We will use open-ssl to generate the private and public key and sign JWT tokens
with the private key.
Other applications can use the public key to verify the JWT tokens.

## Features

- `basic models` for user, access token, and refresh token
- `login api` issue access token and refresh token
- `web login api` issue access token and set refresh token in http only cookie
- `set max login failure attempts` block login for a while if too many failure attempts
- `refresh api` exchange new tokens via refresh token
- `web refresh api` exchange new tokens via the refresh token in http only cookie
- `register api` with email verification
- `logout api` revoke access token via redis blacklist
- `change password api`
- `forget and reset password api`
- `admin api` user activate/deactivate, change role, and getUserList
- validation on `@RequestBody`
- `@RateLimit on user or ip` based on bucket token algorithm and redis
- `api documentation` via swagger (at path `/swagger-ui/index.html`)

## Architecture

![image](architecture.png)

## Online demo

[https://frontend.joejoe2.com](https://frontend.joejoe2.com)

## Example frontend

We provide an example frontend application written in Vue.js [at here](https://github.com/joejoe2/frontend) to
work with this project.

![image](demo_login.png)

![image](demo_admin.png)

## Get started

1. install Docker, JDK-17 and Maven (recommended to use IntelliJ IDE)


2. clone the repo and run `mvn install` or `./mvnw install`


3. set up a postgresql server on localhost:5432 with database `spring-test`


4. set up a redis server on localhost:6379


5. install open-ssl and run `./jwtRSA256.sh`


6. cd to `./src/main/resources/`, then copy `application-dev.properties` and `application-dev.yml`
   to `application.properties` and `application.yml`


7. edit `application.properties` depends on your need
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
   
    # domain for access/refresh tokens in cookie(if you are using web login api)
    # can be exact domain or .example.com for all subdomains
    jwt.cookie.domain=.example.com

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
    - `set reset password url`(redirect user to your reset password page in frontend, we will append token for you to
      send the password reset request)
   ```
   # set reset password url
   reset.password.url=http://localhost:8888/resetPassword?token=
   ```
    - `set allow host`(cors for your frontend)
   ```
   # for frontend applications at any port of the localhost
   allow.host=http://localhost:[*]
   # for the frontend application at frontend.example.com
   allow.host=https://frontend.example.com
   # for frontend applications belong to subdomains of .example.com
   allow.host=https://*.example.com
   ```
    - `login related settings` (ex. block user to login for
      900 seconds after 5 consecutive unsuccessful attempts
      with incorrect password)
   ```
   # login max attempt settings
   login.maxAttempts=5
   # in seconds
   login.attempts.coolTime=900
   ```

8. copy the contents of `private.key` and `public.key` (generated at project root in step 4.) into `application.yml`
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
9. now you can start to develop your own project

## Notice

We use RSA private key to sign tokens and public key
to verify tokens (described in above). So you can use the public key to
parse and verify the tokens out of this application (could use
this application as an AuthService).

## Testing

run `mvn test` or `./mvnw test`

## Lint

run 
```
mvn spotless:check
mvn spotless:apply
``` 
or 
```
./mvnw spotless:check
./mvnw spotless:apply
``` 

## Deploy

1. install docker and docker-compose
2. copy `./env/application.env.example` to `./env/application.env` and edit just like application.properties(mentioned
   above),
   but you need to set `jwt.secret.privateKey` and `jwt.secret.publicKey` like this format
   ```
   jwt.secret.publicKey="-----BEGIN PUBLIC KEY-----
   MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzFVaIiZtFKJgIrrXa9ZQ
   fHeGu3o/CFGAhybGXXcU6XWZpyIHNTUdx7ah1z+pMecXWqOIkmKVN92ktgV+TAEB
   mB91TMr23dMU95JC5wz7H1sxUmO+0HuA5XkGUTXf6GqpIAYLvKnNNhd8eCFm/YAE
   S9LMsRBVZqgAb7GDJDb+B4NTzUGtWn71/2rSnDsXg1+aV271MM7n20AcvRruXDWx
   bz5Wx5kKnTbwrOSvQ1chCo/gg+t+xCUdZ78SyT2bRuUIe+d0qHyqdY6i4lvbiXzC
   noZRygIMYfRyxh0y52Mw6NXLvowOZ2DDYtQMeJglyocOFeYqSgqiRsaELvoQ/5Y8
   1wIDAQAB
   -----END PUBLIC KEY-----"
   ```
3. copy `./nginx/nginx-certbot.env.example` to `./nginx/nginx-certbot.env` (just set `CERTBOT_EMAIL` at first line)
4. edit `./nginx/user_conf.d/server.conf` (just change server_name to your own FQDN)
5. make sure that `POSTGRES_PASSWORD` and `POSTGRES_DB` in `./docker-compose.yml` is same with settings
   in `./env/application.env`
6. `docker-compose up` or `docker-compose up -d`
