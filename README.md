# pulsar-stress

A simple tool to generate load on Apache Pulsar clusters for testing and benchmarking purposes.

## How to build

```bash
mvn clean package -DskipTests
```

By default, it will perform license check via Apache Rat and code formatting check via Spotless. Add the `-Drat.skip=true` and `-Dspotless.apply.skip=true` flags to skip these checks:

```bash
mvn clean package -DskipTests -Drat.skip=true -Dspotless.apply.skip=true
```
