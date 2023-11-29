package com.mobdeve.s15.cabinbin.garcia.mco

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.ComponentActivity

class StartActivity : ComponentActivity() {
    //OST
    private lateinit var ostPlayer: MediaPlayer
    private lateinit var ostHandler: Handler
    private lateinit var ostRunnable: Runnable
    private var isOSTPlaying = true

    //Start Activity Buttons
    private lateinit var startBtn: Button
    private lateinit var highScoreBtn: Button
    private lateinit var sfxBtn: ImageButton
    private lateinit var ostBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Menu Layout
        setContentView(R.layout.start_layout)

        //Music
        this.ostPlayer = MediaPlayer.create(this, R.raw.start_ost)
        initMusicHandler()

        //Start Button
        this.startBtn = findViewById(R.id.startBtn)
        this.startBtn.setOnClickListener{
            onDestroy()
            startActivity(Intent(this, GameActivity::class.java))
        }

        //High Scores Button
        this.highScoreBtn = findViewById(R.id.highScoreBtn)
        this.highScoreBtn.setOnClickListener {
            startActivity(Intent(this, HighScoreActivity::class.java))
        }

        //Sound FX Button
        this.sfxBtn = findViewById(R.id.sfxBtn)

        //Music and Music Button
        this.ostBtn = findViewById(R.id.ostBtn)
        this.ostBtn.setOnClickListener{
            if (this.isOSTPlaying){
                this.ostBtn.foreground = getDrawable(R.drawable.off)
                this.ostHandler.removeCallbacks(this.ostRunnable)
                this.ostPlayer.pause()
            }
            else{
                this.ostBtn.foreground = null
                this.ostHandler.postDelayed(this.ostRunnable, 0)
            }
            this.isOSTPlaying = !this.isOSTPlaying
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        this.ostHandler.postDelayed(this.ostRunnable, 0)
    }

    override fun onPause() {
        super.onPause()
        this.ostHandler.removeCallbacks(this.ostRunnable)
        this.ostPlayer.pause()
    }

    //Music Handler
    private fun initMusicHandler() {
        this.ostHandler = Handler(Looper.getMainLooper())
        this.ostRunnable = Runnable {
            if (!this.ostPlayer.isPlaying) {
                this.ostPlayer.start()
            }
            this.ostHandler.postDelayed(this.ostRunnable, 250) // Adjust the delay as needed
        }
    }
}