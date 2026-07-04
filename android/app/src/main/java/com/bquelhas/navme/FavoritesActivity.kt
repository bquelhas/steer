package com.bquelhas.navme

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Lists the user's favourite destinations and lets them add new ones or start
 * navigation with a tap. Add = name + address/coords dialog; tap a row to
 * navigate (a chooser appears when more than one navigator is installed);
 * long-press a row to delete it.
 */
class FavoritesActivity : AppCompatActivity() {

    private lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        supportActionBar?.title = getString(R.string.favorites_title)

        listContainer = findViewById(R.id.favoritesList)
        findViewById<MaterialButton>(R.id.btnAddFavorite).setOnClickListener { showAddDialog() }
        renderList()
    }

    private fun renderList() {
        listContainer.removeAllViews()
        val favs = FavoritesStore.all(applicationContext)
        findViewById<TextView>(R.id.favEmpty).visibility =
            if (favs.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

        favs.forEachIndexed { index, fav ->
            val btn = MaterialButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).also { it.bottomMargin = dp(8) }
                text = fav.label
                isAllCaps = false
                typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
                setOnClickListener { navigateTo(fav) }
                setOnLongClickListener { confirmDelete(index, fav); true }
            }
            listContainer.addView(btn)
        }

        // Keep the watch's SELECT-button favorite menu in sync with this list.
        if (PebbleEmitter.isWatchConnected(applicationContext)) {
            PebbleEmitter.sendFavorites(applicationContext)
        }
    }

    private fun showAddDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_favorite, null)
        val nameField = view.findViewById<TextInputEditText>(R.id.favName)
        val addrField = view.findViewById<TextInputEditText>(R.id.favAddress)
        AlertDialog.Builder(this)
            .setTitle(R.string.fav_add)
            .setView(view)
            .setPositiveButton(R.string.fav_save) { _, _ ->
                val name = nameField.text?.toString()?.trim().orEmpty()
                val addr = addrField.text?.toString()?.trim().orEmpty()
                if (name.isEmpty() || addr.isEmpty()) {
                    Toast.makeText(this, R.string.fav_incomplete, Toast.LENGTH_SHORT).show()
                } else {
                    FavoritesStore.add(applicationContext, Favorite(name, addr))
                    renderList()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun navigateTo(fav: Favorite) {
        val options = NavLauncher.intentsFor(applicationContext, fav.query)
        when {
            options.isEmpty() ->
                NavLauncher.launch(applicationContext, NavLauncher.genericGeoIntent(fav.query))
            options.size == 1 ->
                NavLauncher.launch(applicationContext, options[0].second)
            else -> {
                val names = options.map { it.first }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle(R.string.fav_choose_nav)
                    .setItems(names) { _, which ->
                        NavLauncher.launch(applicationContext, options[which].second)
                    }
                    .show()
            }
        }
    }

    private fun confirmDelete(index: Int, fav: Favorite) {
        AlertDialog.Builder(this)
            .setTitle(fav.label)
            .setMessage(R.string.fav_delete_confirm)
            .setPositiveButton(R.string.fav_delete) { _, _ ->
                FavoritesStore.removeAt(applicationContext, index)
                renderList()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
