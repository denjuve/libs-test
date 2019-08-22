package com.libs

def remoteDeploy(d_dirp, component_name, ssh_creds, remote_ip) {

string d_dir = d_dirp
//string s_dir = s_dirp
string cmpt_id = component_name
//string ci_rep = ci_branch_repo
//string git_rep = git_branch_repo

def remote = [:]
remote.name = remote_ip
remote.host = remote_ip
remote.allowAnyHosts = true

  withCredentials([sshUserPrivateKey(credentialsId: ssh_creds, keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName'),
      usernamePassword(credentialsId: '5gt-ci', usernameVariable: 'u5g', passwordVariable: 'p5g')]) {
        remote.user = userName
        remote.identityFile = identity

    sshCommand remote: remote, command: "bash $d_dir/${cmpt_id}build_docker.sh"

    sshCommand remote: remote, command: "sudo docker ps -a|grep $cmpt_id"
}}
return this
