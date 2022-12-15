package com.chess.ai;

import com.chess.Board;
import com.chess.Color;
import com.chess.piece.*;

public class BasicEvaluationMethod implements EvaluationMethod{

    final int MAX_DISTANCE_TO_CENTER = 8; // max distance to center

    @Override
    public double evaluateBoard(Board board, int turn) {
        /**
         * Let's evaluate the material value first. Let's multiply by
         * 100 to give some extra weight to it.
         */
        double answer = 0;
        final Piece[][] realBoard = board.getBoard();
        for (int i = 0; i < realBoard.length; i++) {
            for (int j = 0; j < realBoard[i].length; j++) {
                final Piece piece = realBoard[i][j];
                if (piece == null){
                    continue;
                }
                answer += piece.getValue() * (piece.getColor() == Color.WHITE ? 1 : -1) * 1000 ;
                int distanceToCenter = Math.abs(j - 4) + Math.abs(i - 4);

                answer += (piece.getColor() == Color.WHITE ? MAX_DISTANCE_TO_CENTER - distanceToCenter : distanceToCenter - MAX_DISTANCE_TO_CENTER); // let's force to put the pieces in the center of the board
            }
        }



        return answer;
    }
}
