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
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import picocli.CommandLine.Command;

@Slf4j
@Command(name = "consume", description = "Consume messages from a Pulsar topic")
public class ConsumeCommand extends Client implements Callable<Integer> {

  @Override
  public Integer call() throws PulsarClientException {
    @Cleanup final var client = createClient();
    @Cleanup
    final var consumer =
        client
            .newConsumer()
            .topic("my-topic")
            .subscriptionName("sub")
            .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
            .subscribe();
    while (true) {
      final var msg = consumer.receive(3, TimeUnit.SECONDS);
      if (msg == null) {
        log.info("Exit the receive loop since it cannot receive a message in 3 seconds.");
        break;
      }
      log.info("Received message {} from {}", new String(msg.getData()), msg.getMessageId());
      consumer.acknowledge(msg);
    }
    return 0;
  }
}
