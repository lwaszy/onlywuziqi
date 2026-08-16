package com.iwaszy.toolapp;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class MD3Button extends View {
    // 默认配色
    private static final int DEFAULT_BG = 0xFFE8D5B7;
    private static final int DEFAULT_PRESSED = 0xFFD4C0A0;
    private static final int DEFAULT_TEXT = 0xFF5D4037;

    private String text = "按钮";
    private float cornerRadius = 20f;
    private int fixedHeight = 0;

    private int bgColor = DEFAULT_BG;
    private int pressedColor = DEFAULT_PRESSED;
    private int textColor = DEFAULT_TEXT;

    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF rect = new RectF();

    private boolean isPressed = false;
    private boolean isInside = false;
    private float scale = 1f;
    private ValueAnimator scaleAnimator;

    private OnClickListener onClickListener;

    public MD3Button(Context context) {
        super(context);
        init();
    }

    public MD3Button(Context context, String text) {
        super(context);
        this.text = text;
        init();
    }

    private void init() {
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        bgPaint.setAntiAlias(true);

        textPaint.setColor(textColor);
        textPaint.setTextSize(32);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setAntiAlias(true);

        setClickable(true);
        setFocusable(true);
        setWillNotDraw(false);
    }

    public void setFixedHeight(int height) {
        this.fixedHeight = height;
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float centerX = w / 2;
        float centerY = h / 2;

        float scale = this.scale;
        float scaledW = w * scale;
        float scaledH = h * scale;
        float offsetX = (w - scaledW) / 2;
        float offsetY = (h - scaledH) / 2;

        rect.set(offsetX, offsetY, offsetX + scaledW, offsetY + scaledH);

        if (isPressed && isInside) {
            bgPaint.setColor(pressedColor);
        } else {
            bgPaint.setColor(bgColor);
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint);

        float textScale = 0.9f + 0.1f * (1 - scale);
        textPaint.setTextSize(32 * textScale);
        textPaint.setColor(textColor);
        canvas.drawText(text, centerX, centerY + textPaint.getTextSize() / 3, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean inside = (x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (inside) {
                    isPressed = true;
                    isInside = true;
                    animateScale(0.92f);
                    postInvalidate();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (inside) {
                    if (!isInside) {
                        isInside = true;
                        isPressed = true;
                        animateScale(0.92f);
                        postInvalidate();
                    }
                } else {
                    if (isInside) {
                        isInside = false;
                        isPressed = false;
                        postInvalidate();
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (isInside) {
                    isPressed = false;
                    isInside = false;
                    animateScale(1f);
                    postInvalidate();
                    if (onClickListener != null) {
                        onClickListener.onClick(this);
                    }
                } else {
                    isPressed = false;
                    isInside = false;
                    animateScale(1f);
                    postInvalidate();
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                isInside = false;
                animateScale(1f);
                postInvalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void animateScale(float target) {
        if (scaleAnimator != null && scaleAnimator.isRunning()) {
            scaleAnimator.cancel();
        }

        scaleAnimator = ValueAnimator.ofFloat(scale, target);
        scaleAnimator.setDuration(120);
        scaleAnimator.setInterpolator(new DecelerateInterpolator());
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scale = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
        scaleAnimator.start();
    }

    public void setText(String text) {
        this.text = text;
        postInvalidate();
    }

    public String getText() {
        return text;
    }

    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        postInvalidate();
    }

    public void setColors(int bg, int pressed, int text) {
        this.bgColor = bg;
        this.pressedColor = pressed;
        this.textColor = text;
        postInvalidate();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = Math.min(280, widthSize);
        }

        if (fixedHeight > 0) {
            height = fixedHeight;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = Math.min(72, heightSize);
        }

        setMeasuredDimension(width, height);
    }
}
