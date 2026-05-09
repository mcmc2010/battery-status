package com.mcmcx.batterystatus.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.mcmcx.batterystatus.data.model.DataPoint;

import java.util.LinkedList;

public class RealTimeLineChart extends View {

    private final Paint _gridPaint;
    private final Paint _linePaint;
    private final Paint _fillPaint;
    private final Paint _labelPaint;
    private final Paint _valuePaint;
    private final Paint _titlePaint;

    private final Rect _textBounds = new Rect();

    private LinkedList<DataPoint> _series;
    private String _label = "";
    private String _unit = "";
    private int _lineColor = 0xFF3F51B5;
    private int _fillColor = 0x183F51B5;
    private int _durationSeconds = 300;

    public RealTimeLineChart(Context context) {
        this(context, null);
    }

    public RealTimeLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        _gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _gridPaint.setColor(0xFFE0E0E0);
        _gridPaint.setStrokeWidth(1f);
        _gridPaint.setStyle(Paint.Style.STROKE);

        _linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _linePaint.setColor(_lineColor);
        _linePaint.setStrokeWidth(2.5f);
        _linePaint.setStyle(Paint.Style.STROKE);
        _linePaint.setStrokeCap(Paint.Cap.ROUND);
        _linePaint.setStrokeJoin(Paint.Join.ROUND);

        _fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _fillPaint.setColor(_fillColor);
        _fillPaint.setStyle(Paint.Style.FILL);

        _labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _labelPaint.setColor(0xFF757575);
        _labelPaint.setTextSize(dpToPx(10));

        _valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _valuePaint.setColor(0xFF424242);
        _valuePaint.setTextSize(dpToPx(11));

