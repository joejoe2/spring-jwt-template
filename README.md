# spring-jwt-template

[![codecov](https://codecov.io/gh/joejoe2/spring-jwt-template/branch/main/graph/badge.svg?token=24IMFJ0D50)](https://codecov.io/gh/joejoe2/spring-jwt-template)
 
## Description

This is a template to help you to get started with jwt-based spring boot backend.

Including:
- `basic models` for user, access token, and refresh token
- `login api` issue access token and refresh token
- `web login api` issue access token and set refresh token in http only cookie
- `refresh api` exchange new tokens via refresh token
- `web refresh api` exchange new tokens via the refresh token in http only cookie
- `register api` with email verification
- `logout api` revoke access token via redis blacklist
- `change password api`
- `forget and reset password api`
- `admin api` user activate/deactivate, change role, and getUserList
- validation on `@RequestBody`
- `@RateLimit on user or ip` based on bucket token algorithm and redis

we will use open-ssl to generate the private and public key for jwt.

## Example frontend

We provide an example frontend application written in Vue.js [at here](https://github.com/joejoe2/frontend) to 
work with this project.

![image](demo_login.png)

![image](demo_admin.png)

## Get started

1. clone the repo and run `mvn install` or `./mvnw install`


2. set up a postgresql server on localhost:5432 with database `spring-test`


3. set up a redis server on localhost:6379


4. install open-ssl and run `./jwtRSA256.sh` 


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
   - `set reset password url`(redirect user to your reset password page in frontend, we will append token for you to send the password reset request)
   ```
   # set reset password url
   reset.password.url=http://localhost:8888/resetPassword?token=
   ```
   - `set allow host`(this is used for web login/refresh api
   , because we will set refresh token in http only cookie, 
   you should set this to the hostname of your frontend)
   ```
   # set allow host (frontend)
   allow.host=http://localhost:8888
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

run `mvn test` or `./mvnw test`

## Deploy

1. install maven, docker and docker-compose
2. run `mvn package -Dmaven.test.skip=true`
3. copy `./env/application.env.example` to `./env/application.env` and edit just like application.properties(mentioned above), 
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
4. copy `./nginx/nginx-certbot.env.example` to `./nginx/nginx-certbot.env` (just set `CERTBOT_EMAIL` at first line)
5. edit `./nginx/user_conf.d/server.conf` (just change server_name to your own FQDN)
6. make sure that `POSTGRES_PASSWORD` and `POSTGRES_DB` in `./docker-compose.yml` is same with settings in `./env/application.env`
7. `docker-compose up` or `docker-compose up -d`

## ToDo

1. add test for controllers
2. write documentation for api
