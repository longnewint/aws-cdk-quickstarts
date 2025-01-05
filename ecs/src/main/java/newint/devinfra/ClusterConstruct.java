package newint.devinfra;

import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.AddCapacityOptions;
import software.amazon.awscdk.services.ecs.BottleRocketImage;
import software.amazon.awscdk.services.ecs.Cluster;
import software.constructs.Construct;

public class ClusterConstruct extends Construct {
  public ClusterConstruct(final Construct scope, final String id, IVpc vpc) {
    super(scope, id);

    Cluster cluster = Cluster.Builder.create(this, "cluster")
      .vpc(vpc)
    .build();
 
    // Add capacity to it
    cluster.addCapacity("DefaultAutoScalingGroupCapacity", AddCapacityOptions.builder()
      .instanceType(new InstanceType("t2.micro"))
      .machineImage(new BottleRocketImage())
      .desiredCapacity(1)
      .maxCapacity(1)
    .build());
  }
}
