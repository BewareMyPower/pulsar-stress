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
import org.apache.pulsar.client.api.PulsarClientException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Slf4j
@Command(name = "delete", description = "Delete a partitioned topic")
public class DeleteTopicCommand implements Callable<Integer> {

  @ParentCommand private AdminCommand adminCommand;

  @Parameters(index = "0", description = "Topic name")
  private String topic;

  @Override
  public Integer call() throws Exception {
    try {
      final var admin = adminCommand.parent.getAdmin();
      admin.topics().deletePartitionedTopic(topic);
      log.info("Successfully deleted partitioned topic '{}'", topic);
      return 0;
    } catch (PulsarClientException e) {
      log.error("Failed to delete partitioned topic '{}': {}", topic, e.getMessage());
      return 1;
    }
  }
}
