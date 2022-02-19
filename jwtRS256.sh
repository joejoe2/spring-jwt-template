# generate private key
openssl genrsa -out private.pem 2048
# extatract public key from it
openssl rsa -in private.pem -pubout > public.key
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private.pem -out private.key