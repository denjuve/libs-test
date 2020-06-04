package com.libs

// ssh_path: /etc/cloudify/key/jenkins-key
def instBpt(os_uname, os_upas, os_prj, os_tnt, os_url, os_dmn, os_rgn, os_ssh, os_img, os_flvr, os_net, os_sub, ssh_path) {

//string

    sh """
cat > infra.yml << EOF
tosca_definitions_version: cloudify_dsl_1_3
imports:
  - http://www.getcloudify.org/spec/cloudify/4.3/types.yaml
  - plugin:cloudify-openstack-plugin
  - plugin:cloudify-utilities-plugin

inputs:
  username: {default: $os_uname}
  password: {default: $os_upas}
  project_id: {default: $os_prj}
  tenant_name: {default: $os_tnt}
  auth_url: {default: "$os_url"}
  user_domain_name: {default: $os_dmn}
  region: {default: "$os_rgn"}
  ssh_key: {default: $os_ssh}
  img_my: {default: $os_img}
  flavor_name: {default: $os_flvr}
  public_network_name1: {default: $os_net}
  public_subnet_name1: {default: $os_sub}

dsl_definitions:

  openstack_config: &openstack_config
    username: { get_input: username }
    password: { get_input: password }
    project_id: { get_input: project_id }
    auth_url: { get_input: auth_url }
    region: { get_input: region }
    user_domain_name: { get_input: user_domain_name }

node_templates:

  my-openstack-keypair:
    type: cloudify.openstack.nodes.KeyPair
    properties:
      use_external_resource: true
      resource_id: { get_input: ssh_key }
      private_key_path: "$ssh_path"
      openstack_config: *openstack_config

  public_network:
    type: cloudify.openstack.nodes.Network
    properties:
      openstack_config: *openstack_config
      use_external_resource: true
      resource_id: { get_input: public_network_name1 }

  public_subnet:
    type: cloudify.openstack.nodes.Subnet
    properties:
      openstack_config: *openstack_config
      use_external_resource: true
      resource_id: { get_input: public_subnet_name1 }

  node_dev_port:
    type: cloudify.openstack.nodes.Port
    properties:
      resource_id: 'node_dev_port'
      openstack_config: *openstack_config
    relationships:
      - type: cloudify.relationships.contained_in
        target: public_network
      - type: cloudify.relationships.depends_on
        target: public_subnet

  node_dev:
    type: cloudify.openstack.nodes.Server
    properties:
      agent_config:
        install_method: none
      resource_id: 'node_dev'
      openstack_config: *openstack_config
      image: { get_input: img_my }
      flavor: { get_input: flavor_name }
      agent_config:
        install_method: none
    relationships:
      - target: node_dev_port
        type: cloudify.openstack.server_connected_to_port
      - target: my-openstack-keypair
        type: cloudify.openstack.server_connected_to_keypair

outputs:

  endpoint:
    description: ip provider network
    value: { get_attribute: [node_dev, ip]}
END
    """
}
return this