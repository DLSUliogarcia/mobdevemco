package com.mobdeve.s15.cabinbin.garcia.mco

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import com.q42.android.scrollingimageview.ScrollingImageView
import java.lang.Integer.max
import java.lang.Math.min

class GameActivity : ComponentActivity() {
    //Cat
    private lateinit var cat: ImageView
    private lateinit var catBitmap : Bitmap
    private var isCatMidair = false
    private lateinit var catJumpAnimatorSet: AnimatorSet    //Animator Set for Cat Motion
    private lateinit var catJumpAnimator: ObjectAnimator    //Animator for Cat Jumping
    private lateinit var catFallAnimator: ObjectAnimator    //Animator for Cat Falling
    private lateinit var catHiJumpAnimator: ObjectAnimator  //Animator for Cat Hi-Jumping
    private lateinit var catHiFallAnimator: ObjectAnimator  //Animator for Cat Hi-Falling
    private var touchStartTime : Long = 0

    //Background
    private lateinit var screenLayout: ConstraintLayout
    private lateinit var background: ScrollingImageView
    private var isGameRunning : Boolean = true
    private var bgWidth: Float = 0f

    //Obstacles
    private lateinit var obstacles : Array<ImageView>
    private var isObstacleRunning : Boolean = false
    private lateinit var obstacleBitmap : Bitmap
    private lateinit var obstacleThread: HandlerThread
    @Volatile private var obstacleVal = 0
    @Volatile private var lapseVal: Long = 675
    private var distance = 0
    private lateinit var obstacleHandler: Handler
    private lateinit var obstacleRunnable: Runnable
    private lateinit var obstacleAnimator: ObjectAnimator

    //OST
    private lateinit var ostPlayer: MediaPlayer
    private lateinit var ostThread: HandlerThread
    private lateinit var ostHandler: Handler
    private lateinit var ostRunnable: Runnable

    //SFX
    private lateinit var jumpThread: HandlerThread
    private lateinit var jumpHandler: Handler
    private lateinit var jumpSFX: MediaPlayer
    private lateinit var gameOverSFX : MediaPlayer

    //Score
    private lateinit var scoreThread: HandlerThread
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
    private lateinit var finalScore : TextView
    private lateinit var replayBtn: ImageButton
    private lateinit var homeBtn: ImageButton
    private lateinit var shareBtn: ImageButton

    //Shared Preferences
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val prefsName = "MyPrefs"
    private val sfxCheck = "sfxActive"
    private val ostCheck = "ostActive"
    private val scoreCheck = "highScore"
    private var isOSTPlaying = true                         //OST Activation
    private var isSFXOn = true                              //SFX Activation
    private var highScore: Long = 0                         //Game's High Score

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Menu Layout
        setContentView(R.layout.game_layout)

        //Shared Prefs
        loadPrefs()

        //Game Over Menu
        this.gameOverMenu = findViewById(R.id.gameOver)
        this.finalScore = findViewById(R.id.finalScore)
        this.homeBtn = findViewById(R.id.homeBtn)
        this.replayBtn = findViewById(R.id.replayBtn)
        this.shareBtn = findViewById(R.id.shareBtn)

        //Score Updater: Increases Score by 1 Every Half-Second
        this.scoreTextView = findViewById(R.id.score)
        this.scoreThread = HandlerThread("ScoreHandlerThread")
        this.scoreThread.start()
        this.scoreHandler = Handler(this.scoreThread.looper)

        //Music
        this.ostBtn = findViewById(R.id.game_ostBtn)
        this.ostPlayer = MediaPlayer.create(this, R.raw.game_ost)
        if(this.isOSTPlaying){
            this.ostBtn.foreground = null
            this.ostPlayer.setVolume(1f,1f)
        }
        else{
            this.ostBtn.foreground = getDrawable(R.drawable.off)
            this.ostPlayer.setVolume(0f,0f)
        }
        this.ostThread = HandlerThread("OSTHandlerThread")
        this.ostThread.start()
        this.ostHandler = Handler(this.ostThread.looper)

        //Jump SFX
        this.sfxBtn = findViewById(R.id.game_sfxBtn)
        this.jumpSFX = MediaPlayer.create(this, R.raw.jump_sfx)
        this.gameOverSFX = MediaPlayer.create(this, R.raw.gameover_sfx)
        if(isSFXOn){
            this.sfxBtn.foreground = null
            this.jumpSFX.setVolume(1f,1f)
            this.gameOverSFX.setVolume(1f,1f)
        }
        else{
            this.sfxBtn.foreground = getDrawable(R.drawable.off)
            this.jumpSFX.setVolume(0f,0f)
            this.gameOverSFX.setVolume(0f,0f)
        }
        this.jumpThread = HandlerThread("JumpHandlerThread")
        this.jumpThread.start()
        this.jumpHandler = Handler(this.jumpThread.looper)

