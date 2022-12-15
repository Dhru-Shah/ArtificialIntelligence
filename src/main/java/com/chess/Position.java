package com.chess;

import javafx.geometry.Pos;
/**
 * This class represents the position that pieces can be in the game of chess.
 * It includes attributes and methods for position in the 2D array, validity of movement on the 8x8 chess board, and comparators
 * **/
public class Position implements Comparable<Position>{
    private int x, y;

    // the row and column of the chess board to determine positioning of each piece as well as movement
    public Position(String chessNotation) {
        y = chessNotation.toCharArray()[0] - 'a';
        x = chessNotation.toCharArray()[1] - '1';
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // valid movement stays inside the 8x8 board
    public boolean isValid(){
        return x >= 0 && y >= 0 && x < 8 && y < 8;
    }

    // add the position in string format
    @Override
    public String toString() {
        return "" + ((char)(y + 'A')) + ((char)(x + '1'));
    }

    // when moving a piece new position will add
    public Position add(final Position other){
        return new Position(other.getX() + getX(), other.getY() + getY());
    }

    // when moving a piece old position will be removed
    public Position negate(){
        return new Position(-x, -y);
    }

    public Position multiply(final int n){
        return new Position(n * getX(), n * getY());
    }

    // absolute value to normalize the position because negative values may occur due to movement
    public Position normalize(){
        if (x != 0) {
            x = x / Math.abs(x);
        }
        if (y != 0) {
            y = y / Math.abs(y);
        }
        return this;
    }

    @Override
    public Position clone() {
        return new Position(x, y);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (x != position.x) return false;
        return y == position.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public int compareTo(Position position) {
        if (x != position.getX()){
            return x - position.getX();
        }
        return y - position.getY();
    }
}
