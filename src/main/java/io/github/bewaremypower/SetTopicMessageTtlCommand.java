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
import lombok.Cleanup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "set-message-ttl", description = "Set topic messageTTLInSeconds")
public class SetTopicMessageTtlCommand implements Callable<Integer> {

  @ParentCommand private AdminCommand adminCommand;

  @Parameters(index = "0", description = "Topic name")
  private String topic;

  @Option(
      names = {"--ttl"},
      description = "messageTTLInSeconds",
      required = true)
  private int ttl;

  @Override
  public Integer call() throws Exception {
    @Cleanup final var admin = adminCommand.createAdmin();
    for (final var topicName : adminCommand.parent.getExpandedTopicNames(topic)) {
      admin.topicPolicies().setMessageTTL(topicName.toString(), ttl);
      System.out.printf("%s -> %d%n", topicName, ttl);
    }
    return 0;
  }
}
