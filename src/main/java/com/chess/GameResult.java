package com.chess;

import com.chess.piece.King;

import java.util.ArrayList;

/**
 * This class handles the result of the game including checkmate, moves available, and conditions to enforce a draw
 * **/
public class GameResult {
    private boolean isCheck;

    private boolean hasMovementsAvailable;

    private Board board;

    private int amountOfKings = 0;

    // checks if any kings are in a check state
    public GameResult(boolean isCheck, boolean hasMovementsAvailable, Board board) {
        this.isCheck = isCheck;
        this.hasMovementsAvailable = hasMovementsAvailable;
        this.board = board;


        for (int i = 0; i < board.BOARD_SIZE; i++){
            for (int j = 0; j < board.BOARD_SIZE; j++) {
                if (board.getBoard()[i][j] instanceof King){
                    ++amountOfKings;
                }
            }
        }

        // checks if too many repetitions of moves taking place, then enforces a draw
        ArrayList<Movement> previousMovements = board.getPreviousMovements();
        if (previousMovements.size() > 12){
            int last = previousMovements.size() - 1;
            repeatedMovementDraw = true;
            for (int i = 0; i < 4; i++) {
                repeatedMovementDraw &= previousMovements.get(last - i).equals(previousMovements.get(last - i - 4));
            }
        }
    }

    // sets repeated movement draw to 'false' to begin with
    private boolean repeatedMovementDraw = false;

    // figures out if game is over based on either draw, no movement available (stalemate), and 2 kings are not available to play
    public boolean isGameOver(){
        // we should have both kings to ensure the game is still valid.

        return !hasMovementsAvailable || amountOfKings != 2 || repeatedMovementDraw;
    }

    // print checkmate if a king is in a checked state and moving it will result in a loss of the king otherwise print an enforced draw
    @Override
    public String toString() {
        if (isCheck || amountOfKings != 2)
            return "Checkmate";
        return "Draw";
    }
}
