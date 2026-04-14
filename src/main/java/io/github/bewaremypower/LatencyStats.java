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
import java.util.Collections;
import java.util.List;

final class LatencyStats {

  private final List<Long> latenciesNanos = new ArrayList<>();

  void record(long latencyNanos) {
    latenciesNanos.add(latencyNanos);
  }

  String summary() {
    if (latenciesNanos.isEmpty()) {
      return "count=0";
    }

    final var sorted = new ArrayList<>(latenciesNanos);
    Collections.sort(sorted);
    final long total = sorted.stream().mapToLong(Long::longValue).sum();
    final double avgMs = nanosToMillis(total) / sorted.size();
    final double p50Ms = nanosToMillis(percentile(sorted, 0.50));
    final double p95Ms = nanosToMillis(percentile(sorted, 0.95));
    final double p99Ms = nanosToMillis(percentile(sorted, 0.99));
    final double maxMs = nanosToMillis(sorted.get(sorted.size() - 1));
    return String.format(
        "count=%d avgMs=%.3f p50Ms=%.3f p95Ms=%.3f p99Ms=%.3f maxMs=%.3f",
        sorted.size(), avgMs, p50Ms, p95Ms, p99Ms, maxMs);
  }

  private long percentile(List<Long> sorted, double percentile) {
    final int index = (int) Math.ceil(percentile * sorted.size()) - 1;
    return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
  }

  private double nanosToMillis(long nanos) {
    return nanos / 1_000_000.0;
  }
}
