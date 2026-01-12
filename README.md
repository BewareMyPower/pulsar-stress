# pulsar-stress

A simple tool to generate load on Apache Pulsar clusters for testing and benchmarking purposes.

## How to build

```bash
mvn clean package -DskipTests
```

By default, it will perform license check and code formatting check via Spotless. Add the `-Dspotless.skip=true` flags to skip these checks.

## Authentication

Add the `--token` option to specify the JWT token used in Authentication

Example:

```bash
java -jar ./target/app-*.jar --token <your-jwt-token> admin create my-topic
```

## Create and delete a set of topics

Without this option, only 1 topic `my-topic` wil be created.

You can specify the `--range` option to create or delete a set of topics. For example,

```bash
java -jar ./target/app-*.jar --ranges 11..50 admin create my-topic
```

Then 40 topics will be created: `my-topic-11`, `my-topic-12`, ..., `my-topic-50`.

Similarly, you can delete these topics by changing the subcommand from `create` to `delete`.

```bash
java -jar ./target/app-*.jar --ranges 11..50 admin delete my-topic
```
