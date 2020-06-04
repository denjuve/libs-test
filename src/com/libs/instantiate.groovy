package com.libs

def instVm(remote_ip, deplId) {

//string
string r_ip = remote_ip

def remote = [:]
remote.name = r_ip
remote.host = r_ip
remote.allowAnyHosts = true

  withCredentials([sshUserPrivateKey(credentialsId: ssh_creds, keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName'),
]) {
        remote.user = userName
        remote.identityFile = identity
sh """
    sudo docker-compose up --build
    sudo docker system prune -a -f
"""
    sshCommand remote: remote, command: "cfy deployments outputs ${deplId} | grep -i value | awk '{print \$2}' | tee ${deplId}.IP"
    sshCommand remote: remote, command: "echo 'private key is:' | tee -a ${deplId}.IP"
    sshCommand remote: remote, command: "cat /etc/cloudify/key/jenkins-key | tee -a ${deplId}.IP"
    sshGet remote: remote, from: "${deplId}.IP", into: "${deplId}.IP", override: true
    sh "cat ${deplId}.IP"
    nodeIp = sh(script: "head -n 1 ${deplId}.IP", returnStdout: true)

}}
return this