# Plan Feature Skill for KoreOS

> **Version**: 1.0.0  
> **License**: MIT  
> **Compatibility**: Java 25+, Gradle 9.4.1+  
> **Status**: ✅ Ready for use

---

## 🚀 Quick Start

This skill provides an **interactive workflow** for planning GRA-* ticket implementation in KoreOS.

### Basic Usage

```bash
# French
"planifier le ticket GRA-3"
"créer un plan pour GRA-4"
"nouveau ticket"

# English
"plan for ticket GRA-3"
"plan GRA-4"
"start planning"
"plan feature"
```

The skill will:
1. ✅ Ask which GRA-* ticket to plan
2. ✅ Analyze requirements and dependencies
3. ✅ Generate a structured implementation plan
4. ✅ **Create branch from master** (with safety checks)
5. ✅ Save plan to `.plan/GRA-X-implementation-plan.md`

---

## 🎯 Features

### Interactive Workflow
- **6 phases** guided process
- **Questions contextualisées** based on your answers
- **Validation** at each step
- **Bilingual support** (French/English)

### Safety First
- **ALWAYS** checks you're on master/main before creating branch
- **ALWAYS** runs `git fetch origin` first
- **NEVER** creates branch from a feature branch
- **Warns** if branch already exists

### Structured Output
- Generates **professional markdown plans**
- Includes **metadata, objectives, files, steps, estimation**
- Stores in **`.plan/`** directory for tracking

---

## 📁 Structure

```
KoreOS/
├── .vibe/
│   └── skills/
│       └── plan-feature/
│           ├── SKILL.md      # Skill definition
│           └── README.md     # This file
├── .plan/                   # Generated plans storage
│   ├── .gitkeep
│   ├── GRA-1-implementation-plan.md
│   ├── GRA-2-implementation-plan.md
│   └── GRA-3-implementation-plan.md
└── ...
```

---

## 📋 Plan Format

Generated plans include:

```markdown
# Plan d'Implémentation - GRA-3

**Ticket**: GRA-3 - Implémentation du cursor traversal
**Auteur**: user.name
**Date**: 2025-05-04
**Statut**: ⏳ À faire

---

## 📌 Métadonnées
| Propriété | Valeur |
|-----------|--------|
| Type | Wrapper FFM + API haut niveau |
| Complexité | Moyenne |
| Temps | 2 jours |
| Branche | feature/GRA-3-cursor-traversal |

## 🎯 Objectifs
- [ ] Implémenter clang_getCursor()
- [ ] Ajouter ClangCursor
- [ ] Créer tests unitaires

## 📁 Fichiers Concernés
| Fichier | Action | Type | Statut |
|---------|--------|------|--------|

## 🏗️ Étapes d'Implémentation
### Phase 1: Préparation
### Phase 2: Développement
### Phase 3: Tests
### Phase 4: Finalisation

## ⚠️ Règles de Branche (IMPORTANT)
- TOUJOURS créer depuis master/main
- TOUJOURS exécuter git fetch origin avant
- NE JAMAIS créer depuis une feature branch

## 📊 Estimation & Dépendances
...
```

---

## ⚠️ Branch Creation Rules (CRITICAL)

The skill **enforces** these rules:

1. ✅ **Always fetch first**
   ```bash
   git fetch origin
   ```

2. ✅ **Always from master/main**
   - If you're on `feature/old-branch`, the skill will:
     - Warn you
     - Ask: "Voulez-vous checkout master maintenant ?"
     - Automatically run: `git checkout master && git pull origin master`

3. ❌ **Never from feature branch**
   - Prevents merge conflicts
   - Ensures clean history
   - Follows Git best practices

4. ✅ **Check for existing branches**
   - Warns if branch already exists
   - Asks if you want to use existing branch

---

## 🔄 Workflow Phases

### Phase 1: Ticket Selection
- Ask for ticket number (GRA-*)
- Auto-detect from current branch
- Fetch details from Linear (if available) or ask user
- Confirm selection

### Phase 2: Analysis
- Determine feature type (FFM wrapper, API, module, etc.)
- Identify dependencies with other tickets
- Estimate complexity and time
- Identify affected files

### Phase 3: Plan Generation
- Generate structured markdown plan
- Include objectives, steps, estimation
- Adapt content based on feature type

### Phase 4: Branch Creation
- **git fetch origin** (ALWAYS)
- **Verify on master/main** (with auto-correction)
- Generate branch name: `feature/GRA-X-{kebab-case-description}`
- Check branch doesn't exist
- Create branch (with confirmation)

### Phase 5: Validation
- Display full plan
- Ask for confirmation
- Allow modifications

### Phase 6: Save
- Ensure `.plan/` directory exists
- Save as `.plan/GRA-X-implementation-plan.md`
- Output summary

