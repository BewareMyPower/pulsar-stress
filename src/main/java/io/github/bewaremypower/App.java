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
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Command(
    name = "pulsar-stress",
    mixinStandardHelpOptions = true,
    version = "1.0-SNAPSHOT",
    description = "Pulsar stress testing tool",
    subcommands = {ProduceCommand.class, ConsumeCommand.class, AdminCommand.class})
public class App implements Callable<Integer> {

  @Option(
      names = {"--token"},
      description = "Authentication token")
  @Getter
  private String token;

  @Option(
      names = {"-r", "--ranges"},
      description = "Range of suffixes (e.g., 3..5 to operate on name-3, name-4, name-5)")
  private String ranges;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() {
    // When no subcommand is specified, show usage
    new CommandLine(this).usage(System.out);
    return 0;
  }

  public List<String> expandNames(String baseName) {
    if (ranges == null || ranges.isEmpty()) {
      return List.of(baseName);
    }

    final var rangePattern = Pattern.compile("(\\d+)\\.\\.(\\d+)");
    final var matcher = rangePattern.matcher(ranges);

    if (!matcher.matches()) {
      log.error("Invalid range format: '{}'. Expected format: start..end (e.g., 3..5)", ranges);
      throw new IllegalArgumentException("Invalid range format: " + ranges);
    }

    final int start = Integer.parseInt(matcher.group(1));
    final int end = Integer.parseInt(matcher.group(2));

    if (start > end) {
      log.error("Invalid range: start ({}) must be less than or equal to end ({})", start, end);
      throw new IllegalArgumentException("Invalid range: start must be <= end");
    }

    final List<String> names = new ArrayList<>();
    for (int i = start; i <= end; i++) {
      names.add(baseName + "-" + i);
    }

    return names;
  }
}
