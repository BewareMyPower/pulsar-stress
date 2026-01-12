# pulsar-stress

A simple tool to generate load on Apache Pulsar clusters for testing and benchmarking purposes.

## How to build

```bash
mvn clean package -DskipTests
```

By default, it will perform license check and code formatting check via Spotless. Add the `-Dspotless.skip=true` flags to skip these checks.

## Server side configuration

### Authentication

Add the `--token` option to specify the JWT token used in Authentication

Example:

```bash
java -jar ./target/app-*.jar --token <your-jwt-token> admin create my-topic
```

### URL

Each subcommand supports the `--url` option to specify the Pulsar service URL.

```bash
java -jar ./target/app-*.jar produce --url <service-url>
java -jar ./target/app-*.jar consume --url <service-url>
java -jar ./target/app-*.jar admin --url <admin-url> create my-topic
```

## Functions

### Create and delete a set of topics

Without this option, only 1 topic `my-topic` will be created.

You can specify the `--ranges` option to create or delete a set of topics. For example,

```bash
java -jar ./target/app-*.jar --ranges 11..50 admin create my-topic
```

Then 40 topics will be created: `my-topic-11`, `my-topic-12`, ..., `my-topic-50`.

Similarly, you can delete these topics by changing the subcommand from `create` to `delete`.

```bash
java -jar ./target/app-*.jar --ranges 11..50 admin delete my-topic
```

### Produce and Consume

You can use the `produce` and `consume` subcommands to generate load on the Pulsar cluster.

The `produce` subcommand sends messages to the target topic:

```bash
java -jar ./target/app-*.jar produce my-topic
```

or the topics specified by the `--ranges` option:

```bash
java -jar ./target/app-*.jar --ranges 11..50 produce my-topic
```

By default, the rate is 10 messages per second and 100 messages in total for each topic. The behavior can be customized via `--rate` and `-n` options.

The `consume` subcommand receives messages from the target topic:

```bash
java -jar ./target/app-*.jar consume my-topic
```

By default, the subscription name is `sub`, which can be customized via the `--sub` option.
