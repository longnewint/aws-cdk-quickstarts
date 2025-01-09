package newint.devinfra;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.ApplicationLogLevel;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.LoggingFormat;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.SnapStartConf;
import software.amazon.awscdk.services.lambda.SystemLogLevel;
import software.constructs.Construct;

public class LambdaConstruct extends Construct {
  static Map<String,String> RUNTIME_CONFIGURATION = Map.of(
    "JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1");
  static int TIMEOUT = 5 * 60;
  static int ONE_CPU = 1769;
  static int MEMORY = 1 * ONE_CPU;

  static Runtime RUNTIME = Runtime.JAVA_21;
  static Architecture ARCH = Architecture.ARM_64;
  static SnapStartConf SNAPSTART_CONF = SnapStartConf.ON_PUBLISHED_VERSIONS;

  String name = "dev_lambda_functionurl";
  String functionHandler = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
  String zipPath = "../../aws-java-app/lambda-quarkus/target/function.zip";

  private IFunction function;

  public LambdaConstruct(final Construct scope, final String id) {
    super(scope, id);

    var configuration = new HashMap<String, String>(RUNTIME_CONFIGURATION);
    var role = createExecutionRole();

    function = Function.Builder.create(this, "dev-lambda")
      .runtime(RUNTIME)
      .architecture(ARCH)
      .snapStart(SNAPSTART_CONF)
      .functionName(name)
      .handler(functionHandler)
      .code(Code.fromAsset(zipPath))
      .memorySize(MEMORY)
      .role(role)
      .environment(configuration)
      .timeout(Duration.seconds(TIMEOUT))
      .loggingFormat(LoggingFormat.JSON)
      .systemLogLevelV2(SystemLogLevel.WARN)
      .applicationLogLevelV2(ApplicationLogLevel.WARN)
    .build();
  }

  public Role createExecutionRole() {
    var role = Role.Builder.create(this, "dev-lambda-role")
      .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
    .build();

    role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"));
    role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaVPCAccessExecutionRole"));

    return role;
  }

  public String exposeURL() {
    var functionURL = function.addFunctionUrl(FunctionUrlOptions.builder()
      .authType(FunctionUrlAuthType.NONE)
    .build());

    return functionURL.getUrl();
  }

}
