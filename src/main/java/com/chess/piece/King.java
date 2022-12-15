package com.chess.piece;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;
import com.chess.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a piece in chess called King. It includes attributes and methods for moving, setting color,
 * castling, and illegal moves
 * **/
public class King extends Piece{

    // Assigning K for King
    public static final String ANN = "K";

    // depending on which colour of the King is chosen, the correct King image will be assigned to the instanced object
    public King(Color color) {
        super(color, PieceType.KING.value, "/pieces/whiteking.png", "/pieces/blackking.png", ANN);
    }


    // possible movements a king makes on a 2D grid: 1 square movements in general and 2 square movements for castling
    private Position [] moves = {
            new Position(1, -1),
            new Position(-1, -1),
            new Position(1, 1),
            new Position(-1, 1),
            new Position(0, 1),
            new Position(0, -1),
            new Position(1, 0),
            new Position(-1, 0),
            new Position(0, -2),
            new Position(0, 2),};

    // checks if the king is able to move in the way requested by human player or AI player including castling
    @Override
    public boolean canMoveTo(Board board, Movement movement) {

        final Position delta = movement.getDestination().add(movement.getOrigin().negate());

        /** Castling: It consists of moving one’s king two squares toward a rook on the same rank
        and then moving the rook to the square that the king passed over.
        */
        if (Math.abs(delta.getY()) == 2 && delta.getX() == 0){

            Piece rook = null;

            if (delta.getY() < 0){
                rook = board.getBoard()[movement.getDestination().getX()][0];
            }else{
                rook = board.getBoard()[movement.getDestination().getX()][Board.BOARD_SIZE - 1];
            }

            // If the king or rook have been moved at all, then we cannot castle
            if (isHasMoved() || rook == null || rook.isHasMoved()){
                return false;
            }

            final Position normalizedDelta = delta.clone().normalize();
            // making sure there exists no piece in between the King and Rook
            for (int i = 1; i < (delta.getY() < 0 ? 4 : 3); i++) {
                if (board.getBoard()[movement.getOrigin().getX() + normalizedDelta.getX() * i][movement.getOrigin().getY() + normalizedDelta.getY() * i] != null) {
                    return false;
                }
            }

            return true;

        }

        boolean isValidMovement = false;
        for (int i = 0; i < moves.length && !isValidMovement; i++) {
            isValidMovement |= delta.equals(moves[i]);
        }
        if (!isValidMovement){
            return false;
        }

        // the destination selected can either be empty or have an opponent's piece
        Piece piece = board.getBoard()[movement.getDestination().getX()][movement.getDestination().getY()];
        return (piece == null || piece.getColor() != getColor());
    }

    // Creates a list of all valid movements that can be taken by the king from its current position including the avoidance of a check
    @Override
    public List<Movement> getAllPossibleMovements(Board board, boolean autoCheck) {
        ArrayList<Movement> allMovements = new ArrayList<>();

        final Position origin = board.getPiecePosition(this);

        if (origin == null){
            // if the piece is not on the board
            return allMovements;
        }

        // checking if the movement is valid and can be performed by the king and does not cause a check
        for (int iDir = 0; iDir < moves.length; iDir++) {
            final Position potentialPosition = new Position(origin.getX() + moves[iDir].getX(), origin.getY() + moves[iDir].getY());
            Movement newMovement = new Movement(board, "" + origin + potentialPosition);
            if (newMovement.isValid() && board.isValid(newMovement, autoCheck)){
                allMovements.add(newMovement);
            }
        }
        return allMovements;
    }

    // This method allows for movement of both the Rook and the King for castling
    // by checking if no enemy piece has been in the middle of the king and rook as this is a necessary requirement
    @Override
    public void move(Board board, Movement movement) {
        super.move(board, movement);
        final Position delta = movement.getDestination().add(movement.getOrigin().negate());
        if (Math.abs(delta.getY()) == 2 && delta.getX() == 0){
            Piece rook = null;
            if (delta.getY() < 0){
                rook = board.getBoard()[movement.getDestination().getX()][0];
            }else{
                rook = board.getBoard()[movement.getDestination().getX()][Board.BOARD_SIZE - 1];
            }

            if (rook == null){ // this shouldn't happen
                return;
            }

            Position rookPosition = board.getPiecePosition(rook);
            final Movement rookMovement = new Movement(board, "" + rookPosition + new Position(movement.getDestination().getX(), movement.getDestination().getY() - delta.getY() / 2));
            rook.move(board, rookMovement);
        }
    }
}
