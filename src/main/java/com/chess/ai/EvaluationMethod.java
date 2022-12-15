package com.chess.ai;

import com.chess.Board;

public interface EvaluationMethod {

    double evaluateBoard(final Board board, final int turn);
}
