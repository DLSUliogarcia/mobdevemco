package com.mobdeve.s15.cabinbin.garcia.mco

import android.os.Bundle
import android.text.Layout
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.mobdeve.s15.cabinbin.garcia.mco.ui.theme.CabinbinGarciaMCOTheme

class MainActivity : ComponentActivity() {
    //Application Variables
    private lateinit var startBtn: Button
    private lateinit var pauseBtn: ImageButton
    private lateinit var contBtn: Button
    private lateinit var quitBtn: Button
    private lateinit var pauseMenu: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Menu Layout
        setContentView(R.layout.start_layout)
        setButtons()
    }

    private fun setButtons() {
        //Button Actions
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
    }
}