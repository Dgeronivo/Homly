---
name: find-usages
description: Systematic search protocol to find all usage sites before a rename or API change. Use this skill before ANY refactoring that changes a class name, method signature, return type, or interface contract — especially when the change propagates through layers (DAO → repository → use case → ViewModel). Triggers on "renaming X", "refactoring Y to Z", "changing the signature of", "find all usages", "who calls this", "what uses X", or before starting any story that involves renaming or changing an API. Also trigger proactively when the user begins implementing a plan that renames a class or changes a method signature — run this BEFORE writing code.
---

## Why this matters

The most common source of missed files during a refactor: **searching only by class name instead of by method name**. A class rename finds call sites in ViewModel, DI, and tests — but misses files that call the same method through an interface or abstract type. Those files don't import the class; they call a method. Grep won't find them unless you search for the method name itself.

The second source: **not tracing what consumes the return type**. Changing `Flow<X>` to `suspend X` doesn't just affect the declaration site — every caller that uses `.first()`, `.collect`, `flatMapLatest`, or `combine` on the result must change too.

---

## Protocol — run before writing any code

### Step 1: List everything that's changing

Write it out explicitly:
- Class or file names being renamed
- Method names changing
- Parameter types or counts
- Return type (especially reactive → one-shot, e.g. Flow → suspend)
- Interface methods being added, removed, or changed

Each item needs its own Grep pass.

---

### Step 2: Search for each changed element

**Class rename** — run all three:
```
Grep: "OldClassName"          → imports, type annotations, direct refs
Grep: "OldClassName("         → constructor calls
Grep: ": OldClassName"        → implementations, extensions
```

**Method name change:**
```
Grep: "oldMethodName("        → call sites (highest priority)
Grep: "oldMethodName"         → function references, lambdas
```

**Return type change (e.g. `Flow<List<X>>` → `List<X>`):**
```
Grep: "methodName("           → ALL callers, regardless of which class/layer
Grep: ".first()"              → Flow consumption near the method
Grep: ".collect"              → ongoing collection
Grep: "flatMapLatest"         → flow switching
Grep: "combine("              → flow combining
Grep: "stateIn("              → conversion to StateFlow
```

**Interface method change:**
```
Grep: "override fun methodName"   → all implementors (including test fakes)
Grep: ": InterfaceName"           → all implementors
Grep: "methodName("               → all callers through the interface
```

---

### Step 3: Always search by method name — not only by class name

The most common mistake is stopping after the class rename search. Files that call the method through an interface or abstract type don't import the class at all — they won't appear in a class-name search.

**Example from practice:** renaming `ObserveTodoItemsUseCase` and changing `getItems()` from `Flow` to `suspend`:
- Grep for `ObserveTodoItemsUseCase` → finds ViewModel, MainActivity, and its direct test
- **Missed:** `DeleteTodoItemUseCaseTest`, `EditTodoItemUseCaseTest`, `ToggleTodoItemUseCaseTest` — all called `repo.getItems(userId).first()` through the `FakeTodoRepository`, which implements `TodoRepository` interface
- **Fix:** also Grep for `getItems(` and for `.first()` in test files

---

### Step 4: Check test fakes and stubs explicitly

Test fakes implement interfaces directly. When an interface changes, the fake must change too — and so do all tests that use the fake's old API.

```
Glob: **/*Fake*.kt       → find all test fakes
Glob: **/*Stub*.kt
Glob: **/*Mock*.kt
Glob: **/*Test*.kt       → all test files
```

Then Grep for the changed method name within those files.

---

### Step 5: For reactive → one-shot changes, trace consumption chains

When changing from Flow to suspend, find every place the old return type is consumed:

```
Grep: ".first()"
Grep: ".collect"
Grep: "flatMapLatest"
Grep: "combine("
Grep: "flowOf("
Grep: "stateIn("
```

For each match, check whether the surrounding code structure needs to change — not just the call site.

---

### Step 6: For ViewModel changes, trace the StateFlow subscription chain

When restructuring how a ViewModel gets its data, draw the dependency chain before touching code:

```
uiState.collect  →  combine(items, ...)  →  items.flatMapLatest(currentUserId)
```

Ask: if I remove or replace `items`, does `currentUserId` lose its only subscriber? If `currentUserId` uses `SharingStarted.WhileSubscribed`, removing its downstream consumer means `.value` stops updating — silently, with no compile error.

Trace the full chain top-down (what subscribes to what) before removing any link.

---

### Step 7: Build an explicit change list and confirm before coding

After all Grep passes, produce a table:

```
Main sources:
- TodoItemDao.kt              (getByUser: signature)
- TodoRepository.kt           (getItems: interface)
- LocalTodoRepository.kt      (getItems: impl)
- GetTodoItemsUseCase.kt      (new file)
- TodoListViewModel.kt        (subscription chain refactor)
- MainActivity.kt             (constructor call)

Test sources:
- FakeTodoRepository.kt              (getItems: fake impl)
- GetTodoItemsUseCaseTest.kt         (new file)
- TodoListViewModelTest.kt           (constructor)
- DeleteTodoItemUseCaseTest.kt       ← easily missed (calls getItems as Flow)
- EditTodoItemUseCaseTest.kt         ← easily missed (calls getItems as Flow)
- ToggleTodoItemUseCaseTest.kt       ← easily missed (calls getItems as Flow)
- TodoItemDaoTest.kt                 ← easily missed (calls getByUser as Flow)
- LocalTodoRepositoryTest.kt         ← easily missed (calls getItems as Flow)
```

Present this list. Confirm with the user before writing a single line of code.
