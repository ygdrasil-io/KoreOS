---
name: code-review
description: Perform consistent code reviews for KoreOS tickets (GRA-*). Analyzes Java/Kotlin code, verifies ticket requirements, identifies issues, and generates standardized markdown reports in .review/.
version: 1.0.0
license: MIT
compatibility: Java 25+, Gradle 9.4.1+
user-invocable: true
authorized-tools:
  - read_file
  - grep
  - bash
  - write_file
  - ask_user_question
  - todo
  - task
allowed-tools:
  - read_file
  - grep
  - bash
  - write_file
  - ask_user_question
  - todo
  - task
invocation-patterns:
  - "faire une code review pour"
  - "code review for"
  - "review ticket"
  - "review GRA-"
  - "vérifier que le ticket"
---

# Code Review Skill

**Scope**: Project-local (KoreOS)  
**Maintainer**: KoreOS Team  

## Purpose

This skill provides a standardized, automated workflow for performing code reviews on KoreOS tickets. It ensures consistent quality checks, requirement verification, and report generation across all GRA-* tickets.

## When to Use

- When a ticket (GRA-*) needs code review before merge
- When you want to verify ticket requirements are met
- When you need a standardized review format
- When onboarding new contributors who need review guidance

## Usage

### Direct Invocation
```
"faire une code review pour verifier que le ticket GRA-2 est bien traité"
"code review for ticket GRA-3"
"review GRA-4 implementation"
"vérifier que le ticket GRA-5 est complet"
```

### With Context
```
"faire une code review pour ce branch"
"review current changes"
"code review focusing on error handling"
```

---

## Workflow

The skill follows a 5-phase workflow:

### Phase 1: Discover (Automatic)
1. Detect current git branch using `git symbolic-ref --short HEAD`
2. Extract ticket number from branch name (patterns: `.*/(GRA-\d+).*`, `.*GRA-\d+.*`)
3. If not found in branch, search git log for ticket mentions: `git log --oneline -20`
4. **CRITICAL**: If still not found, **IMMEDIATELY** ask user for ticket number via `ask_user_question`
   - Question: "Quel est le numéro du ticket à reviewer ? (ex: GRA-2)"
   - Pattern validation: Must match `GRA-\d+` format
   - Do NOT proceed without valid ticket number
5. Identify changed files:
   - If on feature branch: `git diff origin/master...HEAD --name-only`
   - If on main/master: `git diff HEAD~1 --name-only`
   - Fallback: `git status --porcelain` for uncommitted changes
6. Get commit history: `git log --oneline -10`
7. Get author for attribution: `git log -1 --pretty=format:'%an'`

**IMPORTANT**: This phase MUST succeed. If ticket number cannot be determined, the skill MUST ask the user and NOT proceed with a generic review.

**Tools used**: `bash`, `grep`, `ask_user_question`

### Phase 2: Orient
1. Read and categorize all changed files
2. Separate into:
   - Source files (Java/Kotlin)
   - Test files
   - Configuration files (build.gradle.kts, settings.gradle.kts)
   - Documentation files (README.md, etc.)
   - Resource files
3. Calculate statistics: total files, lines added/removed

**Tools used**: `read_file`, `bash`

### Phase 3: Analyze
1. **Code Quality Checks**:
   - Verify Javadoc on all public classes/methods
   - Check error handling (try-catch, custom exceptions)
   - Validate null checks for public method parameters
   - Check for raw types (should use generics)
   - Identify magic numbers (should be constants)
   - Detect code duplication
   - Measure method complexity (target: < 15)
   - Measure method length (target: < 60 lines)
   - Measure class length (target: < 300 lines)

2. **⚠️ CRITICAL: Execute Tests**:
   - **MUST run** `./gradlew test` or `./gradlew :<module>:test` for all affected modules
   - **MUST run** `./gradlew :<module>:compileTestKotlin` / `compileTestJava` to verify test compilation
   - Document any test failures with root cause (e.g., missing native library, environment issue)
   - **BLOCKING**: Tests that fail due to code errors (not environment) MUST be fixed before approval

3. **FFM-Specific Checks** (Foreign Function & Memory API):
   - Verify Arena scopes are properly managed
   - Check MemorySegment.NULL checks before use
   - Validate downcall handles are cached appropriately
   - Verify function descriptors match native signatures
   - Ensure no direct MemoryAddress usage (use MemorySegment)

3. **Resource Management**:
   - Verify AutoCloseable implemented where appropriate
   - Check resources are properly closed in all code paths
   - Detect potential resource leaks

