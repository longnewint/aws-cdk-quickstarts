package newint.devinfra;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class TaskStack extends Stack {
  public TaskStack(final Construct scope, final String id) {
    this(scope, id, null);
  }

  public TaskStack(final Construct scope, final String id, final StackProps props) {
    super(scope, id, props);

    new TaskConstruct(this, "task-construct");
  }
}
