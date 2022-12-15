package com.chess.player;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;

public abstract class PlayMode {

    private String name;

    public PlayMode(String name) {
        this.name = name;
    }

    public abstract Movement getNextMove(Board board, int turn);

    public String getName() {
        return name;
    }
}
