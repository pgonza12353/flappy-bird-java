package com.example.flappybird;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.ArrayList;
import javafx.geometry.Rectangle2D;

public class FlappyBirdApp extends Application {
    private static Pane gameContent;
    private static Scene scene;

    private static final Color BACKGROUND_COLOR = new Color(55.0/255.0, 207/255.0, 237/255.0, 1);
    private static final int BACKGROUND_WIDTH = 400;
    private static final int BACKGROUND_HEIGHT = 600;

    private static final int BIRD_RADIUS = 17;
    private static double birdVelocity;
    private static final double FALL_VELOCITY = 13;
    private static final double DEATH_X_VELOCITY = 4;
    private static boolean reachedPeak;
    private static final double JUMP_VELOCITY = -11.75;
    private static Circle bird;
    private static AnimationTimer timer;
    private static AnimationTimer fallTimer;
    private static boolean gameStarted = false;
    private static boolean isGameOver;

    private static final ArrayList<Group> pipes = new ArrayList<>();
    private static final int PIPE_WIDTH = 65;
    private static final int BOTTOM_PIPE_OFFSET = 130;
    private static final int TOP_PIPE_MIN_HEIGHT = 30;
    private static final int TOP_PIPE_MAX_HEIGHT = BACKGROUND_HEIGHT - TOP_PIPE_MIN_HEIGHT - BOTTOM_PIPE_OFFSET;
    private static final int PIPE_INITIAL_X = BACKGROUND_WIDTH;
    private static final int PIPE_PAIR_GAP = (BACKGROUND_WIDTH / 2) + PIPE_WIDTH;
    private static final double PIPE_VELOCITY = -2.5;
    private static double lastPipeCreatedX;
    ArrayList<Group> toRemove;
    private static final Color PIPE_COLOR = new Color(79/255.0, 224/255.0, 54/255.0, 1);

    private static Text startMessage;
    private static final int START_MSSG_X = 175;
    private static final int START_MSSG_Y = 50;

    private static Text gameOverMessage;
    private static final int GAME_OVER_MSSG_X = 160;
    private static final int GAME_OVER_MSSG_Y = 80;

    private static int score = 0;
    private static final Text scoreMessage = new Text("Score: " + score);
    private static final int SCORE_X = 30;
    private static final int SCORE_Y = 50;
    private static Text leaderboard;
    private static final int LEADERBOARD_X = BACKGROUND_WIDTH / 2 - 70;
    private static final int LEADERBOARD_Y = BACKGROUND_HEIGHT / 2 + 145;
    private static String username;

    @Override
    public void start(Stage stage) {
        setupGame(stage);

        // Handles when the Space Bar and the key R are pressed
        scene.setOnKeyPressed(event -> {
            switch(event.getCode()) {
                case SPACE:
                    // If the game hasn't started, initialize the game timer
                    if(!gameStarted) {
                        gameStarted = true;
                        gameContent.getChildren().remove(startMessage);
                        scoreMessage.setVisible(true);
                        timer.start();
                    }
                    // Makes the bird jump
                    if(!isGameOver) {
                        jump();
                    }
                    break;
                case R:
                    // Restarts the game
                    if(isGameOver) {
                        restartGame();
                        gameOverMessage.setVisible(false);
                        break;
                    }
            }
        });

        // Timer for the game itself
        timer = new AnimationTimer() {
            public void handle(long now) {
                // Changes the bird's velocity based on gravity
                birdVelocity += 1;
                if(birdVelocity > FALL_VELOCITY)  {
                    birdVelocity = FALL_VELOCITY;
                }

                toRemove = new ArrayList<>();
                Iterator<Group> iterator = pipes.iterator();

                // Updates player score and removes pipes once they're offscreen
                while(iterator.hasNext()) {
                    Group pipe = iterator.next();
                    pipe.setTranslateX(pipe.getTranslateX() + PIPE_VELOCITY);

                    double pipeRightX = pipe.getTranslateX() + PIPE_WIDTH;

                    Object userData = pipe.getUserData();
                    boolean passed = userData instanceof Boolean && (Boolean) userData;

                    if(!passed && pipeRightX < -BACKGROUND_WIDTH / 2.0 + PIPE_WIDTH / 2.0) {
                        score++;
                        scoreMessage.setText("Score: " + score);
                        pipe.setUserData(true);
                    }
                    if(pipeRightX < -BACKGROUND_WIDTH) {
                        toRemove.add(pipe);
                        iterator.remove();
                    }
                }
                Platform.runLater(() -> {
                    for(Group pipe: toRemove) {
                        gameContent.getChildren().remove(pipe);
                    }
                });

                // Generates new pipe pair once the previous goes offscreen
                lastPipeCreatedX += PIPE_VELOCITY;
                if(BACKGROUND_WIDTH - lastPipeCreatedX >= PIPE_PAIR_GAP) {
                    Group pipePair = createPipes(PIPE_INITIAL_X);
                    // Sets passed condition to false for new pipes
                    pipePair.setUserData(false);

                    gameContent.getChildren().add(pipePair);
                    lastPipeCreatedX = PIPE_INITIAL_X;
                }

                // Handles bird movement and checks collisions
                bird.setTranslateY(bird.getTranslateY() + birdVelocity);
                bird.toFront();
                scoreMessage.toFront();
                checkCollision();
            }
        };
    }

