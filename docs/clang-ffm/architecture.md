# Architecture Preliminaire - Wrapper Clang/FFM (GRA-1)

## Diagramme d'Intégration

```
+-------------------+       +-------------------+       +------------------+
|   KoreOS Core     |<----->|  Clang Wrapper   |<----->|   libclang.so    |
|  (Kotlin MP)      |       |  (Java 21+ FFM)  |       |  (LLVM 17+)      |
+-------------------+       +-------------------+       +------------------+
        |                         |                        |
        v                         v                        v
+-------------------+       +-------------------+       +------------------+
|  Platform APIs     |       |  FFM Memory Mgmt  |       |  AST Parsing     |
|  (JVM/iOS/JS)      |       |  (Arena, Segment) |       |  (CST, Visitors) |
+-------------------+       +-------------------+       +------------------+
```

## Composants Principaux

### 1. ClangFFMWrapper (Couche Bas Niveau)
Responsabilité: Appels directs à libclang via FFM

Exemple:
```java
public static MemorySegment createIndex() {
    // FFM call to clang_createIndex
}
public static MemorySegment parseTranslationUnit(String filePath) {
    // ...
}
```

### 2. ClangFacade (Couche Métier)
Responsabilité: API haut-niveau pour KoreOS

Interface:
```kotlin
fun parseFile(path: String): TranslationUnit
fun getAST(filePath: String): ASTNode
fun getDiagnostics(filePath: String): List<Diagnostic>
```

### 3. Structure des fichiers
```
shared/
  clang/
    commonMain/
      kotlin/io/ygdrasil/koreos/clang/
        ClangFacade.kt          // Interface
        TranslationUnit.kt     // Modèles
        ASTNode.kt             // Modèles
    jvmMain/
      kotlin/io/ygdrasil/koreos/clang/
        ClangFacadeJvm.kt      // Implémentation FFM
```

## Modèles de Données

### TranslationUnit
- filePath: String
- astRoot: ASTNode
- diagnostics: List<Diagnostic>
- includes: List<String>

### ASTNode (Sealed Class)
- Cursor(kind: CursorKind, children: List<ASTNode>)
- TypeRef(type: TypeInfo)
- Token(text: String, kind: TokenKind)

### Diagnostic
- severity: Severity (ERROR, WARNING, INFO)
- message: String
- location: SourceLocation
- fixIts: List<FixIt>

## Intégration avec KoreOS

1. Ajouter module dans settings.gradle.kts:
   ```kotlin
   include(":shared:clang")
   ```

2. Configurer build.gradle.kts:
   ```kotlin
   java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
   ```

3. Exposer dans Platform.kt:
   ```kotlin
   expect val clang: ClangFacade
   actual val clang: ClangFacade = ClangFacadeJvm()
   ```

## Risques Identifiés

| Risque | Impact | Mitigation |
|--------|--------|------------|
| FFM non supporté | Bloquant | Vérifier Java 21+ au runtime |
| Version LLVM incompatible | Élevé | Documenter versions supportées |
| Memory leaks (FFM) | Moyen | Utiliser Arena.ofConfined() |
| Performances | Moyen | Cache des TranslationUnit |

## Prochaines Étapes
- [ ] Implémenter ClangFFMWrapper (POC)
- [ ] Valider parsing d'un fichier C simple
- [ ] Intégrer avec l'architecture existante
- [ ] Ajouter tests unitaires

## Références
- [JEP 442: Foreign Function & Memory API](https://openjdk.org/jeps/442)
- [LibClang C Interface](https://clang.llvm.org/doxygen/group__CINDEX.html)