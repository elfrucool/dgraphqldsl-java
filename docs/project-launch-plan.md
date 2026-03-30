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

| Goal             | Priority | Impact                     |
| ---------------- | -------- | -------------------------- |
| Package rename   | High     | Breaking change - do first |
| Java version fix | High     | Consistency                |
| Javadocs         | High     | Usability                  |
| GitHub setup     | High     | CI/CD                      |
| Maven Central    | High     | Distribution               |
| Tutorial/READMEs | Medium   | Onboarding                 |
| Code quality     | Low      | Polish                     |

---

## 1. Package Rename

**Purpose**: Align package with project group ID (`com.github.elfrucool`)

| Task                    | Description                                             |
| ----------------------- | ------------------------------------------------------- |
| Rename package          | `org.frunix.dgraphql` → `com.github.elfrucool.dgraphql` |
| Update imports          | Main source, tests, examples                            |
| Update build.gradle.kts | Ensure package matches                                  |
| Update AGENTS.md        | Reflect new package in docs                             |

**Note**: This is a breaking change for any existing users.

---

## 2. Java Version (Document as Java 21)

**Purpose**: Use Java 21 for broader adoption (not Java 25)

| Task                    | Description                                |
| ----------------------- | ------------------------------------------ |
| Update AGENTS.md        | Document Java 21 (not 25) for broader compatibility |
| ✅ Already correct      | build.gradle.kts already uses Java 21       |

---

## 3. Add Javadocs

**Purpose**: Document all public APIs

| Task                  | Description                                  | Priority |
| --------------------- | -------------------------------------------- | -------- |
| Document classes      | Query, QueryBlock, Block, Func, Filter, etc. | High     |
| Document methods      | All public methods on DSL classes            | High     |
| Add package-info.java | Package-level documentation                  | Medium   |

---

## 4. GitHub Repository Setup

**Purpose**: Establish proper project governance and CI/CD

### CI Workflow

| Task              | Description                                 |
| ----------------- | ------------------------------------------- |
| Add CI workflow   | GitHub Actions to run tests on PRs/pushes   |
| Branch protection | Require reviews, passing tests before merge |

### Templates and Policies

| Task               | Description                           |
| ------------------ | ------------------------------------- |
| CONTRIBUTING.md    | Guidelines for submitting PRs         |
| Issue templates    | Bug report, Feature request templates |
| PR template        | Standard PR description format        |
| Security policy    | How to report vulnerabilities         |
| CODE_OF_CONDUCT.md | Community guidelines                  |

### Files to Create

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

| Task             | Description                                                    |
| ---------------- | -------------------------------------------------------------- |
| POM metadata     | Add developers, SCM, license info for Maven Central acceptance |
| Signing          | Configure JAR signing for release builds                       |
| Javadoc JAR      | Generate and publish Javadoc                                   |
| Test publication | Publish snapshot to GitHub Packages for verification           |
| Release process  | Document how to make releases (version bump, tag, publish)     |

**Notes**:

- Group ID `com.github.elfrucool` is already set - works for both GitHub Packages and Maven Central
- Need to register with Maven Central (Sonatype) for the group ID

---

## 6. Tutorial and README Improvements

**Purpose**: Make the library accessible to beginners

### Tutorial

| Task                | Description                             |
| ------------------- | --------------------------------------- |
| Create tutorial doc | Step-by-step guide for common use cases |

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

---

## 7. Code Quality Tools

**Purpose**: Professional-grade codebase

| Task                    | Description                     |
| ----------------------- | ------------------------------- |
| Add JaCoCo              | Code coverage reporting         |
| Add Spotless/Checkstyle | Code formatting enforcement     |
| Add dependency checker  | Renovate or similar for updates |

---

## Summary

| Phase       | Tasks                               |
| ----------- | ----------------------------------- |
| **Phase 1** | Package rename                      |
| **Phase 2** | Java version fix                    |
| **Phase 3** | Add Javadocs                        |
| **Phase 4** | GitHub setup (workflows, templates) |
| **Phase 5** | Maven Central prep (POM metadata)   |
| **Phase 6** | Tutorial + README improvements      |
| **Phase 7** | Quality tools (JaCoCo, formatting)  |
