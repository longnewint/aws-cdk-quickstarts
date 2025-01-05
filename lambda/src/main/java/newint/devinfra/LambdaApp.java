package newint.devinfra;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;

public class LambdaApp {

  public static void main(final String[] args) {

    var app = new App();

    Tags.of(app).add("environment", "dev");
    Tags.of(app).add("application", "dev-lambda");

    var env = Environment.builder()
      .account("474668380944")
      .region("ca-central-1")
    .build();

    StackProps props = StackProps.builder().env(env).build();

    new FunctionURLStack( app, "dev-lambda-functionurl", props);

    app.synth();
  }
}
