package com.libs

def remoteVerify(d_dirp, s_dirp, component_name, ssh_creds, remote_ip) {

string r_ip = remote_ip
string d_dir = d_dirp
string s_dir = s_dirp
string cmpt_id = component_name
string echo_line = "pytest --junitxml=${cmpt_id}_report_port.xml -x -v test_port.py || true"

def remote = [:]
remote.name = r_ip
remote.host = r_ip
remote.allowAnyHosts = true

  withCredentials([sshUserPrivateKey(credentialsId: ssh_creds, keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName'),
//      usernamePassword(credentialsId: '5gt-ci', usernameVariable: 'u5g', passwordVariable: 'p5g')
]) {
        remote.user = userName
        remote.identityFile = identity

    sshCommand remote: remote, command: "sudo locale-gen de_DE.utf8 en_US.utf8 es_ES.utf8 it_IT.utf8 ru_RU.utf8 uk_UA.utf8"
    sshCommand remote: remote, command: "sudo locale -a"
    sshCommand remote: remote, command: "sudo apt update -y"
    sshCommand remote: remote, command: "sudo apt install -y python-pip virtualenv"
    
    sshCommand remote: remote, command: "mkdir -p -m 0777 ~/virtualenvironment/${cmpt_id}_test || true"
    sshCommand remote: remote, command: "virtualenv ~/virtualenvironment/${cmpt_id}_test"
    sshCommand remote: remote, command: "cp -r $d_dir/test/test_port.py ~/virtualenvironment/${cmpt_id}_test/bin/"

    sshCommand remote: remote, command: "echo '#!/bin/bash' > /tmp/${cmpt_id}_test.sh"
    sshCommand remote: remote, command: "echo cd ~/virtualenvironment/${cmpt_id}_test/bin/ >> /tmp/${cmpt_id}_test.sh"
    sshCommand remote: remote, command: "echo 'source activate' >> /tmp/${cmpt_id}_test.sh"
    sshCommand remote: remote, command: "echo 'pip install pytest' >> /tmp/${cmpt_id}_test.sh"

    sshCommand remote: remote, command: "echo problem >> /tmp/${cmpt_id}_test.sh"
    sshCommand remote: remote, command: "sed -i 's@problem@pytest --junitxml=${cmpt_id}_report_port.xml -x -v test_port.py || true'@ /tmp/${cmpt_id}_test.sh"
    sshCommand remote: remote, command: "echo deactivate >> /tmp/${cmpt_id}_test.sh"
    sshCommand remote: remote, command: "echo $echo_line >> /tmp/${cmpt_id}_test.sh"


//    sshPut remote: remote, from: "/tmp/${cmpt_id}_test.sh", into: "/tmp/${cmpt_id}_test.sh", override: true
    sshCommand remote: remote, command: "chmod +x /tmp/${cmpt_id}_test.sh"
    sshCommand remote: remote, command: "bash /tmp/${cmpt_id}_test.sh"
    sshGet remote: remote, from: "virtualenvironment/${cmpt_id}_test/bin/${cmpt_id}_report_port.xml", into: "$s_dir/${cmpt_id}_report_port.xml", override: true
    junit testResults: "$s_dir/${cmpt_id}_report_port.xml"
    
    sshCommand remote: remote, command: "rm -rf /tmp/*${cmpt_id}* || true"
//    sh "rm -rf /tmp/*${cmpt_id}*"
}}
return this
