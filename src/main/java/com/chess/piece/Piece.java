package com.chess.piece;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;

import java.util.List;

/**
 * This class represents pieces and their methods and attributes that can be inherited by specific
 * It includes attributes and methods for moving, setting color,
 * and illegal moves
 * **/
public abstract class Piece {

    // for the color of the pieces
    private Color color;

    // for value of each piece
    private int value;

    // path for the image of any white colored pieces
    private String imagePathWhite;

    // path for the image of any black colored pieces
    private String imagePathBlack;

    // for the position of a piece in algebraic notation when promoted for ex.
    private String algebraicNotationName;

    // whether the piece has moved or not
    private boolean hasMoved;

    // take in all the parameters of a piece
    public Piece(Color color, int value, String imagePathWhite, String imagePathBlack, String algebraicNotationName) {
        this.color = color;
        this.value = value;
        this.imagePathWhite = imagePathWhite;
        this.imagePathBlack = imagePathBlack;
        this.algebraicNotationName = algebraicNotationName;
    }

    // returns if a piece has moved or not
    public boolean isHasMoved() {
        return hasMoved;
    }

    // ignore
    public void setHasMoved(boolean hasMoved) {this.hasMoved = hasMoved;}

    // checks if movement of a piece is possible on the board
    public abstract boolean canMoveTo(final Board board, final Movement movement);

    // gets all possible movements on a board
    public abstract List<Movement> getAllPossibleMovements(final Board board, boolean autoCheck);

    // moves a piece and saves it
    public void move(final Board board, final Movement movement){
        board.getBoard()[movement.getDestination().getX()][movement.getDestination().getY()] = board.getBoard()[movement.getOrigin().getX()][movement.getOrigin().getY()];
        board.getBoard()[movement.getOrigin().getX()][movement.getOrigin().getY()] = null;
        hasMoved = true;
    }

    // gets the algebraic notation name
    public String getAlgebraicNotationName() {
        return algebraicNotationName;
    }

    // ignore
    public void setAlgebraicNotationName(String algebraicNotationName) {
        this.algebraicNotationName = algebraicNotationName;
    }

    // gets the color of the piece
    public Color getColor() {
        return color;
    }

    // gets the value of the piece
    public int getValue() {
        return value;
    }

    // sets the color of the piece
    public void setColor(Color color) {
        this.color = color;
    }

    // sets the value of the piece
    public void setValue(int value) {
        this.value = value;
    }

    // gets the image path of the white piece
    public String getImagePathWhite() {
        return imagePathWhite;
    }

    //ignore
    public void setImagePathWhite(String imagePathWhite) {
        this.imagePathWhite = imagePathWhite;
    }

    // sets the image path of the white piece
    public String getImagePathBlack() {
        return imagePathBlack;
    }

    //ignore
    public void setImagePathBlack(String imagePathBlack) {
        this.imagePathBlack = imagePathBlack;
    }
    // write the position and colour of the piece in algebraic notation
    @Override
    public String toString() {
        return algebraicNotationName + (Color.WHITE.equals(color) ? "W": "B");
    }
}
