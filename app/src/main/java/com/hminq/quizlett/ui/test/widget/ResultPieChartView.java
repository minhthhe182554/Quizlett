package com.hminq.quizlett.ui.test.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.hminq.quizlett.R;

public class ResultPieChartView extends View {
    private final Paint correctPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint incorrectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF drawRect = new RectF();

    private float correctFraction = 0f;
    private float incorrectFraction = 0f;

    public ResultPieChartView(Context context) {
        this(context, null);
    }

    public ResultPieChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResultPieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        @ColorInt int correctColor = 0xFF4CAF50;   // green
        @ColorInt int incorrectColor = 0xFFF44336; // red
        @ColorInt int backgroundColor = 0x33424F63;

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ResultPieChartView, defStyleAttr, 0);
            correctColor = a.getColor(R.styleable.ResultPieChartView_rpcv_correctColor, correctColor);
            incorrectColor = a.getColor(R.styleable.ResultPieChartView_rpcv_incorrectColor, incorrectColor);
            backgroundColor = a.getColor(R.styleable.ResultPieChartView_rpcv_trackColor, backgroundColor);
            a.recycle();
        }

        correctPaint.setStyle(Paint.Style.FILL);
        correctPaint.setColor(correctColor);

        incorrectPaint.setStyle(Paint.Style.FILL);
        incorrectPaint.setColor(incorrectColor);

        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(backgroundColor);
    }

    public void setResults(int correctCount, int incorrectCount) {
        int total = Math.max(correctCount + incorrectCount, 0);
        if (total <= 0) {
            correctFraction = 0f;
            incorrectFraction = 0f;
        } else {
            correctFraction = correctCount / (float) total;
            incorrectFraction = incorrectCount / (float) total;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = Math.min(getWidth(), getHeight()) * 0.05f;
        drawRect.set(padding, padding, getWidth() - padding, getHeight() - padding);

        canvas.drawArc(drawRect, 0f, 360f, true, backgroundPaint);

        float startAngle = -90f;
        float correctSweep = 360f * correctFraction;
        if (correctSweep > 0f) {
            canvas.drawArc(drawRect, startAngle, correctSweep, true, correctPaint);
            startAngle += correctSweep;
        }

        float incorrectSweep = 360f * incorrectFraction;
        if (incorrectSweep > 0f) {
            canvas.drawArc(drawRect, startAngle, incorrectSweep, true, incorrectPaint);
        }
    }
}

