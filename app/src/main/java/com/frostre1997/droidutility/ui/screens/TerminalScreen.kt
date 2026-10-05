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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.frostre1997.droidutility.terminal.GhosttyVt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
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

    private var cellWidth: Float = 0f
    private var cellHeight: Int = 0
    private var cols: Int = 80
    private var rows: Int = 24

    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private var snapshotBuf: ByteBuffer? = null

    private var lastTouchY: Float = 0f

    init {
        isFocusable = true
        isFocusableInTouchMode = true
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
        val bmp = Bitmap.createBitmap(
            (cols * cellWidth).toInt(),
            rows * cellHeight,
            Bitmap.Config.ARGB_8888
        )
        bitmap = bmp
        bitmapCanvas = Canvas(bmp)
        val size = 4 + cols * rows * 16
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

        val c = buf.short.toInt() and 0xFFFF
        val r = buf.short.toInt() and 0xFFFF

        for (row in 0 until r) {
            val top = row * cellHeight.toFloat()
            for (col in 0 until c) {
                val codepoint = buf.int
                val fg = buf.int
                val bg = buf.int
                buf.int

                val left = col * cellWidth

                if (bg != android.graphics.Color.BLACK) {
                    bc.drawRect(left, top, left + cellWidth, top + cellHeight, Paint().apply { color = bg })
                }

                if (codepoint > 0 && codepoint != 0x20) {
                    val text = String(Character.toChars(codepoint))
                    textPaint.color = fg
                    bc.drawText(text, left, top - textPaint.fontMetrics.ascent, textPaint)
                }
            }
        }

        canvas.drawBitmap(bmp, 0f, 0f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                requestFocus()
                lastTouchY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - lastTouchY
                if (abs(dy) > 20f) {
                    lastTouchY = event.y
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val bytes = GhosttyVt.nativeEncodeKey(
            handle, keyCode, 1, event.metaState, event.unicodeChar, null
        )
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
                    onInputBytes?.invoke(text.toString().toByteArray(Charsets.UTF_8))
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
    val coroutineScope = rememberCoroutineScope()
    val viewRef = remember { arrayOfNulls<GhosttyTerminalView>(1) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val view = GhosttyTerminalView(ctx)

            val zish = File(ctx.filesDir, "zish")
            val shellPath = if (zish.exists() && zish.canExecute()) zish.absolutePath else "/system/bin/sh"
            val process = try {
                ProcessBuilder(shellPath).start()
            } catch (e: Exception) {
                ProcessBuilder("/system/bin/sh").start()
            }

            view.onInputBytes = { bytes ->
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        process.outputStream.write(bytes)
                        process.outputStream.flush()
                    } catch (_: Exception) {}
                }
            }

            coroutineScope.launch(Dispatchers.IO) {
                val buffer = ByteArray(4096)
                val stream = process.inputStream
                while (true) {
                    val read = try { stream.read(buffer) } catch (e: Exception) { -1 }
                    if (read <= 0) break
                    val chunk = buffer.copyOf(read)
                    withContext(Dispatchers.Main) { view.write(chunk) }
                }
            }

            viewRef[0] = view
            view
        },
        update = { it.requestFocus() }
    )

    DisposableEffect(Unit) {
        onDispose {
            viewRef[0]?.destroy()
            viewRef[0] = null
        }
    }
}
