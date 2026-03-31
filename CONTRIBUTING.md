# Contributing to dgraphqldsl-java

Thank you for your interest in contributing to this project.

## Development Setup

1. Clone the repository
2. Install Java 21
3. Run `./gradlew build` to verify the build works
4. Run `./gradlew test` to run the test suite
5. Install taskfile [taskfile.dev](https://taskfile.dev)
6. Run `task run down` to run the examples in a container (and turn down the container)

## Code Style

- Follow the existing code conventions in the project
- Use 4 spaces for indentation
- Keep lines under 120 characters
- Add Javadocs for public APIs

## Submitting Changes

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Make your changes
4. Run tests (`./gradlew test`)
5. Commit with a clear commit message
6. Push to your fork
7. Submit a pull request

### Commit Message Style

Follow [Linus Torvalds' commit message guidelines](https://github.com/torvalds/linux/blob/master/Documentation/process/submitting-patches.rst):

- **Header line**: Explain the commit in one line (imperative mood)
  - Good: `Fix bug that causes NPE on empty input`
  - Bad: `Fixed bug`, `Adding new feature`
- **Body**: Explain _why_, not just _what_
- **Width**: Keep lines under 74 characters
- **Signed-off-by**: Add at end: `Signed-off-by: Name <email>`

## Reporting Issues

Use the GitHub issue tracker to report bugs or request features. Please include:

- A clear description of the issue
- Steps to reproduce (for bugs)
- Java version and environment details

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
