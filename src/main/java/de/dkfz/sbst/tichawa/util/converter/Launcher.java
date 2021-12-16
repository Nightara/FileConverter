package de.dkfz.sbst.tichawa.util.converter;

import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;

import java.io.*;
import java.net.*;

public class Launcher extends Application
{

  public static void main(String[] args)
  {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws IOException
  {
    FXMLLoader loader = new FXMLLoader();
    URL fxml = getClass().getResource("FileConverter.fxml");
    if(fxml != null)
    {
      Parent root = loader.load(fxml.openStream());

      stage.setScene(new Scene(root));
      URL css = getClass().getResource("style.css");
      if(css != null)
      {
        stage.getScene().getStylesheets().add(css.toExternalForm());
      }
      stage.setTitle("File Converter");

      stage.show();
    }
  }
}
