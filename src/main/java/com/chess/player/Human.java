package com.chess.player;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;

import java.io.InputStream;
import java.util.Scanner;

public class Human extends PlayMode {


    private Scanner scanner;

    public Human(String name, Scanner scanner) {
        super(name);
        this.scanner = scanner;
    }

    @Override
    public Movement getNextMove(Board board, int turn) {
        // read the movement requested from inputstream and return it
        return new Movement(board, scanner.next());
    }
}
