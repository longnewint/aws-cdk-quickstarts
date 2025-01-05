package newint.devinfra;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class FunctionURLStack extends Stack {
  public FunctionURLStack(final Construct scope, final String id, final StackProps props) {
    super(scope, id, props);

    var lambdaConstruct = new LambdaConstruct(this, "dev-lambda-construct");

    String url = lambdaConstruct.exposeURL();

    CfnOutput.Builder.create(this, "lambda_url").value(url).build();
  }
}
