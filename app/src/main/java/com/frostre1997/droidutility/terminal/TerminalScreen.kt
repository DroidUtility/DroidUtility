package com.frostre1997.droidutility.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.frostre1997.droidutility.terminal.CellFlags
import com.frostre1997.droidutility.terminal.GhosttyVt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class GhosttyTerminalView(context: android.content.Context) : View(context) {
    var onInputBytes: ((ByteArray) -> Unit)? = null
    var onGridResize: ((cols: Int, rows: Int) -> Unit)? = null

    private var handle: Long = GhosttyVt.nativeCreate(80, 24)
    private var finished = false

    private val textPaint = Paint().apply {
        typeface = Typeface.MONOSPACE
        textSize = 36f
        isAntiAlias = true
    }
    private val boldPaint = Paint().apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = 36f
        isAntiAlias = true
    }
    private val italicPaint = Paint().apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
        textSize = 36f
        isAntiAlias = true
    }

    private var cellWidth: Float = 0f
    private var cellHeight: Int = 0
    private var cols: Int = 80
    private var rows: Int = 24

    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private var snapshotBuf: ByteBuffer? = null

    private var scrollOffset: Int = 0

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        calculateMetrics()
    }

    private fun calculateMetrics() {
        cellWidth = textPaint.measureText("M")
        val fm = textPaint.fontMetrics
        cellHeight = ceil(fm.descent - fm.ascent).toInt()
    }

    fun write(data: ByteArray) {
        if (finished) return
        val response = GhosttyVt.nativeWrite(handle, data)
        if (response != null && response.isNotEmpty()) {
            onInputBytes?.invoke(response)
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val newCols = max(2, floor(w / cellWidth).toInt())
        val newRows = max(2, h / cellHeight)
        if (newCols != cols || newRows != rows) {
            cols = newCols
            rows = newRows
            GhosttyVt.nativeResize(handle, cols, rows)
            allocateGridBuffers()
            onGridResize?.invoke(cols, rows)
        }
    }

    private fun allocateGridBuffers() {
        val bmp = Bitmap.createBitmap(cols * cellWidth.toInt(), rows * cellHeight, Bitmap.Config.ARGB_8888)
        bitmap = bmp
        bitmapCanvas = Canvas(bmp)
        val size = cols * rows * 32
        snapshotBuf = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return
        val buf = snapshotBuf ?: return

        val bytesWritten = GhosttyVt.nativeSnapshot(handle, buf)
        if (bytesWritten <= 0) {
            canvas.drawBitmap(bmp, 0f, 0f, null)
            return
        }

        buf.rewind()
        val bc = bitmapCanvas ?: return
        bc.drawColor(android.graphics.Color.BLACK)

        val rowCount = buf.getShort().toInt() and 0xFFFF
        for (r in 0 until rowCount) {
            val y = buf.getShort().toInt() and 0xFFFF
            val cellCount = buf.getShort().toInt() and 0xFFFF
            for (c in 0 until cellCount) {
                val x = buf.getShort().toInt() and 0xFFFF
                val codepoint = buf.getInt()
                val fg = buf.getInt()
                val bg = buf.getInt()
                val flags = buf.getInt()
                val textLen = buf.getInt()
                val textBytes = ByteArray(textLen)
                buf.get(textBytes)
                val text = String(textBytes, Charsets.UTF_8)

                val left = x * cellWidth
                val top = y * cellHeight.toFloat()

                if ((flags and CellFlags.INVERSE) != 0) {
                    bc.drawRect(left, top, left + cellWidth, top + cellHeight, Paint().apply { color = fg })
                    textPaint.color = bg
                } else {
                    if (bg != 0) {
                        bc.drawRect(left, top, left + cellWidth, top + cellHeight, Paint().apply { color = bg })
                    }
                    textPaint.color = fg
                }

                val paint = when {
                    (flags and CellFlags.BOLD) != 0 -> boldPaint.apply { color = textPaint.color }
                    (flags and CellFlags.ITALIC) != 0 -> italicPaint.apply { color = textPaint.color }
                    else -> textPaint
                }
                if ((flags and CellFlags.INVISIBLE) == 0) {
                    bc.drawText(text, left, top - textPaint.fontMetrics.ascent, paint)
                }
            }
        }

        canvas.drawBitmap(bmp, 0f, 0f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                requestFocus()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - event.historyY(0)
                if (kotlin.math.abs(dy) > 20) {
                    scrollOffset += if (dy > 0) 3 else -3
                    scrollOffset = scrollOffset.coerceIn(0, 1000)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val bytes = GhosttyVt.nativeEncodeKey(handle, keyCode, 0, event.metaState, event.unicodeChar, null)
        if (bytes != null && bytes.isNotEmpty()) {
            onInputBytes?.invoke(bytes)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = EditorInfo.TYPE_NULL
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NONE
        return object : BaseInputConnection(this, true) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                if (text != null) {
                    val bytes = text.toString().toByteArray(Charsets.UTF_8)
                    onInputBytes?.invoke(bytes)
                }
                return true
            }
        }
    }

    fun destroy() {
        if (finished) return
        finished = true
        GhosttyVt.nativeFree(handle)
        bitmap?.recycle()
    }
}

@Composable
fun TerminalScreen() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            GhosttyTerminalView(ctx).apply {
                onInputBytes = { bytes ->
                    (ctx.applicationContext as? com.frostre1997.droidutility.DroidUtilityApp)?.let { app ->
                        app.writeToPty(bytes)
                    }
                }
                onGridResize = { c, r ->
                    (ctx.applicationContext as? com.frostre1997.droidutility.DroidUtilityApp)?.let { app ->
                        app.resizePty(c, r)
                    }
                }
            }
        },
        update = { view ->
            view.requestFocus()
        }
    )

    DisposableEffect(Unit) {
        onDispose { }
    }
}
