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
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.common.util.FutureUtil;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Slf4j
@Command(name = "create", description = "Create a partitioned topic")
public class CreateTopicCommand implements Callable<Integer> {

  @ParentCommand private AdminCommand adminCommand;

  @Parameters(index = "0", description = "Topic name")
  private String topic;

  @Option(
      names = {"-p", "--partitions"},
      description = "Number of partitions",
      defaultValue = "1")
  private int partitions;

  @Override
  public Integer call() throws Exception {
    final var admin = adminCommand.parent.getAdmin();
    final List<String> topicsToCreate = adminCommand.parent.expandNames(topic);

    final var futures = new ArrayList<CompletableFuture<Boolean>>();
    for (final var topic : topicsToCreate) {
      futures.add(
          admin
              .topics()
              .createPartitionedTopicAsync(topic, partitions)
              .thenApply(__ -> true)
              .exceptionally(
                  e -> {
                    log.warn("Failed to create partitioned topic '{}': {}", topic, e.getMessage());
                    return false;
                  }));
    }

    FutureUtil.waitForAll(futures).get(30, TimeUnit.SECONDS);
    return 0;
  }
}
