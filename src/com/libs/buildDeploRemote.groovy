package com.libs

def remoteDeploy($d_dir, $s_dir, component_name, ci_branch_repo, ssh_creds, remote_ip) {

string d_dir = $d_dir
string s_dir = $s_dir
string cmpt_id = component_name
string ci_rep = ci_branch_repo

def remote = [:]
remote.name = remote_ip
remote.host = remote_ip
remote.allowAnyHosts = true

  withCredentials([sshUserPrivateKey(credentialsId: ssh_creds, keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName'),
      usernamePassword(credentialsId: '5gt-ci', usernameVariable: 'u5g', passwordVariable: 'p5g')]) {
        remote.user = userName
        remote.identityFile = identity

////arguments for the function//****************************
//$s_dir [--> $s_path_mon/s_path_mtp etc.]
//$d_dir [--> $d_path_mon/d_path_mtp etc.]
//grep mon_ (component containers deletion) ????????  component_name [--> mon_]
//ci_branch_repo [-->${params.ci_branch_mon}]
//ssh_id [-->${params.ssh_creds}]

//remote_ip (scp)
//ci_creds [$u5g:$p5g] (creds git_ci) ????????????????????????? !!!!!!withcredentials!!!
//${identity} (scp) !!!!!!withcredentials!!!
//${userName} (scp) !!!!!!withcredentials!!!

    sh 'rm -rf repo'
    sshCommand remote: remote, command: "rm -rf $d_dir || true"
    sshCommand remote: remote, command: "sudo docker rm -f \$(sudo docker ps | grep $cmpt_id | awk '{ print \$1}')  || true"
    sshCommand remote: remote, command: "mkdir -p -m 0777 $d_dir"

    sh "git clone -b $ci_rep https://$u5g:$p5g@5g-transformer.eu/git/5g-transformer.5gt-ci repo"
    sh "scp -r -i ${identity} -o StrictHostKeyChecking=no $s_dir/* ${userName}@${remote_ip}:$d_dir"

    sshCommand remote: remote, command: "sed -i 's/5g-transformer.eu/$u5g:$p5g@5g-transformer.eu/g' $d_dir/$cmpt_idbuild_docker.sh"
    sshCommand remote: remote, command: "sed -i 's/GIT_BRANCH=.*/GIT_BRANCH=params.git_branch_mon/' $d_dir/$cmpt_idbuild_docker.sh"
    sshCommand remote: remote, command: "chmod +x $d_dir/$cmpt_idbuild_docker.sh"
    sshCommand remote: remote, command: "sed -i 's/#sudo/sudo/g' $d_dir/$cmpt_idbuild_docker.sh"

    sshCommand remote: remote, command: "bash $d_dir/$cmpt_idbuild_docker.sh"

    sshCommand remote: remote, command: "sudo docker ps -a|grep $cmpt_id"
}}
return this
