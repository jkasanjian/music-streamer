package controller;

import controller.HomePageController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class DeletePlaylistController {
    @FXML
    ChoiceBox<String> playlists;

    @FXML
    Button submitButton;

    HomePageController hpc;

    String action;

    @FXML
    public void initialize(){}

    @FXML
    public void button(ActionEvent event) throws IOException {

        if(event.getSource() == submitButton) {
            if(action.equals("delete")) {
                String playlistName = playlists.getSelectionModel().getSelectedItem();
                hpc.deletePlaylist(playlistName);
                ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();
            }else if(action.equals("add")){
                String playlistName = playlists.getSelectionModel().getSelectedItem();
                hpc.addNewSongToPlaylist(playlistName);
                ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();
            }
        }
    }

    public void setPrevController(HomePageController hpc){
        this.hpc = hpc;
    }

    public void setListOfPlaylists(ListView playlists){

        this.playlists.getItems().addAll(playlists.getItems());
    }

    public void setAction(String action){
        this.action = action;
    }

    @FXML
    public void enter( MouseEvent e ){
        Button button = (Button)e.getSource();
        button.setStyle("-fx-background-color: #70C2FF");
    }

    @FXML
    public void exit( MouseEvent e ){
        Button button = (Button)e.getSource();
        button.setStyle("-fx-background-color: #668FE8");
    }
}
