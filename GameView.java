package com.iwaszy.toolapp;


import android.graphics.Typeface;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.app.AlertDialog;

public class GameView extends FrameLayout {
    private static final int COLOR_SURFACE = 0xFFF5F0EB;
    private static final int COLOR_ON_SURFACE = 0xFF1C1B1F;

    private Board board;
    private AIPlayer ai;
    private int currentPlayer = Board.BLACK;
    private boolean gameOver = false;
    private boolean isAiThinking = false;

    private float cellSize;
    private float offsetX, offsetY;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private TextView statusText;
    private MD3Button difficultyBtn;
    private MD3Button firstMoveBtn;
    private MD3Button moreBtn;
    private MD3Button restartBtn;
    private String[] difficultyNames = {"简单", "中等", "困难", "大师"};
    private String[] firstMoveNames = {"玩家先手", "AI先手"};
    private int currentDifficulty = AIPlayer.EASY;
    private boolean isPlayerFirst = true;

    public GameView(Context context) {
        super(context);
        board = new Board();
        ai = new AIPlayer(board);
        ai.setDifficulty(currentDifficulty);

        setBackgroundColor(COLOR_SURFACE);
        setFocusable(true);

        // 状态文字
        statusText = new TextView(context);
        statusText.setText("⚫ 黑棋走");
        statusText.setTextColor(COLOR_ON_SURFACE);
        statusText.setTextSize(22);
        statusText.setPadding(20, 20, 20, 20);
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statusParams.topMargin = 10;
        statusParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        addView(statusText, statusParams);

        // ===== 左侧按钮 =====

        difficultyBtn = new MD3Button(context, "简单 ▼");
        difficultyBtn.setCornerRadius(20f);
        difficultyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDifficultyDialog();
                }
            });
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnParams.topMargin = 20;
        btnParams.leftMargin = 20;
        btnParams.gravity = Gravity.TOP | Gravity.LEFT;
        addView(difficultyBtn, btnParams);

        firstMoveBtn = new MD3Button(context, "玩家先手");
        firstMoveBtn.setCornerRadius(20f);
        firstMoveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFirstMoveDialog();
                }
            });
        FrameLayout.LayoutParams firstParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        firstParams.topMargin = 118;
        firstParams.leftMargin = 20;
        firstParams.gravity = Gravity.TOP | Gravity.LEFT;
        addView(firstMoveBtn, firstParams);

        // ===== 右侧按钮 =====

        moreBtn = new MD3Button(context, "更多");
        moreBtn.setCornerRadius(20f);
        moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMoreDialog();
                }
            });
        FrameLayout.LayoutParams moreParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        moreParams.topMargin = 20;
        moreParams.rightMargin = 20;
        moreParams.gravity = Gravity.TOP | Gravity.RIGHT;
        addView(moreBtn, moreParams);

        restartBtn = new MD3Button(context, "重开一把");
        restartBtn.setCornerRadius(20f);
        restartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartGame();
                }
            });
        FrameLayout.LayoutParams restartParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        restartParams.topMargin = 118;
        restartParams.rightMargin = 20;
        restartParams.gravity = Gravity.TOP | Gravity.RIGHT;
        addView(restartBtn, restartParams);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int topPadding = 210;
        int size = Math.min(w, h - topPadding);
        size = (int)(size * 0.9f);
        cellSize = size / (float)(Board.SIZE - 1);
        offsetX = (w - size) / 2f;
        offsetY = (h - size) / 2f + topPadding / 2f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameOver || isAiThinking) return true;
            if (currentPlayer != Board.BLACK) return true;

            float x = event.getX() - offsetX;
            float y = event.getY() - offsetY;
            int col = Math.round(x / cellSize);
            int row = Math.round(y / cellSize);

            if (row < 0 || row >= Board.SIZE || col < 0 || col >= Board.SIZE) return true;
            if (board.getPiece(row, col) != Board.EMPTY) return true;

            board.placePiece(row, col, Board.BLACK);
            postInvalidate();

            if (board.checkWin(row, col, Board.BLACK)) {
                gameOver = true;
                statusText.setText("🎉 你赢了！");
                showGameOverDialog("你赢了！ 🎉", "太棒了！");
                return true;
            } else if (board.isFull()) {
                gameOver = true;
                statusText.setText("🤝 平局！");
                showGameOverDialog("平局！", "旗鼓相当！");
                return true;
            }

            currentPlayer = Board.WHITE;
            statusText.setText("⚪ AI思考中...");
            postInvalidate();
            doAiMove();
        }
        return true;
    }

    private void doAiMove() {
        isAiThinking = true;
        postDelayed(new Runnable() {
                @Override
                public void run() {
                    int[] move = ai.getBestMove();
                    if (move[0] < 0 || move[1] < 0) {
                        isAiThinking = false;
                        return;
                    }

                    board.placePiece(move[0], move[1], Board.WHITE);
                    postInvalidate();

                    if (board.checkWin(move[0], move[1], Board.WHITE)) {
                        gameOver = true;
                        statusText.setText("🤖 AI赢了");
                        showGameOverDialog("AI赢了 🤖", "再接再厉！");
                        isAiThinking = false;
                        return;
                    } else if (board.isFull()) {
                        gameOver = true;
                        statusText.setText("🤝 平局！");
                        showGameOverDialog("平局！", "旗鼓相当！");
                        isAiThinking = false;
                        return;
                    }

                    currentPlayer = Board.BLACK;
                    statusText.setText("⚫ 黑棋走");
                    isAiThinking = false;
                    postInvalidate();
                }
            }, 350);
    }

    private void startAiFirst() {
        isAiThinking = true;
        statusText.setText("⚪ AI思考中...");
        postDelayed(new Runnable() {
                @Override
                public void run() {
                    int[] move = ai.getBestMove();
                    if (move[0] < 0 || move[1] < 0) {
                        isAiThinking = false;
                        return;
                    }

                    board.placePiece(move[0], move[1], Board.WHITE);
                    currentPlayer = Board.BLACK;
                    statusText.setText("⚫ 黑棋走");
                    isAiThinking = false;
                    postInvalidate();
                }
            }, 500);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPieces(canvas);
        drawLastMove(canvas);
    }

    private void drawBoard(Canvas canvas) {
        float boardSize = (Board.SIZE - 1) * cellSize;
        float padding = cellSize * 0.6f;
        float left = offsetX - padding;
        float top = offsetY - padding;
        float right = offsetX + boardSize + padding;
        float bottom = offsetY + boardSize + padding;

        paint.setColor(0xFFE8D5B7);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(left, top, right, bottom, 24, 24, paint);

        paint.setColor(0xFF5D4037);
        paint.setStrokeWidth(1.8f);
        for (int i = 0; i < Board.SIZE; i++) {
            float x = offsetX + i * cellSize;
            float y = offsetY + i * cellSize;
            canvas.drawLine(offsetX, y, offsetX + boardSize, y, paint);
            canvas.drawLine(x, offsetY, x, offsetY + boardSize, paint);
        }

        paint.setColor(0xFF5D4037);
        paint.setStyle(Paint.Style.FILL);
        int[][] stars = {{9,9}, {3,3}, {3,16}, {16,3}, {16,16}};
        for (int[] s : stars) {
            canvas.drawCircle(offsetX + s[1] * cellSize, offsetY + s[0] * cellSize, 5, paint);
        }
    }

    private void drawPieces(Canvas canvas) {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                int piece = board.getPiece(i, j);
                if (piece == Board.EMPTY) continue;

                float x = offsetX + j * cellSize;
                float y = offsetY + i * cellSize;
                float radius = cellSize * 0.42f;

                if (piece == Board.BLACK) {
                    paint.setColor(0xFF1C1B1F);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x, y, radius, paint);
                    paint.setColor(0x44FFFFFF);
                    canvas.drawCircle(x - radius * 0.25f, y - radius * 0.3f, radius * 0.3f, paint);
                } else {
                    paint.setColor(0xFFFFFFFF);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x, y, radius, paint);
                    paint.setColor(0xFF79747E);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1.5f);
                    canvas.drawCircle(x, y, radius, paint);
                    paint.setColor(0x88FFFFFF);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(x - radius * 0.2f, y - radius * 0.25f, radius * 0.25f, paint);
                }
            }
        }
    }

    private void drawLastMove(Canvas canvas) {
        int row = board.getLastRow();
        int col = board.getLastCol();
        if (row < 0 || col < 0) return;
        float x = offsetX + col * cellSize;
        float y = offsetY + row * cellSize;
        paint.setColor(0xFFE53935);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, cellSize * 0.12f, paint);
    }

    // ===== MD3Dialog =====

    private void showDifficultyDialog() {
        new MD3Dialog(getContext())
            .setTitle("选择难度")
            .setItems(difficultyNames, new MD3Dialog.OnItemClickListener() {
                @Override
                public void onItemClick(int which) {
                    currentDifficulty = which;
                    ai.setDifficulty(which);
                    difficultyBtn.setText(difficultyNames[which] + " ▼");
                    restartGame();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void showFirstMoveDialog() {
        new MD3Dialog(getContext())
            .setTitle("选择先手")
            .setItems(firstMoveNames, new MD3Dialog.OnItemClickListener() {
                @Override
                public void onItemClick(int which) {
                    isPlayerFirst = (which == 0);
                    firstMoveBtn.setText(firstMoveNames[which]);
                    restartGame();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void showMoreDialog() {
        final String[] items = {"介绍", "关于作者", "帮助"};
        new MD3Dialog(getContext())
            .setTitle("更多选项")
            .setItems(items, new MD3Dialog.OnItemClickListener() {
                @Override
                public void onItemClick(int which) {
                    switch (which) {
                        case 0:
                            showHelpDialog();
                            break;
                        case 1:
                            showAboutDialog();
                            break;
                        case 2:
                            showSettingsDialog();
                            break;
                    }
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void showAboutDialog() {
        // 创建自定义布局
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(0, 0, 0, 0);

        // 图片
        ImageView img = new ImageView(getContext());
        img.setImageResource(R.drawable.author_avatar);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int size = (int) (getContext().getResources().getDisplayMetrics().density * 160);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(size, size);
        p.setMargins(0, 0, 0, 16);
        img.setLayoutParams(p);
        layout.addView(img);

        // 作者名字
        TextView nameText = new TextView(getContext());
        nameText.setText("觉得好玩的话，可以支持一下哦！\nbuy me a coffee☕️ 😭");
        nameText.setTextColor(0xFF5D4037);
        nameText.setTextSize(20);
        nameText.setTypeface(Typeface.DEFAULT_BOLD);
        nameText.setGravity(Gravity.CENTER);
        nameText.setPadding(0, 0, 0, 4);
        layout.addView(nameText);

        // 描述信息
        TextView descText = new TextView(getContext());
        descText.setText("开发者：iwaszy\n抖音&bilibili@iwaszy\n有想法和建议\n🉑进粉丝群787956274\n大家可以一起玩口牙");
        descText.setTextColor(0xFF5D4037);
        descText.setTextSize(15);
        descText.setGravity(Gravity.CENTER);
        descText.setLineSpacing(4, 1.2f);
        layout.addView(descText);

        // 使用 MD3Dialog + 自定义视图
        new MD3Dialog(getContext())
            .setTitle("关于作者")
            .setCustomView(layout)
    .setPositiveButton("确定", null)
    .show();
    }

    private void showSettingsDialog() {
        new MD3Dialog(getContext())
            .setTitle("帮助")
            .setMessage("玩法说明：\n\n1. 点击棋盘交叉点落子\n2. 黑棋先走\n3. 五子连珠即获胜\n4. 左上角可调整难度和先手")
            .setPositiveButton("确定", null)
            .show();
    }

    private void showHelpDialog() {
        new MD3Dialog(getContext())
            .setTitle("介绍")
            .setMessage("这是一款基于 AI 的五子棋对战游戏\n\n采用 20x20 棋盘\n提供四档难度可调\n简洁的 Material Design 风格")
            .setPositiveButton("确定", null)
            .show();
    }

    private void showGameOverDialog(String title, String message) {
        new MD3Dialog(getContext())
            .setTitle(title)
            .setMessage(message + "\n\n点击「重开一把」继续")
            .setPositiveButton("重开一把", new MD3Dialog.OnButtonClickListener() {
                @Override
                public void onClick() {
                    restartGame();
                }
            })
            .show();
    }

    public void restartGame() {
        board.clear();
        gameOver = false;
        isAiThinking = false;

        if (isPlayerFirst) {
            currentPlayer = Board.BLACK;
            statusText.setText("⚫ 黑棋走");
        } else {
            currentPlayer = Board.WHITE;
            statusText.setText("⚪ AI先手");
            startAiFirst();
        }
        postInvalidate();
    }
}
