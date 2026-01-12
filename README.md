# pulsar-stress

A simple tool to generate load on Apache Pulsar clusters for testing and benchmarking purposes.

## How to build

```bash
mvn clean package -DskipTests
```

Build without license check and spotless check:

```bash
mvn clean package -DskipTests -Drat.skip=true -Dspotless.check.skip=true
```

Format the files to pass the spotless check:

```bash
mvn spotless:apply
```
