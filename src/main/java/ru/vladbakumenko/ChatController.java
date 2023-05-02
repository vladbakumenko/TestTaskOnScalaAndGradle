package ru.vladbakumenko;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatController {
    @FXML
    protected ListView<String> listViewOfMembers;

    @FXML
    protected TextArea logArea;

    @FXML
    protected TextField messageField;

    @FXML
    public void initialize() {
    }
}