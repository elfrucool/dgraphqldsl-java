# Project Launch Plan

This document outlines the work required to make the dgraphqldsl-java library ready for public release.

---

## Table of Contents

1. [Package Rename](#1-package-rename)
2. [Java Version Fix](#2-java-version-fix)
3. [Add Javadocs](#3-add-javadocs)
4. [GitHub Repository Setup](#4-github-repository-setup)
5. [Maven Central Preparation](#5-maven-central-preparation)
6. [Tutorial and README Improvements](#6-tutorial-and-readme-improvements)
7. [Code Quality Tools](#7-code-quality-tools)

---

## Overview

The DSL library is technically complete but needs additional work to be useful and accessible to others as an open-source project.

| Goal             | Priority | Impact                     | Status  |
| ---------------- | -------- | -------------------------- | ------- |
| Package rename   | High     | Breaking change - do first | ✅ Done |
| Java version fix | High     | Consistency                | ✅ Done |
| Javadocs         | High     | Usability                  | ✅ Done |
| GitHub setup     | High     | CI/CD                      | ✅ Done |
| Maven Central    | High     | Distribution               | Pending |
| Tutorial/READMEs | Medium   | Onboarding                 | Pending |
| Code quality     | Low      | Polish                     | Pending |

---

## 1. Package Rename ✅

**Purpose**: Align package with project group ID (`com.github.elfrucool`)

| Task                    | Description                                             | Status  |
| ----------------------- | ------------------------------------------------------- | ------- |
| Rename package          | `org.frunix.dgraphql` → `com.github.elfrucool.dgraphql` | ✅ Done |
| Update imports          | Main source, tests, examples                            | ✅ Done |
| Update build.gradle.kts | Ensure package matches                                  | ✅ Done |
| Update AGENTS.md        | Reflect new package in docs                             | ✅ Done |
| Update this document    | Place a white check mark on completion                  | ✅ Done |

**Note**: This is a breaking change for any existing users.

---

## 2. Java Version (Document as Java 21) ✅

**Purpose**: Use Java 21 for broader adoption (not Java 25)

| Task                 | Description                                         | Status  |
| -------------------- | --------------------------------------------------- | ------- |
| Update AGENTS.md     | Document Java 21 (not 25) for broader compatibility | ✅ Done |
| ✅ Already correct   | build.gradle.kts already uses Java 21               | ✅ Done |
| Update this document | Place a white check mark on completion              | ✅ Done |

---

## 3. Add Javadocs ✅

**Purpose**: Document all public APIs

| Task                  | Description                                  | Status  |
| --------------------- | -------------------------------------------- | ------- |
| Document classes      | Query, QueryBlock, Block, Func, Filter, etc. | ✅ Done |
| Document methods      | All public methods on DSL classes            | ✅ Done |
| Add package-info.java | Package-level documentation                  | ✅ Done |
| Update this document  | Place a white check mark on completion       | ✅ Done |

---

## 4. GitHub Repository Setup ✅

**Purpose**: Establish proper project governance and CI/CD

### CI Workflow

| Task                 | Description                                 | Status                                                             |
| -------------------- | ------------------------------------------- | ------------------------------------------------------------------ |
| Add CI workflow      | GitHub Actions to run tests on PRs/pushes   | ✅ Done                                                            |
| Branch protection    | Require reviews, passing tests before merge | ✅ Done                                                            |
| CI status check      | Require CI to pass before merge             | ⏳ Pending - workflow needs to run in PR context first (see below) |
| Update this document | Place a white check mark on completion      | ✅ Done                                                            |

> **Note on CI status check**: GitHub requires the workflow to have run at least once in a PR context before it appears in the "require status checks" dropdown. This will be automatically available after the first PR is opened and CI runs on it.

### Templates and Policies

| Task                 | Description                            | Status  |
| -------------------- | -------------------------------------- | ------- |
| CONTRIBUTING.md      | Guidelines for submitting PRs          | ✅ Done |
| Issue templates      | Bug report, Feature request templates  | ✅ Done |
| PR template          | Standard PR description format         | ✅ Done |
| Security policy      | How to report vulnerabilities          | ✅ Done |
| CODE_OF_CONDUCT.md   | Community guidelines                   | ✅ Done |
| Update this document | Place a white check mark on completion | ✅ Done |

### Files Created

- `.github/workflows/ci.yml`
- `.github/ISSUE_TEMPLATE/bug.md`
- `.github/ISSUE_TEMPLATE/feature.md`
- `.github/pull_request_template.md`
- `CONTRIBUTING.md`
- `CODE_OF_CONDUCT.md`
- `SECURITY.md`

---

## 5. Maven Central Preparation

**Purpose**: Make the library easily consumable via Maven/Gradle

| Task                  | Description                                                                                                 | Status    |
| --------------------- | ----------------------------------------------------------------------------------------------------------- | --------- |
| Explain               | Explain user the activities on this phase, which of these can be performed by an agent and which are manual | ✅ Done   |
| POM metadata          | Add developers, SCM, license info for Maven Central acceptance                                              | ✅ Done   |
| Signing               | Configure JAR signing for release builds                                                                    | ✅ Done   |
| Javadoc JAR           | Generate and publish Javadoc                                                                                | ✅ Done   |
| Publish workflow      | Add GitHub Actions workflow for publishing                                                                  | ✅ Done   |
| Sonatype registration | Register with Sonatype and request group ID `com.github.elfrucool`                                          | ⏳ Manual |
| Maven secrets         | Add MAVEN_USERNAME and MAVEN_PASSWORD secrets                                                               | ⏳ Manual |
| Test publication      | Publish snapshot to Maven Central for verification                                                          | ⏳ Manual |
| Release process       | Document how to make releases (version bump, tag, publish)                                                  | ⏳ Manual |
| Update this document  | Place a white check mark on completion                                                                      | ⏳ Manual |

**Notes**:

- Group ID `com.github.elfrucool` is already set - works for both GitHub Packages and Maven Central
- Need to register with Maven Central (Sonatype) for the group ID
- GPG keys are set up, secrets added to GitHub

---

## 6. Tutorial and README Improvements

**Purpose**: Make the library accessible to beginners

### Tutorial

| Task                 | Description                             |
| -------------------- | --------------------------------------- |
| Create tutorial doc  | Step-by-step guide for common use cases |
| Update this document | Place a white check mark on completion  |

**Tutorial Topics**:

- Setting up with Maven/Gradle
- Your first query
- Using variables
- Mutations basics
- Common patterns

### README Improvements

| Task                    | Description                                        |
| ----------------------- | -------------------------------------------------- |
| Beginner-friendly intro | Simplify introduction, add "Why use this?" section |
| Expand quick start      | More copy-paste examples                           |
| FAQ section             | Common questions and answers                       |
| Add badges              | CI status, Maven Central version badges            |
| Update this document    | Place a white check mark on completion             |

---

## 7. Code Quality Tools

**Purpose**: Professional-grade codebase

| Task                    | Description                            |
| ----------------------- | -------------------------------------- |
| Add JaCoCo              | Code coverage reporting                |
| Add Spotless/Checkstyle | Code formatting enforcement            |
| Add dependency checker  | Renovate or similar for updates        |
| Update this document    | Place a white check mark on completion |

---

## Summary

| Phase       | Tasks                               | Status  |
| ----------- | ----------------------------------- | ------- |
| **Phase 1** | Package rename                      | ✅ Done |
| **Phase 2** | Java version (document as Java 21)  | ✅ Done |
| **Phase 3** | Add Javadocs                        | ✅ Done |
| **Phase 4** | GitHub setup (workflows, templates) | ✅ Done |
| **Phase 5** | Maven Central prep (POM metadata)   | Pending |
| **Phase 6** | Tutorial + README improvements      | Pending |
| **Phase 7** | Quality tools (JaCoCo, formatting)  | Pending |
