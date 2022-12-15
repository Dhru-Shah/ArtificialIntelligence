package com.chess.piece;

// sets unchangeable values of the pieces based on importance with king being most important and pawn being least
public enum PieceType {
    PAWN(1), ROOK(5), QUEEN(9), BISHOP(3), KNIGHT(3), KING(99999);

    public final int value;

    PieceType(final int value){
        this.value = value;
    }
}
