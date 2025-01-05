package newint.devinfra;

import java.util.List;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.KeyPair;
import software.amazon.awscdk.services.ec2.KeyPairFormat;
import software.amazon.awscdk.services.ec2.KeyPairType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.UserData;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;

public class EC2Stack extends Stack {

  public EC2Stack(final Construct scope, final String id, final StackProps props) {
    super(scope, id, props);

    var vpc = getVpc();
    var sn = getSubnet();

    var role = createInstanceRole();

    var sg = createSecurityGroup(vpc);

    var ud = createUserData();

    var key = createKeyPair();

    Instance.Builder.create(this, "dev-ec2").allowAllOutbound(true)
      .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
      .instanceName("dev-instance")
      .role(role)
      .vpc(vpc)
      .vpcSubnets(sn)
      .availabilityZone("ca-central-1a")
      .securityGroup(sg)
      .userData(ud)
      .machineImage(MachineImage.latestAmazonLinux2023())
      .keyPair(key)
      .requireImdsv2(true)
    .build();

  }

  private IVpc getVpc() {
    return Vpc.fromLookup(this, "Vpc", VpcLookupOptions.builder()
      .isDefault(true)
    .build());
  }

  private SubnetSelection getSubnet() {
    return SubnetSelection.builder()
      .onePerAz(true)
      .subnetType(SubnetType.PUBLIC)
    .build();
  }

  private Role createInstanceRole() {
    return Role.Builder.create(this, "dev-instance-role")
        .assumedBy(ServicePrincipal.Builder.create("ec2.amazonaws.com").build())
        .roleName("dev-ec2-role")
        .managedPolicies(List.of(
            ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore")))
        .build();
  }

  private SecurityGroup createSecurityGroup(IVpc vpc) {
    var sg = SecurityGroup.Builder.create(this, "dev-ec2-sg")
      .allowAllOutbound(true)
      .vpc(vpc)
      .securityGroupName("ec2-sg")
    .build();
    
    sg.addIngressRule(Peer.anyIpv4(), Port.allTcp());

    return sg;
  }

  private UserData createUserData() {
    UserData ud = UserData.forLinux();
    ud.addCommands("sudo dnf update");
    ud.addCommands("sudo dnf install -y java-21-amazon-corretto postgresql16");

    return ud;
  }

  private KeyPair createKeyPair() {
    return KeyPair.Builder.create(this, "dev-ssh-key")
      .type(KeyPairType.ED25519)
      .format(KeyPairFormat.PEM)
    .build();
  }
  
}
