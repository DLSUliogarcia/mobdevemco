package com.mobdeve.s15.cabinbin.garcia.mco

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.q42.android.scrollingimageview.ScrollingImageView

class GameActivity : ComponentActivity() {
    //Cat
    private lateinit var cat: ImageView
    private var isCatMidair = false
    private lateinit var catJumpAnimatorSet: AnimatorSet    //Animator Set for Cat Motion
    private lateinit var catJumpAnimator: ObjectAnimator    //Animator for Cat Jumping
    private lateinit var catFallAnimator: ObjectAnimator    //Animator for Cat Jumping

    //Background
    private lateinit var screenLayout: ConstraintLayout
    private lateinit var background: ScrollingImageView
    private var bgWidth: Float = 0f

    //Obstacles
    private lateinit var obstacles : Array<ImageView>
    @Volatile private var obstacleVal = 0
    @Volatile private var lapseVal: Long = 675
    private var distance = 0
    private lateinit var obstacleHandler: Handler
    private lateinit var obstacleRunnable: Runnable
    private lateinit var obstacleAnimator: ObjectAnimator

    //OST
    private lateinit var ostPlayer: MediaPlayer
    private lateinit var ostHandlerThread: HandlerThread
    private lateinit var ostHandler: Handler
    private lateinit var ostRunnable: Runnable
    private var isOSTPlaying = true

    //Score
    private lateinit var scoreHandlerThread: HandlerThread
    private lateinit var scoreHandler: Handler
    private lateinit var scoreRunnable: Runnable
    private lateinit var scoreTextView: TextView
    private var score: Long = 0

    //Pause Menu Buttons
    private lateinit var pauseBtn: ImageButton
    private var isGamePaused = false
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

