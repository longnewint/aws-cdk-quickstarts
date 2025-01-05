package newint.devinfra;

import java.util.List;

import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.ContainerDefinitionOptions;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.Ec2TaskDefinition;
import software.amazon.awscdk.services.ecs.NetworkMode;
import software.amazon.awscdk.services.ecs.PortMapping;
import software.constructs.Construct;

public class TaskConstruct extends Construct {
  public TaskConstruct(final Construct scope, final String id) {
    super(scope, id);

    var repo = Repository.Builder.create(scope, "repo-construct")
      .repositoryName("newint/dev-repo")
    .build();
    
    var ec2TaskDefinition = Ec2TaskDefinition.Builder
      .create(this, "task")
      .networkMode(NetworkMode.BRIDGE)
    .build();

    var portList = List.of(PortMapping.builder().hostPort(8080).containerPort(8080).build());

    var ctnOptions = ContainerDefinitionOptions.builder()
      .image(ContainerImage.fromEcrRepository(repo))
      .cpu(512)
      .memoryLimitMiB(450)
      .portMappings(portList)
    .build();

    ec2TaskDefinition.addContainer("dev-container", ctnOptions);
  }
}