    // Sets the bird to a falling state
    public static void jump() {
        birdVelocity = JUMP_VELOCITY;
    }

    // Checks all collision for the bird
    public static void checkCollision() {
        // Ground and ceiling collisions
        if(bird.getBoundsInParent().getMinY() <= 0)
            gameOver();
        else if (bird.getBoundsInParent().getMaxY() >= BACKGROUND_HEIGHT)
            gameOver();

        // Pipe collisions
        for (Group pipe : pipes) {
            Rectangle topPipe = (Rectangle) pipe.getChildren().get(0);
            Rectangle bottomPipe = (Rectangle) pipe.getChildren().get(1);

            if(topPipe.getTranslateX() + topPipe.getWidth() > 0) {
                topPipe.setStroke(Color.BLACK);
                bottomPipe.setStroke(Color.BLACK);
            }

            Shape intersectTop = Shape.intersect(bird, topPipe);
            Shape intersectBottom = Shape.intersect(bird, bottomPipe);

            if (intersectTop.getBoundsInLocal().getWidth() > 0 || intersectBottom.getBoundsInLocal().getWidth() > 0) {
                gameOver();
                return;
            }
        }
    }

    // Creates an individual pipe using given arguments
    public static Rectangle setPipe(Rectangle pipe, int width, int height, Color c, double x, double y) {
        pipe.setWidth(width);
        pipe.setHeight(height);
        pipe.setFill(c);
        pipe.setTranslateX(x);
        pipe.setTranslateY(y);
        return pipe;
    }

    // Creates top and bottom pipe and puts them in a group
    public static Group createPipes(int startX) {
        Rectangle topPipe = new Rectangle();
        Rectangle bottomPipe = new Rectangle();

        Random random = new Random();
        int height = TOP_PIPE_MIN_HEIGHT + random.nextInt(TOP_PIPE_MAX_HEIGHT - TOP_PIPE_MIN_HEIGHT + 1);

        setPipe(topPipe, PIPE_WIDTH, height, PIPE_COLOR, startX, 0);
        setPipe(bottomPipe, PIPE_WIDTH, BACKGROUND_HEIGHT - height - BOTTOM_PIPE_OFFSET, PIPE_COLOR, startX, height + BOTTOM_PIPE_OFFSET);

        Group pipePair = new Group(topPipe, bottomPipe);
        pipes.add(pipePair);

        // Show Pipe Hitboxes
        topPipe.setStroke(Color.TRANSPARENT);
        bottomPipe.setStroke(Color.TRANSPARENT);
        topPipe.setStrokeWidth(2);
        bottomPipe.setStrokeWidth(2);

        return pipePair;
    }

