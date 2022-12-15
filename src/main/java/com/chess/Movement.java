package com.chess;

import com.chess.piece.*;

import java.util.Objects;

/**
 * This class represents the movement that pieces can take in the game of chess.
 * It includes attributes and methods for pieces, positions, destination, origin, capturing, validity of turns,
 * promotion, castling and enPassant
 * **/
public class Movement {
    public static final String INVALID_TURN = "INV";
    private Piece piece;
    private Position origin;
    private Position destination;

    private boolean isCapture;

    private boolean isValid = true;

    private boolean isEnd = false;

    // if invalid turn occurs then end the movement
    public Movement(Board board, String movement) {
        if (INVALID_TURN.equals(movement)){
            isEnd = true;
            return;
        }

        // check castling
        if (movement == null || movement.length() < 4) {
            isValid = false;
            return;
        }
        movement = movement.toLowerCase();
        origin = new Position(movement.substring(0, 2));
        destination = new Position(movement.substring(2, 4));
        if (!origin.isValid() || !destination.isValid()){ // both positions are into the board
            isValid = false;
            return;
        }
        this.piece = board.getBoard()[origin.getX()][origin.getY()];
        Piece destinationPiece = board.getBoard()[destination.getX()][destination.getY()];
        if (this.piece == null){
            isValid = false;
            return;
        }

        // checks for promotion of pawn to Bishop, Knight, Queen or Rook and creates their instance to replace the pawn
        if (movement.length() == 5 && piece instanceof Pawn && (
                (!(piece.getColor().equals(Color.WHITE) || destination.getY() == 7) ) ||
                        !(piece.getColor().equals(Color.BLACK) || destination.getY() == 0) )){ // we are handling a promotion
            isPromoted = true;
            String promotedToChar = Character.toUpperCase(movement.charAt(4)) + "";

            if (promotedToChar.equals(Bishop.ANN)){
                promotedTo = new Bishop(piece.getColor());
            }else if (promotedToChar.equals(Knight.ANN)){
                promotedTo = new Knight(piece.getColor());
            }else if (promotedToChar.equals(Queen.ANN)){
                promotedTo = new Queen(piece.getColor());
            }else if (promotedToChar.equals(Rook.ANN)){
                promotedTo = new Rook(piece.getColor());
            }
        }

        // checks for enPassant and allows for capturing through the method
        isCapture = board.getBoard()[destination.getX()][destination.getY()] != null ||
                (this.piece instanceof Pawn && !board.getPreviousMovements().isEmpty() && ((Pawn) this.piece).isEnPassant(this, board.getPreviousMovements().get(board.getPreviousMovements().size() - 1)));
    }

    public boolean isEnd() {
        return isEnd;
    }

    private boolean isPromoted;
    private Piece promotedTo;

    public boolean isValid() {
        return isValid;
    }

    public boolean isCapture() {
        return isCapture;
    }

    public void setCapture(boolean capture) {
        isCapture = capture;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Position getOrigin() {
        return origin;
    }

    public void setOrigin(Position origin) {
        this.origin = origin;
    }

    public Position getDestination() {
        return destination;
    }

    public void setDestination(Position destination) {
        this.destination = destination;
    }

    public boolean isPromoted() {
        return isPromoted;
    }

    public void setPromoted(boolean promoted) {
        isPromoted = promoted;
    }

    public Piece getPromotedTo() {
        return promotedTo;
    }

    public void setPromotedTo(Piece promotedTo) {
        this.promotedTo = promotedTo;
    }

    // prints the promotion occurring as a string
    @Override
    public String toString() {
        if (getDestination() != null) {
            int deltaY = getDestination().getY() - getOrigin().getY();
            if (getPiece() instanceof King && Math.abs(deltaY) == 2) {
                if (deltaY < 0) {
                    return "o-o-o";
                } else {
                    return "o-o";
                }
            }
        }
        return origin + (isCapture? "x" : "-") + destination + (isPromoted ? promotedTo.getAlgebraicNotationName() : "");
    }

    public String toStringSimple(){
        return "" + origin + destination + (isPromoted ? promotedTo.getAlgebraicNotationName() : "");
    }

    @Override
    public boolean equals(Object o) {
        return toString().equals(o.toString());
    }

}
