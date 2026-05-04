---
name: plan-feature
description: Interactive feature planning for KoreOS. Select a GRA ticket, analyze requirements, create implementation plan, generate branch from master, and store plan in .plan/ directory.
version: 1.0.0
license: MIT
compatibility: Java 25+, Gradle 9.4.1+
user-invocable: true
authorized-tools:
  - read_file
  - write_file
  - grep
  - bash
  - ask_user_question
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

# Plan Feature Skill

**Scope**: Project-local (KoreOS)  
**Maintainer**: KoreOS Team  
**Purpose**: Interactive workflow to plan GRA-* ticket implementation, create branch from master, and store structured plan in `.plan/`.

---

## When to Use

- When starting work on a new GRA-* ticket
- When you need to create an implementation plan before coding
- When you want to track feature progress in a structured way
- When onboarding new contributors to the planning process

---

## Usage

### Direct Invocation
```
"planifier le ticket GRA-3"
"plan for ticket GRA-4"
"créer un plan pour GRA-5"
"nouveau ticket"
"plan feature"
```

---

## Workflow

### Phase 1: Ticket Selection

1. **Ask for ticket number**
   ```
   Question: "Quel ticket GRA-* voulez-vous planifier ?"
   Options:
     - Détecter automatiquement depuis la branche courante
     - Lister les tickets ouverts (future: Linear API)
     - Saisie manuelle
   ```

2. **If auto-detect from branch**:
   ```bash
   current_branch=$(git symbolic-ref --short HEAD)
   # Extract GRA-X from branch name
   ticket=$(echo "$current_branch" | grep -oE 'GRA-[0-9]+' || echo "")
   ```

3. **If manual entry**:
   ```
   Question: "Quel est le numéro du ticket ? (format: GRA-123)"
   Validation: Must match GRA-[0-9]+
   ```

4. **Retrieve ticket details**
   - Try Linear API (if configured and accessible)
   - If not available, ask user:
     ```
     Question: "Quel est le titre du ticket ?"
     Question: "Pouvez-vous décrire brièvement la feature ?"
     Question: "Quelle est la priorité ?" [High, Medium, Low]
     ```

5. **Confirm selection**
   ```
   Display:
   "Ticket sélectionné: GRA-3 - Implémentation du cursor traversal"
   "Description: Ajouter support pour naviguer dans l'AST Clang..."
   "Priorité: High"
   Question: "Confirmer ce ticket ? (Y/n)"
   ```

**Tools**: `ask_user_question`, `bash`

---

### Phase 2: Ticket Analysis

1. **Analyze existing code**
   - Read relevant files to understand current state
   - Check for dependencies with other tickets (grep for GRA- references)
   ```bash
   grep -r "GRA-" --include="*.java" --include="*.kt" .
   ```

2. **Determine feature type**
   ```
   Question: "Quel type d'implémentation ?"
   Options:
     - Wrapper FFM bas niveau (Java) - Bindings natifs libclang
     - API haut niveau (Kotlin) - Interface utilisateur
     - Nouveau module Gradle - Nouvelle structure de module
     - Amélioration existante - Modification de code existant
     - Bug fix - Correction de bug
     - Documentation seule - Documentation uniquement
   ```

3. **⚠️ CRITICAL: TDD Requirement Confirmation**
   ```
   Question: "Cette implémentation suivra-t-elle strictement le TDD (Test-Driven Development) ?"
   Options:
     - ✅ Oui, TOUS les tests seront écrits AVANT le code de production (REQUIRED)
     - ❌ Non (BLOCKING - GRA tickets MUST use TDD)
   
   If user selects "Non":
     - Display: "❌ ERREUR: Tous les tickets GRA-* DOIVENT utiliser TDD"
     - Display: "Le workflow KoreOS exige:"
       - 1. Écrire les tests UNITAIRES d'abord (rouge)
       - 2. Voir les tests échouer
       - 3. Écrire le code minimal pour faire passer les tests (vert)
       - 4. Refactorer avec les tests qui passent
     - Question: "Voulez-vous annuler ou recommencer avec TDD ?" [Recommencer / Annuler]
   ```

3. **Identify affected files**
   - If improvement: list files to modify (from grep/git diff)
   - If new feature: propose location based on type
   - Show list to user for confirmation

