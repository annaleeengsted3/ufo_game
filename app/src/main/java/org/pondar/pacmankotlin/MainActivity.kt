package org.pondar.pacmankotlin

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), OnClickListener {

    //reference to the game class.
    private var game: Game? = null
    private var animationTimer: Timer = Timer()
    private var countdownTimer: Timer = Timer()
    private val frameRate: Long = 1000 / 30 //30frames/redraws per second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //makes sure it always runs in portrait mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        game = Game(this, pointsView, timerView)

        //intialize the gameview and game class
        game?.setGameView(gameView)
        gameView.setGame(game)
       game?.newGame()
        //non-menu listeners:
        //TODO loop here instead
        moveDown.setOnClickListener(this)
        moveUp.setOnClickListener(this)
        moveLeft.setOnClickListener(this)
        moveRight.setOnClickListener(this)
        timerView.text = resources.getString(R.string.timerView, "")

        animationTimer.schedule(object : TimerTask() {
            override fun run() {
                animationTimerMethod()
            }

        }, 0, frameRate) //30 fps

        countdownTimer.schedule(object : TimerTask() {
            override fun run() {
                timerMethod()
            }

        }, 0, 1000)
    }


    private fun timerMethod() {
        this.runOnUiThread(countdownTick)
    }

    private fun animationTimerMethod() {
        //This method is called directly by the timer and runs in the same thread as the timer.
        //we could do updates here TO GAME LOGIC,but not updates TO ACTUAL UI
        // gameView.move(20)  // BIG NO NO TO DO THIS - WILL CRASH ON OLDER DEVICES!!!!
        //We call the method that will work with the UI through the runOnUiThread method.
        this.runOnUiThread(animationTick)
    }

    private val countdownTick = Runnable {

        if (game!!.levelCountdown > 0 && game!!.isRunning) {
            game!!.levelCountdown--
            timerView.text = resources.getString(R.string.timerView, " ${game!!.levelCountdown}")
        } else if (game!!.levelCountdown <= 0 && game!!.isRunning) game!!.gameOver()


    }

    //This method runs in the same thread as the UI.
    private val animationTick = Runnable {
        if ((game!!.isRunning)) {
           game?.moveUfo()
            game?.calcEnemyDirection()
            game?.moveEnemy()

        }
    }


    //if anything is pressed - we do the checks here
    override fun onClick(v: View) {
        when (v.id) {
            R.id.moveRight -> if (game!!.isRunning) game?.ufoDirection = game!!.RIGHT
            R.id.moveLeft -> if (game!!.isRunning) game?.ufoDirection = game!!.LEFT
            R.id.moveUp -> if (game!!.isRunning) game?.ufoDirection = game!!.UP
            R.id.moveDown -> if (game!!.isRunning) game?.ufoDirection = game!!.DOWN

        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //this adds the menu to the ui
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        when (id) {
            R.id.action_newGame -> {
                game?.newGame(1)
                return true
            }
            R.id.action_pause -> {
                game?.isRunning = false
                return true
            }
            R.id.action_reset -> {
                //TODO optimize this- improve the levelcountdown & other variable calculations and make them accessible from here
                game?.newGame(game!!.level, game!!.ufoMoveDistance, game!!.enemyMoveDistance, game!!.levelCountdown)
                return true
            }
            R.id.action_resume -> {
                game?.isRunning = true
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    override fun onStop() {
        super.onStop()
        animationTimer.cancel()
        countdownTimer.cancel()
    }
}
