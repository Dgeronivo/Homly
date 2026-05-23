---
description: Add @Preview annotations to Composables when editing Kotlin files
paths:
  - "**/*.kt"
---

When editing `.kt` files that contain `@Composable` functions, add `@Preview(showBackground = true)` annotations where they are missing — unless the composable requires parameters that cannot reasonably be given default/preview values.
