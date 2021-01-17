package com.vaks.galina.meditaid

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Math.abs
import java.time.Duration


class MainActivity : AppCompatActivity() {

    // Initialized in onCreate
    lateinit var beachWavesMP : MediaPlayer
    // Initialized in startMeditate
    var countDownTimer : MeditaidCountDownTimer? = null
    var totalMeditationMillis = 0L
    var meditationMillisLeft = 0L
    var meditationPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        beachWavesMP = MediaPlayer.create(this, R.raw.beach_waves)

        findViewById<Button>(R.id.buttonStart).setOnClickListener {
            startMeditate()
        }
    }

    // Time remaining was saved on last tick (uncertainty 1s)
    override fun onPause() {
        super.onPause()
        meditationPaused = true
        beachWavesMP.pause()
        countDownTimer?.cancel()
    }

    // Create a new timer with the remaining time (as saved on last tick)
    override fun onResume() {
        super.onResume()
        if (meditationPaused) {
            beachWavesMP.start()
            countDownTimer = MeditaidCountDownTimer(this, meditationMillisLeft)
            countDownTimer?.start()
            meditationPaused = false
        }
    }

    private fun startMeditate() {
        var timeString = findViewById<Spinner>(R.id.dropDownTime).selectedItem.toString()
        //Modify to ISO 8601 from mm:ss
        timeString = "PT"+timeString.replace(":", "M")+"S"
        val totalTime =  Duration.parse(timeString)
        totalMeditationMillis = totalTime.toMillis()
        meditationMillisLeft = totalMeditationMillis
        countDownTimer = MeditaidCountDownTimer(this, meditationMillisLeft)
        if (countDownTimer?.start() != null) {
            //Disabling start buttons to avoid double counting
            findViewById<Button>(R.id.buttonStart).isEnabled = false
            // Start looping "relaxing" beach sound
            beachWavesMP.start()
            beachWavesMP.isLooping = true

            chime() //Starting gong
        } else {
            Toast.makeText(applicationContext,"Error. Try again.",Toast.LENGTH_SHORT).show()
        }
    }

    // Function called by countDownTimer every second
    fun tick(millisLeft: Long) {
        meditationMillisLeft = millisLeft
        if (abs(millisLeft*2-totalMeditationMillis)<1000)
            //End and midway. Uncertainty in timer assumed 1000ms
            chime()

        // Show countdown in mm:ss format
        val minutesLeft = (millisLeft/60000).toString()
        val secondsLeft = ((millisLeft / 1000) % 60).toString()
        val stringTimeLeft = minutesLeft+ ":" +
                (if (secondsLeft.length<2) "0" else "") + secondsLeft // padding for m:_s_s format
        findViewById<TextView>(R.id.dropDownPromptText).text = stringTimeLeft

    }

    // Function called by countDownTimer at the end of the countdown
    fun endMeditation() {
        chime()
        beachWavesMP.stop()

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
