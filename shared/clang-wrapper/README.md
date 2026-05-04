# KoreOS Clang Wrapper Module

This module provides Java 21+ FFM-based bindings for libclang, enabling type-safe C/C++ code analysis within the KoreOS ecosystem.

## Part of GRA-1
This is the initial implementation for GRA-1: Study and initial configuration for wrapping Clang with FFM.

## Features
- Low-level libclang bindings using Java 21 Foreign Function & Memory API
- Type-safe memory management with Arena
- Cross-platform support (Linux, macOS, Windows)

## Requirements
- Java 21+
- LLVM/Clang 17+
- libclang development files

## Building
```bash
./gradlew :shared:clang-wrapper:build
```

## Usage
See ClangFFMWrapper.java for API documentation.

## Configuration
See docs/clang-ffm/setup-clang-ffm.md for setup instructions.

## Architecture
See docs/clang-ffm/architecture.md for design details.