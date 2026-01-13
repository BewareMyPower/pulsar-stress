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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.util.RateLimiter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "produce", description = "Produce messages to a Pulsar topic")
@Slf4j
public class ProduceCommand extends Client implements Callable<Integer> {

  @Parameters(index = "0", description = "Topic name")
  private String topic;

  @Option(
      names = {"--rate"},
      description = "Message rate (messages per second for each topic)",
      defaultValue = "10")
  private int rate;

  @Option(
      names = {"-n"},
      description = "Number of messages to produce for each topic",
      defaultValue = "100")
  private int numMessages;

  @Override
  public Integer call() throws Exception {
    @Cleanup var client = createClient();
    @Cleanup
    final var rateLimiter =
        RateLimiter.builder().rateTime(1).timeUnit(TimeUnit.SECONDS).permits(rate).build();

    final var producers = new ArrayList<Producer<String>>();
    final var topics =
        parent.getNamespaceToTopicsMap(topic).values().stream()
            .flatMap(list -> list.stream())
            .map(tn -> tn.toString())
            .toList();

    for (final var topic : topics) {
      producers.add(client.newProducer(Schema.STRING).topic(topic).blockIfQueueFull(true).create());
    }

    for (int i = 0; i < numMessages; i++) {
      rateLimiter.acquire();
      for (final var producer : producers) {
        final var value = "msg-" + i;
        final var future =
            producer
                .sendAsync(value)
                .whenComplete(
                    (msgId, ex) -> {
                      if (ex != null) {
                        log.warn(
                            "Failed to send {} to {}: {}",
                            value,
                            producer.getTopic(),
                            ex.getMessage());
                      }
                    });
        if (i == numMessages - 1) {
          try {
            future.get();
          } catch (InterruptedException | ExecutionException ignored) {

          }
        }
      }
    }

    return 0;
  }
}
