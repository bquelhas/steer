package com.bquelhas.navme

/**
 * Turns a navigation notification (title + text) into a [NaviData].
 *
 * Strategy: the maneuver keyword matching and distance extraction are shared
 * across apps (they all speak the same human vocabulary); only the package
 * dispatch differs. Real-world notification strings vary by app version and must
 * be tuned on-device — keep the keyword tables here as the single tuning point.
 */
object NaviParser {

    const val PKG_GOOGLE_MAPS = "com.google.android.apps.maps"
    const val PKG_WAZE = "com.waze"
    const val PKG_OSMAND = "net.osmand.plus"
    const val PKG_HERE = "com.here.app.maps"
    const val PKG_SYGIC = "com.sygic.aura"

    val SUPPORTED = setOf(PKG_GOOGLE_MAPS, PKG_WAZE)

    fun parse(pkg: String, title: String?, text: String?): NaviData? {
        if (pkg !in SUPPORTED) return null
        val t = listOfNotNull(title, text).joinToString(" ").trim()
        if (t.isEmpty()) return null

        val direction = mapManeuver(t)
        val distance = extractDistance(t)
        val instruction = title?.takeIf { it.isNotBlank() } ?: t
        val composed = compose(distance, instruction)
        return NaviData(direction, composed)
    }

    fun compose(distance: String?, instruction: String): String =
        if (distance != null) "$distance — $instruction" else instruction

    /** Extracts a human distance token like "400 m", "1.2 km", "0,5 km". */
    fun extractDistance(s: String): String? {
        val m = DISTANCE_RE.find(s) ?: return null
        val value = m.groupValues[1].replace(',', '.')
        val unit = m.groupValues[2].lowercase()
        return "$value ${if (unit.startsWith("k")) "km" else "m"}"
    }

    /** Maps a free-text instruction to a [Direction]. EN + PT keywords. */
    fun mapManeuver(raw: String): Direction {
        val s = raw.lowercase()

        // Arrival / departure first (they often also contain a side word).
        if (s.containsAny("arrive", "arriving", "destination", "chega", "chegou", "destino")) return Direction.ARRIVE
        if (s.containsAny("head ", "depart", "siga pela", "comece")) {
            // a bare "depart" with no later maneuver
            if (!s.containsAny("turn", "vire", "roundabout", "rotunda")) return Direction.DEPART
        }

        // Ferry
        if (s.containsAny("ferry", "ferry-boat", "barco", "cacilheiro")) return Direction.FERRY

        // Roundabout
        if (s.containsAny("roundabout", "rotunda", "rotatória")) {
            val exit = roundaboutExit(s)
            val left = isLeft(s)
            if (s.containsAny("exit the", "saia da", "sair da"))
                return if (left) Direction.ROUNDABOUT_EXIT_LEFT else Direction.ROUNDABOUT_EXIT_RIGHT
            if (exit in 1..8) {
                return if (left) ROUNDABOUT_LEFT[exit - 1] else ROUNDABOUT_RIGHT[exit - 1]
            }
            return if (left) Direction.GENERIC_ROUNDABOUT_LEFT else Direction.GENERIC_ROUNDABOUT_RIGHT
        }

        // Keep / fork / ramp / merge
        if (s.containsAny("keep left", "mantenha-se à esquerda", "mantenha à esquerda")) return Direction.KEEP_LEFT
        if (s.containsAny("keep right", "mantenha-se à direita", "mantenha à direita")) return Direction.KEEP_RIGHT
        if (s.containsAny("fork")) return if (isLeft(s)) Direction.FORK_LEFT else Direction.FORK_RIGHT
        if (s.containsAny("ramp", "rampa", "acesso à", "saída para a")) return if (isLeft(s)) Direction.RAMP_LEFT else Direction.RAMP_RIGHT
        if (s.containsAny("merge", "junte-se", "entroncamento")) return Direction.GENERIC_MERGE

        // U-turn — default left (right-hand traffic, PT); right only if explicit.
        if (s.containsAny("u-turn", "uturn", "inverta", "meia-volta", "inversão"))
            return if (isRight(s)) Direction.UTURN_RIGHT else Direction.UTURN_LEFT

        // Slight / sharp turns
        if (s.containsAny("slight left", "ligeiramente à esquerda")) return Direction.SLIGHT_LEFT
        if (s.containsAny("slight right", "ligeiramente à direita")) return Direction.SLIGHT_RIGHT
        if (s.containsAny("sharp left", "acentuada à esquerda", "apertada à esquerda")) return Direction.SHARP_LEFT
        if (s.containsAny("sharp right", "acentuada à direita", "apertada à direita")) return Direction.SHARP_RIGHT

        // Plain left / right
        if (s.containsAny("turn left", "vire à esquerda", "à esquerda", "esquerda")) return Direction.LEFT
        if (s.containsAny("turn right", "vire à direita", "à direita", "direita")) return Direction.RIGHT

        // Straight / continue
        if (s.containsAny("straight", "continue", "em frente", "siga em frente", "siga")) return Direction.STRAIGHT

        return Direction.STRAIGHT
    }

    // --- helpers ---

    private val DISTANCE_RE = Regex("""(\d+(?:[.,]\d+)?)\s*(km|m)\b""", RegexOption.IGNORE_CASE)
    // Matches both "3rd exit"/"2ª saída" (number before) and "saída 2"/"exit 2" (number after).
    private val EXIT_RE = Regex(
        """(?:(\d+)\s*(?:st|nd|rd|th|º|ª|a)?\s*(?:exit|saída|saida)|(?:exit|saída|saida)\s*(?:n[º.]?\s*|número\s*)?(\d+))""",
        RegexOption.IGNORE_CASE,
    )

    private val ROUNDABOUT_LEFT = arrayOf(
        Direction.ROUNDABOUT_1_LEFT, Direction.ROUNDABOUT_2_LEFT, Direction.ROUNDABOUT_3_LEFT, Direction.ROUNDABOUT_4_LEFT,
        Direction.ROUNDABOUT_5_LEFT, Direction.ROUNDABOUT_6_LEFT, Direction.ROUNDABOUT_7_LEFT, Direction.ROUNDABOUT_8_LEFT,
    )
    private val ROUNDABOUT_RIGHT = arrayOf(
        Direction.ROUNDABOUT_1_RIGHT, Direction.ROUNDABOUT_2_RIGHT, Direction.ROUNDABOUT_3_RIGHT, Direction.ROUNDABOUT_4_RIGHT,
        Direction.ROUNDABOUT_5_RIGHT, Direction.ROUNDABOUT_6_RIGHT, Direction.ROUNDABOUT_7_RIGHT, Direction.ROUNDABOUT_8_RIGHT,
    )

    private fun roundaboutExit(s: String): Int {
        val m = EXIT_RE.find(s) ?: return 0
        return (m.groupValues[1].ifEmpty { m.groupValues[2] }).toIntOrNull() ?: 0
    }
    private fun isLeft(s: String): Boolean = s.containsAny("left", "esquerda")
    private fun isRight(s: String): Boolean = s.containsAny("right", "direita")
    private fun String.containsAny(vararg needles: String) = needles.any { this.contains(it) }
}
