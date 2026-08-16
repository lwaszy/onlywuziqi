package com.iwaszy.toolapp;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MD3Dialog {

    // 对话框配色
    private static final int COLOR_BG = 0xFFF5EEE6;
    private static final int COLOR_TEXT = 0xFF5D4037;
    private static final int CORNER_RADIUS = 20;

    // 按钮配色
    private static final int BTN_BG = 0xFFE8D5B7;
    private static final int BTN_PRESSED = 0xFFD4C0A0;
    private static final int BTN_TEXT = 0xFF5D4037;
    private static final float BTN_CORNER = 16f;
    private static final int BTN_HEIGHT = 72;

    // 间距
    private static final int BTN_GAP = 26;
    private static final int BTN_GAP_LARGE = 40;

    private Context context;
    private String title;
    private String message;
    private List<String> items = new ArrayList<>();
    private String positiveText;
    private String negativeText;
    private String neutralText;

    private OnItemClickListener itemListener;
    private OnButtonClickListener positiveListener;
    private OnButtonClickListener negativeListener;
    private OnButtonClickListener neutralListener;

    private View customView;

    private DialogView dialogView;
    private FrameLayout container;

    public MD3Dialog(Context context) {
        this.context = context;
    }

    public MD3Dialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public MD3Dialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public MD3Dialog setItems(String[] items, OnItemClickListener listener) {
        for (String item : items) {
            this.items.add(item);
        }
        this.itemListener = listener;
        return this;
    }

    public MD3Dialog setPositiveButton(String text, OnButtonClickListener listener) {
        this.positiveText = text;
        this.positiveListener = listener;
        return this;
    }

    public MD3Dialog setNegativeButton(String text, OnButtonClickListener listener) {
        this.negativeText = text;
        this.negativeListener = listener;
        return this;
    }

    public MD3Dialog setNeutralButton(String text, OnButtonClickListener listener) {
        this.neutralText = text;
        this.neutralListener = listener;
        return this;
    }

    public MD3Dialog setCustomView(View view) {
        this.customView = view;
        return this;
    }

    public void show() {
        container = new FrameLayout(context);
        container.setBackgroundColor(0x88000000);
        container.setClickable(true);
        container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

        dialogView = new DialogView(context);
        dialogView.setTitle(title);
        dialogView.setMessage(message);
        dialogView.setItems(items, itemListener);
        dialogView.setButtons(positiveText, negativeText, neutralText,
                              positiveListener, negativeListener, neutralListener);
        dialogView.setCustomView(customView);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        container.addView(dialogView, params);

        if (context instanceof android.app.Activity) {
            ViewGroup root = ((android.app.Activity) context).findViewById(android.R.id.content);
            root.addView(container, new ViewGroup.LayoutParams(
                             ViewGroup.LayoutParams.MATCH_PARENT,
                             ViewGroup.LayoutParams.MATCH_PARENT
                         ));
        }

        dialogView.setScaleX(0.8f);
        dialogView.setScaleY(0.8f);
        dialogView.setAlpha(0f);
        dialogView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    public void dismiss() {
        if (container != null && container.getParent() != null) {
            ((ViewGroup) container.getParent()).removeView(container);
        }
    }

    private class DialogView extends FrameLayout {
        private String titleText;
        private String messageText;
        private List<String> itemList;
        private String posText, negText, neuText;
        private OnItemClickListener itemClickListener;
        private OnButtonClickListener posListener, negListener, neuListener;
        private View customContentView;

        private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private RectF rect = new RectF();

        public DialogView(Context context) {
            super(context);
            setWillNotDraw(false);
            bgPaint.setStyle(Paint.Style.FILL);
            bgPaint.setColor(COLOR_BG);

            setClickable(true);
            setFocusable(true);
            setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 阻止穿透
                    }
                });
        }

        public void setTitle(String title) {
            this.titleText = title;
        }

        public void setMessage(String message) {
            this.messageText = message;
        }

        public void setItems(List<String> items, OnItemClickListener listener) {
            this.itemList = items;
            this.itemClickListener = listener;
        }

        public void setButtons(String pos, String neg, String neu,
                               OnButtonClickListener posL, OnButtonClickListener negL, OnButtonClickListener neuL) {
            this.posText = pos;
            this.negText = neg;
            this.neuText = neu;
            this.posListener = posL;
            this.negListener = negL;
            this.neuListener = neuL;
        }

        public void setCustomView(View view) {
            this.customContentView = view;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            rect.set(0, 0, w, h);
            canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, bgPaint);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            LinearLayout content = new LinearLayout(getContext());
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(28, 24, 28, 16);

            // 标题
            if (titleText != null && !titleText.isEmpty()) {
                TextView tvTitle = new TextView(getContext());
                tvTitle.setText(titleText);
                tvTitle.setTextColor(COLOR_TEXT);
                tvTitle.setTextSize(18);
                tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
                tvTitle.setPadding(0, 0, 0, 12);
                content.addView(tvTitle);
            }

            // 消息
            if (messageText != null && !messageText.isEmpty()) {
                TextView tvMsg = new TextView(getContext());
                tvMsg.setText(messageText);
                tvMsg.setTextColor(COLOR_TEXT);
                tvMsg.setTextSize(15);
                tvMsg.setPadding(0, 0, 0, 14);
                tvMsg.setLineSpacing(4, 1.2f);
                content.addView(tvMsg);
            }

            // 自定义视图
            if (customContentView != null) {
                content.addView(customContentView);
            }

            // 列表项
            if (itemList != null && !itemList.isEmpty()) {
                for (int i = 0; i < itemList.size(); i++) {
                    final int index = i;
                    MD3Button itemBtn = new MD3Button(getContext(), itemList.get(i));
                    itemBtn.setCornerRadius(BTN_CORNER);
                    itemBtn.setColors(BTN_BG, BTN_PRESSED, BTN_TEXT);
                    itemBtn.setFixedHeight(BTN_HEIGHT);
                    itemBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (itemClickListener != null) {
                                    itemClickListener.onItemClick(index);
                                }
                                dismiss();
                            }
                        });
                    LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    if (i < itemList.size() - 1) {
                        itemParams.setMargins(0, 0, 0, BTN_GAP);
                    } else {
                        itemParams.setMargins(0, 0, 0, 0);
                    }
                    content.addView(itemBtn, itemParams);
                }
            }

            // 按钮容器
            LinearLayout buttonContainer = new LinearLayout(getContext());
            buttonContainer.setOrientation(LinearLayout.VERTICAL);
            buttonContainer.setPadding(0, 0, 0, 0);

            boolean hasItemBefore = (itemList != null && !itemList.isEmpty());

            if (neuText != null) {
                MD3Button btnNeu = new MD3Button(getContext(), neuText);
                btnNeu.setCornerRadius(BTN_CORNER);
                btnNeu.setColors(BTN_BG, BTN_PRESSED, BTN_TEXT);
                btnNeu.setFixedHeight(BTN_HEIGHT);
                btnNeu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (neuListener != null) neuListener.onClick();
                            dismiss();
                        }
                    });
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                if (hasItemBefore) {
                    p.setMargins(0, BTN_GAP_LARGE, 0, 0);
                    hasItemBefore = false;
                } else {
                    p.setMargins(0, 0, 0, 0);
                }
                buttonContainer.addView(btnNeu, p);
            }

            if (negText != null) {
                MD3Button btnNeg = new MD3Button(getContext(), negText);
                btnNeg.setCornerRadius(BTN_CORNER);
                btnNeg.setColors(BTN_BG, BTN_PRESSED, BTN_TEXT);
                btnNeg.setFixedHeight(BTN_HEIGHT);
                btnNeg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (negListener != null) negListener.onClick();
                            dismiss();
                        }
                    });
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                if (buttonContainer.getChildCount() > 0) {
                    p.setMargins(0, BTN_GAP, 0, 0);
                } else {
                    if (hasItemBefore) {
                        p.setMargins(0, BTN_GAP_LARGE, 0, 0);
                        hasItemBefore = false;
                    } else {
                        p.setMargins(0, 0, 0, 0);
                    }
                }
                buttonContainer.addView(btnNeg, p);
            }

            if (posText != null) {
                MD3Button btnPos = new MD3Button(getContext(), posText);
                btnPos.setCornerRadius(BTN_CORNER);
                btnPos.setColors(BTN_BG, BTN_PRESSED, BTN_TEXT);
                btnPos.setFixedHeight(BTN_HEIGHT);
                btnPos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (posListener != null) posListener.onClick();
                            dismiss();
                        }
                    });
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                if (buttonContainer.getChildCount() > 0) {
                    p.setMargins(0, BTN_GAP, 0, 0);
                } else {
                    if (hasItemBefore) {
                        p.setMargins(0, BTN_GAP_LARGE, 0, 0);
                        hasItemBefore = false;
                    } else {
                        p.setMargins(0, 0, 0, 0);
                    }
                }
                buttonContainer.addView(btnPos, p);
            }

            if (posText != null || negText != null || neuText != null) {
                content.addView(buttonContainer);
            }

            int wSpec = MeasureSpec.makeMeasureSpec(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85),
                MeasureSpec.EXACTLY
            );
            int hSpec = MeasureSpec.makeMeasureSpec(
                (int) (context.getResources().getDisplayMetrics().heightPixels * 0.7),
                MeasureSpec.AT_MOST
            );
            content.measure(wSpec, hSpec);
            setMeasuredDimension(content.getMeasuredWidth(), content.getMeasuredHeight());

            removeAllViews();
            addView(content);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int which);
    }

    public interface OnButtonClickListener {
        void onClick();
    }
}
