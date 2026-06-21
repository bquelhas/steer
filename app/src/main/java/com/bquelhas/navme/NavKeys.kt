package com.bquelhas.navme

import java.util.UUID

/**
 * Shared AppMessage contract between this Android app and the NavMe Pebble watchapp.
 * The integer key values MUST match `messageKeys` in the Pebble package.json.
 */
object NavKeys {
    val WATCH_UUID: UUID = UUID.fromString("1bdfe435-6a34-42d5-aed7-ace29fec1260")

    const val NAV_TURN = 0          // int: maneuver index (Direction.id)
    const val NAV_TEXT_BEGIN = 1    // uint8: start of instruction frame
    const val NAV_TEXT = 2          // cstring: full instruction (distance embedded)
    const val NAV_TEXT_END = 3      // uint8: commit instruction to display
    const val NAV_INVERT_COLOR = 4  // int: 0 normal, 1 inverted theme
    const val NAV_GPS_ACCURACY = 5  // cstring: Unknown|Poor|Medium|Good|Excellent
    const val NAV_CANCEL = 99       // uint8: navigation stopped / clear display
}
