# AR Waypoint Navigation - Technical Documentation

## Overview
The AR (Augmented Reality) waypoint navigation feature provides real-time visual guidance to saved waypoints using the device's back camera and compass sensors.

## Critical Implementation Details

### Phone Orientation Fix (MOST IMPORTANT)
**Problem**: When holding a phone in portrait mode with the back camera facing forward, the compass sensor reports the direction the **back of the phone** points, not the direction the user is facing.

**Solution**: Add 180° to the raw azimuth value:
```kotlin
val adjustedAzimuth = (azimuth + 180) % 360
```

**Why This Works**:
- Raw azimuth = direction the back of phone points
- User faces opposite direction when looking at screen
- Adding 180° corrects for this orientation mismatch
- This is the **KEY FIX** that makes AR direction accurate

### Direction Calculation
```kotlin
// 1. Get waypoint bearing (0-360° from True North)
val bearing = viewModel.getWaypointBearing(waypoint)

// 2. Adjust device azimuth for phone orientation
val adjustedAzimuth = (azimuth + 180) % 360

// 3. Calculate relative angle (-180° to +180°)
var diff = bearing - adjustedAzimuth
while (diff < -180) diff += 360
while (diff > 180) diff -= 360

// 4. Rotate arrow by diff degrees
rotate(diff)
```

### Coordinate System
- **Bearing**: Absolute direction to waypoint (0° = North, 90° = East, 180° = South, 270° = West)
- **Azimuth**: Device heading from compass sensor (same convention)
- **Adjusted Azimuth**: Azimuth + 180° (accounts for phone orientation)
- **Diff**: Angular difference, normalized to shortest turn (-180° to +180°)

## Features

### 1. Central 3D Arrow
- **Purpose**: Main directional indicator pointing toward waypoint
- **Behavior**:
  - Rotates to point at waypoint
  - Turns **green** and **pulses** when aligned (within 15°)
  - Blue when not aligned
- **Implementation**: Uses `withTransform` with `rotate(diff)`

### 2. Compass Strip
- **Location**: Top of screen
- **Shows**: Cardinal directions (N, NE, E, SE, S, SW, W, NW)
- **Updates**: Dynamically based on device heading
- **Field of View**: 60° horizontal

### 3. Waypoint Markers
**On-Screen** (waypoint within 60° FOV):
- Circle marker on compass strip
- Waypoint name and distance below strip

**Off-Screen** (waypoint outside FOV):
- Curved turn arrow at screen edge (left or right)
- Indicates shortest turn direction
- Distance displayed below arrow

### 4. No Waypoint Message
When no waypoint is selected:
- Semi-transparent dark background box
- Location pin icon
- Clear instructions:
  1. Go to Waypoints tab
  2. Tap + to save a location
  3. Select a waypoint

### 5. Debug Display
Shows real-time values for troubleshooting:
- **Bearing**: Direction to waypoint
- **Azimuth**: Raw → Adjusted (shows both values)
- **Diff**: Calculated difference (green when aligned)

## Requirements
- **True North (GPS)** must be enabled
- **Camera permission** required
- **Location permission** required (for True North)

## Common Issues & Solutions

### Issue: Arrow points in wrong direction
**Cause**: Missing 180° azimuth adjustment
**Fix**: Ensure `adjustedAzimuth = (azimuth + 180) % 360` is used

### Issue: Arrow doesn't rotate
**Cause**: Using raw azimuth instead of adjusted
**Fix**: Use `adjustedAzimuth` in diff calculation

### Issue: Green alignment when not aligned
**Cause**: Alignment check using wrong azimuth
**Fix**: Calculate diff with adjusted azimuth before checking alignment

## Code Structure

### Key Files
- `ARTab.kt`: Main AR composable
- `CompassViewModel.kt`: Sensor data and waypoint calculations
- `NavigationUtils.kt`: Bearing calculation utilities

### Important Functions
- `getWaypointBearing()`: Calculates bearing to waypoint
- `getWaypointDistance()`: Calculates distance to waypoint
- `calculateBearing()`: Haversine formula for bearing

## Testing Checklist
1. ✅ Enable True North in Compass tab
2. ✅ Save a waypoint in Waypoints tab
3. ✅ Select the waypoint
4. ✅ Go to AR tab
5. ✅ Verify arrow points toward waypoint
6. ✅ Walk around and verify arrow rotates correctly
7. ✅ Check debug values match expected directions
8. ✅ Verify green alignment when facing waypoint
9. ✅ Test off-screen indicators (turn arrows)
10. ✅ Verify no waypoint message appears when deselected

## Performance Notes
- Canvas drawing is efficient for real-time updates
- Sensor updates throttled by Android system
- Camera preview runs on separate thread
- No significant battery impact

## Future Enhancements
- Distance-based arrow scaling
- Multiple waypoint support
- Elevation/altitude guidance
- Augmented reality labels for landmarks
- Route visualization

---

**Last Updated**: 2025-11-25  
**Version**: 1.0  
**Status**: Production Ready ✅
