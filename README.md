# Quick-app (qa)

Quick-app is a modern CLI tool designed to streamline the process of generating and managing application structures.

## About the project

The operating principle of **Quick-app** is inspired by how **Helm** works in the Kubernetes world. Similar to how Helm
manages "charts", Quick-app allows you to define component templates. This enables you to easily repeat and parameterize
the process of creating components for your system or source code.

The project consists of two main modules:

- `qa-core`: The core business logic and template engine.
- `qa-ctl`: Command-line interface (CLI) enabling interaction with the tool.

## Installation

You can quickly install Quick-app using the following command:

```bash
curl -sSfL https://get.quick-app.solidify.pl/ | bash
```