4. **Ask for technical details**
   ```
   Question: "Quelle est la complexité estimée ?" [Haute, Moyenne, Faible]
   Question: "Temps estimé ?" [X heures, X jours, À définir]
   Question: "Y a-t-il des risques techniques spécifiques ?" (free text)
   Question: "Quelles sont les dépendances avec d'autres tickets ?" (ex: "GRA-2 doit être terminé")
   ```

**Tools**: `read_file`, `grep`, `bash`, `ask_user_question`

---

### Phase 3: Plan Generation

**Generate structured markdown plan with:**

```markdown
# Plan d'Implémentation - GRA-X

**Ticket**: GRA-X - {Title}  
**Auteur**: {git config user.name or system user}  
**Date**: {YYYY-MM-DD}  
**Version**: 1.0  
**Statut**: ⏳ À faire  

---

## 📌 Métadonnées

| Propriété | Valeur |
|-----------|--------|
| Type | {Wrapper FFM / API haut niveau / Module / etc.} |
| Complexité | {Haute / Moyenne / Faible} |
| Temps estimé | {X heures/jours} |
| Priorité | {High / Medium / Low} |
| Branche | {feature/GRA-X-{description}} |

---

## 🎯 Objectifs

- [ ] {Objectif 1}
- [ ] {Objectif 2}
- [ ] {Objectif 3}

---

## 🧪 TDD Requirements (OBLIGATOIRE)

**⚠️ CRITICAL: Tous les tickets GRA-* DOIVENT suivre strictement le TDD**

### Workflow TDD Mandatory:
1. **🔴 RED**: Écrire le test UNITARE d'abord (le test DOIT échouer initialement)
2. **🟢 GREEN**: Écrire le code MINIMAL pour faire passer le test
3. **🔵 REFACTOR**: Améliorer le code AVEC les tests qui passent

### TDD Checklist:
- [ ] **TOUS** les tests unitaires écrits AVANT le code de production
- [ ] Chaque classe a son test correspondant (ex: `ClangCursor.kt` → `ClangCursorTest.kt`)
- [ ] Chaque méthode publique a au moins un test
- [ ] Tests couvrent les cas normaux ET les edge cases (null, vide, limites)
- [ ] Tests suivent la naming convention: `methodUnderTest_scenario_expectedBehavior`
- [ ] Couverture de test > 80% pour tout nouveau code
- [ ] Aucun code de production mergé sans son test

---

## 📁 Fichiers Concernés

| Fichier | Action | Type | Statut |
|---------|--------|------|--------|
| {path} | {Créer/Modifier} | {Source/Test/Doc} | ⬜ |

---

## 🏗️ Étapes d'Implémentation (TDD Workflow)

### 🟢 Phase 1: Préparation ({X} jour)
- [ ] Créer la branche `feature/GRA-X-{description}` depuis master
- [ ] Mettre à jour docs/clang-ffm/GRA-X-PROGRESS.md
- [ ] Lire la documentation nécessaire
- [ ] **TDD**: Créer la structure des fichiers de test (ex: `*Test.kt`)

### 🔴 Phase 2: TDD - RED (Écrire les tests d'abord) ({X} jours)
- [ ] **TDD**: Écrire TOUS les tests unitaires pour la première feature
- [ ] **TDD**: Vérifier que les tests échouent (compilation ou assertion)
- [ ] **TDD**: Ne PAS écrire de code de production dans cette phase
- [ ] Répéter pour chaque sous-feature avant de passer à la phase GREEN

### 🟢 Phase 3: TDD - GREEN (Code minimal) ({X} jours)
- [ ] **TDD**: Écrire le code MINIMAL pour faire passer UN test
- [ ] **TDD**: Vérifier que le test passe
- [ ] **TDD**: Ne PAS optimiser ou refactorer ici
- [ ] Répéter pour chaque test un par un

### 🔵 Phase 4: TDD - REFACTOR (Amélioration) ({X} jours)
- [ ] **TDD**: Refactorer le code AVEC tous les tests qui passent
- [ ] **TDD**: Améliorer la conception sans casser les tests
- [ ] **TDD**: Extraire les duplications
- [ ] **TDD**: Améliorer les noms des méthodes/variables

### 🟢 Phase 5: Validation finale ({X} jour)
- [ ] Exécuter TOUS les tests (doit être 100% vert)
- [ ] Exécuter detekt et corriger les issues
- [ ] Vérifier couverture de test > 80%
- [ ] **✅ CI**: Vérifier que la CI GitHub passe (tous les checks verts)
- [ ] **✅ PR**: Créer PR via `gh pr create --base master --fill --title "feat(GRA-X): {description}"`
- [ ] **✅ PR**: Assigner le ticket Linear dans la PR (lien automatique)
- [ ] **✅ PR**: Vérifier que la CI passe sur la PR (attendre tous les checks verts)
- [ ] Demander code review

---

## 📊 Estimation

- **Total**: {X} jours
- **Complexité Technique**: {Haute / Moyenne / Faible}
- **Risques**:
  - [⚠️] {Risque 1}
  - [⚠️] {Risque 2}

## ⚠️ Règles de Branche (IMPORTANT)
- **TOUJOURS** créer depuis `master` (ou `main`)
- **TOUJOURS** exécuter `git fetch origin` avant
- **NE JAMAIS** créer depuis une autre feature branch
- Pattern de nom: `feature/GRA-X-{kebab-case-description}`

## 🔗 Dépendances

- **Tickets bloquants**: {GRA-Y, GRA-Z}
- **Bibliothèques**: {libclang 17+, etc.}
- **Modules**: {shared:clang-wrapper, etc.}
- **Outils**: Java 25+, Gradle 9.4.1+

---

## ✅ Acceptance Criteria

- [ ] **TDD**: TOUS les tests unitaires écrits AVANT le code de production
- [ ] **TDD**: Couverture de test > 80% pour tout nouveau code
- [ ] **TDD**: Aucun code de production mergé sans son test
- [ ] **CI**: TOUS les checks GitHub Actions passent (build, test, detekt)
- [ ] **PR**: PR créée via `gh pr create` avec convention de nommage
- [ ] **PR**: PR liée au ticket Linear
- [ ] {Critère 1}
- [ ] {Critère 2}

---

## 📝 Notes

{notes supplémentaires}

---

*Généré par: plan-feature skill*  
*Date: {YYYY-MM-DD}*
```

