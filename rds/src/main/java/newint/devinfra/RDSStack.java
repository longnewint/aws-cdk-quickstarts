package newint.devinfra;

import java.util.List;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.NetworkType;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.amazon.awscdk.services.rds.StorageType;
import software.constructs.Construct;

public class RDSStack extends Stack {
  public RDSStack(final Construct scope, final String id, final StackProps props) {
    super(scope, id, props);

    var vpc = getVpc();

    var ss = getSubnet();

    var sg = createSecurityGroup(vpc);

    var engine = DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
      .version(PostgresEngineVersion.VER_16_3).build());

    var instance = InstanceType.of(InstanceClass.BURSTABLE4_GRAVITON, InstanceSize.MICRO);

    var credential = Credentials.fromPassword("dev", SecretValue.unsafePlainText("password"));
    String dbname = "northwind";

    DatabaseInstance.Builder.create(this, "dev-rds")
      .engine(engine)
      .databaseName(dbname)
      .credentials(credential)
      .instanceType(instance)
      .storageType(StorageType.GP2)
      .allocatedStorage(20)
      .backupRetention(Duration.days(0))
      .multiAz(false)
      .availabilityZone("ca-central-1a")
      .vpc(vpc)
      .vpcSubnets(ss)
      .networkType(NetworkType.IPV4)
      .securityGroups(List.of(sg))
      .enablePerformanceInsights(false)
      .cloudwatchLogsExports(List.of("postgresql"))
      .cloudwatchLogsRetention(RetentionDays.ONE_DAY)
    .build();
  }

  private IVpc getVpc() {
    return Vpc.fromLookup(this, "Vpc", VpcLookupOptions.builder()
      .isDefault(true)
    .build());
  }

  private SubnetSelection getSubnet() {
    return SubnetSelection.builder()
      .subnetType(SubnetType.PUBLIC)
    .build();
  }

  private SecurityGroup createSecurityGroup(IVpc vpc) {
    var sg = SecurityGroup.Builder.create(this, "dev-rds-sg")
      .vpc(vpc)
      .securityGroupName("rds-sg")
    .build();

    sg.addIngressRule(Peer.anyIpv4(), Port.POSTGRES);

    return sg;
  }

}
