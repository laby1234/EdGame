package org.example.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProfessionalButton extends VBox {
    private Button button;
    private ImageView iconView;
    private boolean hasIcon;

    public ProfessionalButton(String text) {
        this(text, null);
    }

    public ProfessionalButton(String text, String iconPath) {
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");
        setSpacing(0);

        button = new Button(text);
        // Try to use provided button images (normal / hover / pressed). If images are missing, fall back to CSS styles.
        String normalUrl = null;
        String hoverUrl = null;
        String pressedUrl = null;
        try {
            if (AssetManager.class.getResource("/" + AssetManager.BTN_NORMAL) != null)
                normalUrl = AssetManager.class.getResource("/" + AssetManager.BTN_NORMAL).toExternalForm();
            if (AssetManager.class.getResource("/" + AssetManager.BTN_HOVER) != null)
                hoverUrl = AssetManager.class.getResource("/" + AssetManager.BTN_HOVER).toExternalForm();
            if (AssetManager.class.getResource("/" + AssetManager.BTN_PRESSED) != null)
                pressedUrl = AssetManager.class.getResource("/" + AssetManager.BTN_PRESSED).toExternalForm();
        } catch (Exception ignored) {}

        button.setMinWidth(300);
        button.setMinHeight(60);
        button.setFont(AssetManager.getTextFont());

        if (normalUrl != null && hoverUrl != null && pressedUrl != null) {
            // Build inline styles that use background images
            String baseStyle = "-fx-background-color: transparent; -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-size: contain;";
            String normalStyle = baseStyle + " -fx-background-image: url('" + normalUrl + "');";
            String hoverStyle = baseStyle + " -fx-background-image: url('" + hoverUrl + "');";
            String pressedStyle = baseStyle + " -fx-background-image: url('" + pressedUrl + "');";

            button.setStyle(normalStyle);

            // Hover/press effects
            button.setOnMouseEntered(e -> {
                button.setStyle(hoverStyle);
                button.setTranslateY(-2);
                button.setTranslateX(0);
            });

            button.setOnMouseExited(e -> {
                button.setStyle(normalStyle);
                button.setTranslateY(0);
                button.setTranslateX(0);
            });

            button.setOnMousePressed(e -> {
                button.setStyle(pressedStyle);
                button.setTranslateY(2);
            });

            button.setOnMouseReleased(e -> {
                // Keep hover style on release if mouse still over
                button.setStyle(hoverStyle);
                button.setTranslateY(-2);
            });
        } else {
            // Fallback to existing CSS styles
            button.setStyle(UIStyle.BUTTON_NORMAL);
            // Hover effects - Medieval Style with 3D effect
            button.setOnMouseEntered(e -> {
                button.setStyle(UIStyle.BUTTON_HOVER);
                button.setTranslateY(-2);  // Lift button up
                button.setTranslateX(0);
            });

            button.setOnMouseExited(e -> {
                button.setStyle(UIStyle.BUTTON_NORMAL);
                button.setTranslateY(0);
                button.setTranslateX(0);
            });

            button.setOnMousePressed(e -> {
                button.setStyle(UIStyle.BUTTON_PRESSED);
                button.setTranslateY(2);  // Push button down
            });

            button.setOnMouseReleased(e -> {
                button.setStyle(UIStyle.BUTTON_HOVER);
                button.setTranslateY(-2);
            });
        }

        // Add icon if provided
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                Image iconImage = new Image(iconPath);
                iconView = new ImageView(iconImage);
                iconView.setFitWidth(24);
                iconView.setFitHeight(24);
                button.setGraphic(iconView);
                hasIcon = true;
            } catch (Exception e) {
                System.err.println("Could not load icon: " + iconPath);
                hasIcon = false;
            }
        }

        // (mouse handlers set above depending on whether images exist)

        getChildren().add(button);
    }

    public void setOnAction(EventHandler<ActionEvent> handler) {
        button.setOnAction(handler);
    }

    public Button getButton() {
        return button;
    }

    public void setText(String text) {
        button.setText(text);
    }

    public String getText() {
        return button.getText();
    }

    public void setEnabled(boolean enabled) {
        button.setDisable(!enabled);
        if (!enabled) {
            button.setStyle(UIStyle.BUTTON_BASE +
                    " -fx-padding: 12px 40px; " +
                    " -fx-text-fill: #696969; " +
                    " -fx-background-color: linear-gradient(to bottom, #4a4a4a 0%, #2a2a2a 100%); " +
                    " -fx-border-width: 3; " +
                    " -fx-border-color: #696969 #2a2a2a #2a2a2a #696969;");
        } else {
            button.setStyle(UIStyle.BUTTON_NORMAL);
        }
    }

    public void setMinSize(double width, double height) {
        button.setMinWidth(width);
        button.setMinHeight(height);
    }
}

