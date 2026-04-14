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
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Slf4j
@Command(name = "unload", description = "Unload namespace bundles to a broker")
public class UnloadNamespaceCommand implements Callable<Integer> {

  private static final String DEFAULT_BUNDLE = "0x00000000_0xffffffff";

  @ParentCommand private AdminCommand adminCommand;

  @Option(
      names = {"--broker"},
      description = "Broker service URL",
      required = true)
  private String broker;

  @Option(
      names = {"--bundle"},
      description = "Bundle range",
      defaultValue = DEFAULT_BUNDLE)
  private String bundle;

  @Override
  public Integer call() throws Exception {
    @Cleanup final var admin = adminCommand.createAdmin();
    for (final var namespace : adminCommand.parent.getNamespaces()) {
      try {
        admin.namespaces().unloadNamespaceBundle(namespace.toString(), bundle, broker);
        log.info("Unloaded namespace {} bundle {} to broker {}", namespace, bundle, broker);
      } catch (Exception e) {
        log.warn(
            "Failed to unload namespace {} bundle {} to broker {}: {}",
            namespace,
            bundle,
            broker,
            e.getMessage());
      }
    }
    return 0;
  }
}