        //Background
        this.background = findViewById(R.id.scrollingBG)
        this.background.start()

        //Cat and Animations
        this.cat = findViewById(R.id.game_cat)
        this.catBitmap = BitmapFactory.decodeResource(resources, R.drawable.cat_run)
        this.catJumpAnimatorSet = AnimatorSet()
        //Cat Jump Animation
        this.catJumpAnimator = ObjectAnimator.ofFloat(this.cat, "translationY", -dpToPixels(this, 140f).toFloat())
        this.catJumpAnimator.duration = 447 // [Milliseconds]
        this.catJumpAnimator.addUpdateListener { _ ->
            if (checkCollision(cat, obstacles[obstacleVal])) {
                gameOver()
            }
        }
        //Cat Fall Animation
        this.catFallAnimator = ObjectAnimator.ofFloat(this.cat, "translationY", 0f)
        this.catFallAnimator.duration = 447 // [Milliseconds]
        this.catFallAnimator.addUpdateListener { _ ->
            if (checkCollision(cat, obstacles[obstacleVal])) {
                gameOver()
            }
        }
        //Cat Hi Jump Animation
        this.catHiJumpAnimator = ObjectAnimator.ofFloat(this.cat, "translationY", -dpToPixels(this, 280f).toFloat())
        this.catHiJumpAnimator.duration = 632 // [Milliseconds]
        this.catHiJumpAnimator.addUpdateListener { _ ->
            if (checkCollision(cat, obstacles[obstacleVal])) {
                gameOver()
            }
        }
        //Cat Hi Fall Animation
        this.catHiFallAnimator = ObjectAnimator.ofFloat(this.cat, "translationY", 0f)
        this.catHiFallAnimator.duration = 632 // [Milliseconds]
        this.catHiFallAnimator.addUpdateListener { _ ->
            if (checkCollision(cat, obstacles[obstacleVal])) {
                gameOver()
            }
        }

        //Screen: Cat Jump Listener
        this.screenLayout = findViewById(R.id.game_bg)

        //Distance Travel
        this.bgWidth = dpToPixels(this, this.background.layoutParams.width.toFloat()).toFloat()
        this.distance = (this.bgWidth + dpToPixels(this,750f)).toInt()

        //Obstacles
        this.obstacles = arrayOf(
            findViewById(R.id.game_obstacle0),
            findViewById(R.id.game_obstacle1),
            findViewById(R.id.game_obstacle2),
            findViewById(R.id.game_obstacle3)
        )
        this.obstacleBitmap = BitmapFactory.decodeResource(resources, R.drawable.dog)
        this.obstacleAnimator = ObjectAnimator.ofFloat(this.obstacles[0], View.TRANSLATION_X, -(this.distance.toFloat()))
        this.obstacleThread = HandlerThread("ObstacleHandlerThread")
        this.obstacleThread.start()
        this.obstacleHandler = Handler(this.obstacleThread.looper)

        //Pause Menu
        this.pauseMenu = findViewById(R.id.pauseMenu)
        this.pauseMenu.visibility = View.INVISIBLE
        //Pause Menu: Pause Button
        this.pauseBtn = findViewById(R.id.pauseBtn)
        this.pauseBtn.visibility = View.VISIBLE
        this.pauseBtn.setOnClickListener{
            this.pauseMenu.visibility = View.VISIBLE
            this.isGamePaused = true
            this.background.stop()
            this.scoreHandler.removeCallbacks(this.scoreRunnable)
            pauseGameAnimations()
            this.obstacleHandler.removeCallbacks(this.obstacleRunnable)
        }
        //Pause Menu: Continue Button
        this.contBtn = findViewById(R.id.continueBtn)
        this.contBtn.setOnClickListener {
            this.pauseMenu.visibility = View.INVISIBLE
            this.isGamePaused = false
            this.background.start()
            this.scoreHandler.postDelayed(this.scoreRunnable, 500)
            //Obstacle Handler Does Not Have Callbacks
            if(this.isObstacleRunning){
                resumeGameAnimations()
            } else{ //Obstacle Handler Has Callbacks
                this.obstacleHandler.removeCallbacks(this.obstacleRunnable)
                this.obstacleHandler.postDelayed(this.obstacleRunnable, this.lapseVal)
            }
        }

