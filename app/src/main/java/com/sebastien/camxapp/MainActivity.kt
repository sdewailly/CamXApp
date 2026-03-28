package com.sebastien.camxapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.vending.licensing.AESObfuscator
import com.android.vending.licensing.LicenseChecker
import com.android.vending.licensing.LicenseCheckerCallback
import com.android.vending.licensing.StrictPolicy
import com.sebastien.camxapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var checker: LicenseChecker

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
            override fun allow(reason: Int) {
                Log.d("Licensing", "Accès autorisé (Reason: $reason)")
            }

            override fun dontAllow(reason: Int) {
                Log.w("Licensing", "Accès refusé (Reason: $reason)")
                runOnUiThread {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.license_required_title))
                        .setMessage(getString(R.string.license_required_message))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.quit)) { _, _ ->
                            finishAffinity()
                        }
                        .setNeutralButton(getString(R.string.view_on_store)) { _, _ ->
                            val url = "https://play.google.com/store/apps/details?id=$packageName"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                            finishAffinity()
                        }
                        .show()
                }
            }

            override fun applicationError(errorCode: Int) {
                Log.e("Licensing", "Erreur technique de licence : $errorCode")
            }
        })
        // --- Fin Vérification ---

        // Configuration du bouton de démarrage (Activity 2)
        val boutonStart: Button = findViewById(R.id.start_activity)
        boutonStart.setOnClickListener {
            val containerInput: EditText = findViewById(R.id.container)
            val contNum: String = containerInput.text.toString()
            val pattern = Regex("^[A-Z]{4}\\d{7}$")
            if (pattern.matches(contNum)){
                val intent = Intent(this, Activity2::class.java)
                intent.putExtra("container", contNum)
                startActivity(intent)
            } else {
                containerInput.error = "Must start with 4 capital letters followed by exactly 7 digits"
            }
        }

        // Configuration du bouton DONATE
        val donateButton: Button = findViewById(R.id.donate_button)
        donateButton.setOnClickListener {
            // Remplacez l'URL ci-dessous par votre lien PayPal, BuyMeACoffee ou autre
            val url = "https://www.paypal.com/donate/?business=QCL67EUXHESQG&no_recurring=1&currency_code=EUR"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::checker.isInitialized) {
            checker.onDestroy()
        }
    }
}
