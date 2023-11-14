package com.mobdeve.s15.cabinbin.garcia.mco

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HighScoreActivity:ComponentActivity() {
    //High Scores Features
    private val scoreList: ArrayList<Score> = DataHelper.initializeData()
    private lateinit var recyclerView: RecyclerView
    private lateinit var fbBtn: ImageButton
    private lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Menu Layout
        setContentView(R.layout.highscores_layout)

        //Recycler and Adapter
        this.recyclerView = findViewById(R.id.highscoresRv)
        this.recyclerView.adapter = MyAdapter(this.scoreList)
        this.recyclerView.layoutManager = LinearLayoutManager(this)

        //Back Button
        this.backBtn = findViewById(R.id.backBtn)
        this.backBtn.setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
        }

        //'Share on Facebook' Button
        this.fbBtn = findViewById(R.id.fbBtn)
        this.fbBtn.setOnClickListener{
            Toast.makeText(this, "FB Share!", Toast.LENGTH_SHORT).show()
        }
    }
}