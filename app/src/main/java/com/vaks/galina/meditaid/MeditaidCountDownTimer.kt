package com.vaks.galina.meditaid

import android.os.CountDownTimer

// Wrapper class to CountDownTimer  Also: ticks seconds
class MeditaidCountDownTimer (val mainActivity: MainActivity, val millisTotal: Long) : CountDownTimer(millisTotal, 1000) {

    override fun onTick(millisUntilFinished: Long) {
        mainActivity.tick(millisUntilFinished)
    }

    override fun onFinish() {
        mainActivity.endMeditation()
    }
}