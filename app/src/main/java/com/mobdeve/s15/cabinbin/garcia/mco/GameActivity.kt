package com.mobdeve.s15.cabinbin.garcia.mco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.media.MediaPlayer
import com.q42.android.scrollingimageview.ScrollingImageView

class GameActivity : ComponentActivity() {
    //Background Thread
    private lateinit var background: ScrollingImageView

    //OST Thread
    private lateinit var ostPlayer: MediaPlayer
    private lateinit var ostHandler: Handler
    private lateinit var ostRunnable: Runnable
    private var isOSTPlaying = true

    //Score Thread
    private lateinit var scoreHandler: Handler
    private lateinit var scoreRunnable: Runnable
    private lateinit var scoreTextView: TextView
    private var score: Long = 0

    //Pause Menu Buttons
    private lateinit var pauseBtn: ImageButton
    private lateinit var pauseMenu: LinearLayout
    private lateinit var contBtn: Button
    private lateinit var quitBtn: Button
    private lateinit var sfxBtn: ImageButton
    private lateinit var ostBtn: ImageButton

    //Game Over Menu
    private lateinit var gameOverMenu: LinearLayout
    private lateinit var replayBtn: Button
    private lateinit var returnBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Menu Layout
        setContentView(R.layout.game_layout)

        //Background
        this.background = findViewById(R.id.scrollingBG)
        this.background.start()

        //Music
        this.ostPlayer = MediaPlayer.create(this, R.raw.game_ost)
        initMusicHandler()

        //Pause Button and Pause Menu
        this.pauseBtn = findViewById(R.id.pauseBtn)
        this.pauseMenu = findViewById(R.id.pauseMenu)
        this.pauseMenu.visibility = View.INVISIBLE
        this.pauseBtn.setOnClickListener{
            this.background.stop()
            this.pauseMenu.visibility = View.VISIBLE
            this.scoreHandler.removeCallbacks(this.scoreRunnable)
        }
        //Pause Menu: Continue Button
        this.contBtn = findViewById(R.id.continueBtn)
        this.contBtn.setOnClickListener{
            this.background.start()
            this.pauseMenu.visibility = View.INVISIBLE
            this.scoreHandler.postDelayed(this.scoreRunnable, 500)
        }
        //Pause Menu: Quit Button
        this.quitBtn = findViewById(R.id.quitBtn)
        this.quitBtn.setOnClickListener{
            startActivity(Intent(this, StartActivity::class.java))
        }
        //Pause Menu: Ost Button
        this.ostBtn = findViewById(R.id.game_ostBtn)
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

        //Score Updater
        //Increases Score by 1 Every Half-Second
        this.scoreTextView = findViewById(R.id.score)
        initScoreHandler()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        this.ostHandler.postDelayed(this.ostRunnable, 0)
        this.scoreHandler.postDelayed(this.scoreRunnable, 500)
    }

    override fun onPause() {
        super.onPause()
        this.ostHandler.removeCallbacks(this.ostRunnable)
        this.ostPlayer.pause()
        this.scoreHandler.removeCallbacks(this.scoreRunnable)
    }

    private fun initBGHandler() {
    }

    private fun drawBackground() {
    }

    //Music Handler
    private fun initMusicHandler() {
        this.ostHandler = Handler(Looper.getMainLooper())
        this.ostRunnable = Runnable {
            if (!this.ostPlayer.isPlaying) {
                this.ostPlayer.start()
            }
            this.ostHandler.postDelayed(this.ostRunnable, 250)
        }
    }

    //Score Handler
    private fun initScoreHandler() {
        this.scoreHandler = Handler(Looper.getMainLooper())
        this.scoreRunnable = Runnable {
            increaseScore()
            this.scoreHandler.postDelayed(this.scoreRunnable, 500)
        }
    }

    private fun increaseScore() {
        this.score++
        this.scoreTextView.text = this.score.toString()
    }

}