package com.bquelhas.navme

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Starts turn-by-turn navigation to a destination in one of the supported
 * navigator apps. Directions always start from the current location.
 *
 * The [query] may be a free-text address ("Rua da Paz, Lisboa") or a
 * "lat,lng" pair. Google Maps and Waze both accept a text query; OsmAnd needs
 * coordinates, so it is only offered when the query looks like "lat,lng".
 */
object NavLauncher {

    private const val PKG_MAPS = "com.google.android.apps.maps"
    private const val PKG_WAZE = "com.waze"
    private const val PKG_OSMAND = "net.osmand.plus"
    private const val PKG_OSMAND_FREE = "net.osmand"

    private fun isInstalled(context: Context, pkg: String): Boolean = try {
        context.packageManager.getPackageInfo(pkg, 0)
        true
    } catch (e: Exception) {
        false
    }

    private val LATLNG = Regex("""^\s*-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?\s*$""")

    /** Builds the list of navigator intents available for [query] on this device. */
    fun intentsFor(context: Context, query: String): List<Pair<String, Intent>> {
        val out = mutableListOf<Pair<String, Intent>>()
        val isCoords = LATLNG.matches(query)
        val encoded = Uri.encode(query.trim())

        if (isInstalled(context, PKG_MAPS)) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$encoded"))
                .setPackage(PKG_MAPS)
            out += "Google Maps" to i
        }
        if (isInstalled(context, PKG_WAZE)) {
            val uri = if (isCoords) "https://waze.com/ul?ll=$encoded&navigate=yes"
                      else "https://waze.com/ul?q=$encoded&navigate=yes"
            out += "Waze" to Intent(Intent.ACTION_VIEW, Uri.parse(uri)).setPackage(PKG_WAZE)
        }
        if (isCoords) {
            val coords = query.trim()
            val osmPkg = when {
                isInstalled(context, PKG_OSMAND) -> PKG_OSMAND
                isInstalled(context, PKG_OSMAND_FREE) -> PKG_OSMAND_FREE
                else -> null
            }
            if (osmPkg != null) {
                val (lat, lng) = coords.split(",").map { it.trim() }
                val i = Intent(Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q=$lat,$lng")).setPackage(osmPkg)
                out += "OsmAnd" to i
            }
        }
        return out
    }

    /**
     * Launches navigation. If several navigators are available, the caller is
     * expected to have let the user choose; this fires the given [intent].
     * Returns false (and toasts) when nothing can handle the destination.
     */
    fun launch(context: Context, intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, R.string.fav_no_nav_app, Toast.LENGTH_LONG).show()
            false
        }
    }

    /** Generic geo: fallback when no supported navigator is installed. */
    fun genericGeoIntent(query: String): Intent {
        val encoded = Uri.encode(query.trim())
        return Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
