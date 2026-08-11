package com.so.movie.danmaku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

/**
 * 自定义弹幕渲染 View
 * 支持滚动、顶部、底部三种弹幕类型
 * 使用 Canvas 高性能渲染
 */
class DanmakuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 弹幕设置
    var settings: DanmakuSettings = DanmakuSettings()
        set(value) {
            field = value
            textPaint.textSize = value.fontSize * resources.displayMetrics.density
            textPaint.alpha = (value.opacity * 255).toInt()
            updateLanes()
            invalidate()
        }

    // 所有弹幕数据（按时间排序）
    private var allDanmaku: List<DanmakuEntry> = emptyList()

    // 当前活跃的弹幕
    private val activeScrollDanmaku = mutableListOf<ActiveDanmaku>()
    private val activeFixedDanmaku = mutableListOf<ActiveDanmaku>()

    // 已显示过的弹幕时间点（防止重复添加）
    private var lastShownTimeMs: Long = 0L

    // 播放状态
    private var isPlaying = false
    private var currentTimeMs: Long = 0L

    // 画笔
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 16f * resources.displayMetrics.density
        setShadowLayer(2f, 1f, 1f, 0x80000000)
    }

    private val measureBounds = Rect()

    // 轨道管理
    private var laneHeight: Float = 0f
    private var scrollLanes: Int = 0
    private val scrollLaneLastEndTime = mutableMapOf<Int, Long>() // 轨道最后一条弹幕的结束时间
    private val fixedLaneOccupied = mutableMapOf<Int, Long>() // 固定弹幕轨道占用截止时间

    // 渲染循环
    private val handler = Handler(Looper.getMainLooper())
    private var lastRenderTime: Long = 0L
    private val renderRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                update()
                invalidate()
            }
            handler.postDelayed(this, 16) // ~60fps
        }
    }

    /**
     * 活跃弹幕项
     */
    private data class ActiveDanmaku(
        val entry: DanmakuEntry,
        var x: Float,
        var y: Float,
        var width: Float,
        var startTime: Long,
        val lane: Int,
        var isFixed: Boolean = false
    )

    /**
     * 设置弹幕数据
     */
    fun setDanmaku(danmaku: List<DanmakuEntry>) {
        allDanmaku = danmaku.sortedBy { it.time }
        activeScrollDanmaku.clear()
        activeFixedDanmaku.clear()
        lastShownTimeMs = 0L
    }

    /**
     * 更新播放时间
     */
    fun updateTime(timeMs: Long) {
        val prevTime = currentTimeMs
        currentTimeMs = timeMs

        // 检查是否需要添加新弹幕
        if (allDanmaku.isNotEmpty() && settings.enabled) {
            addPendingDanmaku(prevTime, timeMs)
        }
    }

    /**
     * 开始播放
     */
    fun play() {
        isPlaying = true
        lastRenderTime = System.currentTimeMillis()
        handler.post(renderRunnable)
    }

    /**
     * 暂停播放
     */
    fun pause() {
        isPlaying = false
        handler.removeCallbacks(renderRunnable)
    }

    /**
     * 清空弹幕
     */
    fun clear() {
        activeScrollDanmaku.clear()
        activeFixedDanmaku.clear()
        lastShownTimeMs = 0L
        invalidate()
    }

    /**
     * 跳转时间（清空活跃弹幕）
     */
    fun seek(timeMs: Long) {
        clear()
        lastShownTimeMs = timeMs
        currentTimeMs = timeMs
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateLanes()
    }

    private fun updateLanes() {
        laneHeight = settings.fontSize * resources.displayMetrics.density * 1.4f
        scrollLanes = if (height > 0) (height / laneHeight).toInt().coerceAtLeast(1) else 10
    }

    /**
     * 添加到时间点的弹幕
     */
    private fun addPendingDanmaku(prevTimeMs: Long, currTimeMs: Long) {
        val prevSec = prevTimeMs / 1000f
        val currSec = currTimeMs / 1000f

        for (entry in allDanmaku) {
            if (entry.time > prevSec && entry.time <= currSec) {
                addDanmaku(entry)
            } else if (entry.time > currSec) {
                break
            }
        }
    }

    /**
     * 添加单条弹幕
     */
    private fun addDanmaku(entry: DanmakuEntry) {
        // 测量文字宽度
        textPaint.getTextBounds(entry.message, 0, entry.message.length, measureBounds)
        val textWidth = measureBounds.width().toFloat()

        when (entry.type) {
            DanmakuType.SCROLL -> {
                if (!settings.showScroll) return
                if (activeScrollDanmaku.size >= settings.maxDisplay) return

                // 找到可用轨道
                val lane = findAvailableScrollLane(textWidth)
                if (lane < 0) return

                val now = System.currentTimeMillis()
                activeScrollDanmaku.add(ActiveDanmaku(
                    entry = entry,
                    x = width.toFloat(),
                    y = lane * laneHeight + laneHeight * 0.75f,
                    width = textWidth,
                    startTime = now,
                    lane = lane
                ))
            }

            DanmakuType.TOP -> {
                if (!settings.showTop) return
                val lane = findAvailableFixedLane(isTop = true)
                if (lane < 0) return

                activeFixedDanmaku.add(ActiveDanmaku(
                    entry = entry,
                    x = (width - textWidth) / 2f,
                    y = lane * laneHeight + laneHeight * 0.75f,
                    width = textWidth,
                    startTime = System.currentTimeMillis(),
                    lane = lane,
                    isFixed = true
                ))
            }

            DanmakuType.BOTTOM -> {
                if (!settings.showBottom) return
                val lane = findAvailableFixedLane(isTop = false)
                if (lane < 0) return

                activeFixedDanmaku.add(ActiveDanmaku(
                    entry = entry,
                    x = (width - textWidth) / 2f,
                    y = height - (lane + 1) * laneHeight + laneHeight * 0.75f,
                    width = textWidth,
                    startTime = System.currentTimeMillis(),
                    lane = lane,
                    isFixed = true
                ))
            }
        }
    }

    /**
     * 查找可用的滚动弹幕轨道
     */
    private fun findAvailableScrollLane(textWidth: Float): Int {
        val now = System.currentTimeMillis()
        val speed = getScrollSpeed()
        for (lane in 0 until scrollLanes) {
            val lastEnd = scrollLaneLastEndTime[lane] ?: 0L
            if (now >= lastEnd) {
                // 计算这条弹幕完全离开屏幕的时间
                val duration = ((width + textWidth) / speed * 1000).toLong()
                scrollLaneLastEndTime[lane] = now + duration * 2 / 3 // 留间距
                return lane
            }
        }
        // 所有轨道都被占用，返回最少使用的轨道
        return -1
    }

    /**
     * 查找可用的固定弹幕轨道
     */
    private fun findAvailableFixedLane(isTop: Boolean): Int {
        val now = System.currentTimeMillis()
        val maxLanes = scrollLanes / 3 // 固定弹幕最多用 1/3 轨道
        for (lane in 0 until maxLanes) {
            val key = (if (isTop) 1000 else 2000) + lane
            val occupiedUntil = fixedLaneOccupied[key] ?: 0L
            if (now >= occupiedUntil) {
                fixedLaneOccupied[key] = now + 4000 // 固定弹幕显示4秒
                return lane
            }
        }
        return -1
    }

    /**
     * 获取滚动速度（像素/毫秒）
     */
    private fun getScrollSpeed(): Float {
        val baseSpeed = width / 8000f * settings.speed // 基础8秒走完屏幕
        return baseSpeed.coerceAtLeast(0.1f)
    }

    /**
     * 更新弹幕位置
     */
    private fun update() {
        val now = System.currentTimeMillis()
        val dt = if (lastRenderTime > 0) now - lastRenderTime else 16
        lastRenderTime = now

        val speed = getScrollSpeed()

        // 更新滚动弹幕
        val iterator = activeScrollDanmaku.iterator()
        while (iterator.hasNext()) {
            val d = iterator.next()
            d.x -= speed * dt
            if (d.x + d.width < 0) {
                iterator.remove()
            }
        }

        // 更新固定弹幕
        val fixedIterator = activeFixedDanmaku.iterator()
        while (fixedIterator.hasNext()) {
            val d = fixedIterator.next()
            if (now - d.startTime > 4000) {
                fixedIterator.remove()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!settings.enabled) return

        // 绘制滚动弹幕
        for (d in activeScrollDanmaku) {
            textPaint.color = d.entry.color
            textPaint.alpha = (settings.opacity * 255).toInt()
            canvas.drawText(d.entry.message, d.x, d.y, textPaint)
        }

        // 绘制固定弹幕
        for (d in activeFixedDanmaku) {
            textPaint.color = d.entry.color
            textPaint.alpha = (settings.opacity * 255).toInt()
            canvas.drawText(d.entry.message, d.x, d.y, textPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pause()
    }
}
