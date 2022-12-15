package com.chess.piece;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;
import com.chess.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a piece in chess called Rook. It includes attributes and methods for moving, setting color,
 * and illegal moves
 * **/
public class Rook extends Piece{

    // Assigning R for Rook
    public static final String ANN = "R";

    // depending on which colour of the Rook is chosen, the correct Rook image will be assigned to the instanced object
    public Rook(Color color) {
        super(color, PieceType.ROOK.value, "/pieces/whiterook.png", "/pieces/blackrook.png", ANN);


    }
    // checks if the rook is able to move in the way requested by human player or AI player
    @Override
    public boolean canMoveTo(Board board, Movement movement) {
        // check that the movement requested is legal/correct
        final Position delta = movement.getDestination().add(movement.getOrigin().negate());
        if (movement.getDestination().equals(movement.getOrigin()) ||
                !(delta.getX() == 0 || delta.getY() == 0)     ){
            return false;
        }
        final Position normalizedDelta = delta.clone().normalize();
        // make sure there is no piece in the middle of origin and destination point
        for (int i = 1; i < Math.max(Math.abs(delta.getX()), Math.abs(delta.getY())); i++) {
            if (board.getBoard()[movement.getOrigin().getX() + normalizedDelta.getX() * i][movement.getOrigin().getY() + normalizedDelta.getY() * i] != null){
                return false;
            }
        }

        // if there is any piece in the destination, it should be an enemy piece
        Piece destinationPiece = board.getBoard()[movement.getDestination().getX()][movement.getDestination().getY()];
        return (destinationPiece == null || destinationPiece.getColor() != getColor());
    }

    //A list of all movements taken by the rook to store and display
    @Override
    public List<Movement> getAllPossibleMovements(Board board, boolean autoCheck) {
        int [][] movesDirections = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};
        ArrayList<Movement> allMovements = new ArrayList<>();

        final Position origin = board.getPiecePosition(this);

        if (origin == null){
            // if the piece is not on the board
            return allMovements;
        }

        // checks if movement is valid and on the board
        for (int iLength = 1; iLength < 8; iLength++) {
            for (int iDir = 0; iDir < movesDirections.length; iDir++) {
                final Position potentialPosition = new Position(origin.getX() + movesDirections[iDir][0] * iLength, origin.getY() + movesDirections[iDir][1] * iLength);
                Movement newMovement = new Movement(board, "" + origin + potentialPosition);
                if (newMovement.isValid() && board.isValid(newMovement, autoCheck)){
                    allMovements.add(newMovement);
                }
            }
        }
        return allMovements;
    }

}