        //Pause Menu: Quit Button
        this.quitBtn = findViewById(R.id.quitBtn)
        this.quitBtn.setOnClickListener{
            /**/
            this.isGameRunning = false
            this.jumpSFX.stop()
            this.jumpSFX.release()
            this.jumpThread.quitSafely()
            this.background.stop()
            this.ostPlayer.stop()
            this.ostPlayer.release()
            this.ostHandler.removeCallbacks(this.ostRunnable)
            this.ostThread.quitSafely()
            this.catJumpAnimatorSet.cancel()
            this.obstacleAnimator.cancel()
            this.obstacleHandler.removeCallbacks(this.obstacleRunnable)
            this.obstacleThread.quitSafely()
            this.gameOverSFX.stop()
            this.gameOverSFX.release()
            this.jumpThread.quit()
            this.scoreThread.quit()
            this.ostThread.quit()
            this.obstacleThread.quit()
            finish()
            val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(Intent(this, StartActivity::class.java),options.toBundle())
        }
        //Pause Menu: OST Button
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
        //Pause Menu: SFX Button
        this.sfxBtn.setOnClickListener{
            if (this.isSFXOn){
                this.sfxBtn.foreground = getDrawable(R.drawable.off)
                this.jumpSFX.setVolume(0f,0f)
                this.gameOverSFX.setVolume(0f,0f)
            }
            else{
                this.sfxBtn.foreground = null
                this.jumpSFX.setVolume(1f,1f)
                this.gameOverSFX.setVolume(1f,1f)
            }
            this.isSFXOn = !this.isSFXOn
            this.editor.putBoolean(sfxCheck, this.isSFXOn)
            this.editor.apply()
        }

