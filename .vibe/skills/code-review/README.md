# Code Review Skill for KoreOS

> **Version**: 1.0.0  
> **License**: MIT  
> **Compatibility**: Java 21+, Gradle 8+  
> **Status**: ✅ Ready for use

---

## 🚀 Quick Start

This skill provides a **standardized, automated workflow** for performing code reviews on KoreOS tickets (GRA-*). 

### Basic Usage

Simply ask in French or English:

```bash
# French
"faire une code review pour verifier que le ticket GRA-2 est bien traité"
"vérifier que le ticket GRA-3 est complet"

# English  
"code review for ticket GRA-2"
"review GRA-3 implementation"
"review ticket GRA-4"
```

The skill will:
1. ✅ **Detect the current branch and extract the ticket number** (CRITICAL: will ask if not found)
2. ✅ Identify all changed files
3. ✅ Analyze code quality, tests, and documentation
4. ✅ Verify ticket requirements are met
5. ✅ Generate a **standardized markdown report** in `.review/{TICKET}-code-review.md`

---

### ⚠️ IMPORTANT: Ticket Detection

**The skill MUST know which ticket to review.** It tries to auto-detect from:

1. **Branch name patterns** (in order):
   - `alexandremommers/gra-2-koreos-implementation` → `GRA-2`
   - `feature/GRA-3-cursor-traversal` → `GRA-3`
   - `fix/GRA-5-memory-leak` → `GRA-5`
   - `GRA-7-refactor` → `GRA-7`

2. **Git log** (last 20 commits): searches for `GRA-\d+` pattern

3. **If NOT found**: The skill **WILL ASK YOU** for the ticket number
   - Question: "Quel est le numéro du ticket à reviewer ? (ex: GRA-2)"
   - Required format: `GRA-` followed by digits
   - **Review cannot proceed without this information**

> 💡 **Tip**: Always include the ticket number in your branch name for seamless review!

---

## 📁 Structure

```
KoreOS/
├── .vibe/
│   └── skills/
│       └── code-review/
│           ├── SKILL.md      # Skill definition (YAML frontmatter + workflow)
│           └── README.md     # This file
├── .review/
│   └── GRA-2-code-review.md # Generated reports
└── ...
```

---

## 🎯 Features

### 5-Phase Workflow

| Phase | Purpose | Tools Used |
|-------|---------|------------|
| **1. Discover** | Find ticket number and changed files | `bash`, `grep`, `ask_user_question` |
| **2. Orient** | Categorize and understand changes | `read_file`, `bash` |
| **3. Analyze** | Check quality, FFM usage, resources | `read_file`, `grep` |
| **4. Verify** | Confirm ticket requirements met | `bash`, `grep` |
| **5. Generate** | Create standardized report | `write_file` |

### Checklists Included

- ✅ **Code Quality**: Javadoc, error handling, null checks, generics, magic numbers, duplication
- ✅ **Testing**: Unit tests, edge cases, isolation, naming, coverage
- ✅ **Resource Management**: AutoCloseable, cleanup, thread safety
- ✅ **FFM-Specific**: Arena scopes, MemorySegment.NULL, downcall handles, function descriptors
- ✅ **Documentation**: README, examples, complex logic, TODO comments

### Project-Specific Rules (KoreOS)

- Java 25+ required (for FFM API)
- MIT License (SPDX identifier in all files)
- Package: `io.ygdrasil.koreos`
- Gradle 9.4.1+ with Kotlin DSL
- JUnit 5 tests
- detekt for static analysis
- Multi-module structure
- LibClang 17+ for native bindings

---

## 📋 Report Format

Generated reports include:

```markdown
# Code Review - GRA-X: Title

**Ticket**: GRA-X - Description
**Author**: Commit author from git
**Date**: YYYY-MM-DD
**Status**: ✅ APPROVED | ✅ APPROVED WITH REMARKS | ❌ NEEDS WORK

---

## 📋 Summary of Changes
| File | Type | Lines | Description |

## ✅ Strengths
## ⚠️ Issues and Improvements
## 📊 Quality Metrics
## 🎯 Ticket Requirement Verification
## 🔍 Technical Analysis
## 📝 Recommendations
## ✅ Conclusion
```

---

## 🔧 Installation & Configuration

### Prerequisites

- Mistral Vibe CLI installed
- Project is a Git repository
- Java/Kotlin source files present

### Skill Discovery

Mistral Vibe automatically discovers skills in:
1. **Global**: `~/.vibe/skills/`
2. **Project-local**: `.vibe/skills/` ← This skill is here
3. **Custom paths**: Configured in `config.toml`

### Enable/Disable

In your Mistral Vibe `config.toml`:

```toml
# Enable specific skills
enabled_skills = ["code-review"]

# Or disable specific skills
disabled_skills = ["experimental-*"]

# Add custom skill paths
skill_paths = ["./custom-skills"]
```

### Tools Configuration

This skill uses and requires:
- `read_file` - Read source files
- `grep` - Search for patterns
- `bash` - Run git commands
- `write_file` - Generate reports
- `ask_user_question` - Clarify ticket number
- `todo` - Track review progress
- `task` - Delegate sub-tasks

Ensure these tools are enabled in your agent configuration.

---

## 📖 Skill Metadata

From `SKILL.md` frontmatter:

