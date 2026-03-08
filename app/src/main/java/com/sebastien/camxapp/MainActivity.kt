package com.sebastien.camxapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.vending.licensing.AESObfuscator
import com.android.vending.licensing.LicenseChecker
import com.android.vending.licensing.LicenseCheckerCallback
import com.android.vending.licensing.ServerManagedPolicy
import com.sebastien.camxapp.BuildConfig
import com.sebastien.camxapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var checker: LicenseChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Vérification de Licence ---
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val salt = byteArrayOf(
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89
        )
        
        val obfuscator = AESObfuscator(salt, packageName, deviceId)
        val policy = ServerManagedPolicy(this, obfuscator)
        
        // Utilisation de la clé RSA injectée depuis local.properties
        checker = LicenseChecker(this, policy, BuildConfig.PLAY_CONSOLE_PUBLIC_KEY)
        
        checker.checkAccess(object : LicenseCheckerCallback {
            override fun allow(reason: Int) {
                Log.d("Licensing", "Accès autorisé")
            }

            override fun dontAllow(reason: Int) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity, 
                        "Application non licenciée. Veuillez l'utiliser via le Play Store.", 
                        Toast.LENGTH_LONG
                    ).show()
                    finish() 
                }
            }

            override fun applicationError(errorCode: Int) {
                Log.e("Licensing", "Erreur technique de licence : $errorCode")
            }
        })
        // --- Fin Vérification ---

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        val bouton: Button = findViewById(R.id.start_activity)
        bouton.setOnClickListener {
            val intent = Intent(this, Activity2::class.java)
            val containerInput: EditText = findViewById(R.id.container)
            val cont_num: String = containerInput.text.toString()
            val pattern = Regex("^[A-Z]{4}\\d{7}$")
            if (pattern.matches(cont_num)){
                intent.putExtra("container", cont_num)
                startActivity(intent)
            } else {
                containerInput.error = "Must start with 4 capital letters followed by exactly 7 digits"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::checker.isInitialized) {
            checker.onDestroy()
        }
    }
}
