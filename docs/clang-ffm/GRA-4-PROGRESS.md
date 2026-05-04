# GRA-4: Support des types complexes de Clang (AST, diagnostics)

**Ticket**: [Linear - GRA-4](https://linear.app/forge-yg/issue/GRA-4/koreos-support-des-types-complexes-de-clang-ast-diagnostics)  
**Statut**: ⏳ À faire  
**Branche**: feature/GRA-4-koreos-support-des-types-complexes-de-clang-ast-diagnostics  
**Plan**: [.plan/GRA-4-implementation-plan.md](../../.plan/GRA-4-implementation-plan.md)  
**Date de début**: 2025-05-04

---

## 📋 Description

Étendre le wrapper pour gérer les structures complexes de Clang (CXCursor, CXDiagnostic). Implémenter des classes Kotlin pour représenter ces types et ajouter des méthodes pour parcourir l'AST et récupérer les diagnostics.

---

## 🎯 Objectifs

- [ ] Étendre `ClangCursor` avec méthodes de navigation AST complètes
- [ ] Étendre `ClangDiagnostic` avec méthodes d'extraction complètes
- [ ] Implémenter `SourceLocation` pour localiser le code source
- [ ] Implémenter `CursorKind` enum pour les types de curseurs
- [ ] Implémenter `Severity` enum pour les niveaux de diagnostic
- [ ] Ajouter méthodes de parcours récursif de l'AST
- [ ] Créer exemple d'analyse complète d'un fichier C
- [ ] **TDD**: 100% de couverture de test pour le nouveau code

---

## 📊 Avancement

### Phase 1: Préparation
- [x] Plan créé
- [x] Branche créée
- [x] Documentation libclang lue
- [x] Structure des tests créée

### Phase 2: TDD - RED (Tests d'abord)
- [x] Tests CursorKind
- [x] Tests Severity
- [x] Tests SourceLocation
- [x] Tests ClangCursor.getKind()
- [x] Tests ClangCursor.getSpelling()
- [x] Tests ClangCursor.getChildren()
- [x] Tests ClangCursor.getLocation()
- [x] Tests ClangCursor.getSemanticParent()
- [x] Tests ClangDiagnostic.getSeverity()
- [x] Tests ClangDiagnostic.getMessage()
- [x] Tests ClangDiagnostic.getLocation()
- [x] Tests parcours AST

### Phase 3: TDD - GREEN (Code minimal)
- [x] CursorKind implémenté
- [x] Severity implémenté
- [x] SourceLocation implémenté
- [x] ClangCursor.getKind()
- [x] ClangCursor.getSpelling()
- [x] ClangCursor.getChildren()
- [x] ClangCursor.getLocation()
- [x] ClangDiagnostic.getSeverity()
- [x] ClangCursor.getSemanticParent()
- [x] ClangDiagnostic.getMessage()
- [x] ClangDiagnostic.getLocation()
- [x] Parcours AST (traverse, findAll)

### Phase 4: TDD - REFACTOR
- [ ] Refactoring ClangCursor
- [ ] Refactoring ClangDiagnostic
- [ ] AstTraversalExample.kt créé

### Phase 5: Validation
- [ ] Tous les tests passent
- [ ] Detekt OK
- [ ] Couverture > 80%
- [ ] CI passe
- [ ] PR créée
- [ ] Code review passée

---

## 📁 Fichiers

| Fichier | Statut | Lignes |
|---------|--------|--------|
| ClangCursor.kt | À modifier | ~32 |
| ClangCursorTest.kt | À modifier | ~0 |
| ClangDiagnostic.kt | À modifier | ~32 |
| ClangDiagnosticTest.kt | À modifier | ~0 |
| SourceLocation.kt | À créer | - |
| SourceLocationTest.kt | À créer | - |
| CursorKind.kt | À créer | - |
| Severity.kt | À créer | - |
| AstTraversalExample.kt | À créer | - |

---

## ⚠️ Blocages

Aucun pour le moment.

---

## 🔗 Liens utiles

- [Ticket Linear GRA-4](https://linear.app/forge-yg/issue/GRA-4/koreos-support-des-types-complexes-de-clang-ast-diagnostics)
- [Documentation libclang](https://clang.llvm.org/doxygen/group__CINDEX.html)
- [Clang CXCursor](https://clang.llvm.org/doxygen/structCXCursor.html)
- [Clang CXDiagnostic](https://clang.llvm.org/doxygen/structCXDiagnostic.html)
- [Clang CXSourceLocation](https://clang.llvm.org/doxygen/structCXSourceLocation.html)

---

*Dernière mise à jour: 2025-05-04*
