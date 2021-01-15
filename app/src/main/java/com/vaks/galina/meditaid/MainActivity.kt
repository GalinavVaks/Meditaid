package com.vaks.galina.meditaid

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.lang.Math.abs
import java.time.Duration


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonStart).setOnClickListener {
            startMeditate()
        }
    }

    private fun startMeditate() {
        //Disabling start buttons to avoid double counting
        findViewById<Button>(R.id.buttonStart).isEnabled = false

        var timeString = findViewById<Spinner>(R.id.dropDownTime).selectedItem.toString()
        //Modify to ISO 8601 from mm:ss
        timeString = "PT"+timeString.replace(":", "M")+"S"
        val totalTime =  Duration.parse(timeString)
        val millisTotal = totalTime.toMillis()

        // Start looping "relaxing" beach sound
        // mp will be passed to end this at end of meditation
        val mp = MediaPlayer.create(this, R.raw.beach_waves)
        mp.start()
        mp.isLooping = true

        chime() //Starting gong

        object : CountDownTimer(millisTotal, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tick(millisUntilFinished, millisTotal)
            }

            override fun onFinish() {
                endMeditation(mp) // Pass the media player to stop it
            }
        }.start()


    }

    private fun tick(millisLeft: Long, millisTotal: Long) {
        if (abs(millisLeft*2-millisTotal)<1000)
            //End and midway. Uncertainty in timer assumed 1000ms
            chime()

        // Show countdown in mm:ss format
        val minutesLeft = (millisLeft/60000).toString()
        val secondsLeft = ((millisLeft / 1000) % 60).toString()
        val stringTimeLeft = minutesLeft+ ":" +
                (if (secondsLeft.length<2) "0" else "") + secondsLeft // padding for m:_s_s format
        findViewById<TextView>(R.id.dropDownPromptText).text = stringTimeLeft

    }

    private fun endMeditation(mp: MediaPlayer) {
        chime()
        mp.stop() // Stop beach waves

        // Allow new meditation
        findViewById<Button>(R.id.buttonStart).isEnabled = true
        findViewById<TextView>(R.id.dropDownPromptText).text = getString(R.string.dropDownTimePrompt)
    }

    private fun chime() {
        val mp = MediaPlayer.create(this, R.raw.gong)
        mp.start()
    }
}


// TODO: Enlarge and bolden spinner