        _titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _titlePaint.setColor(0xFF616161);
        _titlePaint.setTextSize(dpToPx(12));
        _titlePaint.setFakeBoldText(true);
    }

    public void setDurationSeconds(int seconds) {
        _durationSeconds = seconds;
    }

    /**
     * 切换到新的数据系列，立即重绘。
     */
    public void setSeries(LinkedList<DataPoint> series, String label, String unit, int lineColor) {
        _series = series;
        _label = label;
        _unit = unit;
        _lineColor = lineColor;
        _fillColor = (lineColor & 0x00FFFFFF) | 0x18000000;

        _linePaint.setColor(lineColor);
        _fillPaint.setColor(_fillColor);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return;

        float padLeft = dpToPx(44);
        float padRight = dpToPx(12);
        float padTop = dpToPx(36);
        float padBottom = dpToPx(24);

        float chartLeft = padLeft;
        float chartRight = width - padRight;
        float chartTop = padTop;
        float chartBottom = height - padBottom;

        drawTitle(canvas, width);

        pruneOld();

        float[] bounds = calcBounds();
        float dataMin = bounds[0];
        float dataMax = bounds[1];

        if (_series == null || _series.isEmpty()) {
            drawEmptyHint(canvas, width, height);
            return;
        }

        if (dataMin == dataMax) {
            dataMin -= 5f;
            dataMax += 5f;
        }
        float padding = (dataMax - dataMin) * 0.2f;
        if (padding < 0.5f) padding = 2f;
        float yMin = dataMin - padding;
        float yMax = dataMax + padding;

        drawGrid(canvas, chartLeft, chartRight, chartTop, chartBottom, yMin, yMax);
        drawYLabels(canvas, chartLeft, chartTop, chartBottom, yMin, yMax);
        drawXLabels(canvas, chartLeft, chartRight, chartBottom);

        if (_series.size() < 2) return;

        drawDataLine(canvas, chartLeft, chartRight, chartTop, chartBottom, yMin, yMax);
        drawCurrentValue(canvas, chartRight, chartTop);
    }

    private void pruneOld() {
        if (_series == null) return;
        long cutoff = System.currentTimeMillis() - (_durationSeconds * 1000L);
        while (!_series.isEmpty() && _series.getFirst().timestamp < cutoff) {
            _series.removeFirst();
        }
    }

    private float[] calcBounds() {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        if (_series == null || _series.isEmpty()) return new float[]{min, max};
        for (DataPoint dp : _series) {
            if (dp.value < min) min = dp.value;
            if (dp.value > max) max = dp.value;
        }
        return new float[]{min, max};
    }

    private void drawGrid(Canvas canvas, float left, float right, float top, float bottom,
                          float yMin, float yMax) {
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            float y = top + (bottom - top) * i / gridLines;
            canvas.drawLine(left, y, right, y, _gridPaint);
        }
    }

    private void drawYLabels(Canvas canvas, float chartLeft, float chartTop,
                             float chartBottom, float yMin, float yMax) {
        int labels = 4;
        for (int i = 0; i <= labels; i++) {
            float y = chartTop + (chartBottom - chartTop) * i / labels;
            float value = yMax - (yMax - yMin) * i / labels;
            String text = String.format("%.1f", value);
            if (Math.abs(value) >= 1000) text = String.format("%.0f", value);
            _labelPaint.getTextBounds(text, 0, text.length(), _textBounds);
            float textY = y + _textBounds.height() / 2f;
            canvas.drawText(text, dpToPx(4), textY, _labelPaint);
        }
    }

    private void drawXLabels(Canvas canvas, float chartLeft, float chartRight, float chartBottom) {
        int steps = 5;
        for (int i = 0; i <= steps; i++) {
            float x = chartLeft + (chartRight - chartLeft) * i / steps;
            int secondsAgo = _durationSeconds - (_durationSeconds * i / steps);
            String text = "-" + secondsAgo + "s";
            if (secondsAgo >= 60) {
                text = "-" + (secondsAgo / 60) + "m";
            }
            _labelPaint.getTextBounds(text, 0, text.length(), _textBounds);
            canvas.drawText(text, x - _textBounds.width() / 2f,
                    chartBottom + dpToPx(14), _labelPaint);
        }
    }

    private void drawDataLine(Canvas canvas, float chartLeft, float chartRight,
                              float chartTop, float chartBottom, float yMin, float yMax) {
        long now = System.currentTimeMillis();
        long windowStart = now - (_durationSeconds * 1000L);

        Path linePath = new Path();
        Path fillPath = new Path();
        boolean first = true;

        float baseline = chartBottom;
        float prevX = 0;

        for (DataPoint dp : _series) {
            float xFraction = (dp.timestamp - windowStart) / (float) (_durationSeconds * 1000L);
            // Clamp to visible area
            xFraction = Math.max(0, Math.min(1, xFraction));
            float x = chartLeft + xFraction * (chartRight - chartLeft);

            float yFraction = (dp.value - yMin) / (yMax - yMin);
            float y = chartBottom - yFraction * (chartBottom - chartTop);

            if (first) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, baseline);
                fillPath.lineTo(x, y);
                first = false;
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
            prevX = x;
        }

        fillPath.lineTo(prevX, baseline);
        fillPath.close();

        canvas.drawPath(fillPath, _fillPaint);
        canvas.drawPath(linePath, _linePaint);
    }

    private void drawCurrentValue(Canvas canvas, float chartRight, float chartTop) {
        if (_series == null || _series.isEmpty()) return;
        float value = _series.getLast().value;
        String text = String.format("%.1f %s", value, _unit);
        _valuePaint.getTextBounds(text, 0, text.length(), _textBounds);
        canvas.drawText(text, chartRight - _textBounds.width(), chartTop - dpToPx(4), _valuePaint);
    }

    private void drawTitle(Canvas canvas, float width) {
        int minutes = _durationSeconds / 60;
        String title = _label + " · 最近" + minutes + "分钟数据";
        _titlePaint.getTextBounds(title, 0, title.length(), _textBounds);
        canvas.drawText(title, (width - _textBounds.width()) / 2f, dpToPx(18), _titlePaint);
    }

    private void drawEmptyHint(Canvas canvas, float width, float height) {
        _labelPaint.setTextSize(dpToPx(12));
        String hint = _label + " — waiting for data...";
        _labelPaint.getTextBounds(hint, 0, hint.length(), _textBounds);
        canvas.drawText(hint, (width - _textBounds.width()) / 2f,
                height / 2f, _labelPaint);
        _labelPaint.setTextSize(dpToPx(10));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        _series = null;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
