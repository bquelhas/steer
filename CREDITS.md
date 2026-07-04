# Credits & attribution

The Steer companion app is original work; this file records prior art and
third-party material it relies on.

## Platform & libraries

- **PebbleKit (classic)** — the phone→watch messaging API used to talk to the
  Pebble.
- **Pebble SDK / Rebble / Core Devices** — watch runtime and tooling.
- AndroidX / Material Components (Material You).

## Phone → watch maneuver-bitmap protocol

- Forwarding a map app's maneuver glyph to the watch as a small 1-bpp bitmap
  (`NAV_ICON_BITMAP`) was learned by studying **PebbleNavi**, a separate
  community app. The implementation here is independent; the credit is for the
  idea, not copied code.

## Maneuver classification

- `ManeuverFingerprints.kt` holds compact perceptual fingerprints used to map a
  navigation notification's icon to one of Steer's maneuver types. These
  fingerprints were derived by observing map-app notification icons at runtime;
  the original artwork itself is **not** included in this repository.

## Not included (intentionally excluded)

- **Google Maps maneuver artwork** (`app/src/debug/assets/gmaps_maneuvers/`) —
  Google's proprietary artwork, kept out of the published source. It was used
  only by the debug MockNav tool for auditing icon reading.

If you think something should be credited differently, please open an issue.
