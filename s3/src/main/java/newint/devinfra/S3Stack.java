package newint.devinfra;

import java.util.List;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.constructs.Construct;

public class S3Stack extends Stack {

  public S3Stack(final Construct scope, final String id) {
    this(scope, id, null);
  }

  public S3Stack(final Construct scope, final String id, final StackProps props) {
    super(scope, id, props);

    var bucket = Bucket.Builder.create(this, "testservice-testbucket")
      .encryption(BucketEncryption.KMS)
      .bucketKeyEnabled(true)
      .enforceSsl(true)
      .publicReadAccess(true)
      .blockPublicAccess(BlockPublicAccess.Builder.create()
        .blockPublicAcls(false)
        .blockPublicPolicy(false)
        .ignorePublicAcls(false)
        .restrictPublicBuckets(false)
        .build()) 
      .removalPolicy(RemovalPolicy.DESTROY)
      .autoDeleteObjects(true)
    .build();

    boolean isPolicyAdded = addResourcePolicy(bucket);

    CfnOutput.Builder.create(this, "add_resource_policy_status")
      .value(isPolicyAdded ? "success" : "fail").build();

    BucketDeployment.Builder.create(this, "dev-initial-deployment")
         .sources(List.of(Source.asset("./init")))
         .destinationBucket(bucket)
         .build();
  }

  private boolean addResourcePolicy(Bucket b) {
    var result = b.addToResourcePolicy(
      PolicyStatement.Builder.create()
        .actions(List.of("s3:*"))
        .principals(List.of(new ServicePrincipal("lambda.amazonaws.com")))
        .resources(List.of(b.getBucketArn(), b.arnForObjects("*")))
      .build()
    );

    return result.getStatementAdded();
  }

}