4. **Thread Safety**:
   - Identify shared mutable state
   - Check for proper synchronization
   - Verify volatile flags for shared state

**Tools used**: `read_file`, `grep`

### Phase 4: Verify Ticket Requirements
1. Extract requirements from:
   - Ticket title and description (if accessible)
   - Commit messages mentioning the ticket
   - Branch name conventions

2. For each identified requirement:
   - [x] Fully implemented
   - [~] Partially implemented (note what's missing)
   - [ ] Not implemented

3. Check for:
   - Feature completeness
   - Error handling as specified
   - Documentation requirements
   - Test coverage requirements
   - **⚠️ CRITICAL**: Verify tests pass or document blocking issues (environment vs. code bugs)

**Tools used**: `bash`, `grep`

### Phase 5: Generate Report
1. Create report directory: `.review/`
2. Generate report file: `.review/{TICKET}-code-review.md`
3. Populate using the report template
4. Include:
   - Summary of all changes (table format)
   - Strengths (what's done well)
   - Critical issues (must fix before merge)
   - Optional improvements
   - Quality metrics
   - Ticket requirement verification
   - Technical analysis with ASCII diagrams
   - Clear recommendations and conclusion

**Tools used**: `write_file`

---

## Report Template

```markdown
# Code Review - {TICKET}: {Title}

**Ticket**: {TICKET} - {Title}  
**Author**: {Author from git}  
**Date**: {YYYY-MM-DD}  
**Status**: {✅ APPROVED | ✅ APPROVED WITH REMARKS | ❌ NEEDS WORK}

---

## 📋 Summary of Changes

| File | Type | Lines | Description |
|------|------|-------|-------------|
| {file} | {New/Modified} | {+X/-Y} | {description} |

**Total**: X files, +Y lines, -Z lines

---

## ✅ Strengths

### 1. {Category}
- {Specific strength}

### 2. {Category}
- {Specific strength}

---

## ⚠️ Issues and Improvements

### 🟡 Critical - Fix Before Merge

| # | File | Line | Issue | Severity | Solution |
|---|------|------|-------|----------|----------|
| CR-01 | {file} | {line} | {description} | {High/Medium} | {solution} |

### 🟢 Improvements - Optional

| # | File | Line | Suggestion | Impact |
|---|------|------|------------|--------|
| AM-01 | {file} | {line} | {description} | {Low} |

---

## 📊 Quality Metrics

| Metric | Value | Status | Threshold |
|--------|-------|--------|-----------|
| Test Coverage | X% | {✅/⚠️/❌} | >80% |
| Max Complexity | {N} | {✅/⚠️} | <30 |
| Max Method Length | {N} lines | {✅/⚠️} | <60 |
| Max Class Length | {N} lines | {✅/⚠️} | <300 |
| Code Duplication | {None/Detected} | {✅/⚠️} | None |
| Detekt Issues | {N} | {✅/❌} | 0 |

---

## 🎯 Ticket Requirement Verification

### ✅ Satisfied
- [x] {Requirement}: {Implementation details}

### ⚠️ Partial
- [~] {Requirement}: {What's done, what's missing}

### ❌ Not Satisfied
- [ ] {Requirement}: {Reason}

---

## 🔍 Technical Analysis

### {Component/Class Name}
\`\`\`
✅ Strengths:
  - {strength 1}
  - {strength 2}

⚠️ Risks/Concerns:
  - {risk 1}
  - Mitigation: {solution}
\`\`\`

### Architecture Diagram
\`\`\`
{ASCII diagram showing component relationships}
\`\`\`

---

## 📝 Recommendations

### Before Merge
1. {Action 1}
2. {Action 2}

### Future Versions
1. {Suggestion 1}
2. {Suggestion 2}

---

## ✅ Conclusion

**The ticket {TICKET} is {WELL HANDLED | PARTIALLY HANDLED | NOT READY}**

The implementation:
- {✅/❌} Meets all functional requirements
- {✅/❌} Is production-ready (after fixes)
- {✅/❌} Follows Java/Kotlin best practices
- {✅/❌} Follows project conventions (FFM, etc.)
- {✅/❌} Is well tested and documented

**Action**: {Merge after fixes | Merge as-is | Needs major rework | Request changes}

---

*Review by: {Reviewer}*  
*Date: {YYYY-MM-DD}*
```

---

## Checklists

### Code Quality Checklist
- [ ] All public classes have Javadoc with `@param` and `@return`
- [ ] All public methods have Javadoc
- [ ] Error handling is present and appropriate for all failure modes
- [ ] Null checks for all public method parameters (or `@NotNull` annotations)
- [ ] No raw types used (generics properly specified)
- [ ] No magic numbers (use named constants)
- [ ] No duplicated code blocks
- [ ] Method complexity under 15 (detekt: <30)
- [ ] Method length under 60 lines
- [ ] Class length under 300 lines
- [ ] Package declarations match project structure (`io.ygdrasil.koreos`)
- [ ] SPDX license identifier present in all source files

### ⚠️ Testing Checklist (CRITICAL - MUST VERIFY)
**IMPORTANT**: Tests execution is MANDATORY. Do NOT approve without running tests.

- [ ] **✅ MUST RUN**: All tests execute via `./gradlew test` or module-specific test tasks
- [ ] **✅ MUST VERIFY**: Test compilation succeeds for all new/modified test files
- [ ] Unit tests exist for all new/modified public classes
- [ ] Unit tests exist for all new/modified public methods
- [ ] Edge cases tested (null, empty, boundary values, error conditions)
- [ ] Tests are isolated (proper `@BeforeEach`/`@AfterEach` setup/teardown)
- [ ] Test names are descriptive (follow `methodUnderTest_scenario_expectedBehavior`)
- [ ] Assertions are specific (not just `assertNotNull`, use meaningful matches)
- [ ] Test coverage >80% for new code (where applicable)
- [ ] Integration tests added for component interactions
- [ ] **❌ BLOCKING**: Any test failure due to code logic (not environment) blocks approval

### Resource Management Checklist
- [ ] `AutoCloseable` implemented for resources requiring cleanup
- [ ] Resources are properly closed in all code paths (try-finally or try-with-resources)
- [ ] No resource leaks (MemorySegment, FileHandle, Socket, etc.)
- [ ] Shared resources are thread-safe (synchronized, volatile, or thread-local)
- [ ] Static mutable state is properly managed

### FFM-Specific Checklist
- [ ] Arena scopes are properly managed (not leaked)
- [ ] MemorySegment.NULL checks before dereferencing
- [ ] Downcall handles are cached for performance
- [ ] Function descriptors correctly match native library signatures
- [ ] No direct MemoryAddress usage (use MemorySegment)
- [ ] Arena.ofAuto() used appropriately (not stored in static fields)
- [ ] Linker.nativeLinker() used for downcall handles
- [ ] SymbolLookup properly configured for library loading

### Documentation Checklist
- [ ] README updated if new features added
- [ ] Example usage provided for new APIs
- [ ] Complex logic has inline comments explaining the why
- [ ] TODO/FIXME comments addressed or converted to issues
- [ ] Breaking changes documented
- [ ] Migration guide provided if applicable

---

## Project-Specific Rules (KoreOS)

1. **Java Version**: Java 25+ required (for FFM API)
2. **License**: MIT (SPDX-License-Identifier: MIT in all source files)
3. **Package**: `io.ygdrasil.koreos` for all new code
4. **Build Tool**: Gradle 9.4.1+ with Kotlin DSL
5. **Test Framework**: JUnit 5 (`org.junit.jupiter`)
6. **Code Style**: detekt configuration in project root (`detekt.yml`)
7. **Modules**: Multi-module project structure (shared, demo, etc.)
8. **FFM**: Foreign Function & Memory API for native bindings
9. **LibClang**: LLVM/Clang 17+ for native library

---

## Commands Reference

```bash
# Get current branch
git symbolic-ref --short HEAD

# Get changed files compared to master
git diff origin/master...HEAD --name-only

# Get changed files in last commit
git diff HEAD~1 --name-only

# Get commit messages
git log --oneline -10

# Get author of last commit
git log -1 --pretty=format:'%an'

# Run detekt static analysis
./gradlew detekt

# ⚠️ CRITICAL: Run tests (MUST execute for code review)
./gradlew test

# Run specific tests (MUST run for affected modules)
./gradlew :shared:clang-wrapper:test

# Compile tests only (to verify test code compiles)
./gradlew compileTestKotlin
./gradlew compileTestJava

# Check code formatting
./gradlew spotlessCheck

# Get file statistics
wc -l path/to/file.java

# Find usages of a symbol
grep -r "symbolName" --include="*.java" --include="*.kt" .

# Check for TODO comments
grep -r "TODO" --include="*.java" --include="*.kt" .
```

---

## Example Review Sessions

### Example 1: Ticket Detected from Branch Name

**User Input:**
```
User: "faire une code review pour verifier que le ticket GRA-3 est bien traité"
```

**Skill Execution Flow:**
```
1. Parse user request
   - Extract ticket: "GRA-3"
   
2. Phase 1: Discover
   - Get current branch: git symbolic-ref --short HEAD
   - Branch is: alexandremommers/gra-3-cursor-implementation
   - Extract ticket: GRA-3 ✓
   - Get changed files: git diff origin/master...HEAD --name-only
   - Files: ClangCursor.java, ClangCursorTest.java, example-usage.c
   
3. Phase 2: Orient
   - Read ClangCursor.java (41 lines, new file)
   - Read ClangCursorTest.java (120 lines, new file)
   - Read example-usage.c (25 lines, new file)
   - Categorize: 2 source, 1 test, 0 config
   
4. Phase 3: Analyze
   - Check Javadoc: ✅ All public methods documented
   - Check null checks: ✅ Constructor validates parameters
   - Check complexity: ✅ All methods < 10
   - Check FFM usage: ✅ Proper MemorySegment handling
   - Check resources: ✅ AutoCloseable not needed (value object)
   
5. Phase 4: Verify Requirements
   - GRA-3.1: Implement cursor traversal - [x] Done
   - GRA-3.2: Add cursor kind detection - [x] Done
   - GRA-3.3: Add cursor spelling - [~] Partial (placeholder)
   - GRA-3.4: Add unit tests - [x] Done
   
6. Phase 5: Generate Report
   - Create .review/GRA-3-code-review.md
   - Populate with findings
   - Status: ✅ APPROVED WITH REMARKS
   
7. Output to user:
   - Report generated: .review/GRA-3-code-review.md
   - Status: APPROVED WITH REMARKS
   - 0 critical issues, 2 optional improvements
```

### Example 2: Ticket NOT in Branch Name (User Prompted)

**User Input:**
```
User: "faire une code review"
```

**Skill Execution Flow:**
```
1. Parse user request
   - No ticket number in request
   
2. Phase 1: Discover
   - Get current branch: git symbolic-ref --short HEAD
   - Branch is: main (or feature/improvements, no ticket pattern)
   - Search git log: git log --oneline -20
   - No GRA-* pattern found in recent commits
   
   **CRITICAL STEP:**
   - ask_user_question triggered
   - Question: "Quel est le numéro du ticket à reviewer ? (ex: GRA-2)"
   - User responds: "GRA-5"
   - Validation: matches GRA-\d+ pattern ✓
   
3. Continue with Phase 2-5 using GRA-5
4. Generate report: .review/GRA-5-code-review.md
```

### Example 3: Review Specific Ticket (Explicit)

**User Input:**
```
User: "code review for GRA-4"
```

**Skill Execution Flow:**
```
1. Parse user request
   - Extract ticket: "GRA-4" from request ✓
   
2. Phase 1: Discover
   - Skip branch detection (ticket provided)
   - Search git log for GRA-4: git log --oneline -20 --all
   - Find commits: "feat(GRA-4): add diagnostic support"
   - Get changed files from those commits
   - Files: ClangDiagnostic.java, ClangDiagnosticTest.java
   
3. Continue with Phases 2-5 for GRA-4
4. Generate report: .review/GRA-4-code-review.md
```

---

## Dependencies

This skill requires:
- Git repository (for branch/commit detection)
- Java/Kotlin source files (for analysis)
- Standard project structure (recommended)
- Gradle project (optional, for detekt/test commands)

---

## Customization

To customize this skill for KoreOS:

1. **Update Project-Specific Rules**:
   - Modify the "Project-Specific Rules (KoreOS)" section
   - Add/remove rules as needed

2. **Update Checklists**:
   - Add project-specific checks to the checklists
   - Remove irrelevant checks

3. **Update Report Template**:
   - Modify the report template to match project preferences
   - Add/remove sections as needed

4. **Add Invocation Patterns**:
   - Add more patterns to the `invocation-patterns` in frontmatter
   - Support French and English requests

5. **Configure Tools**:
   - Add/remove tools from `allowed-tools` as needed
   - Ensure all required tools are listed

---

## Related Skills

This skill can work with:
- `test-writer`: For generating test cases
- `documentation`: For improving documentation
- `refactor`: For code improvement suggestions

---

## Version History

- **1.0.0** (2025-05-04): Initial version for KoreOS project
  - Added full workflow for GRA-* ticket reviews
  - Added comprehensive checklists
  - Added report template
  - Added FFM-specific checks
