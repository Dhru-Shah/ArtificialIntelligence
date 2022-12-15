package com.chess.piece;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;
import com.chess.Position;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a piece in chess called Queen. It includes attributes and methods for moving, setting color,
 * and illegal moves
 * **/
public class Queen extends Piece {

    // Assigning Q for Queen
    public static final String ANN = "Q";

    // depending on which colour of the Queen is chosen, the correct Queen image will be assigned to the instanced object
    public Queen(Color color) {
        super(color, PieceType.QUEEN.value, "/pieces/whitequeen.png", "/pieces/blackqueen.png", ANN);
    }

    // checks if the queen is able to move in the way requested by human player or AI player
    @Override
    public boolean canMoveTo(Board board, Movement movement) {

        // check that the movement is possible
        final Position delta = movement.getDestination().add(movement.getOrigin().negate());
        if (movement.getDestination().equals(movement.getOrigin()) ||
                !(delta.getX() == 0 || delta.getY() == 0 || Math.abs(delta.getX()) == Math.abs(delta.getY()))) {
            return false;
        }
        final Position normalizedDelta = delta.clone().normalize();
        // only move if there is no piece in the middle
        for (int i = 1; i < Math.max(Math.abs(delta.getX()), Math.abs(delta.getY())); i++) {
            if (board.getBoard()[movement.getOrigin().getX() + normalizedDelta.getX() * i][movement.getOrigin().getY() + normalizedDelta.getY() * i] != null) {
                return false;
            }
        }

        // the destination selected can either be empty or have an opponent's piece
        Piece destinationPiece = board.getBoard()[movement.getDestination().getX()][movement.getDestination().getY()];
        return (destinationPiece == null || destinationPiece.getColor() != getColor());
    }

    // A list of all movements taken by the queen to store and display
    @Override
    public List<Movement> getAllPossibleMovements(Board board, boolean autoCheck) {
        int [][] movesDirections = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}, {0, -1}, {0, 1}, {1, 0}, {-1, 0}};
        ArrayList<Movement> allMovements = new ArrayList<>();

        final Position origin = board.getPiecePosition(this);

        if (origin == null){
            // if the piece is not on the board
            return allMovements;
        }

        // checks if movement is valid and on the board
        for (int iLength = 1; iLength < 8; iLength++) {
            for (int iDir = 0; iDir < 8; iDir++) {
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
