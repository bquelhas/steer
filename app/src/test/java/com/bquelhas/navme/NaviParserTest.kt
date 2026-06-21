package com.bquelhas.navme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NaviParserTest {

    private fun gmaps(title: String?, text: String? = null) =
        NaviParser.parse(NaviParser.PKG_GOOGLE_MAPS, title, text)

    @Test fun unsupportedPackageIgnored() {
        assertNull(NaviParser.parse("com.unknown.app", "Turn left", "300 m"))
    }

    @Test fun turnLeftEn() {
        assertEquals(Direction.LEFT, gmaps("Turn left onto Main St", "300 m")!!.direction)
    }

    @Test fun turnRightPt() {
        assertEquals(Direction.RIGHT, gmaps("Vire à direita na Rua A", "250 m")!!.direction)
    }

    @Test fun slightBeatsPlainLeft() {
        assertEquals(Direction.SLIGHT_LEFT, gmaps("Slight left onto A1", "1.2 km")!!.direction)
    }

    @Test fun sharpRight() {
        assertEquals(Direction.SHARP_RIGHT, gmaps("Sharp right", "80 m")!!.direction)
    }

    @Test fun uTurn() {
        assertEquals(Direction.UTURN_LEFT, gmaps("Make a U-turn", "120 m")!!.direction)
    }

    @Test fun roundaboutThirdExit() {
        assertEquals(Direction.ROUNDABOUT_3_RIGHT, gmaps("At the roundabout, take the 3rd exit", "200 m")!!.direction)
    }

    @Test fun roundaboutPtSecondExit() {
        assertEquals(Direction.ROUNDABOUT_2_RIGHT, gmaps("Na rotunda, saída 2", "150 m")!!.direction)
    }

    @Test fun keepLeft() {
        assertEquals(Direction.KEEP_LEFT, gmaps("Keep left", "500 m")!!.direction)
    }

    @Test fun ferry() {
        assertEquals(Direction.FERRY, gmaps("Take the ferry", "1 km")!!.direction)
    }

    @Test fun arrival() {
        assertEquals(Direction.ARRIVE, gmaps("You have arrived", null)!!.direction)
    }

    @Test fun distanceMeters() {
        assertEquals("400 m", NaviParser.extractDistance("In 400 m, turn left"))
    }

    @Test fun distanceKmComma() {
        assertEquals("2.5 km", NaviParser.extractDistance("Continue por 2,5 km"))
    }

    @Test fun composeWithDistance() {
        val d = gmaps("Turn left onto Rua da Paz", "300 m")!!
        assertEquals("300 m — Turn left onto Rua da Paz", d.instructionText)
    }

    @Test fun fallbackStraight() {
        assertEquals(Direction.STRAIGHT, gmaps("Continue on A5", "3 km")!!.direction)
    }

    @Test fun directionRoundTripsThroughId() {
        Direction.entries.forEach { assertEquals(it, Direction.fromId(it.id)) }
    }
}
