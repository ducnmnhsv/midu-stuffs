path=keys/aaa/tradex
mkdir -p $path
ssh-keygen -N "" -f $path/rsa
mv $path/rsa $path/jwt-private.key
mv $path/rsa.pub $path/jwt-public.key


ssh-keygen -N "" -f $path/rsa
mv $path/rsa $path/rsa-private.key
mv $path/rsa.pub $path/rsa-public.key