---

## 📝 Questions Asked

### Phase 1
- "Quel ticket GRA-* voulez-vous planifier ?"
- "Quel est le numéro du ticket ?" (if manual)
- "Quel est le titre du ticket ?"
- "Pouvez-vous décrire brièvement la feature ?"
- "Quelle est la priorité ?" [High, Medium, Low]
- "Confirmer ce ticket ?"

### Phase 2
- "Quel type d'implémentation ?" [Wrapper FFM, API haut niveau, Module, Amélioration, Bug fix, Documentation]
- "Quelle est la complexité estimée ?" [Haute, Moyenne, Faible]
- "Temps estimé ?" [1-2h, 0.5j, 1j, 2j, 3+j, À définir]
- "Y a-t-il des risques techniques spécifiques ?"
- "Quelles sont les dépendances avec d'autres tickets ?"

### Phase 4
- "Voulez-vous checkout master maintenant ?" (if not on master)
- "Voulez-vous créer la branche '{name}' depuis master ?"

### Phase 5
- "Ce plan vous convient-il ?" [Oui, Modifier, Annuler]

---

## 🎨 Example Sessions

### Example 1: Not on Master (Auto-Correction)

```
User: "planifier le ticket GRA-3"

Skill:
1. Asks: "Quel ticket ?" → User enters "GRA-3"
2. Retrieves ticket details
3. Analyzes and generates plan
4. Runs: git fetch origin
5. Detects: current branch is feature/GRA-2-old-work
6. ⚠️ Warns: "Vous n'êtes pas sur master !"
7. Asks: "Voulez-vous checkout master maintenant ?" → User: Y
8. Runs: git checkout master && git pull origin master
9. Proposes: feature/GRA-3-cursor-traversal
10. Creates branch from master
11. Saves plan to .plan/GRA-3-implementation-plan.md
12. Output: ✅ "Plan créé et branche prête !"
```

### Example 2: Already on Master

```
User: "plan for ticket GRA-4"

Skill:
1-3. Same as Example 1
4. Runs: git fetch origin
5. Detects: current branch is master ✓
6. Proposes: feature/GRA-4-diagnostics
7. Creates branch from master
8-12. Same as Example 1
```

### Example 3: Manual Ticket Entry

```
User: "nouveau ticket"

Skill:
1. Asks: "Numéro du ticket ?" → User: "GRA-5"
2. Not found in Linear
3. Asks: "Titre ?" → User: "Support des macros"
4. Asks: "Description ?"
5. Asks: "Priorité ?" → User: "Medium"
6. Confirms selection
7-12. Continues with planning
```

---

## 🔧 Installation

### Prerequisites
- Mistral Vibe CLI
- Git repository
- Standard project structure

### Skill Discovery
Mistral Vibe automatically discovers skills in `.vibe/skills/`.

### Verify Installation
```bash
# Check skill exists
ls .vibe/skills/plan-feature/SKILL.md

# Check .plan directory
test -d .plan && echo "✅ .plan directory exists"
```

---

## 📖 Skill Metadata

```yaml
---
name: plan-feature
description: Interactive feature planning for KoreOS...
version: 1.0.0
license: MIT
compatibility: Java 25+, Gradle 9.4.1+
user-invocable: true
allowed-tools:
  - read_file
  - write_file
  - grep
  - bash
  - ask_user_question
invocation-patterns:
  - "planifier le ticket"
  - "plan for ticket"
  - "plan GRA-"
  - "créer un plan pour"
  - "start planning"
  - "nouveau ticket"
  - "plan feature"
  - "plan implémentation"
---
```

---

## 🎯 Use Cases

### Starting a New Feature
```
User: "planifier le ticket GRA-3"
→ Interactive planning session
→ Branch created from master
→ Plan saved for reference
```

### Tracking Progress
```
# All plans stored in .plan/
ls .plan/
# View a specific plan
cat .plan/GRA-3-implementation-plan.md
```

### Team Onboarding
```
# New team member can see:
# - How features are planned
# - What was decided
# - Estimations and dependencies
```

---

## 🛠️ Customization

To customize this skill:

1. **Modify invocation patterns** in `SKILL.md` frontmatter
2. **Update plan template** in Phase 3 section
3. **Add project-specific questions** in Phase 2
4. **Adjust branch naming** pattern in Phase 4

---

## 📄 License

MIT License - Part of KoreOS project.

---

## 🔗 See Also

- [Code Review Skill](../code-review/README.md) - For reviewing implemented features
- [KoreOS Project](../../README.md)
- [Mistral Vibe Skills Documentation](https://docs.mistral.ai/mistral-vibe/agents-skills#skills)
