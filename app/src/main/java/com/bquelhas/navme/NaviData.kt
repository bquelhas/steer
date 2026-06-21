package com.bquelhas.navme

/**
 * One navigation update ready to send to the watch. [instructionText] is the
 * finished display string (distance already embedded) — the watch does no parsing.
 */
data class NaviData(
    val direction: Direction,
    val instructionText: String,
    val gpsAccuracy: String? = null,
) {
    val directionId: Int get() = direction.id
}
