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

import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class Client {

  @CommandLine.ParentCommand private App parent;

  @Option(
      names = {"--url"},
      description = "Pulsar service URL",
      defaultValue = "pulsar://localhost:6650")
  private String url;

  protected PulsarClient createClient() throws PulsarClientException {
    final var builder = PulsarClient.builder().serviceUrl(url).statsInterval(0, TimeUnit.SECONDS);
    if (parent.getToken() != null) {
      builder.authentication(AuthenticationFactory.token(parent.getToken()));
    }
    return builder.build();
  }
}
