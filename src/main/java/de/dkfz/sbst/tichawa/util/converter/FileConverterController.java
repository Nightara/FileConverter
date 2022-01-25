package de.dkfz.sbst.tichawa.util.converter;

import de.dkfz.sbst.tichawa.util.converter.parser.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import lombok.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;

@Getter(AccessLevel.PRIVATE)
public class FileConverterController implements Initializable
{
  private final StringProperty status = new SimpleStringProperty("No configuration loaded.");
  private final ObjectProperty<Parser<String, String>> parser = new SimpleObjectProperty<>();

  @FXML
  private BorderPane rootPane;
  @FXML
  private Menu configMenu;
  @FXML
  private MenuItem configLabel;
  @FXML
  private MenuItem loadConfig;
  @FXML
  private Label statusLabel;
  @FXML
  private Pane dropArea;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
    dropArea.getStyleClass().add("dashed-border");
    dropArea.setOnDragEntered(dragEvent ->
    {
      dropArea.getStyleClass().add(getParser().isNull().get() ? "blocked" : "highlight");
      dragEvent.consume();
    });
    dropArea.setOnDragExited(dragEvent ->
    {
      dropArea.getStyleClass().remove(getParser().isNull().get() ? "blocked" : "highlight");
      dragEvent.consume();
    });
    dropArea.setOnDragOver(dragEvent ->
    {
      if(dragEvent.getDragboard().hasFiles() && getParser().getValue().isReady())
      {
        dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
      }
      dragEvent.consume();
    });

    dropArea.setOnDragDropped(dragEvent ->
    {
      boolean success = false;
      Dragboard db = dragEvent.getDragboard();
      if(db.hasFiles() && getParser().isNotNull().get())
      {
        dropArea.getStyleClass().add("working");
        Map<Path, List<String>> parsed = db.getFiles().stream()
            .filter(File::isFile)
            .filter(File::canRead)
            .map(File::toPath)
            .collect(Collectors.toMap(Function.identity(), this::parseLines));

        List<Integer> lineCounts = parsed.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .filter(e -> exportResult(e.getKey(), e.getValue()))
            .map(e -> e.getValue().size())
            .collect(Collectors.toList());

        if(parsed.size() == lineCounts.size())
        {
          success = true;
          new Alert(Alert.AlertType.INFORMATION,"Parsed " + lineCounts.stream().reduce(0, Integer::sum)
              + " lines over " + lineCounts.size() + " files.").showAndWait();
        }
        else
        {
          new Alert(Alert.AlertType.ERROR,"Parsed " + lineCounts.stream().reduce(0, Integer::sum)
              + " lines over " + lineCounts.size() + " files. " + (parsed.size() - lineCounts.size())
              + " files produced errors.").showAndWait();
        }

        dropArea.getStyleClass().remove("working");
      }

      dragEvent.setDropCompleted(success);
      dragEvent.consume();
    });

    configLabel.textProperty().bind(Bindings.createStringBinding(() ->
        "Configuration: " + (parser.isNull().get() ? "-/-" : parser.get().getName()), parser));
    statusLabel.textProperty().bind(status);
  }

  @FXML
  private void loadConfig(ActionEvent a)
  {
    AtomicBoolean success = new AtomicBoolean(false);
    try
    {
      FXMLLoader loader = new FXMLLoader();
      URL fxml = getClass().getResource("Settings.fxml");
      if(fxml != null)
      {
        Parent root = loader.load(fxml.openStream());

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        URL css = getClass().getResource("style.css");
        if(css != null)
        {
          stage.getScene().getStylesheets().add(css.toExternalForm());
        }
        stage.setTitle("File Converter");

        ((SettingsController) loader.getController()).parserProperty().addListener((obs, oldVal, newVal) ->
        {
          if(newVal != null)
          {
            getParser().set(newVal);
            success.set(true);
          }
        });
        stage.showAndWait();
      }
    }
    catch(IOException ex)
    {
      success.set(legacyLoadConfig(a));
    }

    if(success.get())
    {
      getDropArea().getStyleClass().add("ready");
      getStatus().set("Configuration loaded.");
    }
    else
    {
      getDropArea().getStyleClass().remove("ready");
      getStatus().set("Error in configuration file..");
    }

    new Alert(getStatus().get().contains("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION,
        getStatus().get()).showAndWait();
  }

  @FXML
  @SuppressWarnings("unused")
  private boolean legacyLoadConfig(ActionEvent a)
  {
    FileChooser configChooser = new FileChooser();
    configChooser.getExtensionFilters().clear();
    configChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Configuration files","*.cfg"));
    configChooser.setTitle("Open Configuration File");

    File configFile = configChooser.showOpenDialog(getRootPane().getScene().getWindow());
    return Optional.ofNullable(configFile)
        .flatMap(Configuration::fromFile)
        .flatMap(config ->
        {
          FileChooser templateChooser = new FileChooser();
          templateChooser.getExtensionFilters().clear();
          templateChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Template files","*.csv"));
          templateChooser.setTitle("Open Input Template File");

          Optional<String> configName = Optional.ofNullable(templateChooser.showOpenDialog(getRootPane().getScene().getWindow()))
              .map(File::toPath)
              .map(path ->
              {
                try(BufferedReader reader = Files.newBufferedReader(path))
                {
                  return reader.readLine();
                }
                catch(IOException ex)
                {
                  return null;
                }
              }).flatMap(getParser().get()::parseHeaderLine)
              .filter(inHeaders -> getParser().get().configure(config, inHeaders))
              .map(h -> configFile.getName());

          getConfigLabel().setText("Current Configuration: " + configName.orElse("-/-"));
          return configName;
        }).isPresent();
  }

  private List<String> parseLines(Path path)
  {
    try
    {
      return Stream.concat(Stream.of(getParser().get().encodeHeader()), Files.lines(path)
          .skip(1)
          .map(getParser().get()::translate))
          .collect(Collectors.toList());
    }
    catch(IOException ex)
    {
      return Collections.emptyList();
    }
  }

  private boolean exportResult(Path path, List<String> data)
  {
    try
    {
      Files.write(path.resolveSibling(path.getFileName().toString() + "_converted.tsv"), data);
      return true;
    }
    catch(IOException ex)
    {
      return false;
    }
  }
}
