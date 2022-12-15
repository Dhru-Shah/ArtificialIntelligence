package com.chess.piece;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;
import com.chess.Position;

import java.util.ArrayList;
import java.util.List;
/**
 * This class represents a piece in chess called Knight. It includes attributes and methods for moving, setting color,
 * and illegal moves
 * **/
public class Knight extends Piece{

    // Assigning N for Knight
    public static final String ANN = "N";

    // possible movements a Knight makes on a 2D grid: 2 movements in the same direction and 1 in either left or right
    private Position[] moves = {new Position(1, -2),
            new Position(-1, -2),
            new Position(1, 2),
            new Position(-1, 2),
            new Position(-2, 1),
            new Position(-2, -1),
            new Position(2, 1),
            new Position(2, -1), };

    // depending on which colour of the Knight is chosen, the correct Knight image will be assigned to the instanced object
    public Knight(Color color) {
        super(color, PieceType.KNIGHT.value, "/pieces/whiteknight.png", "/pieces/blackknight.png", ANN);
    }

    // checks if the knight is able to move in the way requested by human player or AI player
    @Override
    public boolean canMoveTo(Board board, Movement movement) {

        //  Check if the movement that knight would like to move to is valid i.e. 2 straight 1 left or right (L-shape)
        final Position delta = movement.getDestination().add(movement.getOrigin().negate());

        boolean isValidMovement = false;

        for (int i = 0; i < moves.length && !isValidMovement; i++) {
            isValidMovement |= delta.equals(moves[i]);
        }
        if (!isValidMovement){
            return false;
        }

        // Check if there is any enemy piece at the destination point
        Piece piece = board.getBoard()[movement.getDestination().getX()][movement.getDestination().getY()];
        return (piece == null || piece.getColor() != getColor());
    }

    //A list of all movements taken by the knight to store and display them
    @Override
    public List<Movement> getAllPossibleMovements(Board board, boolean autoCheck) {
        // Use the Movement constructor to create the movement in algebraic notation
        ArrayList<Movement> allMovements = new ArrayList<>();

        final Position origin = board.getPiecePosition(this);

        // Check if there is no piece on the board
        if (origin == null){
            return allMovements;
        }

        // checks if movement is valid and on the board
        for (int iDir = 0; iDir < moves.length; iDir++) {
            final Position potentialPosition = new Position(origin.getX() + moves[iDir].getX(), origin.getY() + moves[iDir].getY());
            Movement newMovement = new Movement(board, "" + origin + potentialPosition);
            if (newMovement.isValid() && board.isValid(newMovement, autoCheck)){
                allMovements.add(newMovement);
            }
        }
        return allMovements;
    }

}
