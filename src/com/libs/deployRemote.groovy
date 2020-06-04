package com.libs

def remoteDeploy(d_dirp, component_name, ssh_creds, remote_ip) {

string r_ip = remote_ip
string d_dir = d_dirp
string cmpt_id = component_name

def remote = [:]
remote.name = r_ip
remote.host = r_ip
remote.allowAnyHosts = true

  withCredentials([sshUserPrivateKey(credentialsId: ssh_creds, keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName'),
//      usernamePassword(credentialsId: '5gt-ci', usernameVariable: 'u5g', passwordVariable: 'p5g')
]) {
        remote.user = userName
        remote.identityFile = identity

    sshCommand remote: remote, command: "bash $d_dir/${cmpt_id}build_docker.sh"

    sshCommand remote: remote, command: "sudo docker ps -a|grep $cmpt_id"
}}
return this
