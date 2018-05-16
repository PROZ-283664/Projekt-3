package application;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import application.GameLogic.GameState;
import application.GameLogic.Sign;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class GameController implements MessageListener {
	GameLogic gameLogic;
	Integer gameID;
	Sign playerSign;
	Boolean isPlayerMove;
	Integer playerID;
	final Color playerColor;
	final Color opponentColor;
	MessagesHandler messagesHandler;

	@FXML
	GridPane board;
	@FXML
	Text stateText1;
	@FXML
	Text stateText2;
	@FXML
	Text stateText3;
	@FXML
	AnchorPane anchorPane;
	@FXML
	HBox stateBox;
	@FXML
	Text logo;
	@FXML
	Button gameButton;
	Group gameBoard;

	public final CountDownLatch isWaitingAnimationFinished = new CountDownLatch(1);

	public GameController() {
		playerColor = Color.color(0, 253 / (double) 255, 220 / (double) 255);
		opponentColor = Color.color(255 / (double) 255, 86 / (double) 255, 102 / (double) 255);
		isPlayerMove = false;
		messagesHandler = new MessagesHandler();
		playerID = new Random().nextInt();
		gameID = new Random().nextInt();
	}

	@FXML
	private void initialize() {
	}

	@FXML
	private void findOpponent() {
		playWaitingAnimations();

		messagesHandler.setConsumer("PlayerID <> " + playerID + " and Type = 'SEARCHING'");
		Message opponentGame = messagesHandler.receiveQueueMessageSync(1);

		if (opponentGame == null) {
			sendMessage("SEARCHING");
			System.out.println("Nie znaleziono przeciwnika, tworzę nową grę o ID: " + gameID);
			playerSign = Sign.CIRCLE;
		} else {
			try {
				gameID = opponentGame.getIntProperty("GameID");
				System.out.println("Przeciwnik znaleziony, dołączam do gry: " + gameID);
				playerSign = Sign.CROSS;
				sendMessage("START");
				createGame();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}

		messagesHandler.setConsumer("GameID = " + gameID + " and PlayerID <> " + playerID);
		messagesHandler.receiveQueueMessagesAsync(this);
	}

	@FXML
	private void createGame() {
		gameLogic = new GameLogic();

		if (playerSign == Sign.CIRCLE)
			isPlayerMove = true;
		else
			isPlayerMove = false;

		checkGameStatus(gameLogic.gameState);

		try {
			FXMLLoader fxmlLoader = new FXMLLoader(GameController.class.getResource("GameBoard.fxml"));
			fxmlLoader.setControllerFactory(type -> {
				if (type == GameController.class) {
					return this;
				}
				return null;
			});

			anchorPane.getScene().getWindow().setHeight(666);

			gameBoard = fxmlLoader.load();
			anchorPane.getChildren().add(gameBoard);
			gameBoard.setLayoutX(79);
			gameBoard.setLayoutY(100);
			gameBoard.setVisible(false);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (isWaitingAnimationFinished.getCount() == 0) {
			playStartGameAnimations();
		} else {
			new Thread() {
				@Override
				public void run() {
					try {
						isWaitingAnimationFinished.await();
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								playStartGameAnimations();
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	private void resetGame() {
		gameButton.setDisable(true);
		gameLogic.resetGame();
		clearBoard();
		if (isPlayerState(gameLogic.getState()))
			isPlayerMove = true;
		else
			isPlayerMove = false;

		checkGameStatus(gameLogic.gameState);
	}

	@FXML
	private void resetGame_click() {
		sendMessage("RESET");
		resetGame();
	}

	@FXML
	private void gameBoard_Click(MouseEvent event) {
		if (!isPlayerMove || gameLogic.isGameEnded())
			return;

		Node source = (Node) event.getSource();
		Integer column = GridPane.getColumnIndex(source);
		Integer row = GridPane.getRowIndex(source);

		makeMove(column, row);
		sendMessage("MOVE", column + ":" + row);
	}

	private void changeTurn() {
		isPlayerMove = !isPlayerMove;
	}

	private void makeMove(Integer column, Integer row) {
		Sign currentMoveSign = gameLogic.setField(column, row);
		removeSign(column, row);
		drawSign(column, row, currentMoveSign);

		changeTurn();

		checkGameStatus(gameLogic.gameState);
	}

	private void checkGameStatus(GameState state) {
		if (!gameLogic.isGameEnded()) {
			if (isPlayerMove) {
				setStateText("It's ", "your", " turn");
			} else {
				setStateText("It's ", "opponent", " turn");
			}
		} else {
			if (gameLogic.isDraw()) {
				setStateText("Game is a ", "draw", "!");
			} else if (isPlayerState(state)) {
				setStateText("You ", "won", " the game");
			} else {
				setStateText("You ", "lost", " the game");
			}
			gameButton.setDisable(false);
		}
	}

	private void processMessage(Message message) {
		try {
			String type = message.getStringProperty("Type");
			String content = message.getStringProperty("Content");
			if (type.equals("START")) {
				createGame();
			} else if (type.equals("MOVE")) {
				Integer column = Integer.parseInt(content.substring(0, 1));
				Integer row = Integer.parseInt(content.substring(2, 3));
				makeMove(column, row);
			} else if (type.equals("RESET")) {
				resetGame();
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(String type) {
		sendMessage(type, "");
	}

	private void sendMessage(String type, String content) {
		try {
			Message message = messagesHandler.createMessage();
			message.setStringProperty("Type", type);
			message.setIntProperty("GameID", gameID);
			message.setIntProperty("PlayerID", playerID);
			message.setStringProperty("Content", content);

			messagesHandler.sendQueueMessage(message);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void drawSign(int column, int row, Sign sign) {
		StackPane field;
		Color color = isPlayerMove ? playerColor : opponentColor;

		if (sign == Sign.CIRCLE)
			field = new CircleShape(35, color);
		else if (sign == Sign.CROSS)
			field = new CrossShape(35, color);
		else if (sign == Sign.EMPTY) {
			field = new StackPane();
			field.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> gameBoard_Click(event));
			field.setCursor(Cursor.HAND);
		} else
			return;

		board.add(field, column, row);
	}

	private void removeSign(int column, int row) {
		for (Node node : board.getChildren()) {
			if (GridPane.getColumnIndex(node) == column && GridPane.getRowIndex(node) == row) {
				board.getChildren().remove(node);
				return;
			}
		}
	}

	private void clearBoard() {
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				removeSign(i, j);
				drawSign(i, j, Sign.EMPTY);
			}
		}
	}

	private Boolean isPlayerState(GameState state) {
		return (playerSign == Sign.CIRCLE && (state == GameState.CIRCLE_TURN || state == GameState.CIRCLE_WIN))
				|| (playerSign == Sign.CROSS && (state == GameState.CROSS_TURN || state == GameState.CROSS_WIN));
	}

	private void setStateText(String text1, String text2, String text3) {
		stateText1.setText(text1);
		stateText2.setText(text2);
		stateText3.setText(text3);
	}

	private void playWaitingAnimations() {
		TranslateTransition tt = new TranslateTransition(Duration.millis(300), logo);
		tt.setByY(-110f);
		tt.setInterpolator(Interpolator.EASE_OUT);
		tt.play();

		TranslateTransition tt2 = new TranslateTransition(Duration.millis(300), stateBox);
		tt2.setByY(137f);
		tt2.setInterpolator(Interpolator.EASE_OUT);
		tt2.play();

		tt2.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				isWaitingAnimationFinished.countDown();
			}
		});

		gameButton.setDisable(true);
		gameButton.setText("waiting...");
		setStateText("", "Searching for opponent...", "");
	}

	private void playStartGameAnimations() {
		TranslateTransition tt = new TranslateTransition(Duration.millis(300), logo);

		tt.setByY(-140f);
		tt.setInterpolator(Interpolator.EASE_OUT);
		tt.play();

		TranslateTransition tt2 = new TranslateTransition(Duration.millis(300), stateBox);
		tt2.setByY(-210f);
		tt2.setInterpolator(Interpolator.EASE_OUT);
		tt2.play();

		TranslateTransition tt3 = new TranslateTransition(Duration.millis(300), gameButton);
		tt3.setByY(250f);
		tt3.setInterpolator(Interpolator.EASE_OUT);
		tt3.play();

		FadeTransition ft = new FadeTransition(Duration.millis(300), gameBoard);
		ft.setFromValue(0.0);
		ft.setToValue(1.0);
		ft.play();

		gameBoard.setVisible(true);
		gameButton.setText("reset game");
		gameButton.setOnAction(event -> resetGame_click());
	}

	@Override
	public void onMessage(Message message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				processMessage(message);
			}
		});
	}

	public void close() {
		messagesHandler.recieveAll();
		messagesHandler.close();
	}

}