        //Pause Menu
        this.pauseMenu = findViewById(R.id.pauseMenu)
        this.pauseMenu.visibility = View.INVISIBLE
        //Pause Menu: Pause Button
        this.pauseBtn = findViewById(R.id.pauseBtn)
        this.pauseBtn.setOnClickListener{
            this.pauseMenu.visibility = View.VISIBLE
            this.isGamePaused = true
            this.background.stop()
            this.scoreHandler.removeCallbacks(this.scoreRunnable)
            pauseGameAnimations()
            //this.obstacleHandler.removeCallbacks(this.obstacleRunnable)
        }
        //Pause Menu: Continue Button
        this.contBtn = findViewById(R.id.continueBtn)
        this.contBtn.setOnClickListener {
            this.pauseMenu.visibility = View.INVISIBLE
            this.isGamePaused = false
            this.background.start()
            this.scoreHandler.postDelayed(this.scoreRunnable, 500)
            //this.obstacleHandler.postDelayed(this.obstacleRunnable, this.lapseVal)
            resumeGameAnimations()
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
        this.scoreHandlerThread = HandlerThread("ScoreHandlerThread")
        this.scoreHandlerThread.start()
        this.scoreHandler = Handler(this.scoreHandlerThread.looper)
        initScoreHandler()

        //Music
        this.ostPlayer = MediaPlayer.create(this, R.raw.game_ost)
        this.ostHandlerThread = HandlerThread("OSTHandlerThread")
        this.ostHandlerThread.start()
        this.ostHandler = Handler(this.ostHandlerThread.looper)
        initMusicHandler()

        //Background
        this.background = findViewById(R.id.scrollingBG)
        this.background.start()

        //Cat
        this.cat = findViewById(R.id.game_cat)
        this.catJumpAnimatorSet = AnimatorSet()
        //Cat Jump Animation
        this.catJumpAnimator = ObjectAnimator.ofFloat(cat, "translationY", -dpToPixels(this, 500f).toFloat())
        this.catJumpAnimator.duration = 500 // Adjust the duration of the jump animation as needed
        //Cat Fall Animation
        this.catFallAnimator = ObjectAnimator.ofFloat(cat, "translationY", 0f)
        this.catFallAnimator.duration = 500 // Adjust the duration of the fall animation as needed

        //Screen
        this.screenLayout = findViewById(R.id.game_bg)
        this.screenLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Perform the jump animation if the cat is not midair
                    if (!this.isCatMidair) {
                        catJump()
                    }
                }
            }
            true
        }

        //Distance Travel
        this.bgWidth = dpToPixels(this, this.background.layoutParams.width.toFloat()).toFloat()
        this.distance = (this.bgWidth + dpToPixels(this,750f)).toInt()

        //Obstacles
        this.obstacles = arrayOf(findViewById(R.id.game_obstacle0), findViewById(R.id.game_obstacle1), findViewById(R.id.game_obstacle2), findViewById(R.id.game_obstacle3))
        this.obstacleAnimator = ObjectAnimator.ofFloat(this.obstacles[0], View.TRANSLATION_X, -(this.distance.toFloat()))
        this.obstacleHandler = Handler(Looper.getMainLooper())
        initObstacleHandler()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        this.ostHandler.postDelayed(this.ostRunnable, 0)
        this.scoreHandler.postDelayed(this.scoreRunnable, 500)
        this.obstacleHandler.postDelayed(this.obstacleRunnable, this.lapseVal)
        resumeGameAnimations()
    }

    override fun onPause() {
        super.onPause()
        this.ostHandler.removeCallbacks(this.ostRunnable)
        this.ostPlayer.pause()
        this.scoreHandler.removeCallbacks(this.scoreRunnable)
        pauseGameAnimations()
        this.obstacleHandler.removeCallbacks(this.obstacleRunnable) // Remove pending obstacleRunnable callbacks
    }

    //Music Handler
    private fun initMusicHandler() {
        this.ostRunnable = Runnable {
            if (!this.ostPlayer.isPlaying) {
                this.ostPlayer.start()
            }
            this.ostHandler.postDelayed(this.ostRunnable, 250)
        }
    }

    //Score Handler
    private fun initScoreHandler() {
        this.scoreRunnable = Runnable {
            this.score++
            runOnUiThread {
                this.scoreTextView.text = this.score.toString()
            }
            this.scoreHandler.postDelayed(this.scoreRunnable, 500)
        }
    }

    //Obstacle Handler
    private fun initObstacleHandler() {
        //Obstacle Thread Resets Upon Animation Ending
        //Runnable
        this.obstacleRunnable = Runnable {
            // Generate a Random Value to make an obstacle
            this.obstacleVal = (0..3).random()
            this.obstacles[this.obstacleVal].visibility = View.VISIBLE
            setObstacle(this.obstacleVal)
            this.obstacleAnimator.start()
        }
    }

    private fun setObstacle(i : Int){
        //Obstacle Animator, With Translation
        this.obstacleAnimator = ObjectAnimator.ofFloat(this.obstacles[i], View.TRANSLATION_X, -(this.distance.toFloat()))
        this.obstacleAnimator.duration = 1000 * (this.distance / dpToPixels(this, 150f)).toLong()
        this.obstacleAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                obstacles[i].translationX = 0f
                obstacles[i].visibility = View.GONE
                obstacleHandler.postDelayed(obstacleRunnable, lapseVal)
            }
        })
    }

    private fun catJump() {
        if (!isCatMidair && !isGamePaused) {
            //The Cat is Midair
            isCatMidair = true
            //Sequentially Play Jump then Fall
            this.catJumpAnimatorSet = AnimatorSet()
            this.catJumpAnimatorSet.playSequentially(this.catJumpAnimator , this.catFallAnimator)
            //After Animation, the cat is not Midair
            this.catJumpAnimatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isCatMidair = false
                }
            })
            //Start Animation
            this.catJumpAnimatorSet.start()
        }
    }

    private fun pauseGameAnimations() {
        if (this.catJumpAnimator.isRunning) {
            this.catJumpAnimator.pause()
        }
        if (this.catFallAnimator.isRunning) {
            this.catFallAnimator.pause()
        }
        if (this.obstacleAnimator.isRunning) {
            this.obstacleAnimator.pause()
        }
    }

    private fun resumeGameAnimations() {
        if (this.catJumpAnimator.isPaused) {
            this.catJumpAnimator.resume()
        }
        if (this.catFallAnimator.isPaused) {
            this.catFallAnimator.resume()
        }
        if (this.obstacleAnimator.isPaused) {
            this.obstacleAnimator.resume()
        }
    }

    private fun dpToPixels(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

}