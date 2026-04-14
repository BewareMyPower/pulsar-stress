# AGENTS.md

## Purpose

This repository contains a small Java CLI for Apache Pulsar stress testing and admin workflows.
The main entrypoint is `io.github.bewaremypower.App`, built as a shaded jar.

## Stack

- Java 17
- Maven
- Picocli for CLI parsing
- Pulsar client and Pulsar admin client `4.1.2`
- Lombok
- SLF4J with Log4j 1.x binding

## Repository Layout

- `src/main/java/io/github/bewaremypower/`
  - `App.java`: top-level CLI options and shared expansion logic for `--namespaces` and `--ranges`
  - `AdminCommand.java`: admin command group
  - `CreateTopicCommand.java`, `DeleteTopicCommand.java`, `LookupTopicCommand.java`, `UnloadNamespaceCommand.java`: admin subcommands
  - `ProduceCommand.java`, `ConsumeCommand.java`: load-generation commands
- `src/main/resources/log4j.properties`: logging configuration
- `pom.xml`: dependencies, formatting, and shaded-jar packaging

## Build And Verification

- Compile: `mvn -q -DskipTests compile`
- Package: `mvn clean package -DskipTests`
- Spotless formatting is applied during the Maven build lifecycle.
- If a Java edit is made, prefer verifying with at least `mvn -q -DskipTests compile`.

## Coding Guidelines

- Follow the existing Picocli command style:
  - top-level shared options in `App`
  - grouped admin subcommands under `AdminCommand`
  - `Callable<Integer>` commands returning `0` on success
- Reuse `App` helpers for namespace and topic expansion instead of duplicating parsing logic.
- For Pulsar admin operations that fan out across many topics or namespaces, prefer async admin APIs with `CompletableFuture` and `FutureUtil.waitForAll(...)`.
- Keep output and logging simple and operationally useful.
- New Java files should retain the existing Apache 2.0 license header.

## Operational Notes

- `--token` is the shared auth mechanism exposed by `App`.
- Admin commands take `--url` on the `admin` command group.
- Topic expansion semantics:
  - `--namespaces` accepts a comma-separated list like `public/ns1,public/ns2`
  - `--ranges 3..5` expands a base topic name into `name-3`, `name-4`, `name-5`
- Namespace unload-to-broker uses Pulsar namespace bundle unload APIs, not topic-level unload.

## Change Expectations

- Prefer minimal, targeted changes that match the current structure.
- Do not introduce new frameworks, testing infrastructure, or broad refactors unless explicitly requested.
- Preserve command-line compatibility unless the task explicitly asks for a behavior change.
