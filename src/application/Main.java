package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("gameWindow.fxml"));
			AnchorPane root = fxmlLoader.load();

			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setScene(scene);
			primaryStage.setTitle("Tic Tac Toe");
			primaryStage.setOnHiding(e -> primaryStage_Hiding(e, fxmlLoader));
			primaryStage.setResizable(false);
			primaryStage.sizeToScene();
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void primaryStage_Hiding(WindowEvent e, FXMLLoader fxmlLoader) {
		if (e == null)
			System.out.println("e is null");
		if (fxmlLoader == null)
			System.out.println("fxmlLoader is null");
		if (fxmlLoader.getController() == null)
			System.out.println("fxmlLoader.getController() is null");
		((GameController) fxmlLoader.getController()).close();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
