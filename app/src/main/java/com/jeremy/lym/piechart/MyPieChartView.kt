package com.jeremy.lym.piechart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * Created by liyimin on 2018/8/30.
 * GitHub：https://github.com/JeremyLeeL
 */
class MyPieChartView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attributeSet, defStyleAttr)  {

    /**画笔-画文字*/
    private val textPaint = Paint()

    /**画笔-画扇形*/
    private val arcPaint = Paint()

    /**画笔-画线*/
    private val linePaint = Paint()

    /**饼图所在圆半径*/
    private val radius = 400f

    /**第一段折线长度*/
    private val lineLength = 30

    /**文字和圆形的最小间距*/
    private val spacing = 60f

    /**绘制扇形开始的角度*/
    private var startAngle = 0f

    /**圆形规格*/
    private val rectF = RectF(-radius, -radius, radius, radius)

    /**突出扇形偏移量*/
    private val offset = 30

    private val strs = arrayOf("Marshmallow", "Froyo", "Gingerbread", "Ice Cream Sandwich", "Jelly Bean", "KitKat")
    private val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GRAY)
    private val percents = intArrayOf(10, 5, 15, 20, 30, 20)

    /*##############点击相关###########*/
    private var arcAngleMap = HashMap<Int, Angle>()
    /**上次点击的扇形index*/
    private var lastClickIndex = -1
    /**当前点击的扇形index*/
    private var currentClickIndex = -1
    init {
        textPaint.color = Color.WHITE
        textPaint.textSize = 30f

        arcPaint.isAntiAlias = true

        linePaint.isAntiAlias = true
        linePaint.color = Color.WHITE
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.style = Paint.Style.STROKE
//        linePaint.strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.translate(width / 2f, height /2f)
        var startAngle = 0f     //起始点（0（360）度为3点钟方向，顺时针方向递增）
        strs.forEachIndexed { index, s ->
            val sweepAngle = 360 * percents[index] / 100f
            val theta = (startAngle + sweepAngle / 2) * Math.PI / 180
            arcAngleMap[index] = Angle(startAngle * Math.PI / 180, (startAngle + sweepAngle) * Math.PI / 180)
            if (index == lastClickIndex){
                canvas?.save()
                canvas?.translate(-offset * Math.cos(theta).toFloat(), -offset * Math.sin(theta).toFloat())
//                drawContent(canvas, startAngle, sweepAngle, theta, s, index)
                canvas?.restore()
            }
            if (index == currentClickIndex && currentClickIndex != lastClickIndex){
                canvas?.save()
                canvas?.translate(offset * Math.cos(theta).toFloat(), offset * Math.sin(theta).toFloat())
                drawContent(canvas, startAngle, sweepAngle, theta, s, index)
                canvas?.restore()
                lastClickIndex = currentClickIndex
            }else {
                drawContent(canvas, startAngle, sweepAngle, theta, s, index)
            }
            startAngle += sweepAngle
        }
    }

    private fun drawContent(canvas: Canvas?, startAngle: Float, sweepAngle: Float, theta: Double, str: String, i: Int){
        arcPaint.color = colors[i]
        canvas?.drawArc(rectF, startAngle, sweepAngle, true, arcPaint)

        val lineStartX = radius * Math.cos(theta).toFloat()
        Log.e("lym", "第${i + 1}个theta值：  $theta")
        Log.e("lym", "第${i + 1}个cos值：  ${Math.cos(theta).toFloat()}")
        //对比一下就会发现，theta大于π/2的时候结果等于2π - theta；所以下面点击的时候要区分角是大于180度还是小于180度
        Log.e("lym", "第${i + 1}个反三角值：  ${Math.acos(Math.cos(theta))}")
//        Log.e("lym", "第${i + 1}个X：  $lineStartX")
        val lineStartY = radius * Math.sin(theta).toFloat()
        val lineEndX = (radius + lineLength) * Math.cos(theta).toFloat()
        val lineEndY = (radius + lineLength) * Math.sin(theta).toFloat()

        val textRect = getTextBounds(str, textPaint)

        //圆的左边
        if(theta > Math.PI / 2 && theta <= Math.PI * 3/2){
            canvas?.drawLine(lineStartX, lineStartY, lineEndX, lineEndY, linePaint)
            canvas?.drawLine(lineEndX, lineEndY, -spacing - radius, lineEndY, linePaint)
            canvas?.drawText(str, -spacing - radius - 10 - textRect.width(), lineEndY + textRect.height() / 2, textPaint)
        }else{//圆的右边
            canvas?.drawLine(lineStartX, lineStartY, lineEndX, lineEndY, linePaint)
            canvas?.drawLine(lineEndX, lineEndY, spacing + radius, lineEndY, linePaint)
            canvas?.drawText(str, spacing + radius + 10, lineEndY + textRect.height() / 2, textPaint)
        }
    }

    /**测量文字的规格*/
    private fun getTextBounds(text: String, paint: Paint): Rect{
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (MotionEvent.ACTION_DOWN == event?.action){
            Log.e("lym", "点击的x:  ${event.x - width/2}")
            Log.e("lym", "点击的y:  ${event.y - height/2}")
            val x = event.x - width/2
            val y = event.y - height/2
            val absX = Math.abs(x).toDouble()
            val absY = Math.abs(y).toDouble()
            //点击点到圆心的直线距离
            val length = Math.sqrt(Math.pow(absX, 2.0) + Math.pow(absY, 2.0))
            if (length > radius)
                return super.onTouchEvent(event)
            val theta = if (y > 0)  //angle小于180°
                Math.acos(x / length)
            else                    //angle大于180°
                2 * Math.PI - Math.acos(x / length)
            percents.forEachIndexed { index, i ->
                val angle = arcAngleMap[index]
                val startAngle = angle?.startAngle ?: 0.0
                val endAngle = angle?.endAngle ?: 0.0
                if (theta > startAngle && theta < endAngle){
                    if (currentClickIndex == index)
                        return@forEachIndexed
                    currentClickIndex = index
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    /**一个扇形块的起始角度和终点角度（弧度制）*/
    data class Angle(var startAngle: Double, var endAngle: Double)
}
