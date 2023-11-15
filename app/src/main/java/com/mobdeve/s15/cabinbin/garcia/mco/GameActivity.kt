package com.mobdeve.s15.cabinbin.garcia.mco

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.media.MediaPlayer
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.view.drawToBitmap

class GameActivity : ComponentActivity() {
    //Background Thread
    private lateinit var bgHandler: Handler
    private lateinit var bgRunnable: Runnable
    private lateinit var bgImageView: ImageView
    private lateinit var bgBitmap: Bitmap
    private var bgOffset: Int = 0
    private var bgSpeed: Int = 5

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

        //Music
        this.ostPlayer = MediaPlayer.create(this, R.raw.game_ost)
        initMusicHandler()

        //Pause Button and Pause Menu
        this.pauseBtn = findViewById(R.id.pauseBtn)
        this.pauseMenu = findViewById(R.id.pauseMenu)
        this.pauseMenu.visibility = View.INVISIBLE
        this.pauseBtn.setOnClickListener{
            this.pauseMenu.visibility = View.VISIBLE
            this.bgHandler.removeCallbacks(this.bgRunnable)
            this.scoreHandler.removeCallbacks(this.scoreRunnable)
        }
        //Pause Menu: Continue Button
        this.contBtn = findViewById(R.id.continueBtn)
        this.contBtn.setOnClickListener{
            this.pauseMenu.visibility = View.INVISIBLE
            this.bgHandler.postDelayed(this.bgRunnable, 0)
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

        //Background
        // Load the background image
        this.bgImageView = findViewById(R.id.game_background)
        this.bgBitmap = BitmapFactory.decodeResource(resources, R.drawable.wallpaper_bg)
        initBGHandler()
        this.bgImageView.post {
            // Set the background image as the drawable
            this.bgImageView.setImageDrawable(BitmapDrawable(resources, bgBitmap))
            // Start the background scrolling
            this.bgHandler.postDelayed(this.bgRunnable, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.bgHandler.removeCallbacks(this.bgRunnable)
    }

    override fun onResume() {
        super.onResume()
        this.bgHandler.postDelayed(this.bgRunnable, 0)
        this.ostHandler.postDelayed(this.ostRunnable, 0)
        this.scoreHandler.postDelayed(this.scoreRunnable, 500)
    }

    override fun onPause() {
        super.onPause()
        this.bgHandler.removeCallbacks(this.bgRunnable)
        this.ostHandler.removeCallbacks(this.ostRunnable)
        this.ostPlayer.pause()
        this.scoreHandler.removeCallbacks(this.scoreRunnable)
    }

    //Background Handler
    private fun initBGHandler() {
        this.bgHandler = Handler(Looper.getMainLooper())
        this.bgRunnable = Runnable {
            // Update the background offset
            this.bgOffset -= bgSpeed

            // If the entire background is scrolled, reset the offset
            if (this.bgOffset < -bgBitmap.width) {
                this.bgOffset = 0
            }

            // Redraw the background
            drawBackground()

            // Repeat the background scrolling
            this.bgHandler.postDelayed(this.bgRunnable, 16) // Adjust the delay as desired
        }
    }

    private fun drawBackground() {
        val bitmap = Bitmap.createBitmap(this.bgImageView.width, this.bgImageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Clear the canvas
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)

        // Calculate the scaling factor for the background image
        val scale = this.bgImageView.height.toFloat() / this.bgBitmap.height.toFloat()

        // Calculate the adjusted width of the background image
        val adjustedWidth = (this.bgBitmap.width * scale).toInt()

        // Draw the scaled background image
        val scaledBitmap = Bitmap.createScaledBitmap(this.bgBitmap, adjustedWidth, this.bgImageView.height, true)
        canvas.drawBitmap(scaledBitmap, this.bgOffset.toFloat(), 0f, Paint())

        // Update the ImageView with the new bitmap
        this.bgImageView.setImageBitmap(bitmap)
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