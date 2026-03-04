package com.sebastien.camxapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sebastien.camxapp.R.id.container

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        val bouton: Button = findViewById(R.id.start_activity)
        bouton.setOnClickListener {
            val intent = Intent(this, Activity2::class.java)
            val container: EditText = findViewById(container)
            val cont_num: String = container.text.toString()
            val pattern = Regex("^[A-Z]{4}\\d{7}$")
            if (pattern.matches(cont_num)){
                intent.putExtra("container", cont_num)
                startActivity(intent)
            } else {
                container.error = "Must start with 4 capital letters followed by exactly 7 digits"
            }
        }
    }
}
