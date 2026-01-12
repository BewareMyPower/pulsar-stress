# pulsar-stress

A simple tool to generate load on Apache Pulsar clusters for testing and benchmarking purposes.

## How to build

```bash
mvn clean package -DskipTests
```

By default, it will perform license check and code formatting check via Spotless. Add the `-Dspotless.skip=true` flags to skip these checks.
