package com.chess.piece;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;
import com.chess.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a piece in chess called Pawn. It includes attributes and methods for moving, setting color,
 * and illegal moves
 * **/
public class Pawn extends Piece{

    // Assigning P for Pawn
    public static final String ANN = "P";

    private int deltaX;

    // depending on which colour of the Pawn is chosen, the correct Pawn image will be assigned to the instanced object
    public Pawn(Color color) {
        super(color, PieceType.PAWN.value, "/pieces/whitepawn.png", "/pieces/blackpawn.png", ANN);
        deltaX = getColor() == Color.WHITE ? 1 : -1;
    }

    // checks if the pawn is able to move in the way requested by human player or AI player
    @Override
    public boolean canMoveTo(Board board, Movement movement) {

        // moves the pawn forward by 1 step
        Piece destinationPiece = board.getBoard()[movement.getDestination().getX()][movement.getDestination().getY()];
        if (movement.getDestination().getX() - movement.getOrigin().getX() == deltaX &&
            movement.getDestination().getY() - movement.getOrigin().getY() == 0 &&
            destinationPiece == null){
            return true;
        }
        // moves the pawn forward by 2 steps
        if (((getColor() == Color.WHITE && movement.getOrigin().getX() == 1) ||
                (getColor() == Color.BLACK && movement.getOrigin().getX() == 6)) &&
                movement.getDestination().getX() - movement.getOrigin().getX() == deltaX * 2 &&
                movement.getDestination().getY() - movement.getOrigin().getY() == 0 &&
                board.getBoard()[movement.getDestination().getX() - deltaX][movement.getDestination().getY()] == null &&
                destinationPiece == null) {
            return true;
        }
        // captures opponent piece if movement is diagonally and piece exists in destination location
        if (isPawnCapture(movement, destinationPiece)){
            // good capture
            return true;
        }
        // checks for the board and pawn position for whether en passant is possible
        Movement previousMovement = null;
        if (!board.getPreviousMovements().isEmpty()){
            previousMovement = board.getPreviousMovements().get(board.getPreviousMovements().size() - 1);
        }
        if (previousMovement != null){
            if (isEnPassant(movement, previousMovement)){
                return true;
            }
        }
        return false;
    }

    // performs en passant where player 1 pawn moves 2 steps and ends up beside player 2 pawn, player 2 is able
    // to then capture the player 1 pawn by diagonally performing en passant right behind the player 1 pawn
    public boolean isEnPassant(Movement movement, Movement previousMovement) {
        if (previousMovement == null){
            return false;
        }
        Piece previousPiece = previousMovement.getPiece();
        return previousPiece instanceof Pawn &&
                previousMovement.getDestination().getX() == movement.getOrigin().getX() && // new pawn is behind the old one
                Math.abs(previousMovement.getDestination().getY() - movement.getDestination().getY()) == 0 && // it is next to it before movement
                Math.abs(previousMovement.getDestination().getX() - movement.getDestination().getX()) == 1 && // it is next to it before movement
                movement.getDestination().getX() - movement.getOrigin().getX() == deltaX && // it is moving in the correct direction
                Math.abs(previousMovement.getDestination().getX() - previousMovement.getOrigin().getX()) == 2 &&  // previous movement was 2 space
                !previousPiece.getColor().equals(getColor()) // pawn has a different color
                ;

    }

    // checks if pawn is captured
    public boolean isPawnCapture(Movement movement, Piece destinationPiece) {
        return movement.getDestination().getX() - movement.getOrigin().getX() == deltaX &&
                Math.abs(movement.getDestination().getY() - movement.getOrigin().getY()) == 1 &&
                destinationPiece != null && destinationPiece.getColor() != getColor();
    }


     /** A list of all movements taken by the pawn to store and display them, including promotion, and en passant
     * **/
    @Override
    public List<Movement> getAllPossibleMovements(Board board, boolean autoCheck) {
        int [][] movesDirections = {{deltaX, 0}, {deltaX * 2, 0}, {deltaX, 1}, {deltaX, -1}};
        ArrayList<Movement> allMovements = new ArrayList<>();

        final Position origin = board.getPiecePosition(this);

        if (origin == null){
            // Check if there is no piece on the board
            return allMovements;
        }

        // checks if movement is valid and on the board and if it is a promotion, then possible promotion to queen,
        // rook, bishop or knight
        for (int iDir = 0; iDir < movesDirections.length; iDir++) {
            final Position potentialPosition = new Position(origin.getX() + movesDirections[iDir][0], origin.getY() + movesDirections[iDir][1]);
            Movement newMovement = new Movement(board, "" + origin + potentialPosition);
            if (newMovement.isValid() && board.isValid(newMovement, autoCheck)){
                if ((board.getTurn() == Color.WHITE.ordinal() && newMovement.getDestination().getX() == Board.BOARD_SIZE - 1) ||
                        (board.getTurn() == Color.BLACK.ordinal() && newMovement.getDestination().getX() == 0)){
                    String movementString = newMovement.toStringSimple();
                    allMovements.add(new Movement(board, movementString + Queen.ANN));
                    allMovements.add(new Movement(board, movementString + Rook.ANN));
                    allMovements.add(new Movement(board, movementString + Bishop.ANN)); // all possible promotions
                    allMovements.add(new Movement(board, movementString + Knight.ANN));
                }else {
                    allMovements.add(newMovement);
                }
            }
        }
        return allMovements;
    }

    // moves a piece based on which type of movement it is: en passant, initial 2 step, promotion, capture or initial 1 step
    @Override
    public void move(Board board, Movement movement) {
        Movement previousMovement = null;
        if (!board.getPreviousMovements().isEmpty()){
            previousMovement = board.getPreviousMovements().get(board.getPreviousMovements().size() - 1);
        }
        boolean isEnPassant = isEnPassant(movement, previousMovement);

        super.move(board, movement);

        // moves based on enPassant
        if (isEnPassant){
            board.getBoard()[previousMovement.getDestination().getX()][previousMovement.getDestination().getY()] = null;
        }
        // moves based on promotion
        if (movement.isPromoted()){
            board.getBoard()[movement.getDestination().getX()][movement.getDestination().getY()] = movement.getPromotedTo();
        }
    }
}
