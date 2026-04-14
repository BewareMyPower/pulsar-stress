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
import java.util.concurrent.TimeUnit;
import lombok.Cleanup;
import org.apache.pulsar.common.util.RateLimiter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(
    name = "set-message-ttl-latency",
    description = "Continuously set topic messageTTLInSeconds and report latency")
public class SetTopicMessageTtlLatencyCommand implements Callable<Integer> {

  @ParentCommand private AdminCommand adminCommand;

  @Parameters(index = "0", description = "Topic name")
  private String topic;

  @Option(
      names = {"--ttl"},
      description = "Base messageTTLInSeconds",
      defaultValue = "60")
  private int ttl;

  @Option(
      names = {"--rate"},
      description = "Operations per second",
      defaultValue = "100")
  private int rate;

  @Option(
      names = {"-n"},
      description = "Number of operations",
      defaultValue = "100")
  private int numOperations;

  @Override
  public Integer call() throws Exception {
    @Cleanup final var admin = adminCommand.createAdmin();
    @Cleanup
    final var rateLimiter =
        RateLimiter.builder().rateTime(1).timeUnit(TimeUnit.SECONDS).permits(rate).build();

    final var topics =
        adminCommand.parent.getExpandedTopicNames(topic).stream()
            .map(topicName -> topicName.toString())
            .toList();
    final var stats = new LatencyStats();

    for (int i = 0; i < numOperations; i++) {
      rateLimiter.acquire();
      final var topicName = topics.get(i % topics.size());
      final var ttlToSet = ttl + (i % 2);
      final long startNanos = System.nanoTime();
      admin.topicPolicies().setMessageTTL(topicName, ttlToSet);
      stats.record(System.nanoTime() - startNanos);
    }

    System.out.printf(
        "set-message-ttl-latency topics=%d rate=%d ops=%d %s%n",
        topics.size(), rate, numOperations, stats.summary());
    return 0;
  }
}
