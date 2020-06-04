package com.libs

def instCfy_conf(cfyCtl_ip) {

//string

//stage('cleanup') {
    sh '''
    rm -rf Dockerfile || true
    rm -rf *.yml || true
    sudo docker system prune -a -f || true
    '''
//    }
//stage('config') {
//parallel dockerfile: {
    sh '''
cat > Dockerfile << EOF
FROM ubuntu:latest
RUN apt-get update
RUN apt-get install -y wget
RUN wget http://repository.cloudifysource.org/cloudify/19.07.18/community-release/cloudify-cli-community-19.07.18.deb
RUN dpkg -i *.deb
RUN mkdir infraDev
COPY infra.yml infraDev
RUN cfy profiles use $cfyCtl_ip -u admin -p admin -t default_tenant
RUN cfy install infraDev/infra.yml -b myinfra
RUN cfy deployments outputs myinfra | grep -i value | awk '{print $2}' | tee myinfra.IP
EOF
  '''
//                },
//compose: {
    sh """
cat > docker-compose.yml << EOF
version: '3'
services:
  cfydev:
    container_name: cfydev
    image: ubuntu:latest
    build:
      context: .
      dockerfile: ./Dockerfile
EOF
"""
}
return this