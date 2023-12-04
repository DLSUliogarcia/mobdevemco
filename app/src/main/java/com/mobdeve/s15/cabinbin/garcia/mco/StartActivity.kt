package com.mobdeve.s15.cabinbin.garcia.mco

import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity

import com.facebook.FacebookSdk
import com.facebook.login.widget.LoginButton
import com.facebook.share.widget.ShareButton;

class StartActivity : ComponentActivity() {
    //OST
    private lateinit var ostPlayer: MediaPlayer
    private lateinit var ostThread: HandlerThread
    private lateinit var ostHandler: Handler
    private lateinit var ostRunnable: Runnable

    //High Score
    private lateinit var scoreView: TextView

    //Start Activity Buttons
    private lateinit var startBtn: Button
    private lateinit var highScoreBtn: Button
    private lateinit var sfxBtn: ImageButton
    private lateinit var ostBtn: ImageButton
    private lateinit var loginBtn: ImageButton

    //Shared Preferences
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var editor: Editor
    private val prefsName = "MyPrefs"
    private val sfxCheck = "sfxActive"
    private val ostCheck = "ostActive"
    private val scoreCheck = "highScore"
    private var isOSTPlaying = true                         //OST Activation
    private var isSFXOn = true                              //SFX Activation
    private var highScore: Long = 0                         //Game's High Score

    //Crash Button moment
    private lateinit var crashBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)

        // Start Menu Layout
        setContentView(R.layout.start_layout)

        //Shared Preferences
        loadPrefs()

        //Music
        this.ostBtn = findViewById(R.id.ostBtn)
        this.ostPlayer = MediaPlayer.create(this, R.raw.start_ost)
        if(isOSTPlaying){
            this.ostBtn.foreground = null
            this.ostPlayer.setVolume(1f,1f)
        }
        else{
            this.ostBtn.foreground = getDrawable(R.drawable.off)
            this.ostPlayer.setVolume(0f,0f)
        }
        initMusicHandler()

        //SFX
        this.sfxBtn = findViewById(R.id.sfxBtn)
        if(isSFXOn){
            this.sfxBtn.foreground = null
        }
        else{
            this.sfxBtn.foreground = getDrawable(R.drawable.off)
        }

        //High Score
        this.scoreView = findViewById(R.id.start_highScore)
        if(this.highScore != 0L){
            this.scoreView.text = this.highScore.toString()
        }
        else{
            findViewById<ImageButton>(R.id.highScoreBtn).visibility = View.INVISIBLE
            this.scoreView.text = "-"
        }

        //Sound FX Button
        this.sfxBtn.setOnClickListener{
            if (this.isSFXOn){
                this.sfxBtn.foreground = getDrawable(R.drawable.off)
            }
            else{
                this.sfxBtn.foreground = null
            }
            this.isSFXOn = !this.isSFXOn
            this.editor.putBoolean(sfxCheck, this.isSFXOn)
            this.editor.apply()
        }

        //Music and Music Button
        this.ostBtn.setOnClickListener{
            if (this.isOSTPlaying){
                this.ostBtn.foreground = getDrawable(R.drawable.off)
                this.ostPlayer.setVolume(0f,0f)
            }
            else{
                this.ostBtn.foreground = null
                this.ostPlayer.setVolume(1f,1f)
            }
            this.isOSTPlaying = !this.isOSTPlaying
            this.editor.putBoolean(ostCheck, this.isOSTPlaying)
            this.editor.apply()
        }

        //Start Button
        this.startBtn = findViewById(R.id.startBtn)
        this.startBtn.setOnClickListener{
            finish()
            val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(Intent(this, GameActivity::class.java),options.toBundle())
        }

        this.crashBtn = findViewById(R.id.crashBtn)
        this.crashBtn.setOnClickListener {
            throw RuntimeException("Test Crash") // Force a crash
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
        this.ostThread = HandlerThread("StartOSTHandlerThread")
        this.ostThread.start()
        this.ostHandler = Handler(this.ostThread.looper)
        this.ostRunnable = Runnable {
            this.ostPlayer.start()
            this.ostHandler.postDelayed(this.ostRunnable, 100) // Adjust the delay as needed
        }
    }

    //Load Settings from Shared Prefs
    private fun loadPrefs() {
        this.sharedPrefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        this.editor = this.sharedPrefs.edit()
        this.highScore = this.sharedPrefs.getLong(scoreCheck, 0)
        this.isOSTPlaying = this.sharedPrefs.getBoolean(ostCheck, true)
        this.isSFXOn = this.sharedPrefs.getBoolean(sfxCheck, true)
    }
}