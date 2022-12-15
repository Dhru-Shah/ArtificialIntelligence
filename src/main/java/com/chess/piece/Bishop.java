package com.chess.piece;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;
import com.chess.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a piece in chess called Bishop. It includes attributes and methods for moving, setting color,
 * and illegal moves
 * **/
public class Bishop extends Piece{

    // Assigning B for Bishop
    public static final String ANN = "B";

    // depending on which colour of the Bishop is chosen, the correct Bishop image will be assigned to the instanced object
    public Bishop(Color color) {
        super(color, PieceType.BISHOP.value, "/pieces/whitebishop.png", "/pieces/blackbishop.png", ANN);
    }

    // checks if the bishop is able to move in the way requested by human player or AI player
    @Override
    public boolean canMoveTo(Board board, Movement movement) {
        if (!movement.getDestination().isValid()){
            return false;
        }

        //  Check if the movement that bishop would like to move to is valid i.e. in the diagonals only
        final Position delta = movement.getDestination().add(movement.getOrigin().negate());
        if (Math.abs(delta.getX()) != Math.abs(delta.getY()) ||
            movement.getDestination().equals(movement.getOrigin())){
            return false;
        }

        final Position normalizedDelta = delta.clone().normalize();
        // Check if there are any pieces between the origin point and destination point
        for (int i = 1; i < Math.abs(movement.getDestination().getX() - movement.getOrigin().getX()); i++) {
            if (board.getBoard()[movement.getOrigin().getX() + normalizedDelta.getX() * i][movement.getOrigin().getY() + normalizedDelta.getY() * i] != null){
                return false;
            }
        }

        // Check if there is any enemy piece at the destination point
        Piece destinationPiece = board.getBoard()[movement.getDestination().getX()][movement.getDestination().getY()];
        return (destinationPiece == null || destinationPiece.getColor() != getColor());
    }

    // A list of all movements taken by the bishop to store and display
    @Override
    public List<Movement> getAllPossibleMovements(Board board, boolean autoCheck) {
        // Use the Movement constructor to create the movement in algebraic notation
        int [][] movesDirections = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        ArrayList<Movement> allMovements = new ArrayList<>();

        final Position origin = board.getPiecePosition(this);

        // Check if there is no piece on the board
        if (origin == null){
            return allMovements;
        }

        // checks if movement is valid and on the board
        for (int iLength = 1; iLength < 8; iLength++) {
            for (int iDir = 0; iDir < 4; iDir++) {
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
