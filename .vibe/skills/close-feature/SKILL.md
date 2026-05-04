---
name: close-feature
description: Close current feature branch by cleaning up plan and review directories, updating master, and switching to master branch.
version: 1.0.0
license: MIT
compatibility: Git 2.x+
user-invocable: true
authorized-tools:
  - bash
  - read_file
allowed-tools:
  - bash
  - read_file
invocation-patterns:
  - "close feature"
  - "close-feature"
  - "fermer la feature"
  - "fermer le ticket"
  - "terminer la feature"
  - "terminer le ticket"
  - "switch to master"
  - "retour master"
---

# Close Feature Skill

**Scope**: Project-local (KoreOS)  
**Maintainer**: KoreOS Team  
**Purpose**: Clean up feature development artifacts (contents of `.plan/` and `.review/` directories) and return to master branch.

---

## When to Use

- When a feature (GRA-*) is completed and merged
- When you need to clean up `.plan/` and `.review/` directories
- When you want to switch back to master and ensure it's up-to-date
- When starting a new feature and want a clean state

---

## Usage

### Direct Invocation
```
"close feature"
"close-feature"
"fermer la feature"
"fermer le ticket"
"retour master"
"switch to master"
```

---

## Workflow

### Phase 1: Confirmation

1. **Detect current branch**
   ```bash
   current_branch=$(git symbolic-ref --short HEAD)
   ```

2. **Check if on master**
   ```bash
   if [ "$current_branch" = "master" ] || [ "$current_branch" = "main" ]; then
     Question: "Vous êtes déjà sur master. Voulez-vous quand même nettoyer le contenu des dossiers .plan/ et .review/ ? (Y/n)"
     if No: Exit skill
   fi
   ```

3. **If on feature branch, ask for confirmation**
   ```bash
   Question: "Vous êtes sur la branche: $current_branch\nVoulez-vous:\n  1. Supprimer le contenu de .plan/ et .review/\n  2. Faire git fetch origin master\n  3. Faire git checkout master\n  4. Faire git pull origin master\nConfirmer ? (Y/n)"
   if No: Exit skill
   ```

**Tools**: `bash`, `ask_user_question`

---

### Phase 2: Cleanup

1. **Remove contents of .plan/ directory (preserve directory and .gitkeep)**
   ```bash
   if [ -d ".plan" ]; then
     # Remove all files except .gitkeep
     find .plan/ -mindepth 1 ! -name '.gitkeep' -exec rm -rf {} + 2>/dev/null
     echo "✅ Contenu de .plan/ supprimé"
   else
     echo "ℹ️  Dossier .plan/ introuvable"
   fi
   ```

2. **Remove contents of .review/ directory (preserve directory and .gitkeep)**
   ```bash
   if [ -d ".review" ]; then
     # Remove all files except .gitkeep
     find .review/ -mindepth 1 ! -name '.gitkeep' -exec rm -rf {} + 2>/dev/null
     echo "✅ Contenu de .review/ supprimé"
   else
     echo "ℹ️  Dossier .review/ introuvable"
   fi
   ```

**Tools**: `bash`

---

### Phase 3: Git Operations

1. **Fetch origin master**
   ```bash
   git fetch origin master
   echo "✅ Fetch origin/master effectué"
   ```

2. **Checkout master branch**
   ```bash
   git checkout master
   echo "✅ Checkout master effectué"
   ```

3. **Pull latest changes from origin/master**
   ```bash
   git pull origin master
   echo "✅ Pull origin/master effectué"
   ```

4. **Verify current branch**
   ```bash
   current_branch=$(git symbolic-ref --short HEAD)
   if [ "$current_branch" != "master" ]; then
     echo "⚠️  ERREUR: Impossible de basculer sur master"
     echo "Branche actuelle: $current_branch"
   fi
   ```

**Tools**: `bash`

---

### Phase 4: Completion

1. **Display summary**
   ```
   echo "========================="
   echo "✅ Close Feature Complété"
   echo "========================="
   echo "Actions effectuées:"
   echo "  - Suppression du contenu de .plan/"
   echo "  - Suppression du contenu de .review/"
   echo "  - git fetch origin master"
   echo "  - git checkout master"
   echo "  - git pull origin master"
   echo "========================="
   echo "Branche actuelle: $(git symbolic-ref --short HEAD)"
   ```

2. **Optional: Suggest next actions**
   ```
   echo ""
   echo "Prochaines actions possibles:"
   echo "  - planifier un nouveau ticket: 'planifier le ticket GRA-X'"
   echo "  - créer une nouvelle branche: 'git checkout -b feature/GRA-X-description'"
   ```

---

## Command Reference

```bash
# Get current branch
git symbolic-ref --short HEAD

# Check if directory exists
if [ -d ".plan" ]; then ...

# Remove directory recursively
rm -rf .plan/

# Fetch from remote
git fetch origin master

# Checkout branch
git checkout master

# Pull latest changes
git pull origin master

# Verify current branch
git branch --show-current
```

---

## Example Sessions

### Example 1: Close feature from feature branch

**User Input:**
```
User: "close feature"
```

**Skill Execution:**
```
1. Current branch: feature/GRA-3-cursor-traversal
2. Question: "Voulez-vous nettoyer et revenir sur master ? (Y/n)" → Y
3. Remove contents of .plan/ → ✅
4. Remove contents of .review/ → ✅
5. git fetch origin master → ✅
6. git checkout master → ✅
7. git pull origin master → ✅
8. Output: "✅ Close Feature Complété - Branche actuelle: master"
```

### Example 2: Already on master

**User Input:**
```
User: "close-feature"
```

**Skill Execution:**
```
1. Current branch: master
2. Question: "Vous êtes déjà sur master. Voulez-vous nettoyer le contenu de .plan/ et .review/ ? (Y/n)" → Y
3. Remove contents of .plan/ → ✅
4. Remove contents of .review/ → ✅
5. git fetch origin master → ✅
6. git pull origin master → ✅
7. Output: "✅ Nettoyage terminé"
```

### Example 3: Cancel operation

**User Input:**
```
User: "fermer le ticket"
```

**Skill Execution:**
```
1. Current branch: feature/GRA-4-diagnostics
2. Question: "Voulez-vous nettoyer et revenir sur master ? (Y/n)" → n
3. Exit skill - No changes made
```

---

## Safety Checks

1. **Always confirm** with user before making destructive changes
2. **Check current branch** before switching
3. **Verify directories exist** before deletion (silent skip if not found)
4. **Use git pull** (not git merge) to ensure fast-forward update

---

## Dependencies

This skill requires:
- Git repository
- Standard project structure
- bash shell for git operations

---

## Version History

- **1.0.0** (2025-05-04): Initial version
  - Cleanup contents of .plan/ and .review/ directories (preserve .gitkeep)
  - Git fetch, checkout, and pull master
  - User confirmation before actions
  - Bilingual support (FR/EN)
