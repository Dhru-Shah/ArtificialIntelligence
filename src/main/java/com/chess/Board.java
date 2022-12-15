package com.chess;

import com.chess.ai.BasicEvaluationMethod;
import com.chess.piece.*;
import com.chess.player.MiniMaxAI;
import com.chess.player.Human;
import com.chess.player.PlayMode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * This class represents a board on which chess will be played by two players, AI vs AI, AI vs Human, and Human vs Human.
 * It includes attributes and methods for results, size, players, pieces, colors, grid, moves, draw, checkmate, etc.
 * **/
public class Board extends Application {

    //initializing board size, GUI, players, AI, starting depth, colors, and current piece selected
    public static final int BOARD_SIZE = 8;
    public static final double BOARD_SIZE_UI = 90;
    public static final String PLAYER_1 = "Player 1";
    public static final String PLAYER_2 = "Player 2";
    public static final String AI_1 = "AI 1";
    public static final String AI_2 = "AI 2";
    public static final int DEPTH = 3;

    // null means no piece
    private Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];
    private PlayMode[] players = new PlayMode[2];
    private ArrayList<Movement> previousMovements = new ArrayList<>();
    private int turn = 0;

    public static Color [] turnColors = {Color.WHITE, Color.BLACK};

    private GridPane pane = new GridPane();

    private TextArea previousMovementsText = new TextArea();

    private TextField commandText = new TextField();

    private final PipedOutputStream[] pipedOutputStream = {new PipedOutputStream(), new PipedOutputStream()};

    private final PipedInputStream[] playerInputStream = {new PipedInputStream(pipedOutputStream[0]), new PipedInputStream(pipedOutputStream[1])};

    private final PrintWriter[] printWriter = {new PrintWriter(pipedOutputStream[0], true), new PrintWriter(pipedOutputStream[1], true)};

    private final Scanner[] playerScanners = {new Scanner(playerInputStream[0]), new Scanner(playerInputStream[1])};

    private Label turnLabel = new Label("No turn: ");

    private Piece selectedPiece = null;

    public Board() throws IOException {
    }

    // clones the board
    public Board clone(){
        try {
            Board boardCloned = new Board();
            Piece[][] realBoardCloned = new Piece[BOARD_SIZE][BOARD_SIZE];
            boardCloned.board = realBoardCloned;
            for (int i = 0; i < realBoardCloned.length; i++) {
                for (int j = 0; j < realBoardCloned[i].length; j++) {
                    realBoardCloned[i][j] = board[i][j];
                }
            }
            boardCloned.turn = turn;

            return boardCloned;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // figures out the game result based on moves available, check and checkmate
    public GameResult isGameOver(){
        boolean amICheck = isCheck();
        final ArrayList<Movement> allMyMovements = getAllMyMovements(true);
        boolean hasMovementsAvailable = !allMyMovements.isEmpty();

        return new GameResult(amICheck, hasMovementsAvailable, this);
    }

    /**
     *
     * @param one True if we want only 1 movement.
     * @return
     */
    public ArrayList<Movement> getAllMyMovements(boolean one) {
        ArrayList<Movement> allMovements = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColor().equals(turnColors[turn])){
                    allMovements.addAll(piece.getAllPossibleMovements(this, false));
                    if (one && !allMovements.isEmpty()){
                        return allMovements;
                    }
                }
            }
        }
        return allMovements;
    }

    // any valid moves as well as if moving will result in a check
    public boolean isValid(Movement movement, boolean autoCheck){
        if (movement.getPiece() == null){
            return false; // do not have a valid piece
        }
        Piece destinationPiece = getBoard()[movement.getDestination().getX()][movement.getDestination().getY()];
        if (destinationPiece != null && destinationPiece.getColor().equals(movement.getPiece().getColor())){
            // cannot move the piece over a piece of the same color
            return false;
        }

        // moving a piece in current turn
        if (!movement.getPiece().getColor().equals(turnColors[turn])){
            return false;
        }

        // moving a piece to a valid position
        if (!movement.getPiece().canMoveTo(this, movement)){
            return false;
        }

        // if piece is in check or will be in check after move
        if (!autoCheck && isCheckAfterMove(movement)){
            return false;
        }

        // otherwise allow movement
        return true;
    }

    private Thread playThread;

    private boolean waitingMove = false;

    // play the game turn by turn by either AI or as a human (as a human can play with mouse or via input text command)
    public void play(){

        final Board myBoard = this;
        final BasicEvaluationMethod basicEvaluationMethod = new BasicEvaluationMethod();

        playThread = new Thread(){
            @Override
            public void run() {
                // play only if no check and movements possible so pretty much if it is not a draw nor a checkmate
                GameResult gameOn = new GameResult(false, true, myBoard);
                gameOn:
                while(!gameOn.isGameOver())
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            draw();
                            // disable the input text command if it is AI vs AI or AI vs Human and it is AI's turn
                            commandText.setDisable(players[turn] instanceof MiniMaxAI);

                            turnLabel.setText(players[turn].getName());
                        }
                    });

                    // if movement is valid, then allow players to take their individual turns and wait in between turns
                    final PlayMode currentPlayer = players[turn];
                    Movement currentMove;
                    do{
                        waitingMove = true;
                        currentMove = currentPlayer.getNextMove(myBoard, turn);
                        waitingMove = false;
                        if (currentMove.isEnd()){
                            break gameOn;
                        }
                    }while (!isValid(currentMove, false));

                    // print valid movement only
                    System.out.println("Movement received is valid " + currentMove);
                    currentMove.getPiece().move(myBoard, currentMove);
                    previousMovements.add(currentMove);

                    // assign the value from currentMove to finalCurrentMovement variable
                    final Movement finalCurrentMovement = currentMove;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            previousMovementsText.setText(previousMovementsText.getText() + "\n" + finalCurrentMovement);
                        }
                    });
                    // stops running current thread for 500 ms but can be interrupted by any other thread as an InterruptedException
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }


                    turn = (turn + 1) % 2;
                    gameOn = isGameOver();
                }

                // assign the value from gameOn to gameResultFinal variable
                final GameResult gameResultFinal = gameOn;

                //This method will run later as on demand, and it's initializing the variables
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        draw();
                        turnLabel.setText(gameResultFinal.toString());
                        commandText.setDisable(true);
                    }
                });
            }
        };

        playThread.start();
    }

    // return moves as strings along with previous moves to keep track and display
    @Override
    public String toString() {
        final StringBuilder answer = new StringBuilder();
        for (Movement movement : previousMovements){
            answer.append(movement);
        }
        return answer.toString();
    }

    // take string and display on board
    public String toStringBoard(){
        final StringBuilder answer = new StringBuilder();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null){
                    answer.append(" ");
                }else{
                    answer.append(board[i][j]);
                }
            }
        }

        return answer.toString();
    }

    // checks if moving will result in a check
    public boolean isCheckAfterMove(final Movement movement){
        // removes the piece and checks if the enemy pieces can attack the king
        Piece removedPiece = board[movement.getOrigin().getX()][movement.getOrigin().getY()];
        Piece originalPiece = board[movement.getDestination().getX()][movement.getDestination().getY()];
        board[movement.getOrigin().getX()][movement.getOrigin().getY()] = null;
        board[movement.getDestination().getX()][movement.getDestination().getY()] = removedPiece;
        turn = (turn + 1) % 2;

        boolean answer = isCheck(movement);

        // if this is the case, then undo the movement
        board[movement.getOrigin().getX()][movement.getOrigin().getY()] = removedPiece;
        board[movement.getDestination().getX()][movement.getDestination().getY()] = originalPiece;
        turn = (turn + 1) % 2;
        return answer;
    }

    // check if moving will check the enemy king
    private boolean isCheck(Movement movement) {
        boolean answer = false;
        allLoops:
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColor().equals(turnColors[turn])){
                    for (Movement m : piece.getAllPossibleMovements(this, true)){
                        Position destination = m.getDestination();

                        if (!(movement.getPiece() instanceof King)) {
                            Piece destinationPiece = board[destination.getX()][destination.getY()];
                            if (destinationPiece instanceof King && !destinationPiece.getColor().equals(turnColors[turn])) {
                                answer = true;
                                break allLoops;
                            }
                        }else{
                            if (destination.equals(movement.getDestination())) {
                                answer = true;
                                break allLoops;
                            }
                        }
                    }
                }
            }
        }
        return answer;
    }

    //Check if an enemy piece does check to player playing
    public boolean isCheck(){
        turn = (turn + 1) % 2;

        boolean answer = false;
        allLoops:
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColor().equals(turnColors[turn])) {
                    for (Movement m : piece.getAllPossibleMovements(this, true)){
                        Position destination = m.getDestination();

                        Piece destinationPiece = board[destination.getX()][destination.getY()];
                        if (destinationPiece instanceof King && !destinationPiece.getColor().equals(turnColors[turn])) {
                            answer = true;
                            break allLoops;
                        }
                    }
                }
            }
        }
        turn = (turn + 1) % 2;
        return answer;
    }

    // returns position on the board of size defined i.e. 8x8
    public Position getPiecePosition(final Piece piece){
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (getBoard()[i][j] == piece){
                    return new Position(i, j);
                }
            }
        }
        return null;
    }

    // array of pieces on a board
    public Piece[][] getBoard() {
        return board;
    }

    // set board size with array of pieces
    public void setBoard(Piece[][] board) {
        this.board = board;
    }

    // ignore
    public PlayMode[] getPlayers() {
        return players;
    }

    // ignore
    public void setPlayers(PlayMode[] players) {
        this.players = players;
    }

    // returns list of all movements previously conducted
    public ArrayList<Movement> getPreviousMovements() {
        return previousMovements;
    }

    // ignore
    public void setPreviousMovements(ArrayList<Movement> previousMovements) {
        this.previousMovements = previousMovements;
    }

    // get whose turn it is
    public int getTurn() {
        return turn;
    }

    // set turn of players playing
    public void setTurn(int turn) {
        this.turn = turn;
    }

    // draw the board onto the gui
    public void draw(){
        pane.getChildren().clear();

        // Let's highlight all the possible movements
        TreeSet<Position> potentialMovements = new TreeSet<>();
        if (selectedPiece != null) {
            List<Movement> allPossibleMovements = selectedPiece.getAllPossibleMovements(this, false);
            if (allPossibleMovements != null) {
                for (Movement m : allPossibleMovements) {
                    potentialMovements.add(m.getDestination());
                }
            }
        }

        // return if check is applied to current player
        boolean amICheck = isCheck();

        int count = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            count++;
            for (int j = 0; j < BOARD_SIZE; j++) {
                javafx.scene.paint.Color background;
                if (count % 2 == 0) {
                    background = javafx.scene.paint.Color.DARKGREY;
                }else{
                    background = javafx.scene.paint.Color.LIGHTGRAY;
                }
                Canvas canvas = new Canvas(BOARD_SIZE_UI, BOARD_SIZE_UI);
                GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

                graphicsContext.setFill(background);
                graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // highlight the potential movements
                if (potentialMovements.contains(new Position(BOARD_SIZE - 1 - i,j))){
                    graphicsContext.setFill(javafx.scene.paint.Color.LIGHTGREEN);
                    drawHighlight(canvas, graphicsContext);
                }

                // highlight the draw
                final Piece piece = board[BOARD_SIZE - 1 - i][j];
                if (piece != null){
                    if (piece instanceof King && piece.getColor().equals(turnColors[turn]) && amICheck){
                        graphicsContext.setFill(javafx.scene.paint.Color.INDIANRED);
                        drawHighlight(canvas, graphicsContext);
                    }

                    Image image = new Image(piece.getColor() == Color.WHITE ? piece.getImagePathWhite() : piece.getImagePathBlack());
                    graphicsContext.drawImage(image, 0, 0);
                }

                count++;
                pane.add(canvas, j, i);
            }
        }
    }

    // draws the highlight based on situation on the GUI
    private void drawHighlight(Canvas canvas, GraphicsContext graphicsContext) {
        graphicsContext.fillRect(0, 0, canvas.getWidth(), 5);
        graphicsContext.fillRect(0, 0, 5, canvas.getHeight());
        graphicsContext.fillRect(0, canvas.getWidth() - 5, canvas.getWidth(), canvas.getHeight());
        graphicsContext.fillRect(canvas.getHeight() - 5, 0, canvas.getWidth(), canvas.getHeight());
    }

    // creates default board with pieces in default position, i.e. the starting point for both black and white
    public void loadDefaultBoard(){
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = null;
            }
        }
        board[0][0] = new Rook(Color.WHITE);
        board[0][1] = new Knight(Color.WHITE);
        board[0][2] = new Bishop(Color.WHITE);
        board[0][3] = new Queen(Color.WHITE);
        board[0][4] = new King(Color.WHITE);
        board[0][5] = new Bishop(Color.WHITE);
        board[0][6] = new Knight(Color.WHITE);
        board[0][7] = new Rook(Color.WHITE);
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(Color.WHITE);
        }

        board[7][0] = new Rook(Color.BLACK);
        board[7][1] = new Knight(Color.BLACK);
        board[7][2] = new Bishop(Color.BLACK);
        board[7][3] = new Queen(Color.BLACK);
        board[7][4] = new King(Color.BLACK);
        board[7][5] = new Bishop(Color.BLACK);
        board[7][6] = new Knight(Color.BLACK);
        board[7][7] = new Rook(Color.BLACK);
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[6][i] = new Pawn(Color.BLACK);
        }
    }

    // load default board into its array and draw on the board, including creation of all visuals: buttons, input textboxes and status area
    @Override
    public void start(Stage stage) throws Exception {
        loadDefaultBoard();
        draw();

        HBox mainHBox = new HBox();
        VBox buttonsPanel = new VBox();
        buttonsPanel.setSpacing(15);
        buttonsPanel.setAlignment(Pos.CENTER);

        previousMovementsText.setPrefHeight(500);
        previousMovementsText.setPrefWidth(250);
        previousMovementsText.setMaxWidth(250);
        previousMovementsText.setEditable(false);

        buttonsPanel.setPrefWidth(300);

        commandText.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                printWriter[turn].println(commandText.getText());
                commandText.setText("");
            }
        });
        commandText.setDisable(true);

        final Button humanHuman = new Button("Game - Human vs Human");
        humanHuman.setOnAction(
                new EventHandler<ActionEvent>() {
                   @Override
                   public void handle(ActionEvent actionEvent) {
                       resetBoard();
                       players[0] = new Human(PLAYER_1, playerScanners[0]);
                       players[1] = new Human(PLAYER_2, playerScanners[1]);
                       play();
                   }
               });
        HBox verticalBoxHumanAI = new HBox();
        HBox verticalBoxAIAI = new HBox();
        final TextField humanAIDepthTextField = new TextField("3");
        final TextField aIAITextField = new TextField("3");

        final Button humanAI = new Button("Game - Human vs AI");
        humanAI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resetBoard();
                players[0] = new Human(PLAYER_1, playerScanners[0]);
                int depth = DEPTH;
                try{
                    depth = Integer.parseInt(humanAIDepthTextField.getText());
                }catch (Exception e){

                }
                players[1] = new MiniMaxAI(AI_1, depth, Color.BLACK);
                play();
            }
        });

        // creation of button AI vs AI
        final Button AIAI = new Button("Game - AI vs AI");
        AIAI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resetBoard();
                int depth = DEPTH;
                try{
                    depth = Integer.parseInt(aIAITextField.getText());
                }catch (Exception e){

                }
                players[0] = new MiniMaxAI(AI_1, depth, Color.WHITE);
                players[1] = new MiniMaxAI(AI_2, depth, Color.BLACK);
                play();
            }
        });

        // assign current values of the object to boardFinal variable
        final Board boardFinal = this;

        //This method will Create a new event when we pressed the mouse left click Button and get the Position from the board with x and y co-ordinates
        pane.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int x = BOARD_SIZE - (int) (mouseEvent.getY() / BOARD_SIZE_UI) - 1;
                int y = (int) (mouseEvent.getX() / BOARD_SIZE_UI);

                // if we are suggesting a move
                if (selectedPiece != null){
                    boolean moved = false;
                    List<Movement> allPossibleMovements = selectedPiece.getAllPossibleMovements(boardFinal, false);
                    final Position selectedPosition = new Position(x,y);
                    for (Movement m : allPossibleMovements){
                        if (m.getDestination().equals(selectedPosition)){
                            // we suggested an actual movement.
                            moved = true;
                            printWriter[turn].println("" + getPiecePosition(selectedPiece) + selectedPosition);
                            selectedPiece = null;
                        }
                    }

                    if (!moved){
                        selectedPiece = board[x][y];
                    }

                }else{
                    selectedPiece = board[x][y];
                }

                draw();
            }
        });

        AIAI.setPrefWidth(200);
        verticalBoxHumanAI.getChildren().addAll(humanAI, humanAIDepthTextField);
        verticalBoxAIAI.getChildren().addAll(AIAI, aIAITextField);
        humanHuman.setPrefWidth(250);
        humanAI.setPrefWidth(200);
        verticalBoxHumanAI.setSpacing(5);
        verticalBoxAIAI.setSpacing(5);
        verticalBoxAIAI.setPadding(new Insets(0, 25, 0, 25));
        verticalBoxHumanAI.setPadding(new Insets(0, 25, 0, 25));

        final HBox commandBox = new HBox();
        commandBox.setSpacing(5);
        commandBox.setPadding(new Insets(0, 50, 0, 50));
        commandBox.getChildren().addAll(turnLabel, commandText);

        buttonsPanel.getChildren().addAll(humanHuman, verticalBoxHumanAI, verticalBoxAIAI, previousMovementsText, commandBox);
        mainHBox.getChildren().addAll(pane ,buttonsPanel);
        // Create a scene and place it in the stage
        Scene scene = new Scene(mainHBox);
        stage.setTitle("Tree Chess AI");
        stage.setScene(scene); // Place in scene in the stage
        stage.show();
    }

    // resets board to play again
    private void resetBoard() {

        //
        if (waitingMove) {
            printWriter[turn].println(Movement.INVALID_TURN);
        }

        previousMovements.clear();
        turn = 0;
        loadDefaultBoard();
        previousMovementsText.clear();
    }


}

