package de.dkfz.sbst.tichawa.util.converter;

import de.dkfz.sbst.tichawa.util.converter.parser.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import javafx.event.*;
import javafx.fxml.*;
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
  private final Parser<String, String> parser = new SimpleStringParser(";", "\t");

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
      if(getParser().isReady())
      {
        dropArea.getStyleClass().add("highlight");
      }
      else
      {
        dropArea.getStyleClass().add("blocked");
      }
      dragEvent.consume();
    });
    dropArea.setOnDragExited(dragEvent ->
    {
      if(getParser().isReady())
      {
        dropArea.getStyleClass().remove("highlight");
      }
      else
      {
        dropArea.getStyleClass().remove("blocked");
      }
      dragEvent.consume();
    });
    dropArea.setOnDragOver(dragEvent ->
    {
      if(dragEvent.getDragboard().hasFiles() && getParser().isReady())
      {
        dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
      }
      dragEvent.consume();
    });

    dropArea.setOnDragDropped(dragEvent ->
    {
      Dragboard db = dragEvent.getDragboard();
      AtomicBoolean success = new AtomicBoolean(false);
      if(db.hasFiles() && getParser().isReady())
      {
        dropArea.getStyleClass().add("working");
        success.set(true);
        Map<Path, List<String>> parsed = db.getFiles().stream()
            .filter(File::isFile)
            .filter(File::canRead)
            .map(File::toPath)
            .collect(Collectors.toMap(Function.identity(), path ->
            {
              try
              {
                return Stream.concat(Stream.of(getParser().encodeHeader()), Files.lines(path)
                    .skip(1)
                    .map(getParser()::translate))
                    .collect(Collectors.toList());
              }
              catch(IOException ex)
              {
                success.set(false);
                return Collections.emptyList();
              }
            }));

        List<Integer> lineCounts = parsed.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .filter(e ->
            {
              try
              {
                Files.write(e.getKey().resolveSibling(e.getKey().getFileName().toString() + "_converted.tsv"),
                    e.getValue());
                return true;
              }
              catch(IOException ex)
              {
                success.set(false);
                return false;
              }
            }).map(e -> e.getValue().size())
            .collect(Collectors.toList());

        if(success.get())
        {
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

      dragEvent.setDropCompleted(success.get());
      dragEvent.consume();
    });
  }

  @FXML
  private void loadConfig(ActionEvent actionEvent)
  {
    actionEvent.consume();

    FileChooser configChooser = new FileChooser();
    configChooser.getExtensionFilters().clear();
    configChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Configuration files","*.cfg"));
    configChooser.setTitle("Open Configuration File");

    File configFile = configChooser.showOpenDialog(getRootPane().getScene().getWindow());
    boolean success = Optional.ofNullable(configFile)
        .flatMap(Configuration::fromFile)
        .flatMap(config ->
        {
          FileChooser templateChooser = new FileChooser();
          templateChooser.getExtensionFilters().clear();
          templateChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Template files", "*.csv"));
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
              }).flatMap(getParser()::parseHeaderLine)
              .filter(inHeaders -> getParser().configure(config, inHeaders))
              .map(h -> configFile.getName());

          getConfigLabel().setText("Current Configuration: " + configName.orElse("-/-"));
          return configName;
        }).isPresent();

    if(success)
    {
      getDropArea().getStyleClass().add("ready");
      new Alert(Alert.AlertType.INFORMATION,"Configuration loaded.").showAndWait();
      getStatusLabel().setText("Configuration loaded.");
    }
    else
    {
      getDropArea().getStyleClass().remove("ready");
      new Alert(Alert.AlertType.ERROR,"Error in configuration file.").showAndWait();
      getStatusLabel().setText("Error in configuration file.");
    }
  }
}
