# Plan d'Implémentation - GRA-3

**Ticket**: GRA-3 - [KoreOS] Conversion du code Java en Kotlin pour le wrapper Clang  
**Auteur**: chaos  
**Date**: 2025-01-17  
**Version**: 1.0  
**Statut**: ⏳ À faire  

---

## 📌 Métadonnées

| Propriété | Valeur |
|-----------|--------|
| Type | API haut niveau (Kotlin) |
| Complexité | Moyenne |
| Temps estimé | 3.5 jours |
| Priorité | No priority |
| Branche | feature/GRA-3-koreos-conversion-du-code-java-en-kotlin-pour-le-wrapper-clang |

---

## 🎯 Objectifs

- [ ] Convertir `ClangFFMWrapper.java` en Kotlin avec équivalent FFM
- [ ] Convertir `ClangCursor.java` en Kotlin
- [ ] Convertir `ClangDiagnostic.java` en Kotlin
- [ ] Convertir `ClangIndex.java`, `ClangTranslationUnit.java` en Kotlin
- [ ] Convertir `ExampleUsage.java` en Kotlin
- [ ] Valider que toutes les fonctionnalités restent identiques après conversion
- [ ] Mettre à jour les tests unitaires en Kotlin
- [ ] Créer `docs/clang-ffm/GRA-3-PROGRESS.md`

---

## 📁 Fichiers Concernés

| Fichier | Action | Type | Statut |
|---------|--------|------|--------|
| `shared/clang-wrapper/src/main/java/io/ygdrasil/koreos/clang/ClangFFMWrapper.java` | Convertir | Source | ⬜ |
| `shared/clang-wrapper/src/main/java/io/ygdrasil/koreos/clang/ClangCursor.java` | Convertir | Source | ⬜ |
| `shared/clang-wrapper/src/main/java/io/ygdrasil/koreos/clang/ClangDiagnostic.java` | Convertir | Source | ⬜ |
| `shared/clang-wrapper/src/main/java/io/ygdrasil/koreos/clang/ClangIndex.java` | Convertir | Source | ⬜ |
| `shared/clang-wrapper/src/main/java/io/ygdrasil/koreos/clang/ClangTranslationUnit.java` | Convertir | Source | ⬜ |
| `shared/clang-wrapper/src/main/java/io/ygdrasil/koreos/clang/ExampleUsage.java` | Convertir | Source | ⬜ |
| `shared/clang-wrapper/src/test/java/io/ygdrasil/koreos/clang/ClangFFMWrapperTest.java` | Convertir | Test | ⬜ |
| `shared/clang-wrapper/src/test/java/io/ygdrasil/koreos/clang/HighLevelWrapperTest.java` | Convertir | Test | ⬜ |
| `docs/clang-ffm/GRA-3-PROGRESS.md` | Créer | Doc | ⬜ |

---

## 🏗️ Étapes d'Implémentation

### 🟢 Phase 1: Préparation (0.5 jour)
- [ ] Créer la branche `feature/GRA-3-koreos-conversion-du-code-java-en-kotlin-pour-le-wrapper-clang` depuis master
- [ ] Mettre à jour `docs/clang-ffm/GRA-3-PROGRESS.md` (statut: En cours)
- [ ] Lire la documentation Kotlin/FFM et les bonnes pratiques

### 🟡 Phase 2: Développement (2 jours)
- [ ] Configurer l'environnement Kotlin dans `shared/clang-wrapper`
- [ ] Convertir `ClangException.java` et ses sous-classes en Kotlin
- [ ] Convertir `ClangFFMWrapper.java` en Kotlin (binding FFM principal)
- [ ] Convertir `ClangIndex.java` et `ClangTranslationUnit.java`
- [ ] Convertir `ClangCursor.java` et `ClangDiagnostic.java`
- [ ] Convertir `ExampleUsage.java` (exemple d'utilisation)
- [ ] Convertir les tests unitaires en Kotlin

### 🟢 Phase 3: Tests (0.5 jour)
- [ ] Exécuter tous les tests pour valider l'équivalence fonctionnelle
- [ ] Vérifier la couverture de test > 80%
- [ ] Tester avec différents fichiers C/C++

### 🟢 Phase 4: Finalisation (0.5 jour)
- [ ] Exécuter detekt sur le code Kotlin
- [ ] Corriger les warnings et problèmes de style
- [ ] Créer PR vers master
- [ ] Demander code review

---

## 📊 Estimation

- **Total**: 3.5 jours
- **Complexité Technique**: Moyenne
- **Risques**:
  - [⚠️] Problèmes de compatibilité Java↔Kotlin avec les MemorySegment (FFM)
  - [⚠️] Tests existants peuvent nécessiter des adaptations

---

## 🔗 Dépendances

- **Tickets bloquants**: GRA-2 (doit être terminé et validé)
- **Bibliothèques**: libclang 17+, Kotlin 2.0+
- **Modules**: shared:clang-wrapper
- **Outils**: Java 25+, Gradle 9.4.1+, Kotlin/JVM

---

## ✅ Acceptance Criteria

- [ ] Tout le code Java de GRA-2 est converti en Kotlin idiomatique
- [ ] Toutes les fonctionnalités sont préservées (100% équivalence)
- [ ] Tous les tests passent
- [ ] Couverture de test ≥ 80%
- [ ] Code valide detekt
- [ ] Documentation mise à jour

---

## ⚠️ Règles de Branche (IMPORTANT)

- **TOUJOURS** créer depuis `master` (ou `main`)
- **TOUJOURS** exécuter `git fetch origin` avant
- **NE JAMAIS** créer depuis une autre feature branch
- Pattern de nom: `feature/GRA-X-{kebab-case-description}`

---

## 📝 Notes

- La conversion doit préserver les signatures FFM (MemorySegment, Arena, etc.)
- Utiliser les idiomes Kotlin: data classes, extension functions, null safety
- Conserver les commentaires et documentation existants
- Vérifier la compatibilité binaire avec le code Java existant

---
*Généré par: plan-feature skill*
*Date: 2025-01-17*
