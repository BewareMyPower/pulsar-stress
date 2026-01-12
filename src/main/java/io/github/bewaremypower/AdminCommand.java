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
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "admin",
    description = "Admin operations for Pulsar topics",
    subcommands = {CreateTopicCommand.class, DeleteTopicCommand.class})
public class AdminCommand implements Callable<Integer> {

  @CommandLine.ParentCommand App parent;

  @Override
  public Integer call() {
    // When no subcommand is specified, show usage
    new CommandLine(this).usage(System.out);
    return 0;
  }
}
