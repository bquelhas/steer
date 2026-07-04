# Credits & attribution

Steer is original work, but it stands on prior art from the Pebble and
open-source navigation communities. This file records what inspired or was
reused, and under what terms.

## Pebble platform

- **Pebble SDK**, **Rebble** and **Core Devices** — the SDK, tooling and
  present-day watch runtime that make this app possible.

## Phone → watch maneuver-bitmap protocol

- The approach of forwarding a map app's own maneuver glyph to the watch as a
  small 1-bpp bitmap (surfaced here as the `NAV_ICON_BITMAP` message key) was
  learned by studying **PebbleNavi**, a separate community navigation app.
  Steer's implementation was written independently; the credit is for the
  protocol idea, not for any copied code or assets.

## Iconography

- The menu/car icon and the maneuver icon set (`resources/pebble_icons/pdc/`)
  are **derived from Pebble's own icon artwork** (the timeline / navigation icon
  set, e.g. `Car_rental`). The vectors were redrawn/traced from those Pebble
  icons and adapted to Pebble Draw Command format for Steer.
- That source artwork is published by pebble-dev under the **Apache License
  2.0**; this derivative is used and redistributed under those terms, with
  attribution. See <https://github.com/pebble-dev> for the originals.

## Not included in this repository

To keep the published source clean of third-party or proprietary material, the
following are intentionally **excluded** (see `.gitignore`) and are *not* part
of the MIT-licensed source:

- Decompiled third-party watchapps used only for protocol research
  (`_extracted/`, `pbw/`, `analysis/`).
- Map-provider maneuver artwork (never redistributed).

If you believe something here should be attributed differently, please open an
issue.
