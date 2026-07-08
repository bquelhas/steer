# Roundabout icon mapping (how it works)

This note explains, once and for all, how a Google Maps roundabout maneuver becomes one of
**our** watch icons. Written 2026-07-08 after the v1.3 fix so we don't re-litigate it later.

## The two independent things

A roundabout maneuver has **two** properties that are easy to conflate but are completely
independent:

1. **Circulation direction** — which way you drive *around* the island. This is
   **country-fixed**, not per-maneuver:
   - Portugal / right-hand traffic → you go **counter-clockwise (CCW)**.
   - UK / left-hand traffic → you go **clockwise (CW)**.
2. **Exit angle** — how sharply you leave the roundabout (sharp/normal/slight, left/right,
   straight, u-turn). This is per-maneuver and has **nothing to do** with the circulation.

> Bruno's rule: *"a seta não tem nada a ver com o sentido de circulação — eu tanto posso
> conduzir à direita e sair para a direita na rotunda como para a esquerda."*
> (The exit arrow is unrelated to which way you circulate.)

Google Maps encodes both in the glyph name:
`roundabout_enter_and_exit_<ccw|cw>_<angle>` — e.g. `..._ccw_slight_right`.

## How it maps to our icons

Our PDC/VectorDrawable icon set is split into two mirror families:

| Our family | `Direction` ids | Used for |
|------------|-----------------|----------|
| `_RIGHT`   | 21..28 (RBT 1..8), 30 (EXIT) | **CCW / Portugal** circulation |
| `_LEFT`    | 13..20 (RBT 1..8), 29 (EXIT) | **CW / UK** circulation |

**Circulation picks the family. Exit angle picks the number within the family.**

The icon families are horizontal mirror images of each other, which is exactly why they mirror
*both* the island's rotation and the exit arrow at once — that coupling is what caused all the
earlier confusion. It's fine: we never pick a family from the arrow, only from the country's
circulation.

### CCW (Portugal → `_RIGHT` family)

The exit angle grows right → straight → left, in 45° buckets:

| Maps suffix     | angle | our Direction            | id |
|-----------------|-------|--------------------------|----|
| `sharp_right`   | 45°   | `ROUNDABOUT_1_RIGHT`     | 21 |
| `normal_right`  | 90°   | `ROUNDABOUT_2_RIGHT`     | 22 |
| `slight_right`  | 135°  | `ROUNDABOUT_3_RIGHT`     | 23 |
| `straight`      | 180°  | `ROUNDABOUT_EXIT_RIGHT`  | 30 |
| `slight_left`   | 215°  | `ROUNDABOUT_5_RIGHT`     | 25 |
| `normal_left`   | 270°  | `ROUNDABOUT_6_RIGHT`     | 26 |
| `sharp_left`    | 315°  | `ROUNDABOUT_7_RIGHT`     | 27 |
| `u_turn`        | 360°  | `ROUNDABOUT_8_RIGHT`     | 28 |

Note: **straight uses the dedicated EXIT icon (id 30), not RBT 4.** RBT 4 is currently unused.

### CW (UK → `_LEFT` family)

Exact mirror: swap left↔right in the Maps suffix, target the `_LEFT` id.

| Maps suffix     | angle | our Direction           | id |
|-----------------|-------|-------------------------|----|
| `sharp_left`    | 45°   | `ROUNDABOUT_1_LEFT`     | 13 |
| `normal_left`   | 90°   | `ROUNDABOUT_2_LEFT`     | 14 |
| `slight_left`   | 135°  | `ROUNDABOUT_3_LEFT`     | 15 |
| `straight`      | 180°  | `ROUNDABOUT_EXIT_LEFT`  | 29 |
| `slight_right`  | 215°  | `ROUNDABOUT_5_LEFT`     | 17 |
| `normal_right`  | 270°  | `ROUNDABOUT_6_LEFT`     | 18 |
| `sharp_right`   | 315°  | `ROUNDABOUT_7_LEFT`     | 19 |
| `u_turn`        | 360°  | `ROUNDABOUT_8_LEFT`     | 20 |

### Angle-less glyphs ("generico" and "sair")

Maps also emits roundabout glyphs with no angle suffix. We map them to the closest arrow:

| Maps glyph                         | our Direction (CCW / CW)             |
|------------------------------------|--------------------------------------|
| `roundabout_enter[_and_exit]_ccw`  | `ROUNDABOUT_3_RIGHT` (generico)      |
| `roundabout_enter[_and_exit]_cw`   | `ROUNDABOUT_3_LEFT`  (generico)      |
| `roundabout_exit_ccw`              | `ROUNDABOUT_2_RIGHT` (sair)          |
| `roundabout_exit_cw`               | `ROUNDABOUT_2_LEFT`  (sair)          |

## Where the mapping lives in code

Two files, kept in sync. **Only the `Direction` target on the right changes; never reorder
`Direction` ids (append-only), and never hand-edit the baked hex fingerprints.**

- `android/app/src/main/java/com/bquelhas/steer/ManeuverFingerprints.kt` — the **runtime**
  nearest-neighbour table (Maps notification `largeIcon` → 256-bit signature → `Direction`).
  This is what actually drives the watch.
- `android/app/src/main/java/com/bquelhas/steer/MapsGlyphs.kt` — the ground-truth `LABELS`
  used by the mock-nav debug audit tool. Must match the runtime table.

If a whole country's roundabouts come out mirrored, the fix is to swap the CCW↔CW family
assignment in **both** files — it is purely a mapping change, never an icon redraw.

## Reference images

- `1_correspondencia_CCW.png` — CCW ↔ `_RIGHT` family 1-to-1 correspondence.
- `2_paleta_ids.png` — the full id palette.
- `3_proposta_bruno.png` — Bruno's confirmed proposal (this mapping).
- `4_duvidas.png` — the open-questions sheet that led to the confirmation.
