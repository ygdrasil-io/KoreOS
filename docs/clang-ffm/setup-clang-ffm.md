# Configuration Clang + FFM pour KoreOS (GRA-1)

## Prérequis

### 1. Java 21+
Version requise: Java 21 ou supérieur (pour FFM - Foreign Function & Memory API)

Vérification:
```bash
java -version
```

### 2. LLVM/Clang
Version recommandée: LLVM 17+ (compatible avec libclang)

#### Installation:
- **Linux (Debian/Ubuntu)**: `sudo apt-get install llvm-17 clang-17 libclang-17-dev`
- **macOS (Homebrew)**: `brew install llvm@17`
- **Windows**: Télécharger depuis [LLVM Releases](https://github.com/llvm/llvm-project/releases/tag/llvmorg-17.0.0)

#### Variables d'environnement:
- Linux/macOS:
```bash
export PATH="/usr/lib/llvm-17/bin:$PATH"
export LD_LIBRARY_PATH="/usr/lib/llvm-17/lib:$LD_LIBRARY_PATH"
```

- macOS (Homebrew):
```bash
export PATH="/opt/homebrew/opt/llvm@17/bin:$PATH"
```

### 3. Vérification de l'installation
```bash
clang --version
# Doit afficher: clang version 17.0.0 ou supérieur
```

## Configuration du projet

### Dépendances Gradle
FFM est intégré à Java 21+, aucune dépendance supplémentaire requise.

Pour libclang (bindings natifs - optionnel):
```kotlin
// Dans shared/build.gradle.kts
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-c-interop:0.5.0")
}
```

### Activation FFM
FFM est automatiquement disponible avec Java 21+.

Vérification en code:
```java
public static boolean isFFMSupported() {
    return Runtime.version().feature() >= 21;
}
```

## API Clang à wrapper (Priorité)

### Niveau 1 - Essentiel
1. **Indexation du code**: `clang_createIndex`
2. **Parsing des fichiers**: `clang_parseTranslationUnit`
3. **Visiteur de l'AST**: `clang_visitChildren`
4. **Gestion des diagnostics**: `clang_getNumDiagnostics`

### Niveau 2 - Avancé
1. **Manipulation du code**: `clang_Cursor_getSpelling`
2. **Type checking**: `clang_getCursorType`
3. **Génération de bindings**: `clang_Cursor_getResultType`

### Niveau 3 - Optionnel
1. **Code completion**: `clang_codeCompleteAt`
2. **Refactoring**: `clang_renameRef`
3. **Analyse statique**: `clang_analyzeTranslationUnit`

## Étapes suivantes
- [ ] Valider l'installation de LLVM/Clang
- [ ] Créer un POC d'appel FFM vers libclang
- [ ] Documenter les bindings générés
- [ ] Intégrer dans l'architecture KoreOS

## Ressources
- [Java FFM Documentation (JEP 442)](https://openjdk.org/jeps/442)
- [LibClang Documentation](https://clang.llvm.org/docs/LibClang.html)
- [KoreOS Architecture](../../README.md)