**For FFM wrapper features, auto-generate objectives:**
- Implémenter {function_name}() binding FFM
- Ajouter validation des paramètres
- Gérer la mémoire (Arena, MemorySegment.NULL checks)
- Créer des tests unitaires

**For high-level API features:**
- Créer interface Kotlin
- Implémenter pour JVM
- Ajouter documentation
- Intégrer avec wrapper bas niveau

**Tools**: `ask_user_question`

---

### Phase 4: Branch Creation

1. **Always fetch first** (CRITICAL)
   ```bash
   git fetch origin
   ```

2. **Verify current branch**
   ```bash
   current_branch=$(git symbolic-ref --short HEAD)
   ```

3. **Ensure we're on master/main**
   ```bash
   if [ "$current_branch" != "master" ] && [ "$current_branch" != "main" ]; then
     echo "⚠️  Vous n'êtes pas sur master/main"
     echo "La branche doit être créée depuis master pour éviter les conflits"
     
     Question: "Voulez-vous checkout master maintenant ? (Y/n)"
     if Yes:
       git checkout master
       git pull origin master
   fi
   ```

4. **Generate branch name**
   ```bash
   # Convert ticket title to kebab-case
   title="$ticket_title"
   kebab_title=$(echo "$title" | tr '[:upper:]' '[:lower:]' | tr ' ' '-' | tr '.' '-' | sed 's/--*/-/g')
   branch_name="feature/GRA-${ticket_number}-${kebab_title}"
   
   # Ensure branch name is valid (no special chars except -)
   branch_name=$(echo "$branch_name" | sed 's/[^a-zA-Z0-9-/]//g')
   ```

5. **Check branch doesn't exist**
   ```bash
   git fetch origin  # Already done in step 1
git branch -a | grep -q "$branch_name"
   if [ $? -eq 0 ]; then
     echo "⚠️  La branche $branch_name existe déjà"
     Question: "Voulez-vous utiliser cette branche existante ? (Y/n)"
     if No: return to step 4 with new name suggestion
   fi
   ```

