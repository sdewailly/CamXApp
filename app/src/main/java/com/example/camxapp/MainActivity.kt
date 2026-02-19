package com.example.camxapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.camxapp.R.id.container

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
         */
        val bouton: Button = findViewById(R.id.start_activity)
        bouton.setOnClickListener {
            // throw RuntimeException("Test Crash") Force a crash
            val intent = Intent(this,Activity2::class.java)
            val container: EditText = findViewById(container)
            val cont_num: String = container.text.toString()
            val pattern = Regex("^[A-Z]{4}\\d{7}$")
            if (pattern.matches(cont_num)){
                // Starts with a letter
                intent.putExtra("container",cont_num)
                startActivity(intent)
            } else {
                container.error = "Must start with 4 capital letters followed by exactly 7 digits"
            }
        }
    }
}