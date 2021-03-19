package org.pondar.pacmankotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


//note we now create our own view class that extends the built-in View class
class GameView : View {

    private var game: Game? = null
    private var h: Int = 0
    private var w: Int = 0 //used for storing our height and width of the view


    fun setGame(game: Game?) {
        this.game = game
    }



    /* The next 3 constructors are needed for the Android view system,
	when we have a custom view.
	 */
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    //In the onDraw we put all our code that should be
    //drawn whenever we update the screen.
    override fun onDraw(canvas: Canvas) {
        //Here we get the height and weight
        h = canvas.height
        w = canvas.width
        //update the size for the canvas to the game.
        game?.setSize(h, w)

        if (!game!!.collectiblesInitialized) game?.initializeCollectibles()
        //Making a new paint object
        val paint = Paint()
        //canvas.drawColor(Color.WHITE) //clear entire canvas to white color
        canvas.drawColor(Color.parseColor("#d0dcb1")) //clear entire canvas to white color

        canvas.drawBitmap(game!!.enemyBitmap, game?.enemy!!.x.toFloat(),
                game?.enemy!!.y.toFloat(), paint)
        canvas.drawBitmap(game!!.ufoBitmap, game?.ufox!!.toFloat(),
                game?.ufoy!!.toFloat(), paint)

        for (item: Collectible in game!!.collectibles) {
            if (!item.taken) {
                canvas.drawBitmap(game!!.collectibleBitmap, item.x.toFloat(),
                        item.y.toFloat(), paint)

            }
        }


        game?.doCollisionCheck()
        super.onDraw(canvas)
    }

}
