package newint.devinfra;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;

public class ClusterStack extends Stack {
  public ClusterStack(final Construct scope, final String id) {
    this(scope, id, null);
  }

  public ClusterStack(final Construct scope, final String id, final StackProps props) {
    super(scope, id, props);
    
    var vpc = getVpc();

    new ClusterConstruct(this, "cluster-construct", vpc);

  }

  public IVpc getVpc() {
    return Vpc.fromLookup(this, "Vpc", VpcLookupOptions.builder()
      .isDefault(true)
    .build());
  }
}
