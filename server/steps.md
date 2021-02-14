# Repro build env

```bash
docker run -it --name try_please -v /$(pwd):/usr/src/server -w /usr/src/server golang:latest

./pleasew
apt-get update
apt-get install tar
apt-get install xz
apt-get install xz-utils
ln -s /usr/local/go/bin/go /usr/local/bin
apt-get install unzip
apt-get install zip
apt-get install openjdk-11-jdk
curl -s https://get.sdkman.io | bash
source "/root/.sdkman/bin/sdkman-init.sh"
sdk install kotlin
ln -s /root/.sdkman/candidates/kotlin/current/bin/kotlinc /usr/local/bin/
./pleasew build -p
```
