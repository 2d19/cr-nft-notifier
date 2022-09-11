package com.bla.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers
@RunWith(MockitoJUnitRunner.class)
public class LambdaIntegrationTest {
  /**
   * Use junit4 syntax to test testcontainers combination of localstack (for lambda) + mockserver
   * (for external apis opensea + telegram). Start two containers (localstack + mockserver), create
   * the function and invoke the lambda. The lambda should call the opensea mock several times.
   * Finally, the result of these calls should be sent to the telegram mock. Check if all processing
   * went OK and the telegram message is as expected.
   */
  public static MockServerContainer mockServerContainer =
      new MockServerContainer(DockerImageName.parse("mockserver/mockserver"))
          .withFileSystemBind(
              "src/test/resources/mockserver/initializer.json", // use preconfigured http mappings
              "/config/initializer.json",
              BindMode.READ_ONLY)
          .withEnv(Map.of("MOCKSERVER_INITIALIZATION_JSON_PATH", "/config/initializer.json"))
          .withStartupTimeout(Duration.ofSeconds(25));

  public static LocalStackContainer localStackContainer =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
          .dependsOn(mockServerContainer)
          .withServices(LocalStackContainer.Service.LAMBDA)
          .withStartupTimeout(Duration.ofSeconds(25))
          .withCopyFileToContainer(
              MountableFile.forHostPath(
                  new File("target/cr-nft-notifier-1.0-SNAPSHOT.jar").getPath()),
              "/opt/code/localstack/lambda.jar");

  @Before
  public void setup() {
    mockServerContainer.start();
    localStackContainer.start();
    System.out.println("Test mockserver on: " + mockServerContainer.getEndpoint() + "/test");
  }

  @After
  public void tearDown() {
    Stream.of(mockServerContainer, localStackContainer).parallel().forEach(GenericContainer::stop);
  }

  @Test
  public void lambda_success() throws IOException, InterruptedException {
    localStackContainer.execInContainer(
        "awslocal",
        "lambda",
        "create-function",
        "--function-name",
        "crNftNotifier",
        "--runtime",
        "java11",
        "--handler",
        "com.bla.handlers.CronHandler::handleRequest",
        "--role",
        "arn:aws:lambda:us-east-1:000000000000:function:crNftNotifer",
        "--zip-file",
        "fileb://lambda.jar",
        "--architecture",
        "x86_64",
        "--timeout",
        "25",
        "--environment",
        "Variables={AWS_REGION=\"eu-central-1\","
            + "OPENSEA_COLLECTION_URL=\"http://host.docker.internal:"
            + mockServerContainer.getServerPort()
            + "/opensea/\","
            + "TG_URL=\"http://host.docker.internal:"
            + mockServerContainer.getServerPort()
            + "/telegram/\","
            + "TG_CHAT_ID=\"tg_mocked_chat_id\","
            + "COLLECTIONS=\"collection_1,collection_2\"}");
    localStackContainer.execInContainer(
        "awslocal", "lambda", "invoke", "--function-name", "crNftNotifier", "output0.json");

    final var mockServerLogs = mockServerContainer.getLogs();
    checkMockServerLogs(mockServerLogs);

    // Check expected result of lambda in localstack logs
    final var localStackLogs = localStackContainer.getLogs();
    checkLocalStackLogs(localStackLogs);
  }

  private void checkMockServerLogs(final String mockServerLogs) {
    // Check if lambda called collection endpoint for both collections
    assertTrue(
        mockServerLogs.contains(
            "received request:\n"
                + "\n"
                + "  {\n"
                + "    \"method\" : \"GET\",\n"
                + "    \"path\" : \"/opensea/collection_1/stats\","));
    assertTrue(
        mockServerLogs.contains(
            "received request:\n"
                + "\n"
                + "  {\n"
                + "    \"method\" : \"GET\",\n"
                + "    \"path\" : \"/opensea/collection_2/stats\","));

    // Check if lambda pushed message to telegram containing 1 collection of interest.
    assertTrue(
        mockServerLogs.contains(
            "returning response:\n"
                + "\n"
                + "  {\n"
                + "    \"statusCode\" : 200\n"
                + "  }\n"
                + "\n"
                + " for request:\n"
                + "\n"
                + "  {\n"
                + "    \"method\" : \"POST\",\n"
                + "    \"path\" : \"/telegram/sendMessage\",\n"
                + "    \"queryStringParameters\" : {\n"
                + "      \"text\" : [ \"<b>Collections of interest</b>: \\n1. <a href=\\\"https://opensea.io/collection/collection_1\\\">collection_1</a>, <i>floor</i> = 0.03, <i>change</i> = 5.61 \\n\\nData gathered from <a href=\\\"https://opensea.io/\\\">opensea</a>\" ],\n"
                + "      \"parse_mode\" : [ \"html\" ],\n"
                + "      \"chat_id\" : [ \"tg_mocked_chat_id\" ]"));
  }

  private void checkLocalStackLogs(final String localStackLogs) {
    assertTrue(localStackLogs.contains("AWS lambda.CreateFunction => 200"));
    assertTrue(localStackLogs.contains("AWS lambda.Invoke => 200"));
  }
}
