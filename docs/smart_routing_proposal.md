# Feature Proposal: Quick Share Smart Routing

**Status**: Proposal / Draft
**Goal**: Enhance Quick Share to intelligently route users to either the **Photo Editor** or **Batch Converter** based on the content shared.

## Current Behavior
- All shared images (`ACTION_SEND` or `ACTION_SEND_MULTIPLE`) are routed directly to **Batch Converter**.

## Proposed Logic
Instead of hardcoding `BatchConverterScreen` as the destination, `MainActivity` should inspect the intent payload:

### 1. Single Image Shared (`ACTION_SEND`)
If the user shares **exactly one** image:
1. Show a **BottomSheet** or **Dialog** asking the user's intent.
    - **Option A**: "Edit Image" -> Opens `PhotoEditorScreen` with the shared URI.
    - **Option B**: "Batch Convert" -> Opens `BatchConverterScreen` with the single URI (as a list of 1).
2. **(Optional)**: Save user preference ("Remember my choice") to auto-route in the future.

### 2. Multiple Images Shared (`ACTION_SEND_MULTIPLE`)
If the user shares **multiple** images:
1. Automatically route to **Batch Converter**.
    - *Reasoning*: The Photo Editor is designed for single-image manipulation. Batch processing is the only logical path for multiple files.

## Implementation Details

### MainActivity.kt
Modify the `startDestination` logic to intercept the single-image case.

```kotlin
// Pseudo-code concept
val uris = getSharedUris(intent)
if (uris.size == 1) {
    // Launch a transparent "RouterActivity" or navigate to a "ChoiceScreen"
    startDestination = Screen.QuickShareRouter.route 
    // Pass the single URI to this router
} else {
    // Existing logic
    mainViewModel.setSharedUris(uris)
    startDestination = Screen.BatchConverter.route
}
```

### New Screen: `QuickShareRouterScreen`
A simple transparent or dialog-themed Composable that presents the two options.

## Future Extensions
- **Video Support**: If a video is shared, route to a future Video Editor or Compressor.
- **Document Support**: If a PDF is shared, route to PDF Viewer/Editor.
