package application;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CrossShape extends StackPane {
	private final Rectangle rectangle1;
	private final Rectangle rectangle2;

	CrossShape(double size, Color color) {
		rectangle1 = new Rectangle(size * 0.2, size * 2 , color);
		rectangle2 = new Rectangle(size * 0.2, size * 2, color);
		rectangle1.setRotate(45);
		rectangle2.setRotate(-45);

		getChildren().addAll(rectangle1, rectangle2);
	}
}
