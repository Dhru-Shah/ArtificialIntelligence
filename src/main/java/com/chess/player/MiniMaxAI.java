package com.chess.player;

import com.chess.Board;
import com.chess.Color;
import com.chess.Movement;
import com.chess.ai.BasicEvaluationMethod;
import com.chess.piece.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * This class represents a MiniMaxAI
 * It includes attributes and methods for Movement, depth, colors etc.
 * **/
public class MiniMaxAI extends PlayMode {

    public static class MiniMaxResult {
        // initializing the variables value, movement
        private double value;

        private Movement movement;

        // result method
        public MiniMaxResult(double value, Movement movement) {
            this.value = value;
            this.movement = movement;
        }

        // getter method for getting value
        public double getValue() {
            return value;
        }

        // setter method for setting value
        public void setValue(double value) {
            this.value = value;
        }

        // getter method for getting movement
        public Movement getMovement() {
            return movement;
        }

        // setter method for setting movement
        public void setMovement(Movement movement) {
            this.movement = movement;
        }
    }
    // initializing the variables: depth, myColor
    private int depth;

    private Color myColor;

    // the constructor accepts name, depth and myColor and calls parent constructor with argument name i.e. from PlayMode.java
    public MiniMaxAI(String name, int depth, Color myColor) {
        super(name);
        this.depth = depth;
        this.myColor = myColor;
    }
    // Gets the next move by cloning board, calling another miniMax method
    @Override
    public Movement getNextMove(Board board, int turn) {
        evaluated = 0;
        int inf = 1 << 30;
        Board boardCloned = board.clone();
        final MiniMaxResult miniMaxResult = miniMax(boardCloned, depth, -1 * inf, inf);
        System.out.println("Value " + miniMaxResult.getValue() + " " + miniMaxResult.getMovement());
        return miniMaxResult.getMovement();
    }

    private static int evaluated = 0;

    // result stored in HashMap
    HashMap<String, MiniMaxResult> cached = new HashMap<>();

    // returns the result of mini max through alpha-beta pruning
    public MiniMaxResult miniMax(Board board, int depth, double alpha, double beta){
        String boardKey = board.toStringBoard() + depth + board.getTurn();
        if (cached.containsKey(boardKey)){
            return cached.get(boardKey);
        }

        final BasicEvaluationMethod basicEvaluationMethod = new BasicEvaluationMethod();

        // the bottom of the tree is reached
        if (depth == 0 || board.isGameOver().isGameOver()){
            ++evaluated;
            if (evaluated % 1000 == 0)
                System.out.println(evaluated);
            return new MiniMaxResult(basicEvaluationMethod.evaluateBoard(board, board.getTurn()), null);
        }

        final Piece[][] realBoard = board.getBoard();
        final ArrayList<Movement> movementsToExplore = board.getAllMyMovements(false);

        // increasing variety of possible moves
        Collections.shuffle(movementsToExplore);
        MiniMaxResult bestMovement;

        if ((Board.turnColors[board.getTurn()].equals(myColor) && Color.WHITE.equals(myColor)) ||
                (!Board.turnColors[board.getTurn()].equals(myColor) && Color.BLACK.equals(myColor))){
            // defaults to negative inf
            bestMovement = new MiniMaxResult((1 << 30) * -1, null);
            for (Movement move :
                    movementsToExplore) {

                Piece originalDestination = realBoard[move.getDestination().getX()][move.getDestination().getY()];
                Piece originalOrigin = realBoard[move.getOrigin().getX()][move.getOrigin().getY()];
                // perform the movement
                realBoard[move.getDestination().getX()][move.getDestination().getY()] = realBoard[move.getOrigin().getX()][move.getOrigin().getY()];
                realBoard[move.getOrigin().getX()][move.getOrigin().getY()] = null;
                board.setTurn((board.getTurn() + 1) % 2);
                if (move.isPromoted()){
                    realBoard[move.getDestination().getX()][move.getDestination().getY()] = move.getPromotedTo();
                }

                MiniMaxResult currentResult = miniMax(board, depth - 1, alpha, beta);

                // reverse the move
                realBoard[move.getDestination().getX()][move.getDestination().getY()] = originalDestination;
                realBoard[move.getOrigin().getX()][move.getOrigin().getY()] = originalOrigin;
                board.setTurn((board.getTurn() + 1) % 2);

                if (bestMovement == null || currentResult.getValue() > bestMovement.getValue()){
                    bestMovement = currentResult;
                    bestMovement.setMovement(move);
                }
                alpha = Math.max(alpha, bestMovement.getValue());
                if (beta <= alpha){
                    break;
                }
            }
            cached.put(boardKey, bestMovement);
            return bestMovement;
        }else{
            bestMovement = new MiniMaxResult((1 << 30), null);
            for (Movement move :
                    movementsToExplore) {

                Piece originalDestination = realBoard[move.getDestination().getX()][move.getDestination().getY()];
                Piece originalOrigin = realBoard[move.getOrigin().getX()][move.getOrigin().getY()];
                // perform the movement
                realBoard[move.getDestination().getX()][move.getDestination().getY()] = realBoard[move.getOrigin().getX()][move.getOrigin().getY()];
                realBoard[move.getOrigin().getX()][move.getOrigin().getY()] = null;
                board.setTurn((board.getTurn() + 1) % 2);

                MiniMaxResult currentResult = miniMax(board, depth - 1, alpha, beta);

                // reverse the move
                realBoard[move.getDestination().getX()][move.getDestination().getY()] = originalDestination;
                realBoard[move.getOrigin().getX()][move.getOrigin().getY()] = originalOrigin;
                board.setTurn((board.getTurn() + 1) % 2);

                if (bestMovement == null || currentResult.getValue() < bestMovement.getValue()){
                    bestMovement = currentResult;
                    bestMovement.setMovement(move);
                }
                beta = Math.min(beta, bestMovement.getValue());
                if (beta <= alpha){
                    break;
                }
            }

            cached.put(boardKey, bestMovement);
            return bestMovement;
        }

        // this should not happen
    }
}
