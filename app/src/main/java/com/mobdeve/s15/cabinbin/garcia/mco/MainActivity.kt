package com.mobdeve.s15.cabinbin.garcia.mco

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {
    //Application Variables
    private lateinit var startBtn: Button
    private lateinit var pauseBtn: ImageButton
    private lateinit var contBtn: Button
    private lateinit var quitBtn: Button
    private lateinit var pauseMenu: LinearLayout

    private lateinit var highScoreBtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var backBtn: ImageButton
    private val scoreList: ArrayList<Score> = DataHelper.initializeData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Menu Layout
        setContentView(R.layout.start_layout)
        setButtons()



    }

    private fun setButtons() {
        //Play Actions
        startBtn = findViewById(R.id.startBtn)
        startBtn.setOnClickListener {
            // Game Layout
            setContentView(R.layout.game_layout)
            //Pause Button
            pauseBtn = findViewById(R.id.pauseBtn)
            pauseMenu = findViewById(R.id.pauseMenu)
            pauseBtn.setOnClickListener{
                pauseMenu.visibility = View.VISIBLE
            }
            //Pause Menu: Continue Button
            contBtn = findViewById(R.id.continueBtn)
            contBtn.setOnClickListener{
                pauseMenu.visibility = View.INVISIBLE
            }
            //Pause Menu: Quit Button
            quitBtn = findViewById(R.id.quitBtn)
            quitBtn.setOnClickListener{
                setContentView(R.layout.start_layout)
                setButtons()
            }
        }
        //High Score Button
        highScoreBtn = findViewById(R.id.highScoreBtn)
        highScoreBtn.setOnClickListener {
            // High-Scores Layout
            setContentView(R.layout.highscores_layout)
            this.recyclerView = findViewById(R.id.highscoresRv)
            this.recyclerView.adapter = MyAdapter(this.scoreList)
            this.recyclerView.layoutManager = LinearLayoutManager(this)
            backBtn = findViewById(R.id.backBtn)
            backBtn.setOnClickListener {
                    setContentView(R.layout.start_layout)
                    setButtons()
                }
            }

    }
}