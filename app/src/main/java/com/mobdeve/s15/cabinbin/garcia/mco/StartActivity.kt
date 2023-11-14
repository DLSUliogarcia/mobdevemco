package com.mobdeve.s15.cabinbin.garcia.mco

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.ComponentActivity

class StartActivity : ComponentActivity() {
    //Start Activity Buttons
    private lateinit var startBtn: Button
    private lateinit var highScoreBtn: Button
    private lateinit var sfxBtn: ImageButton
    private lateinit var ostBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Menu Layout
        setContentView(R.layout.start_layout)

        //Start Button
        this.startBtn = findViewById(R.id.startBtn)
        this.startBtn.setOnClickListener{
            startActivity(Intent(this, GameActivity::class.java))
        }

        //High Scores Button
        this.highScoreBtn = findViewById(R.id.highScoreBtn)
        this.highScoreBtn.setOnClickListener {
            startActivity(Intent(this, HighScoreActivity::class.java))
        }

        //Sound FX Button
        this.sfxBtn = findViewById(R.id.sfxBtn)

        //Music Button
        this.ostBtn = findViewById(R.id.ostBtn)
    }
}