package com.test.drawingcanvas;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class HelloController {

    private static final int ROWS = 16;
    private static final int COLS = 16;

    private Color curColor = Color.BLACK;
    private Mode curMode = Mode.Pencil;

    @FXML
    private GridPane grid;

    @FXML
    private ColorPicker colorPicker;

    private Rectangle[][] pixels;

    @FXML
    public void initialize() {

        pixels = new Rectangle[ROWS][COLS];

        // CRITICAL: allow GridPane to expand
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        grid.setSnapToPixel(true);
        // clear any constraints
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        // make columns expand evenly
        for (int i = 0; i < COLS; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / COLS);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        // make rows expand evenly
        for (int i = 0; i < ROWS; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / ROWS);
            row.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(row);
        }

        grid.setOnMouseDragged(e -> {
            int col = (int)(e.getX() / (grid.getWidth() / COLS));
            int row = (int)(e.getY() / (grid.getHeight() / ROWS));

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS){
                applyTool(row, col);
            }
        });

        colorPicker.setValue(curColor);
        colorPicker.setOnAction(e -> curColor = colorPicker.getValue());

        // create cells
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                StackPane cell = new StackPane();

                // CRITICAL: allow cell to expand
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                Rectangle rect = new Rectangle();
                rect.setFill(Color.WHITE);
                rect.setStroke(Color.LIGHTGRAY);

                rect.setStrokeType(StrokeType.INSIDE);
                rect.setStrokeWidth(1);
                // rectangle fills the cell
                rect.widthProperty().bind(cell.widthProperty());
                rect.heightProperty().bind(cell.heightProperty());

                cell.getChildren().add(rect);

                int r = row;
                int c = col;

                cell.setOnMousePressed(e -> applyTool(r, c));
                cell.setOnMouseDragged(e -> applyTool(r, c));

                pixels[row][col] = rect;

                grid.add(cell, col, row);
            }
        }
    }

    private void applyTool(int row, int col) {

        switch (curMode) {

            case Pencil:
                pixels[row][col].setFill(curColor);
                break;

            case Eraser:
                pixels[row][col].setFill(Color.WHITE);
                break;

            case Fill:
                break;
        }
    }

    @FXML
    public void selectPencil() {
        curMode = Mode.Pencil;
    }

    @FXML
    public void selectEraser() {
        curMode = Mode.Eraser;
    }

    @FXML
    public void selectFill() {
        curMode = Mode.Fill;
    }

    @FXML
    public void selectClear() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                pixels[r][c].setFill(Color.WHITE);
    }
}