package com.bquelhas.steer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Regression net for the baked fingerprint table ([ManeuverFingerprints]): every entry
 * must map its source Maps glyph to the ground-truth [Direction] in [MapsGlyphs.expected].
 * A mapping slip (like the mirrored-roundabout bug fixed in v1.2/1.3) fails here
 * immediately — no image processing needed, the table carries the glyph names as data.
 */
class MapsGlyphTableTest {

    // Gradle runs unit tests with the module dir (android/app) as the working directory.
    private val glyphDir = File("src/debug/assets/gmaps_maneuvers")

    @Test
    fun everyTableEntryMatchesTheGroundTruthLabel() {
        val failures = ManeuverFingerprints.TABLE.mapNotNull { entry ->
            val expected = MapsGlyphs.expected(entry.glyph)
                ?: return@mapNotNull "${entry.glyph}: not in MapsGlyphs.LABELS"
            if (entry.direction != expected)
                "${entry.glyph}: table says ${entry.direction}, ground truth is $expected"
            else null
        }
        assertTrue("table/ground-truth mismatches:\n" + failures.joinToString("\n"),
            failures.isEmpty())
    }

    @Test
    fun everyLabelledGlyphHasATableEntry() {
        // The table was generated from the full glyph set; a missing name means an entry
        // was accidentally dropped (that maneuver would silently fall back to geometry).
        val inTable = ManeuverFingerprints.TABLE.map { it.glyph }.toSet()
        assumeTrue(glyphDir.isDirectory)
        val missing = pngNames().filter { it !in inTable }
        assertTrue("glyph assets with no table entry: $missing", missing.isEmpty())
    }

    @Test
    fun everyGlyphAssetHasAGroundTruthLabel() {
        assumeTrue(glyphDir.isDirectory)
        val unlabelled = pngNames().filter { MapsGlyphs.expected(it) == null }
        assertTrue("glyphs missing from MapsGlyphs.LABELS: $unlabelled", unlabelled.isEmpty())
    }

    @Test
    fun signaturesAreWellFormed() {
        for (entry in ManeuverFingerprints.TABLE) {
            assertEquals("${entry.glyph}: bad signature length",
                ManeuverClassifier.SIG_BYTES, entry.sig.size)
        }
    }

    @Test
    fun uniqueSignaturesResolveToTheirOwnEntry() {
        // Maps reuses one artwork for several maneuvers (turn/ramp, fork/keep, ccw/cw
        // roundabout pairs); those DUPLICATE signatures deliberately resolve to the first
        // (highest-priority) entry. But a glyph with a UNIQUE signature must resolve to
        // its own Direction — this catches a colliding entry inserted above it.
        val counts = ManeuverFingerprints.TABLE.groupingBy { it.sig.joinToString { b -> "%02x".format(b) } }
            .eachCount()
        for (entry in ManeuverFingerprints.TABLE) {
            val sigHex = entry.sig.joinToString { b -> "%02x".format(b) }
            if (counts[sigHex] != 1) continue
            // Replicates the classifier's nearest-neighbour scan (strictly-smaller wins).
            var best: Direction? = null
            var bestDist = Int.MAX_VALUE
            for ((tsig, tdir) in ManeuverFingerprints.TABLE) {
                val d = ManeuverClassifier.hamming(entry.sig, tsig)
                if (d < bestDist) { bestDist = d; best = tdir }
            }
            assertEquals("${entry.glyph}: unique signature hijacked by another entry",
                entry.direction, best)
        }
    }

    private fun pngNames(): List<String> =
        glyphDir.listFiles { f -> f.name.endsWith(".png") }!!
            .map { it.name.removeSuffix(".png") }
            .sorted()
}