6. **Create branch**
   ```bash
git checkout -b "$branch_name"
   ```

7. **Ask for confirmation**
   ```
   Question: "Voulez-vous créer la branche '$branch_name' depuis master maintenant ? (Y/n)"
   ```

**Strict Rules:**
- ❌ NEVER create from a feature branch
- ✅ ALWAYS create from master (or main)
- ✅ ALWAYS run `git fetch origin` first

**Tools**: `bash`, `ask_user_question`

---

### Phase 5: Plan Presentation & Validation

1. **Display the generated plan**
   - Show full markdown plan
   - Use formatting for readability

2. **Ask for validation**
   ```
   Question: "Ce plan vous convient-il ?"
   Options:
     - ✅ Oui, créer la branche et sauvegarder le plan
     - ⏎ Modifier le plan (return to appropriate phase)
     - ❌ Annuler
   ```

3. **If modifications needed**:
   - Ask: "Quelle partie souhaitez-vous modifier ?"
   - Options: [Objectifs, Fichiers, Étapes, Estimation, Dépendances, Tout]
   - Loop back to appropriate phase

4. **If validated**:
   - **⚠️ CRITICAL: Verify TDD is included in plan**
   - Check that plan contains:
     - TDD Requirements section
     - TDD workflow phases (RED, GREEN, REFACTOR)
     - TDD checklist items
   - If missing: Display error and loop back to Phase 3
   - Proceed to Phase 6

**Tools**: `ask_user_question`

---

### Phase 6: Save Plan

1. **Ensure .plan/ directory exists**
   ```bash
   mkdir -p .plan
   touch .plan/.gitkeep  # Ensure directory is tracked
   ```

2. **Save plan file**
   ```bash
   filename=".plan/GRA-${ticket_number}-implementation-plan.md"
   write_file(path="$filename", content="$generated_plan")
   ```

3. **Optional: Create todo tracking**
   ```
   Use todo tool to create tracking items from plan objectives
   ```

4. **Output summary**
   ```
   echo "✅ Plan sauvegardé: $filename"
   echo "✅ Branche créée: $branch_name"
   echo "📋 Vous pouvez commencer le développement !"
   ```

**Tools**: `write_file`, `bash`

---

## Questions Reference

### Phase 1: Ticket Selection
1. **"Quel ticket GRA-* voulez-vous planifier ?"**
   - Type: free text with validation (GRA-\d+)
   - Options: [Détecter depuis branche, Saisie manuelle]

2. **If manual entry**:
   - "Quel est le numéro du ticket ? (ex: GRA-3)"
   - "Quel est le titre du ticket ?"
   - "Pouvez-vous décrire brièvement la feature ?"
   - "Quelle est la priorité ?" [High, Medium, Low]

3. **Confirmation**:
   - "Vous avez sélectionné: GRA-3 - {title}\nDescription: {description}\nConfirmer ? (Y/n)"

### Phase 2: Analysis
4. "Quel type d'implémentation ?"
   - Options: [Wrapper FFM, API haut niveau, Nouveau module, Amélioration, Bug fix, Documentation]

5. "Quelle est la complexité estimée ?"
   - Options: [Haute, Moyenne, Faible]

6. "Temps estimé ?"
   - Options: [1-2 heures, 0.5 jour, 1 jour, 2 jours, 3+ jours, À définir]

7. "Y a-t-il des risques techniques spécifiques ?"
   - Type: free text (optional)

8. "Quelles sont les dépendances avec d'autres tickets ?"
   - Type: free text (ex: "GRA-2 doit être terminé")

9. **⚠️ CRITICAL: Confirm TDD understanding**
   - Question: "Confirmez-vous que vous comprenez et appliquerez le workflow TDD (RED → GREEN → REFACTOR) pour TOUTE cette implémentation ?"
   - Options: [Oui, je confirme / Non, j'ai besoin d'explications]
   - If "Non": Display TDD workflow and loop back to question

### Phase 4: Branch
9. **If not on master**: "Voulez-vous checkout master maintenant ? (Y/n)"

10. "Voulez-vous créer la branche '{branch_name}' depuis master maintenant ? (Y/n)"

### Phase 5: Validation
11. "Ce plan vous convient-il ?"
    - Options: [Oui - sauvegarder, Modifier, Annuler]

