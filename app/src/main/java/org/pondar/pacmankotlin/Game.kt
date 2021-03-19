package org.pondar.pacmankotlin

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import java.util.ArrayList
import kotlin.math.*


class Game(private var context: Context, view: TextView, timer: TextView) {
    //game props
    private var pointsView: TextView = view
    private var timerView: TextView = timer

    private var points = 0
    private var numOfCollectibles = 10

    var isRunning = false
    var collectiblesInitialized = false
    val LEFT = 1
    val UP = 2
    val DOWN = 3
    val RIGHT = 4

    //level props:
    private var baseLevelCountdown = 60
    var levelCountdown = 60
    var level = 1
    var ufoMoveDistance = 6
    var enemyMoveDistance = 4

    //UFO
    var ufoBitmap: Bitmap
    var ufox = 0
    var ufoy = 0
    var ufoDirection = RIGHT

    //collectible & enemy:
    var collectibleBitmap: Bitmap
    var enemyBitmap: Bitmap
    lateinit var enemy: Enemy
    var collectibles = ArrayList<Collectible>()
    // var enemies = ArrayList<Enemy>() //ready for multiple enemies

    //a reference to the gameview
    private var gameView: GameView? = null
    private var h = 0
    private var w = 0 //height and width of screen


    init {
        ufoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.alien)
        collectibleBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.cow)
        enemyBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.cops)

    }

    fun setGameView(view: GameView) {
        this.gameView = view
    }

    fun newGame(currentLevel: Int = 1, ufoMoveDist: Int = 6, enemyMoveDist: Int = 4, levelCount: Int = 60) {
        ufox = 50
        ufoy = 400
        ufoDirection = RIGHT
        ufoMoveDistance = ufoMoveDist
        enemyMoveDistance = enemyMoveDist
        collectiblesInitialized = false
        level = currentLevel
        levelCountdown = levelCount
        collectibles.clear()
        points = 0
        pointsView.text = context.resources.getString(R.string.points, " $points")
        timerView.text = context.resources.getString(R.string.timerView, " $levelCountdown")
        initializeEnemies()
        gameView?.invalidate() //redraw screen
    }

    // TODO: Refactor. Reaaaaaalllly rough method of securing cows dont overlap.
    //  Problem: canvas dimensions not established until GameView.onDraw has been called (so I wait with coins init until onDraw)
    fun initializeCollectibles() {
        // Run a loop where cows with random coordinates are created,
        // if they overlap previous cows: discard, else add to arraylist
        var counter = 0
        val buffer = 500
        var isOverlapping = false
        while (collectibles.size < numOfCollectibles &&
                counter < buffer) {
            val x = (0..(w - collectibleBitmap.width)).random()
            val y = (0..(h - collectibleBitmap.height)).random()
            val coin = Collectible(x, y)
            isOverlapping = false
            for (i in 0 until collectibles.size) {
                val existing = collectibles[i];
                val d = calcDistance(coin.x, coin.y, existing.x, existing.y)
                if (calcCircleIntersect(d, collectibleBitmap.width / 2, collectibleBitmap.width / 2)) {
                    isOverlapping = true;
                    break;
                }
            }
            if (!isOverlapping) {
                collectibles.add(coin);
            }
            counter++;
        }
        collectiblesInitialized = true
        isRunning = true
    }

    fun initializeEnemies() {
        // TODO again, canvas height not initialized until gameView.onDraw has been called. Figure out how to init these before.
        enemy = Enemy(400, h - enemyBitmap.height)
    }

    fun setSize(h: Int, w: Int) {
        this.h = h
        this.w = w
    }

    fun moveUfo(){
        when(ufoDirection){
            RIGHT-> if (ufox + ufoMoveDistance + ufoBitmap.width < w)ufox += ufoMoveDistance
            LEFT-> if (ufox - ufoMoveDistance > 0) ufox -= ufoMoveDistance
            UP-> if (ufoy - ufoMoveDistance > 0) ufoy -= ufoMoveDistance
            DOWN-> if (ufoy + ufoMoveDistance + ufoBitmap.height < h) ufoy += ufoMoveDistance
        }
    }

    fun doCollisionCheck() {
        //center x,y coordinates, determine radii
        val R1 = ufoBitmap.width / 2
        val X1 = ufox + R1
        val Y1 = ufoy + R1

        //cows
        val R2 = collectibleBitmap.width / 2
        for (i in collectibles.indices) {
            val X2 = collectibles[i].x + R2
            val Y2 = collectibles[i].y + R2
            val dist = calcDistance(X1, Y1, X2, Y2)
            if (calcCircleIntersect(dist, R1, R2) && !collectibles[i].taken) {
                collectibles[i].taken = true
                points++
                pointsView.text = context.resources.getString(R.string.points, " $points")

            }
        }
        val collectiblesTaken: List<Collectible> = collectibles.filter { c -> c.taken }
        if (collectiblesTaken.size == collectibles.size) advanceLevel()
        if (calcCircleIntersect(calcDistance(X1, Y1, enemy.x+(enemyBitmap.width/2), enemy.y+(enemyBitmap.height/2)), R1, enemyBitmap.height / 2  )) gameOver()

    }

    private fun calcDistance(x1: Int, y1: Int, x2: Int, y2: Int): Double {
        val distSqrd = ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()
        return sqrt(distSqrd)
    }

    private fun calcCircleIntersect(dist: Double, R1: Int, R2: Int): Boolean {
        //returns true if distance is equal to or less than the 2 hitbox radii combined
        return dist <= R1 + R2;
    }

    fun calcEnemyDirection() {
        //destination is ufo: ufox,ufoy
        val vectorX = ufox - enemy.x
        val vectorY = ufoy - enemy.y

        if (vectorX.absoluteValue >= vectorY.absoluteValue) {
            if (vectorX < 0) enemy.direction = LEFT
            else enemy.direction = RIGHT
        } else if (vectorY.absoluteValue > vectorX.absoluteValue) {
            if (vectorY < 0) enemy.direction = UP
            else enemy.direction = DOWN
        }
    }

    fun moveEnemy() {
        val direction = enemy.direction

        when (direction) {
            LEFT -> {
                if (enemy.x - enemyMoveDistance > 0) {
                    enemy.x = enemy.x - enemyMoveDistance
                    gameView!!.invalidate()
                }
            }
            UP -> {
                if (enemy.y - enemyMoveDistance > 0) {
                    enemy.y = enemy.y - enemyMoveDistance
                    gameView!!.invalidate()
                }
            }
            DOWN -> {
                if (enemy.y + enemyMoveDistance + enemyBitmap.height < h) {
                    enemy.y = enemy.y + enemyMoveDistance
                    gameView!!.invalidate()
                }
            }
            RIGHT -> {
                if (enemy.x + enemyMoveDistance + enemyBitmap.width < w) {
                    enemy.x = enemy.x + enemyMoveDistance
                    gameView!!.invalidate()
                }
            }


        }

    }

    fun gameOver() {
        isRunning = false
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.gameoverTitle)
        builder.setMessage(R.string.gameoverMessage)
        builder.setIcon(R.drawable.gameover_1)
        //startover?
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            newGame()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun advanceLevel() {
        isRunning = false
        level++
        ufoMoveDistance = (ufoMoveDistance * 1.4).toInt()
        enemyMoveDistance = (enemyMoveDistance * 1.4).toInt()
        levelCountdown = (baseLevelCountdown - ((level-1) * 15)).toInt()


        val levelbuilder = AlertDialog.Builder(context)
        levelbuilder.setTitle("${context.resources.getString(R.string.levelTitle)} $level")
        levelbuilder.setMessage(R.string.levelMessage)
        //levelbuilder.setIcon(R.drawable.??) //TODO find a level-up icon

        //Proceed to next level?
        levelbuilder.setPositiveButton("Yes") { dialogInterface, which ->
            newGame(level, ufoMoveDistance, enemyMoveDistance, levelCountdown)
            isRunning = true
        }
        //performing cancel action
        levelbuilder.setNeutralButton("Start over") { dialogInterface, which ->
            newGame(1, 6, 4, 60)
            isRunning = true
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = levelbuilder.create()
        alertDialog.show()
    }


}