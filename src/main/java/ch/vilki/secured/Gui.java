package ch.vilki.secured;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Gui extends Application {

    public Stage get_primaryStage() {
        return _primaryStage;
    }

    private Stage _primaryStage;
    MainWindowController _mainWindowController = null;
    @Override
    public void start(Stage primaryStage) throws Exception {

           Platform.startup(()->{
            Stage stage = new Stage();

            FXMLLoader loader = new FXMLLoader();
            URL url = Gui.class.getResource("/ch/vilki/secured/MainWindow.fxml");

            if(url == null) try {
                throw new Exception("URL could not be resolved MainWindow.xml " ) ;
            } catch (Exception e) {
                e.printStackTrace();
            }
            loader.setLocation(url);
            Parent parent = null;
            try {
                parent = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            _mainWindowController = loader.getController();
            _mainWindowController.set_main(this);
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();

            this._primaryStage = stage;

            Scene scene = new Scene(parent, bounds.getWidth()-200, bounds.getHeight()-200);

            this._primaryStage.setTitle("Secured Properties, Version=" + ApplicationVersion.VERSION );
            this._primaryStage.setOnCloseRequest(x->{System.exit(0);});

             _primaryStage.setScene(scene);
            BorderPane borderPane = (BorderPane) parent;
            borderPane.getChildren();

            _primaryStage.sizeToScene();
            _primaryStage.show();
        });
    }
    @FXML
    private void initialize() {}

}