```yaml
---
name: code-review
description: Perform consistent code reviews for KoreOS tickets (GRA-*)...
version: 1.0.0
license: MIT
compatibility: Java 25+, Gradle 9.4.1+
user-invocable: true
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
```

---

## 🎨 Customization

### Add Custom Invocation Patterns

Edit `SKILL.md` frontmatter to add more patterns:

```yaml
invocation-patterns:
  - "faire une code review pour"
  - "code review for"
  - "review GRA-"
  - "auditer le ticket"      # New pattern
  - "analyser GRA-"         # New pattern
```

### Update Project-Specific Rules

Modify the "Project-Specific Rules (KoreOS)" section in `SKILL.md` to match your project's conventions.

### Modify Report Template

Edit the report template in `SKILL.md` to:
- Add/remove sections
- Change formatting
- Add project-specific metrics

### Adjust Quality Thresholds

Update the checklists in `SKILL.md`:
- Method complexity threshold (default: <15, detekt: <30)
- Method length threshold (default: <60 lines)
- Class length threshold (default: <300 lines)
- Test coverage threshold (default: >80%)

---

## 📚 Examples

### Example 1: Review Current Branch

```
User: "faire une code review pour ce branch"

Skill:
1. Detects branch: alexandremommers/gra-2-koreos-implementation
2. Extracts ticket: GRA-2
3. Finds changed files (15 files, +1030 lines)
4. Analyzes code quality
5. Verifies GRA-2 requirements
6. Generates: .review/GRA-2-code-review.md
7. Outputs: "Report generated. Status: ✅ APPROVED WITH REMARKS"
```

### Example 2: Review Specific Ticket

```
User: "code review for GRA-5"

Skill:
1. Uses GRA-5 directly (no branch detection needed)
2. Searches git log for GRA-5 mentions
3. Finds commits: "feat(GRA-5): cursor traversal"
4. Gets changed files from those commits
5. Analyzes and generates report
```

### Example 3: With Specific Focus

```
User: "review GRA-3 focusing on error handling"

Skill:
1. Performs full review
2. Pays special attention to:
   - Exception handling
   - Null checks
   - Error propagation
   - Resource cleanup on errors
```

---

## 🔍 Commands Reference

The skill uses these commands internally:

```bash
# Branch detection
git symbolic-ref --short HEAD
git log --oneline -10

# Changed files
git diff origin/master...HEAD --name-only
git diff HEAD~1 --name-only

# Code analysis (Gradle)
./gradlew detekt
./gradlew test
./gradlew spotlessCheck

# File analysis
wc -l path/to/file.java
grep -r "pattern" --include="*.java" .
```

---

## 🛠️ Troubleshooting

### Ticket Not Detected

**Problem**: Skill cannot determine which ticket to review

**Solution**:
- The skill will **automatically ask** you for the ticket number
- Provide the ticket in format: `GRA-2`, `GRA-3`, etc.
- Ensure your branch name contains the ticket number (recommended)
- Example branch names that work:
  - `feature/GRA-2-implementation`
  - `alexandremommers/gra-3-fix`
  - `GRA-5-refactor`

### Skill Not Found

**Problem**: Vibe doesn't recognize the skill

**Solution**:
- Ensure skill is in `.vibe/skills/code-review/`
- Check `SKILL.md` has valid YAML frontmatter
- Verify `name: code-review` in frontmatter
- Check `user-invocable: true` is set

### Invocation Not Working

**Problem**: Skill doesn't trigger on my request

**Solution**:
- Use exact invocation patterns from frontmatter
- Try both French and English patterns
- Check for typos in ticket number (GRA-*, not gra-*)

### Report Not Generated

**Problem**: No report file created

**Solution**:
- Check `.review/` directory exists
- Verify write permissions
- Check for errors in skill execution

### Git Commands Failing

**Problem**: Git commands return errors

**Solution**:
- Ensure you're in a git repository
- Run `git fetch` to update remote references
- Check branch exists: `git branch -a`

---

## 📝 Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-05-04 | Initial version for KoreOS |
| | | 5-phase workflow |
| | | Comprehensive checklists |
| | | Standardized report template |
| | | FFM-specific checks |
| | | Bilingual (FR/EN) support |

---

## 🤝 Contributing

To improve this skill:

1. **Fork** the KoreOS repository
2. **Modify** files in `.vibe/skills/code-review/`
3. **Test** the skill locally:
   ```bash
   # Trigger the skill
   vibe --agent auto-approve
   > faire une code review pour GRA-2
   ```
4. **Commit** and submit a PR

### Adding New Checks

Add to the appropriate checklist in `SKILL.md`:

```markdown
### New Category Checklist
- [ ] New check item 1
- [ ] New check item 2
```

### Adding New Tools

1. Add to `allowed-tools` in frontmatter
2. Document in the workflow
3. Update tool usage in phases

---

## 📄 License

This skill is part of the **KoreOS** project and is licensed under the **MIT License**.

See [LICENSE](../LICENSE) for details.

---

## 🔗 Links

- [KoreOS Repository](https://github.com/ygdrasil-to/KoreOS)
- [Mistral Vibe Documentation](https://docs.mistral.ai/mistral-vibe/agents-skills)
- [Agent Skills Specification](https://agentskills.io/specification)
- [Detekt Documentation](https://detekt.dev)
- [JUnit 5](https://junit.org/junit5/)
