package com.bquelhas.steer

/**
 * Decides WHEN the watch should buzz for the upcoming maneuver — replacing the old
 * buzz-on-every-new-instruction with a single "get ready" warning per maneuver, fired
 * at a lead distance that adapts to how fast you are travelling.
 *
 * Cascade, best signal first:
 *  1. TIME — with a fresh GPS speed, buzz when time-to-maneuver (distance / speed)
 *     drops to [PREPARE_SECONDS]. At 120 km/h that is ~830 m before a motorway exit;
 *     at 40 km/h in town, ~280 m. Speed IS the road-type detector: no map data needed,
 *     and it adapts to traffic (a jammed motorway behaves like a street).
 *  2. SEGMENT LENGTH — no usable speed (tunnel, no location permission, cold GPS):
 *     bucket by the longest distance ever announced for this maneuver (~ the segment
 *     length). A "50 km until the exit" announcement can only mean a motorway.
 *  3. LEGACY — no parseable distance at all: buzz once when the instruction changes,
 *     exactly like the old behaviour.
 *
 * One buzz per maneuver: a latch arms when the maneuver changes and REARMS when the
 * announced distance jumps back up (reroute, or the same instruction on a new leg).
 * State is process-wide and reset per navigation session by the listener service.
 */
object VibePlanner {

    /** Lead time of the "get ready" buzz. */
    private const val PREPARE_SECONDS = 25.0

    /** Below this the GPS speed is parked/noise — fall through to the segment buckets. */
    private const val MIN_SPEED_KMH = 3

    /** Clamp of the speed-derived lead so walking still gets a usable warning and a
     *  motorway sprint doesn't buzz absurdly early. */
    private const val MIN_LEAD_M = 50.0
    private const val MAX_LEAD_M = 1500.0

    /** A distance increase this large on the SAME instruction means a reroute / new leg. */
    private const val REARM_JUMP_M = 150.0

    // Fallback 2: lead by announced segment length when speed is unavailable.
    private const val SEG_MOTORWAY_M = 10_000.0
    private const val SEG_ROAD_M = 2_000.0
    private const val LEAD_MOTORWAY_M = 700.0
    private const val LEAD_ROAD_M = 400.0
    private const val LEAD_STREET_M = 200.0

    private var maneuverKey: String? = null
    private var segmentMeters = 0.0
    private var buzzed = false
    private var lastDistance = 0.0

    /** Clears all per-session state. Call at navigation session start and end. */
    @Synchronized
    fun reset() {
        maneuverKey = null
        segmentMeters = 0.0
        buzzed = false
        lastDistance = 0.0
    }

    /**
     * Feed one navigation update; returns true when the watch should buzz NOW.
     *
     * @param directionId the maneuver id (Direction.id) of this update.
     * @param instructionText the composed display text ("300 m — Rua X"); the live
     *   distance prefix is ignored for identity — only the instruction part is stable.
     * @param distanceMeters parsed numeric distance to the maneuver, or null.
     * @param speedKmh current GPS speed, or -1 when unknown/stale.
     */
    @Synchronized
    fun onUpdate(directionId: Int, instructionText: String, distanceMeters: Double?, speedKmh: Int): Boolean {
        val key = "$directionId|${instructionText.substringAfter(" — ", instructionText)}"

        if (distanceMeters == null) {
            // Fallback 3 (legacy): nothing to reason about -> one buzz per new instruction.
            val isNew = key != maneuverKey
            maneuverKey = key
            segmentMeters = 0.0
            buzzed = true // this maneuver has had its buzz
            lastDistance = Double.MAX_VALUE // block a spurious rearm if distance appears later
            return isNew
        }

        if (key != maneuverKey) {
            maneuverKey = key
            segmentMeters = distanceMeters
            buzzed = false
        } else {
            if (distanceMeters > lastDistance + REARM_JUMP_M) {
                segmentMeters = distanceMeters
                buzzed = false
            }
            if (distanceMeters > segmentMeters) segmentMeters = distanceMeters
        }
        lastDistance = distanceMeters

        if (buzzed) return false
        if (distanceMeters <= leadMeters(speedKmh)) {
            buzzed = true
            return true
        }
        return false
    }

    /** The lead distance (m) at which the buzz should fire, given the current speed. */
    private fun leadMeters(speedKmh: Int): Double {
        if (speedKmh >= MIN_SPEED_KMH) {
            return (speedKmh / 3.6 * PREPARE_SECONDS).coerceIn(MIN_LEAD_M, MAX_LEAD_M)
        }
        return when {
            segmentMeters >= SEG_MOTORWAY_M -> LEAD_MOTORWAY_M
            segmentMeters >= SEG_ROAD_M -> LEAD_ROAD_M
            else -> LEAD_STREET_M
        }
    }
}
