package com.vaks.galina.meditaid

import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.*
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
    var meditationStarted = false // True between pressing start and countdown end
    var meditationPaused = false // True when meditationStarted and activity paused

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        beachWavesMP = MediaPlayer.create(this, R.raw.beach_waves)


        val durationSpinner = findViewById<Spinner>(R.id.dropDownTime)
        ArrayAdapter.createFromResource(
                this,
                R.array.dropDownTimeItems,
                R.layout.spinner_item_text
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_item_dropdown_text)
            // Apply the adapter to the spinner
            durationSpinner.adapter = adapter
        }

        findViewById<Button>(R.id.buttonStart).setOnClickListener {
            startMeditate()
        }
    }

    // Time remaining was saved on last tick (uncertainty 1s)
    override fun onPause() {
        super.onPause()
        if (meditationStarted) {
            meditationPaused = true
            beachWavesMP.pause()
            countDownTimer?.cancel()
        }

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
        meditationStarted = true
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
            Toast.makeText(applicationContext, "Error. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    // Function called by countDownTimer every second
    fun tick(millisLeft: Long) {
        meditationMillisLeft = millisLeft
        if (abs(millisLeft * 2 - totalMeditationMillis)<1000)
            //Midway. Uncertainty in timer assumed 1000ms
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
        meditationStarted = false
        chime()
        beachWavesMP.stop()

        // Allow new meditation
        beachWavesMP.prepare()
        findViewById<Button>(R.id.buttonStart).isEnabled = true
        findViewById<TextView>(R.id.dropDownPromptText).text = getString(R.string.dropDownTimePrompt)
    }

    private fun chime() {
        val mp = MediaPlayer.create(this, R.raw.gong)
        mp.start()
        mp.setOnCompletionListener({
            mp.release()
        })
    }
}

