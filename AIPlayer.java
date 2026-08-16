package com.iwaszy.toolapp;



import java.util.ArrayList;
import java.util.Random;

public class AIPlayer {
    public static final int EASY = 0;
    public static final int MEDIUM = 1;
    public static final int HARD = 2;
    public static final int MASTER = 3;

    private Board board;
    private int difficulty;
    private Random random = new Random();
    private int myColor = Board.WHITE;
    private int enemyColor = Board.BLACK;

    public AIPlayer(Board board) {
        this.board = board;
        this.difficulty = EASY;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int[] getBestMove() {
        int[][] grid = board.getGrid();

        switch (difficulty) {
            case EASY: return getEasyMove(grid);
            case MEDIUM: return getMediumMove(grid);
            case HARD: return getHardMove(grid);
            case MASTER: return getMasterMove(grid);
            default: return getEasyMove(grid);
        }
    }

    private int[] getEasyMove(int[][] grid) {
        int[] threat = findBiggestThreat(grid);
        if (threat != null && random.nextFloat() < 0.5f) return threat;
        return getRandomMove(grid);
    }

    private int[] getMediumMove(int[][] grid) {
        return getScoredMove(grid, false);
    }

    private int[] getHardMove(int[][] grid) {
        return getScoredMove(grid, true);
    }

    private int[] getMasterMove(int[][] grid) {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                if (grid[i][j] == Board.EMPTY) {
                    grid[i][j] = myColor;
                    if (board.checkWin(i, j, myColor)) {
                        grid[i][j] = Board.EMPTY;
                        return new int[]{i, j};
                    }
                    grid[i][j] = Board.EMPTY;
                }
            }
        }
        return getScoredMove(grid, true);
    }

    private int[] getScoredMove(int[][] grid, boolean aggressive) {
        int maxScore = -1;
        int bestRow = Board.SIZE / 2;
        int bestCol = Board.SIZE / 2;

        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                if (grid[i][j] != Board.EMPTY) continue;

                int myScore = evaluatePosition(grid, i, j, myColor);
                int enemyScore = evaluatePosition(grid, i, j, enemyColor);

                int total = aggressive ? myScore * 2 + enemyScore : myScore + enemyScore;

                if (total > maxScore) {
                    maxScore = total;
                    bestRow = i;
                    bestCol = j;
                }
            }
        }
        return new int[]{bestRow, bestCol};
    }

    private int evaluatePosition(int[][] grid, int row, int col, int color) {
        int score = 0;
        int[][] directions = {{1,0}, {0,1}, {1,1}, {1,-1}};

        for (int[] d : directions) {
            int count = 1;
            int openLeft = 0, openRight = 0;

            for (int dir = -1; dir <= 1; dir += 2) {
                for (int step = 1; step < 5; step++) {
                    int r = row + d[0] * step * dir;
                    int c = col + d[1] * step * dir;
                    if (r < 0 || r >= Board.SIZE || c < 0 || c >= Board.SIZE) break;
                    if (grid[r][c] == color) {
                        count++;
                    } else if (grid[r][c] == Board.EMPTY) {
                        if (dir == -1) openLeft++;
                        else openRight++;
                        break;
                    } else {
                        break;
                    }
                }
            }

            if (count >= 5) score += 100000;
            else if (count == 4) {
                if (openLeft > 0 && openRight > 0) score += 30000;
                else if (openLeft > 0 || openRight > 0) score += 1000;
            } else if (count == 3) {
                if (openLeft > 0 && openRight > 0) score += 500;
                else if (openLeft > 0 || openRight > 0) score += 50;
            } else if (count == 2) {
                if (openLeft > 0 && openRight > 0) score += 10;
                else if (openLeft > 0 || openRight > 0) score += 2;
            }
        }
        return score;
    }

    private int[] findBiggestThreat(int[][] grid) {
        int maxScore = 0;
        int bestRow = -1, bestCol = -1;

        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                if (grid[i][j] != Board.EMPTY) continue;
                int score = evaluatePosition(grid, i, j, enemyColor);
                if (score > maxScore) {
                    maxScore = score;
                    bestRow = i;
                    bestCol = j;
                }
            }
        }
        if (maxScore > 50) return new int[]{bestRow, bestCol};
        return null;
    }

    private int[] getRandomMove(int[][] grid) {
        ArrayList<int[]> empty = new ArrayList<>();
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                if (grid[i][j] == Board.EMPTY) empty.add(new int[]{i, j});
            }
        }
        if (empty.isEmpty()) return new int[]{-1, -1};
        return empty.get(random.nextInt(empty.size()));
    }
}
