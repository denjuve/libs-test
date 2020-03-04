package com.libs

def remoteBuild(d_dirp, s_dirp, component_name, ci_branch_repo, git_branch_repo, ssh_creds, remote_ip) {

string r_ip = remote_ip
string d_dir = d_dirp
string s_dir = s_dirp
string cmpt_id = component_name
string ci_rep = ci_branch_repo
string git_rep = git_branch_repo

def remote = [:]
remote.name = r_ip
remote.host = r_ip
remote.allowAnyHosts = true

  withCredentials([sshUserPrivateKey(credentialsId: ssh_creds, keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName'),
      usernamePassword(credentialsId: '5gt-ci', usernameVariable: 'u5g', passwordVariable: 'p5g')]) {
        remote.user = userName
        remote.identityFile = identity

    sh 'rm -rf repo'
    sshCommand remote: remote, command: "rm -rf $d_dir || true"
    sshCommand remote: remote, command: "sudo docker rm -f \$(sudo docker ps -a | grep $cmpt_id | awk '{ print \$1}')  || true"
    sshCommand remote: remote, command: "mkdir -p -m 0777 $d_dir"

    sh "git clone -b $ci_rep https://$u5g:$p5g@5g-transformer.eu/git/5g-transformer.5gt-ci repo"
//    sh "scp -r -i ${identity} -o StrictHostKeyChecking=no $s_dir/* ${userName}@${remote_ip}:${d_dir}"

def  FILES_LIST = sh (script: "ls   $s_dir", returnStdout: true).trim()
//PARSING
for(String item : FILES_LIST.split("\\r?\\n")){ 
sshPut remote: remote, from: "$s_dir/$item", into: "$d_dir", override: true
}

    sshCommand remote: remote, command: "sed -i 's/5g-transformer.eu/$u5g:$p5g@5g-transformer.eu/g' $d_dir/${cmpt_id}build_docker.sh"
    sshCommand remote: remote, command: "sed -i 's/GIT_BRANCH=.*/GIT_BRANCH=$git_rep/' $d_dir/${cmpt_id}build_docker.sh"
    sshCommand remote: remote, command: "chmod +x $d_dir/${cmpt_id}build_docker.sh"
}}
return this
