package com.mobdeve.s15.cabinbin.garcia.mco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GameActivity : ComponentActivity() {
    //Background
    private lateinit var bgLoop: BGLoopThread      //GUI Thread: Canvas Loops BG Image

    //OST
    

    //Score
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

        // Surface View
        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        val surfaceHolder = surfaceView.holder

        //Pause Button and Pause Menu
        this.pauseBtn = findViewById(R.id.pauseBtn)
        this.pauseMenu = findViewById(R.id.pauseMenu)
        this.pauseMenu.visibility = View.INVISIBLE
        this.pauseBtn.setOnClickListener{
            this.pauseMenu.visibility = View.VISIBLE
            this.scoreHandler.removeCallbacks(scoreRunnable)
        }
        //Pause Menu: Continue Button
        this.contBtn = findViewById(R.id.continueBtn)
        this.contBtn.setOnClickListener{
            this.pauseMenu.visibility = View.INVISIBLE
            this.scoreHandler.postDelayed(this.scoreRunnable, 500)
        }
        //Pause Menu: Quit Button
        this.quitBtn = findViewById(R.id.quitBtn)
        this.quitBtn.setOnClickListener{
            startActivity(Intent(this, StartActivity::class.java))
        }

        //Score Updater
        //Increases Score by 1 Every Half-Second
        this.scoreTextView = findViewById(R.id.score)
        this.scoreHandler = Handler()
        this.scoreRunnable = Runnable {
            increaseScore()
            this.scoreHandler.postDelayed(this.scoreRunnable, 500) // Run the Runnable every 500 milliseconds (half-second)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        scoreHandler.postDelayed(scoreRunnable, 500)
    }

    override fun onPause() {
        super.onPause()
        scoreHandler.removeCallbacks(scoreRunnable)
    }

    private fun increaseScore() {
        this.score++
        this.scoreTextView.text = this.score.toString()
    }

}