12. **If modify**: "Quelle partie souhaitez-vous modifier ?"
    - Options: [Objectifs, Fichiers, Étapes, Estimation, Dépendances, Tout]

---

## Command Reference

```bash
# Get current branch
git symbolic-ref --short HEAD

# Fetch from remote
git fetch origin

# Check if on master/main
current=$(git symbolic-ref --short HEAD)
if [ "$current" != "master" ] && [ "$current" != "main" ]; then ...

# Checkout master
git checkout master
git pull origin master

# Create branch
git checkout -b feature/GRA-X-description

# Check branch exists
git branch -a | grep branch-name

# Get git username
git config user.name

# List files in directory
ls -la path/to/directory

# Create directory
mkdir -p .plan

# Get date
date +%Y-%m-%d

# ⚠️ GitHub CLI commands (for PR creation)
# Create PR with conventional commit title
gh pr create --base master --fill --title "feat(GRA-X): description"

# List PRs for current branch
gh pr list --head

# Check PR status (wait for CI)
gh pr view --json statusChecks

# View CI checks for current branch
gh api repos/{owner}/{repo}/commits/{sha}/check-runs
```

---

## Example Sessions

### Example 1: Full Flow with Master Check

```
User: "planifier le ticket GRA-3"

Skill:
1. Phase 1:
   - Question: "Quel ticket ?" → User: "GRA-3"
   - Récupère depuis Linear: "GRA-3: Implémentation du cursor traversal"
   - Confirmer: Y

2. Phase 2:
   - Détecte dépendance: GRA-2 requis
   - Type: Wrapper FFM + API haut niveau
   - Complexité: Moyenne
   - Temps: 2 jours

3. Phase 3:
   - Génère plan complet

4. Phase 4:
   - git fetch origin
   - Current branch: feature/GRA-2-old-work
   - ⚠️ "Vous n'êtes pas sur master !"
   - Question: "Checkout master ?" → Y
   - git checkout master && git pull origin master
   - Propose: feature/GRA-3-cursor-traversal
   - Confirmer: Y

5. Phase 5:
   - Affiche plan markdown
   - Question: "Valider ?" → Y

6. Phase 6:
   - Crée branche depuis master
   - Sauve dans .plan/GRA-3-implementation-plan.md
   - Output: "✅ Plan créé et branche prête !"
```

### Example 2: Already on Master

```
User: "plan for ticket GRA-4"

Skill:
1-3. Same as Example 1

4. Phase 4:
   - git fetch origin
   - Current branch: master ✓
   - Propose: feature/GRA-4-diagnostics
   - Confirmer: Y

5-6. Same as Example 1
```

### Example 3: Manual Entry

```
User: "nouveau ticket"

Skill:
1. Phase 1:
   - Question: "Numéro du ticket ?" → User: "GRA-5"
   - Non trouvé dans Linear
   - Question: "Titre ?" → User: "Support des macros"
   - Question: "Description ?" → User: "Ajouter support pour parser les macros C..."
   - Question: "Priorité ?" → User: "Medium"
   - Confirmer: Y

2-6. Continue normally
```

---

## Project-Specific Rules (KoreOS)

1. **Java Version**: Java 25+ (required for FFM)
2. **Gradle Version**: 9.4.1+
3. **License**: MIT (SPDX identifier in all files)
4. **Package**: `io.ygdrasil.koreos.clang` for clang-related features
5. **Branch Pattern**: `feature/GRA-{X}-{kebab-case-description}`
6. **Plan Storage**: `.plan/GRA-{X}-implementation-plan.md`
7. **Documentation**: Progress tracked in `docs/clang-ffm/GRA-{X}-PROGRESS.md`
8. **⚠️ CRITICAL: TDD MANDATORY** - ALL GRA-* tickets MUST follow strict TDD (RED → GREEN → REFACTOR)

---

## Dependencies

This skill requires:
- Git repository
- Standard project structure
- bash shell for git operations

---

## Version History

- **1.0.0** (2025-05-04): Initial version
  - Interactive ticket selection
  - Structured plan generation
  - Branch creation from master with safety checks
  - Plan storage in .plan/
  - Bilingual support (FR/EN)
