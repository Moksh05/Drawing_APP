package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class Drawingview(context: Context, attrs: AttributeSet) : View(context, attrs) {
    //here we passed contructor context as view class need context and attr
    //we inherited from view class becoz we want our app to treat this class as view not only as simple class, recog as a ui component
    //NOW CAN Call this draawing view in xml , as a widget , basically made our own widget
    /*
    To draw anything you need 4 things
    1) Bitmap : it holds the pixels
    2) canvas : it holds all the draw calls - on this we draw
    3) primitive : like path , what path is traced
    4) paint : to describe color background etc
    */
    //drawing path variable
    private lateinit var drawPath: FingerPath  //var that put items in place where finger goes

    //defines what to draw
    private lateinit var canvasPaint: Paint

    //defines how to draw
    private lateinit var drawPaint: Paint
    private var color = Color.BLACK

    //layer on which we draw
    private lateinit var canvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private var brushSize: Float = 0.toFloat()


    // to keep our drawing on screen - we make a var paths that contains all the path we made till now
    private var Paths = mutableListOf<FingerPath>()

    init {
        setUpDrawing()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)  //use whenever we change size of view class
        canvasBitmap = Bitmap.createBitmap(
            w,
            h,
            Bitmap.Config.ARGB_8888
        ) //sets up a newbitmap with alpha rgb888 colors available
        //width and ht passed to tell bitmap its size
        canvas =
            Canvas(canvasBitmap)  //canvas is created that has all functionn to draw on/in bitmap
    }

    override fun onDraw(canvas: Canvas?) { //to make drawings on canvas
        super.onDraw(canvas) //class from it inherits is called
        canvas?.drawBitmap(
            canvasBitmap,
            0f,
            0f,
            drawPaint
        ) //called and made the canvas , with no space in left and top side,making use of drawpaint

        for (path in Paths) {
            drawPaint.strokeWidth = path.BrushThickness
            //used path instead of drawpath as every drawpath inside paths can have diff
            drawPaint.color = path.Color  //diff color and thickness
            canvas?.drawPath(path, drawPaint)
        }


        if (!drawPath.isEmpty) {
            drawPaint.strokeWidth = drawPath.BrushThickness
            drawPaint.color = drawPath.Color
            canvas?.drawPath(
                drawPath,
                drawPaint
            ) //it draw path on canvas with color width given above
        }
    }

    fun setUpDrawing() {
        drawPaint = Paint() //initialising obj of paint class that contain drawing style
        drawPath = FingerPath(
            color,
            brushSize
        ) //instance of finger path class stores the path followed by user finger
        drawPaint.color = color //color of the instance
        drawPaint.style = Paint.Style.STROKE //tha path is filled with line and not any shape
        drawPaint.strokeJoin =
            Paint.Join.ROUND  //it sets the corners i.e joining of diff path to be round
        drawPaint.strokeCap = Paint.Cap.ROUND  //it sets the end of a stroke/ppath to be round
        canvasPaint =
            Paint(Paint.DITHER_FLAG)  //to det style of cnavas , ditherflag it says use the terrain to smooth the colors
        brushSize = 20.toFloat() //setting initial brush size

    }

    //automatically called when user touch screen
    override fun onTouchEvent(event: MotionEvent?): Boolean { //overiding it to respond to ,when user touches screen of canvas
        val TouchX = event?.x
        val TouchY = event?.y

        when (event?.action) {
            //i.e when user put his finger on screen
            MotionEvent.ACTION_DOWN -> {
                drawPath.Color = color
                drawPath.BrushThickness = brushSize.toFloat()
                //setting up the path color and thickness to be used as soon as user put down his fiinger on screen
                //reseting the initial postion of path
                drawPath.reset()
                drawPath.moveTo(
                    TouchX!!,
                    TouchY!!
                ) //moving the path from intial pos to x and y cordinates of bitmap

            }
            //when user is sliding is finger on screen untill it pick up the finger
            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(TouchX!!, TouchY!!)
            }

            //when user pick up his finger from screen
            MotionEvent.ACTION_UP -> {
                Paths.add(drawPath) //we added it to list but need to draw it to
                drawPath = FingerPath(color, brushSize)
            }

            else -> {
                return false
            }

        }
        invalidate() //refreshing the canvas to reflect the changes of drawing
        return true
    }

    fun changeBrushSize(newSize: Float) {
        //can't just equate brushsize to newsize as every screen has diff sizze
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )

        //it apply dimension on new size based on complex dip(display independent pixel , according to the display matric of phone provided by resopurces to app
        drawPaint.strokeWidth = brushSize
    }

    fun changecolor(newColor:Any) {
        if (newColor is String) {
            color = Color.parseColor(newColor.toString())
        }
        else{
            color = newColor as Int
        }
        drawPaint.color = color
    }
    fun Undo() {
        if (Paths.size > 0) {
            Paths.removeLast()
            invalidate()
        }
    }

    internal inner class FingerPath(var Color: Int, var BrushThickness: Float) :
        Path() //class that will control where finger gows
}