package com.iwaszy.toolapp;

public class Board {
    public static final int SIZE = 20;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;

    private int[][] grid = new int[SIZE][SIZE];
    private int lastRow = -1, lastCol = -1;

    public Board() {
        clear();
    }

    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = EMPTY;
            }
        }
        lastRow = -1;
        lastCol = -1;
    }

    public boolean placePiece(int row, int col, int player) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return false;
        if (grid[row][col] != EMPTY) return false;
        grid[row][col] = player;
        lastRow = row;
        lastCol = col;
        return true;
    }

    public int getPiece(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return EMPTY;
        return grid[row][col];
    }

    public boolean checkWin(int row, int col, int player) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return false;
        if (grid[row][col] != player) return false;

        int[][] directions = {{1,0}, {0,1}, {1,1}, {1,-1}};
        for (int[] d : directions) {
            int count = 1;
            for (int dir = -1; dir <= 1; dir += 2) {
                for (int step = 1; step < 5; step++) {
                    int r = row + d[0] * step * dir;
                    int c = col + d[1] * step * dir;
                    if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) break;
                    if (grid[r][c] != player) break;
                    count++;
                }
            }
            if (count >= 5) return true;
        }
        return false;
    }

    public boolean isFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == EMPTY) return false;
            }
        }
        return true;
    }

    public int getLastRow() { return lastRow; }
    public int getLastCol() { return lastCol; }
    public int[][] getGrid() { return grid; }
}
