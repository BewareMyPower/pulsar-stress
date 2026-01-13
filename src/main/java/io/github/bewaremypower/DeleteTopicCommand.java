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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.common.naming.TopicName;
import org.apache.pulsar.common.util.FutureUtil;
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
    @Cleanup final var admin = adminCommand.createAdmin();
    for (final var entry : adminCommand.parent.getNamespaceToTopicsMap(topic).entrySet()) {
      final var namespace = entry.getKey();
      final var topics = entry.getValue();
      log.info(
          "Deleting {} partitioned topic(s) under namespace {}",
          topics.size(),
          namespace.toString());
      deleteTopics(admin, topics);
    }
    return 0;
  }

  private void deleteTopics(PulsarAdmin admin, List<TopicName> topicsToDelete) throws Exception {
    final var futures = new ArrayList<CompletableFuture<Boolean>>();
    for (final var topic : topicsToDelete) {
      futures.add(
          admin
              .topics()
              .deletePartitionedTopicAsync(topic.toString(), true)
              .thenApply(__ -> true)
              .exceptionallyCompose(
                  e -> {
                    if (e.getCause() instanceof PulsarAdminException.ConflictException
                        && e.getMessage().contains("is a non-partitioned topic")) {
                      return admin
                          .topics()
                          .deleteAsync(topic.toString())
                          .thenApply(__ -> true)
                          .exceptionally(
                              ex -> {
                                log.warn(
                                    "Failed to delete non-partitioned topic '{}': {}",
                                    topic,
                                    ex.getMessage());
                                return false;
                              });
                    } else {
                      log.warn(
                          "Failed to delete partitioned topic '{}': {}", topic, e.getMessage());
                      return CompletableFuture.completedFuture(false);
                    }
                  }));
    }

    FutureUtil.waitForAll(futures).get(30, TimeUnit.SECONDS);
  }
}
