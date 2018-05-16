package application;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class CircleShape extends StackPane {
	private final Shape circle;

	CircleShape(double radius, Color color) {
		circle = Shape.subtract(new Circle(radius), new Circle(radius * 0.8));
		circle.setFill(color);
		
		getChildren().add(circle);
	}
}
