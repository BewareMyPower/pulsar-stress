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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.common.naming.TopicName;
import org.apache.pulsar.common.util.FutureUtil;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Slf4j
@Command(name = "lookup", description = "Lookup brokers for the expanded topics")
public class LookupTopicCommand implements Callable<Integer> {

  @ParentCommand private AdminCommand adminCommand;

  @Parameters(index = "0", description = "Topic name")
  private String topic;

  @Override
  public Integer call() throws Exception {
    @Cleanup final var admin = adminCommand.createAdmin();
    final var futures = new ArrayList<CompletableFuture<List<LookupResult>>>();
    for (final var entry : adminCommand.parent.getNamespaceToTopicsMap(topic).entrySet()) {
      for (final var topicName : entry.getValue()) {
        futures.add(lookupTopicAsync(admin, topicName));
      }
    }

    FutureUtil.waitForAll(futures).get(30, TimeUnit.SECONDS);
    futures.stream()
        .map(CompletableFuture::join)
        .flatMap(List::stream)
        .sorted(Comparator.comparing(LookupResult::topic))
        .forEach(result -> System.out.printf("%s -> %s%n", result.topic(), result.broker()));
    return 0;
  }

  private CompletableFuture<List<LookupResult>> lookupTopicAsync(
      PulsarAdmin admin, TopicName topicName) {
    final var topicString = topicName.toString();
    return admin
        .topics()
        .getPartitionedTopicMetadataAsync(topicString)
        .thenCompose(
            metadata -> {
              if (metadata.partitions > 0) {
                return admin
                    .lookups()
                    .lookupPartitionedTopicAsync(topicString)
                    .thenApply(
                        partitionToBroker -> toLookupResults(topicString, partitionToBroker));
              }
              return admin
                  .lookups()
                  .lookupTopicAsync(topicString)
                  .thenApply(broker -> List.of(new LookupResult(topicString, broker)));
            })
        .exceptionally(
            e -> {
              log.warn("Failed to lookup topic '{}': {}", topicString, e.getMessage());
              return List.of();
            });
  }

  private List<LookupResult> toLookupResults(
      String topicString, Map<String, String> partitionToBroker) {
    if (partitionToBroker.isEmpty()) {
      return List.of(new LookupResult(topicString, "no partition lookup result"));
    }
    return partitionToBroker.entrySet().stream()
        .map(entry -> new LookupResult(entry.getKey(), entry.getValue()))
        .toList();
  }

  private record LookupResult(String topic, String broker) {}
}