        //Initialize Game
        initMusicHandler()
        initScoreHandler()
        initObstacleHandler()
    }

    override fun onResume() {
        super.onResume()
        this.ostHandler.postDelayed(this.ostRunnable, 0)
        this.scoreHandler.postDelayed(this.scoreRunnable, 500)
        if(this.isObstacleRunning){
            resumeGameAnimations()
        } else{
            this.obstacleHandler.removeCallbacks(this.obstacleRunnable)
            this.obstacleHandler.postDelayed(this.obstacleRunnable, this.lapseVal)
        }
    }

    override fun onPause() {
        super.onPause()
        this.ostHandler.removeCallbacks(this.ostRunnable)
        this.scoreHandler.removeCallbacks(this.scoreRunnable)
        pauseGameAnimations()
        this.obstacleHandler.removeCallbacks(this.obstacleRunnable)
    }

    //Screen: Cat Jump
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                this.touchStartTime = System.currentTimeMillis()
                this.screenLayout.postDelayed({
                    if (this.isGameRunning && !this.isCatMidair) {
                        playJumpSFX()
                        catHiJump()
                    }
                }, 150)
            }
            MotionEvent.ACTION_UP -> {
                this.screenLayout.removeCallbacks(null)
                if (this.isGameRunning && !this.isCatMidair) {
                    val touchDuration = System.currentTimeMillis() - this.touchStartTime
                    if (touchDuration < 151) {
                        playJumpSFX()
                        catJump()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun gameOver() {
        //Game Stops
        this.isGameRunning = false

        //Stop Sound FX
        this.jumpSFX.stop()
        this.jumpSFX.release()
        this.jumpThread.quitSafely()

        //Make Pause Button Disappear
        this.pauseBtn.visibility = View.INVISIBLE

        //Stop Background
        this.background.stop()

        //Stop Score Thread
        this.scoreHandler.removeCallbacks(this.scoreRunnable)
        this.scoreThread.quitSafely()

        //Stop OST Handler
        this.ostPlayer.stop()
        this.ostPlayer.release()
        this.ostHandler.removeCallbacks(this.ostRunnable)
        this.ostThread.quitSafely()

        //Stop Animations
        this.catJumpAnimatorSet.cancel()
        this.obstacleAnimator.cancel()

        //Stop Obstacle Handler
        this.obstacleHandler.removeCallbacks(this.obstacleRunnable)
        this.obstacleThread.quitSafely()

        //Play Game Over Sound
        this.gameOverSFX.start()

        //Cat Crash Animations
        this.cat.setImageResource(R.drawable.crash)
        this.cat.layoutParams.width = dpToPixels(this,160f)
        this.cat.layoutParams.height = dpToPixels(this, 160f)

        //Game Over Menu
        this.gameOverMenu.visibility = View.VISIBLE

        //High Score
        this.finalScore.text = this.score.toString()
        if(this.score > this.highScore){
            findViewById<ImageView>(R.id.game_crown).visibility = View.VISIBLE
            this.editor.putLong(this.scoreCheck, this.score)
            this.editor.apply()
        }

        //Game Over Menu: Buttons
        this.homeBtn.setOnClickListener{
            this.gameOverSFX.stop()
            this.gameOverSFX.release()
            this.jumpThread.quit()
            this.scoreThread.quit()
            this.ostThread.quit()
            this.obstacleThread.quit()
            finish()
            val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(Intent(this, StartActivity::class.java),options.toBundle())
        }
        this.replayBtn.setOnClickListener{
            this.gameOverSFX.stop()
            this.gameOverSFX.release()
            this.scoreThread.quit()
            this.jumpThread.quit()
            this.ostThread.quit()
            this.obstacleThread.quit()
            finish()
            val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(Intent(this, GameActivity::class.java),options.toBundle())
        }
    }

    //Jump SFX Handler
    private fun playJumpSFX() {
        this.jumpHandler.post {
            this.jumpSFX.start()
        }
    }

    //Music Handler
    private fun initMusicHandler() {
        this.ostRunnable = Runnable {
            if (this.isGameRunning  && !this.ostPlayer.isPlaying) {
                this.ostPlayer.start()
            }
            this.ostHandler.postDelayed(this.ostRunnable, 100)
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
        this.obstacleRunnable = Runnable {
            //Generate a Random Value to make an obstacle
            this.obstacleVal = (0..3).random()
            setObstacle(this.obstacleVal)
            runOnUiThread {
                obstacles[obstacleVal].visibility = View.VISIBLE
                obstacleAnimator.start()
                this.isObstacleRunning = true
            }
        }
    }

    private fun setObstacle(i : Int){
        //Obstacle Animator, With Translation
        this.obstacleAnimator = ObjectAnimator.ofFloat(this.obstacles[i], View.TRANSLATION_X, -(this.distance.toFloat()))
        if(i==0){ // Dog moves quickly
            this.obstacleAnimator.duration = 1000 * (this.distance / dpToPixels(this, 280f)).toLong()
        }
        if(i==1){ // Grandpa is not moving
            this.obstacleAnimator.duration = 1000 * (this.distance / dpToPixels(this, 240f)).toLong()
        }
        if(i==2){ // Baby moves slowly
            this.obstacleAnimator.duration = 1000 * (this.distance / dpToPixels(this, 280f)).toLong()
        }
        if(i==3){ // Hamster moves very quickly
            this.obstacleAnimator.duration = 1000 * (this.distance / dpToPixels(this, 320f)).toLong()
        }
        this.obstacleAnimator.addUpdateListener { animation ->
            if (checkCollision(cat, obstacles[obstacleVal])) {
                gameOver()
            }
        }
        this.obstacleAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                obstacles[i].translationX = 0f
                obstacles[i].visibility = View.GONE
                obstacleHandler.postDelayed(obstacleRunnable, lapseVal)
                isObstacleRunning = false
            }
        })
    }

    private fun checkCollision(view1: View, view2: View): Boolean {
        val catRect = Rect()
        view1.getHitRect(catRect)

        val obstacleRect = Rect()
        view2.getHitRect(obstacleRect)

        //Cat Bitmap
        val catBitmap = (view1 as ImageView).drawable.toBitmap()

        //Obstacle Bitmap
        val obstacleBitmap = (this.obstacles[obstacleVal]).drawable.toBitmap()

        //Calculating Bitmap Hitboxes
        for (i in max(catRect.left, obstacleRect.left) until min(catRect.right, obstacleRect.right)) {
            for (j in max(catRect.top, obstacleRect.top) until min(catRect.bottom, obstacleRect.bottom)) {
                val catPixel = catBitmap.getPixel(i - catRect.left, j - catRect.top)
                val obstaclePixel = obstacleBitmap.getPixel(i - obstacleRect.left, j - obstacleRect.top)

                if (Color.alpha(catPixel) != 0 && Color.alpha(obstaclePixel) != 0) {
                    return true
                }
            }
        }
        return false
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

    private fun catHiJump() {
        if (!isCatMidair && !isGamePaused) {
            //The Cat is Midair
            isCatMidair = true
            //Sequentially Play Jump then Fall
            this.catJumpAnimatorSet = AnimatorSet()
            this.catJumpAnimatorSet.playSequentially(this.catHiJumpAnimator , this.catHiFallAnimator)
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
        if (this.catHiJumpAnimator.isRunning) {
            this.catHiJumpAnimator.pause()
        }
        if (this.catHiFallAnimator.isRunning) {
            this.catHiFallAnimator.pause()
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
        if (this.catHiJumpAnimator.isPaused) {
            this.catHiJumpAnimator.resume()
        }
        if (this.catHiFallAnimator.isPaused) {
            this.catHiFallAnimator.resume()
        }
        if (this.obstacleAnimator.isPaused) {
            this.obstacleAnimator.resume()
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

    private fun dpToPixels(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}