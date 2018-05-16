package application;

public class GameLogic {
	enum Sign {
		EMPTY, CIRCLE, CROSS
	}

	enum GameState {
		CIRCLE_TURN, CROSS_TURN, CIRCLE_WIN, CROSS_WIN, DRAW_GAME
	}

	public static final int NUM_ROWS = 3;
	public static final int NUM_COLUMNS = 3;

	Sign[][] boardArray;
	GameState gameState;

	int crossResults;
	int circleResults;

	public GameLogic() {
		this(Sign.CIRCLE);
	}

	public GameLogic(Sign whoStarts) {
		boardArray = new Sign[NUM_COLUMNS][NUM_ROWS];
		crossResults = circleResults = 0;
		gameState = GameState.CIRCLE_TURN;
		if (whoStarts == Sign.CROSS)
			gameState = GameState.CROSS_TURN;

		resetGame();
	}

	private Boolean checkHorizontally(Sign sign) {
		Boolean allTheSame;
		for (int row = 0; row < NUM_ROWS; ++row) {
			allTheSame = true;
			for (int col = 0; col < NUM_COLUMNS; ++col) {
				if (boardArray[col][row] != sign) {
					allTheSame = false;
					break;
				}
			}
			if (allTheSame)
				return true;
		}
		return false;
	}

	private Boolean checkVertically(Sign sign) {
		Boolean allTheSame;
		for (int col = 0; col < NUM_COLUMNS; ++col) {
			allTheSame = true;
			for (int row = 0; row < NUM_ROWS; ++row) {
				if (boardArray[col][row] != sign) {
					allTheSame = false;
					break;
				}
			}
			if (allTheSame)
				return true;
		}
		return false;
	}

	private Boolean checkDiagonally(Sign sign) {
		Boolean allTheSame = true;

		/* Check form left bottom to right top */
		for (int row = NUM_ROWS - 1, col = 0; row >= 0 && col < NUM_COLUMNS; --row, ++col) {
			if (boardArray[col][row] != sign) {
				allTheSame = false;
				break;
			}
		}
		if (allTheSame)
			return true;

		/* Check form left top to right bottom */
		allTheSame = true;
		for (int col = 0; col < NUM_COLUMNS; ++col) {
			if (boardArray[col][col] != sign) {
				allTheSame = false;
				break;
			}
		}
		if (allTheSame)
			return true;

		return false;
	}

	private void checkForWin() {
		if (checkHorizontally(Sign.CIRCLE) || checkVertically(Sign.CIRCLE) || checkDiagonally(Sign.CIRCLE)) {
			gameState = GameState.CIRCLE_WIN;
			circleResults++;
		} else if (checkHorizontally(Sign.CROSS) || checkVertically(Sign.CROSS) || checkDiagonally(Sign.CROSS)) {
			gameState = GameState.CROSS_WIN;
			crossResults++;
		} else if (isTheBoardFull())
			gameState = GameState.DRAW_GAME;
	}

	private Boolean isTheBoardFull() {
		for (int col = 0; col < NUM_COLUMNS; ++col) {
			for (int row = 0; row < NUM_ROWS; ++row) {
				if (boardArray[col][row] == Sign.EMPTY)
					return false;
			}
		}
		return true;
	}

	private void changeTurn() {
		if (gameState == GameState.CIRCLE_TURN)
			gameState = GameState.CROSS_TURN;
		else if (gameState == GameState.CROSS_TURN)
			gameState = GameState.CIRCLE_TURN;
	}

	public Sign setField(int column, int row) {
		if (row < 0 || row >= NUM_ROWS || column < 0 || column >= NUM_COLUMNS)
			return null; // Outside board
		if (isGameEnded())
			return null; // Game ended
		if (boardArray[column][row] != Sign.EMPTY)
			return null; // Not empty field

		Sign sign = gameState == GameState.CIRCLE_TURN ? Sign.CIRCLE : Sign.CROSS;
		boardArray[column][row] = sign;

		changeTurn();
		checkForWin();

		return sign;
	}

	public Boolean isGameEnded() {
		return gameState != GameState.CIRCLE_TURN && gameState != GameState.CROSS_TURN;
	}

	public Boolean isDraw() {
		return gameState == GameState.DRAW_GAME;
	}

	public void resetGame() {
		for (int col = 0; col < NUM_COLUMNS; ++col) {
			for (int row = 0; row < NUM_ROWS; ++row) {
				boardArray[col][row] = Sign.EMPTY;
			}
		}
		gameState = GameState.CIRCLE_TURN;
	}

	public GameState getState() {
		return gameState;
	}
}
