/*
 * Copyright 2026 Yunze Xu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bewaremypower;

import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Command(
    name = "pulsar-stress",
    mixinStandardHelpOptions = true,
    version = "1.0-SNAPSHOT",
    description = "Pulsar stress testing tool",
    subcommands = {ProduceCommand.class, ConsumeCommand.class})
public class App implements Callable<Integer> {

  @Option(
      names = {"--broker-url"},
      description = "Pulsar broker service URL",
      defaultValue = "pulsar://localhost:6650")
  String brokerUrl;

  @Option(
      names = {"--token"},
      description = "Authentication token")
  String token;

  private volatile PulsarClient client;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() {
    // When no subcommand is specified, show usage
    new CommandLine(this).usage(System.out);
    return 0;
  }

  public PulsarClient getClient() throws PulsarClientException {
    if (client != null) {
      return client;
    }
    synchronized (this) {
      final var builder = PulsarClient.builder().serviceUrl(brokerUrl);
      if (token != null) {
        builder.authentication(AuthenticationFactory.token(token));
      }
      client = builder.build();
      return client;
    }
  }
}
