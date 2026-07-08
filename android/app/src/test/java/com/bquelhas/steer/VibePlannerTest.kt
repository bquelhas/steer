package com.bquelhas.steer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VibePlannerTest {

    private val STRAIGHT = Direction.STRAIGHT.id
    private val LEFT = Direction.LEFT.id

    @Before fun clean() = VibePlanner.reset()

    private fun update(dist: Double?, speed: Int, dir: Int = LEFT, street: String = "Rua A"): Boolean {
        val text = if (dist != null) "${dist.toInt()} m — $street" else street
        return VibePlanner.onUpdate(dir, text, dist, speed)
    }

    // 1) TIME path: at 120 km/h the lead is 25 s ≈ 833 m — motorway exits buzz far out.
    @Test fun motorwaySpeedBuzzesFarFromExit() {
        assertFalse(update(5000.0, 120))
        assertFalse(update(2000.0, 120))
        assertFalse(update(900.0, 120))   // 900 > 833
        assertTrue(update(800.0, 120))    // crossed the 25 s line
        assertFalse(update(500.0, 120))   // latched: one buzz per maneuver
        assertFalse(update(50.0, 120))
    }

    // TIME path: in town at 40 km/h the same 25 s is only ~278 m.
    @Test fun citySpeedBuzzesClose() {
        assertFalse(update(500.0, 40))
        assertTrue(update(270.0, 40))
    }

    // TIME path: walking speeds clamp to the 50 m floor so the buzz isn't uselessly early.
    @Test fun walkingClampsToMinLead() {
        assertFalse(update(100.0, 4))
        assertTrue(update(45.0, 4))
    }

    // 2) SEGMENT fallback: no speed, a 15 km announcement means motorway -> 700 m lead.
    @Test fun noSpeedMotorwaySegmentBucket() {
        assertFalse(update(15000.0, -1))
        assertFalse(update(800.0, -1))
        assertTrue(update(690.0, -1))
    }

    // SEGMENT fallback: a short 900 m street segment -> 200 m lead.
    @Test fun noSpeedStreetSegmentBucket() {
        assertFalse(update(900.0, -1))
        assertFalse(update(300.0, -1))
        assertTrue(update(190.0, -1))
    }

    // 3) LEGACY fallback: no distance at all -> one buzz per new instruction, like before.
    @Test fun noDistanceBuzzesOncePerInstruction() {
        assertTrue(update(null, -1, street = "Turn left"))
        assertFalse(update(null, -1, street = "Turn left"))
        assertTrue(update(null, -1, street = "Turn right"))
    }

    // A reroute (distance jumps back up on the same instruction) re-arms the latch.
    @Test fun rerouteRearmsTheLatch() {
        assertFalse(update(400.0, 50))    // lead at 50 km/h ≈ 347 m
        assertTrue(update(340.0, 50))
        assertFalse(update(2000.0, 50))   // reroute: same street, distance jumped up
        assertTrue(update(340.0, 50))     // buzzes again for the new approach
    }

    // A new maneuver re-arms; small GPS wobble upward does NOT.
    @Test fun newManeuverRearmsButWobbleDoesNot() {
        assertTrue(update(200.0, 50, street = "Rua A"))
        assertFalse(update(230.0, 50, street = "Rua A"))  // +30 m wobble, still latched
        assertFalse(update(3000.0, 50, street = "Rua B", dir = STRAIGHT))
        assertTrue(update(300.0, 50, street = "Rua B", dir = STRAIGHT))
    }

    // The countdown embedded in the display text must not change the maneuver identity.
    @Test fun distanceCountdownKeepsIdentity() {
        assertFalse(VibePlanner.onUpdate(LEFT, "500 m — Rua X", 500.0, 40))
        assertTrue(VibePlanner.onUpdate(LEFT, "250 m — Rua X", 250.0, 40))
        assertFalse(VibePlanner.onUpdate(LEFT, "100 m — Rua X", 100.0, 40))
    }
}
