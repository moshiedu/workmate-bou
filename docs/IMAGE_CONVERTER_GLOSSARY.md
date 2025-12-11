# Image Converter Feature Glossary

This document outlines the core logic and features of the Batch Image Converter module.

## Core Features

### 1. Smart Resizing
**Problem**: Users want to hit a specific file size (e.g. 500KB) but reducing quality isn't always enough.
**Solution**:
- If `Target Size` is set, the app first tries to reduce JPEG/WEBP quality.
- If quality reduction hits a limit (e.g. 10%) and the file is *still* too big, the app iteratively downscales the image resolution.
- **Algorithm**:
    - If file > 2x Data Limit: Scale by 0.7x
    - Else: Scale by 0.9x
    - Loop until target size is met or min dimensions (50x50) reached.

### 2. Auto Format ("Original")
**Problem**: Batch converting mixed files (PNGs and JPEGs) often forces them all to becoming JPEGs, losing transparency for PNGs.
**Solution**:
- "Auto" mode detects the input MIME type.
- If input is PNG -> Output is PNG (Lossless).
- If input is JPEG -> Output is JPEG.
- Allows resizing mixed batches without "flattening" transparent images.

### 3. HEIF Support
**Problem**: JPEGs are large.
**Solution**:
- On Android P (API 28+), enables `HeifWriter`.
- Creates `.heic` files which are ~50% smaller than JPEGs at similar quality.
- Fallback to JPEG if device encoding fails.

### 4. Per-File Progress
**Problem**: `Bitmap.compress` is a thread-blocking operation. The UI would normally freeze or show static progress during large saves.
**Solution**:
- A background coroutine "simulates" progress (70% -> 95%) during the compression phase.
- Gives users visual feedback that the app is working, even when the OS isn't reporting byte progress.

## Upcoming Features (Planned)

### Metadata Control
- **Privacy Mode**: Strip all EXIF data (GPS, Camera Model) for safe sharing.
- **Preserve Mode**: Copy original EXIF tags to the new file (requires `ExifInterface`).

### Custom Presets
- Save common configurations (e.g. "Passport Photo", "WhatsApp Sticker") for one-tap reuse.
- Stored in User Preferences.

### PDF Generation
- Merge selected images into a single multi-page PDF document.
- Ideal for scanning documents or consolidating receipts.
