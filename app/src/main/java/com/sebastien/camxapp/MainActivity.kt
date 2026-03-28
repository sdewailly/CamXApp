package com.sebastien.camxapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.vending.licensing.AESObfuscator
import com.android.vending.licensing.LicenseChecker
import com.android.vending.licensing.LicenseCheckerCallback
import com.android.vending.licensing.StrictPolicy
import com.sebastien.camxapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var checker: LicenseChecker
    private val PREFS_NAME = "HistoryPrefs"
    private val HISTORY_KEY = "container_history"
    private lateinit var historyList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- Vérification de Licence ---
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val salt = byteArrayOf(
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89
        )
        val obfuscator = AESObfuscator(salt, packageName, deviceId)
        val policy = StrictPolicy()
        checker = LicenseChecker(this, policy, BuildConfig.PLAY_CONSOLE_PUBLIC_KEY)
        
        checker.checkAccess(object : LicenseCheckerCallback {
            override fun allow(reason: Int) { Log.d("Licensing", "Accès autorisé") }
            override fun dontAllow(reason: Int) {
                runOnUiThread {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.license_required_title))
                        .setMessage(getString(R.string.license_required_message))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.quit)) { _, _ -> finishAffinity() }
                        .setNeutralButton(getString(R.string.view_on_store)) { _, _ ->
                            val url = "https://play.google.com/store/apps/details?id=$packageName"
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            finishAffinity()
                        }.show()
                }
            }
            override fun applicationError(errorCode: Int) { Log.e("Licensing", "Erreur : $errorCode") }
        })

        // --- Historique des entrées ---
        val containerInput: AutoCompleteTextView = findViewById(R.id.container)
        
        // Charger l'historique initial
        historyList = getHistory().toMutableList()
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, historyList)
        containerInput.setAdapter(adapter)

        // Afficher les suggestions dès que l'utilisateur clique sur le champ
        containerInput.setOnClickListener {
            containerInput.showDropDown()
        }

        // Bouton Start
        findViewById<Button>(R.id.start_activity).setOnClickListener {
            val input = containerInput.text.toString().uppercase().trim()
            if (Regex("^[A-Z]{4}\\d{7}$").matches(input)){
                
                // Mettre à jour l'historique imméditament
                if (!historyList.contains(input)) {
                    historyList.add(0, input)
                    if (historyList.size > 10) historyList.removeAt(historyList.size - 1)
                    saveHistory(historyList)
                    // On recrée l'adapter ou on notifie pour être sûr que l'UI est à jour
                    adapter.notifyDataSetChanged()
                }

                val intent = Intent(this, Activity2::class.java).apply {
                    putExtra("container", input)
                }
                startActivity(intent)
            } else {
                containerInput.error = "Format: 4 lettres + 7 chiffres"
            }
        }

        // Bouton DONATE
        findViewById<Button>(R.id.donate_button).setOnClickListener {
            val url = "https://www.paypal.com/donate/?business=QCL67EUXHESQG&no_recurring=1&currency_code=EUR"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchir l'historique quand on revient sur l'activité (ex: après Activity2)
        val containerInput: AutoCompleteTextView = findViewById(R.id.container)
        historyList.clear()
        historyList.addAll(getHistory())
        adapter.notifyDataSetChanged()
    }

    private fun saveHistory(history: List<String>) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(HISTORY_KEY, history.toSet()).apply()
    }

    private fun getHistory(): List<String> {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historySet = prefs.getStringSet(HISTORY_KEY, emptySet()) ?: emptySet()
        // Les Set ne garantissent pas l'ordre, donc on pourrait vouloir stocker une chaîne JSON 
        // ou simplement accepter que l'ordre soit aléatoire ici. 
        // Pour un ordre chronologique strict, il faudrait une autre méthode.
        return historySet.toList()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::checker.isInitialized) checker.onDestroy()
    }
}
