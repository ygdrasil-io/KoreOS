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
- [ ] Documentation libclang lue
- [ ] Structure des tests créée

### Phase 2: TDD - RED (Tests d'abord)
- [ ] Tests CursorKind
- [ ] Tests Severity
- [ ] Tests SourceLocation
- [ ] Tests ClangCursor.getKind()
- [ ] Tests ClangCursor.getSpelling()
- [ ] Tests ClangCursor.getChildren()
- [ ] Tests ClangCursor.getLocation()
- [ ] Tests ClangCursor.getSemanticParent()
- [ ] Tests ClangDiagnostic.getSeverity()
- [ ] Tests ClangDiagnostic.getMessage()
- [ ] Tests ClangDiagnostic.getLocation()
- [ ] Tests parcours AST

### Phase 3: TDD - GREEN (Code minimal)
- [ ] CursorKind implémenté
- [ ] Severity implémenté
- [ ] SourceLocation implémenté
- [ ] ClangCursor.getKind()
- [ ] ClangCursor.getSpelling()
- [ ] ClangCursor.getChildren()
- [ ] ClangCursor.getLocation()
- [ ] ClangDiagnostic.getSeverity()
- [ ] ClangDiagnostic.getMessage()
- [ ] ClangDiagnostic.getLocation()
- [ ] Parcours AST

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
