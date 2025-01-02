package newint.devinfra;

import java.util.List;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.DefaultInstanceTenancy;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.IpProtocol;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VPCStack extends Stack {

  public VPCStack(final Construct scope, final String id) {
    this(scope, id, null);
  }

  public VPCStack(final Construct scope, final String id, final StackProps props) {
    super(scope, id, props);

    SubnetConfiguration sn1 = SubnetConfiguration.builder()
      .cidrMask(20)
      .name("public")
      .subnetType(SubnetType.PUBLIC)
      .mapPublicIpOnLaunch(true)
      .reserved(false)
      .ipv6AssignAddressOnCreation(true)
    .build();

    SubnetConfiguration sn2 = SubnetConfiguration.builder()
      .cidrMask(20)
      .name("private")
      .subnetType(SubnetType.PRIVATE_ISOLATED)
      .reserved(false)
      .ipv6AssignAddressOnCreation(true)
    .build();

    Vpc.Builder.create(this, "dev-vpc")
      .ipProtocol(IpProtocol.DUAL_STACK)
      .ipAddresses(IpAddresses.cidr("10.16.0.0/16"))
      .defaultInstanceTenancy(DefaultInstanceTenancy.DEFAULT)
      .enableDnsSupport(true)
      .enableDnsHostnames(true)
      .maxAzs(2)
      .subnetConfiguration(List.of(sn1, sn2))
      // .natGateways(0)
      // .natGatewayProvider(NatProvider.gateway())
    .build();
  }

}
