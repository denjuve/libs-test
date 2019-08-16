package com.libs

//def dockerScriptRemote() {
//def dockerScriptRemote(String componentType) {
def dockerScriptRemote(componentType) {
println(componentType)
println ${componentType}

//string ssh_creds1 = params.ssh_creds
def remote = [:]
//remote.name = params.rhost_ip_mon
//remote.host = params.rhost_ip_mon
remote.name = ${componentType}
//println(componentType)
//${componentType}
remote.host = ${componentType}
//println(componentType)
//params.rhost_ip_${componentType}
remote.allowAnyHosts = true

  withCredentials([sshUserPrivateKey(credentialsId: params.ssh_creds, keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName'),
      usernamePassword(credentialsId: '5gt-ci', usernameVariable: 'u5g', passwordVariable: 'p5g')]) {
        remote.user = userName
        remote.identityFile = identity

    sshCommand remote: remote, command: "rm -rf /tmp/scripts-my"
    sshCommand remote: remote, command: "if sudo docker version; then echo 'Docker installed'; else rm -rf /tmp/scripts-my; git clone https://github.com/denjuve/scripts.git /tmp/scripts-my; bash /tmp/scripts-my/docker_install.sh; fi"
    sshCommand remote: remote, command: "if sudo docker-compose --version; then echo 'Compose installed'; else sudo curl -L https://github.com/docker/compose/releases/download/1.23.0-rc3/docker-compose-Linux-x86_64 -o /usr/local/bin/docker-compose; sudo chmod +x /usr/local/bin/docker-compose; fi"
    sshCommand remote: remote, command: "git config --global http.sslVerify false"
}
}

return this