    public static void setupGame(Stage stage) {
        // Gives the dimensions of the screen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Sets up the game space where objects will be generated
        gameContent = new Pane();
        gameContent.setPrefSize(BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        gameContent.setClip(new Rectangle(BACKGROUND_WIDTH, BACKGROUND_HEIGHT));

        // Wraps gameContent inside a group
        Group scaledRoot = new Group(gameContent);
        StackPane parentPane = new StackPane();
        parentPane.getChildren().add(scaledRoot);
        parentPane.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, null, null)));

        // Creates scene and stage
        scene = new Scene(parentPane, screenWidth, screenHeight);
        stage.setTitle("Flappy Bird");
        stage.setScene(scene);
        stage.show();

        // Scaling for scaledRoot
        double scaleX = screenWidth / BACKGROUND_WIDTH;
        double scaleY = screenHeight / BACKGROUND_HEIGHT;
        double scale = Math.min(scaleX, scaleY);
        scaledRoot.setScaleX(scale);
        scaledRoot.setScaleY(scale);
        scaledRoot.setLayoutX((screenWidth - BACKGROUND_WIDTH * scale) / 2);
        scaledRoot.setLayoutY((screenHeight - BACKGROUND_HEIGHT * scale) / 2);

        // Sets the game stage and background
        stage.setFullScreen(true);
        parentPane.setStyle("-fx-border-color: blue;");
        Image backgroundImage = new Image("background.png");
        BackgroundSize backgroundSize = new BackgroundSize(screenWidth, screenHeight, false, false, true, true);
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                backgroundSize
        );
        gameContent.setBackground(new Background(bgImage));

        // Creates the bird
        bird = new Circle(BIRD_RADIUS);
        bird.setFill(Color.YELLOW);
        bird.setStroke(Color.BLACK);  // Set stroke color
        bird.setStrokeWidth(2);  // Set stroke width
        bird.setTranslateX(BACKGROUND_WIDTH / 2.0);
        bird.setTranslateY(BACKGROUND_HEIGHT / 2.0);
        gameContent.getChildren().add(bird);

        // Creates the start and game over message
        startMessage = new Text("Press SPACE to Start");
        startMessage.setFont(Font.font("Courier New", FontWeight.BOLD, 30 ));
        startMessage.setFill(Color.WHITE);
        startMessage.setTranslateX(BACKGROUND_WIDTH / 2.0 - START_MSSG_X);
        startMessage.setTranslateY(BACKGROUND_HEIGHT / 2.0 - START_MSSG_Y);
        gameContent.getChildren().add(startMessage);

        gameOverMessage = new Text("    GAME OVER\nPress R to Restart");
        gameOverMessage.setFont(Font.font("Courier New", FontWeight.BOLD, 30 ));
        gameOverMessage.setFill(Color.WHITE);
        gameOverMessage.setTranslateX(BACKGROUND_WIDTH / 2.0 - GAME_OVER_MSSG_X);
        gameOverMessage.setTranslateY(BACKGROUND_HEIGHT / 2.0 - GAME_OVER_MSSG_Y);
        gameContent.getChildren().add(gameOverMessage);
        gameOverMessage.setVisible(false);

        // Creates score message
        scoreMessage.setFont(Font.font("Courier New", FontWeight.BOLD, 36));
        scoreMessage.setFill(Color.WHITE);
        scoreMessage.setX(SCORE_X);
        scoreMessage.setY(SCORE_Y);
        gameContent.getChildren().add(scoreMessage);
        scoreMessage.setVisible(false);

        // Creates first pair of pipes
        Group pipePair = createPipes(PIPE_INITIAL_X);
        gameContent.getChildren().add(pipePair);
        lastPipeCreatedX = PIPE_INITIAL_X;

        // Prompts the user to enter their username
        TextInputDialog dialog = new TextInputDialog("Player");
        dialog.setTitle("Welcome!");
        dialog.setHeaderText("Enter your username");
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.initOwner(stage);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> username = name.trim());

        if(username == null || username.isEmpty()) {
            username = "Player";
        }
    }

    // Sets the game to a game over state
    public static void gameOver() {
        isGameOver = true;
        gameOverMessage.toFront();
        gameOverMessage.setVisible(true);
        timer.stop();
        birdVelocity = JUMP_VELOCITY;
        reachedPeak = false;

        // Saves username and score to database
        if(username != null && !username.trim().isEmpty()) {
            DatabaseManager.saveScoreAndUsername(username, score);
        }

        // Displays the all-time top 3 scores
        ArrayList <String> topScores = DatabaseManager.getTopThreeScore();
        leaderboard = new Text();
        leaderboard.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        leaderboard.setFill(Color.WHITE);

        String leaderboardText = "TOP SCORES: \n";
        for(String line: topScores) {
            leaderboardText += line + "\n";
        }

        leaderboard.setText(leaderboardText.trim());
        leaderboard.setX(LEADERBOARD_X);
        leaderboard.setY(LEADERBOARD_Y);
        gameContent.getChildren().add(leaderboard);

        // Handles the death animation
        fallTimer = new AnimationTimer() {
            public void handle(long now) {
                bird.setRotate(bird.getRotate() + 3);
                bird.setTranslateX(bird.getTranslateX() + DEATH_X_VELOCITY);
                bird.setTranslateY(bird.getTranslateY() + birdVelocity);
                if(!reachedPeak) {
                    birdVelocity++;
                    if(birdVelocity >= 0) {
                        reachedPeak = true;
                    }
                }
                else {
                    birdVelocity++;
                }
                if(bird.getTranslateY() >= BACKGROUND_HEIGHT + BIRD_RADIUS) {
                    stop();
                }
            }
        };
        fallTimer.start();
    }

    // Restarts the game and resets all visual elements
    public static void restartGame() {
        if(timer != null)
            timer.stop();
        if(fallTimer != null) {
            fallTimer.stop();
        }

        birdVelocity = 0;
        bird.setTranslateX(BACKGROUND_WIDTH / 2.0);
        bird.setTranslateY(BACKGROUND_HEIGHT / 2.0);

        for(Group pipe: pipes) {
            gameContent.getChildren().remove(pipe);
        }

        Group pipePair = createPipes(PIPE_INITIAL_X);
        gameContent.getChildren().add(pipePair);
        lastPipeCreatedX = 0;
        pipes.add(pipePair);
        pipes.clear();

        score = 0;
        scoreMessage.setText("Score: " + score);
        gameContent.getChildren().remove(leaderboard);

        gameStarted = false;
        isGameOver = false;
        reachedPeak = false;
    }

    public static void main(String[] args) {
        launch();
    }
}