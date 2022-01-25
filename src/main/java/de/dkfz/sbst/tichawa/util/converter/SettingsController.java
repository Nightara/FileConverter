package de.dkfz.sbst.tichawa.util.converter;

import de.dkfz.sbst.tichawa.util.converter.parser.*;
import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.util.*;
import lombok.*;
import lombok.experimental.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

@Getter(AccessLevel.PRIVATE)
public class SettingsController implements Initializable
{
  private static final Map<Character, String> SEPARATOR_MAP = new HashMap<>();
  static
  {
    SEPARATOR_MAP.put('\t',"Tab");
    SEPARATOR_MAP.put(',',"Comma");
    SEPARATOR_MAP.put(';',"Semicolon");
    SEPARATOR_MAP.put('\n',"Line Break");
  }

  @Accessors(fluent=true)
  @Getter(AccessLevel.PUBLIC)
  private final Property<Parser<String, String>> parserProperty = new SimpleObjectProperty<>();

  @Setter(AccessLevel.PRIVATE)
  private String[] headers;
  @Setter(AccessLevel.PRIVATE)
  private Configuration config;

  @FXML
  private TitledPane rootPane;
  @FXML
  private ComboBox<Character> fieldSeparatorBox;
  @FXML
  private TextField selectConfigField;
  @FXML
  private TextField selectTemplateField;
  @FXML
  private ComboBox<Character> outFieldSeparatorBox;
  @FXML
  private TextField selectOutputField;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)
  {
    getFieldSeparatorBox().setConverter(new SeparatorConverter(';'));
    getFieldSeparatorBox().setItems(FXCollections.observableArrayList('\t', ',', ';'));

    getOutFieldSeparatorBox().setConverter(new SeparatorConverter('\t'));
    getOutFieldSeparatorBox().setItems(FXCollections.observableArrayList('\t', ',', ';'));
  }

  @FXML
  @SuppressWarnings("unused")
  private void selectConfigFile(ActionEvent a)
  {
    FileChooser configChooser = new FileChooser();
    configChooser.getExtensionFilters().clear();
    configChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Configuration files","*.cfg"));
    configChooser.setTitle("Open Configuration File");

    File configFile = configChooser.showOpenDialog(getRootPane().getScene().getWindow());
    Optional<Configuration> conf = Configuration.fromFile(configFile);
    if(conf.isPresent())
    {
      setConfig(conf.get());
      getSelectConfigField().setText(configFile.getName());
    }
    else
    {
      new Alert(Alert.AlertType.ERROR,"This file does not exist or is not a valid configuration.")
          .showAndWait();
    }
  }

  @FXML
  @SuppressWarnings({"java:S2095", "unused"})
  private void selectTemplateFile(ActionEvent a)
  {
    FileChooser configChooser = new FileChooser();
    configChooser.getExtensionFilters().clear();
    configChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Character separated files","*.csv", "*.tsv"));
    configChooser.setTitle("Open Template File");

    File templateFile = configChooser.showOpenDialog(getRootPane().getScene().getWindow());
    try
    {
      Files.lines(templateFile.toPath())
          .map(line -> line.split(getSelectedOrDefault(fieldSeparatorBox).toString()))
          .findFirst()
          .ifPresent(head ->
          {
            setHeaders(head);
            getSelectTemplateField().setText(templateFile.getName());
          });
    }
    catch(NullPointerException | IOException ex)
    {
      new Alert(Alert.AlertType.ERROR,"This file does not exist or is not a valid template.")
          .showAndWait();
    }
  }

  @FXML
  @SuppressWarnings("unused")
  private void selectOutputDir(ActionEvent a)
  {
    // TODO: Store output dir.
  }

  @FXML
  @SuppressWarnings("unused")
  private void saveOptions(ActionEvent a)
  {
    String fieldSeparator = getSelectedOrDefault(fieldSeparatorBox).toString();
    String outFieldSeparator = getSelectedOrDefault(outFieldSeparatorBox).toString();

    Parser<String, String> parser = new SimpleStringParser("Custom", fieldSeparator, outFieldSeparator);
    getConfig().ifPresent(conf -> getHeaders().ifPresent(head -> parser.configure(conf, head)));
    parserProperty().setValue(parser);

    ((Stage) getRootPane().getScene().getWindow()).close();
  }

  private Character getSelectedOrDefault(ComboBox<Character> comboBox)
  {
    Character selected = comboBox.getSelectionModel().getSelectedItem();
    if(selected == null)
    {
      selected = ((SeparatorConverter) comboBox.getConverter()).getDefaultChar();
    }

    return selected;
  }

  private Optional<Configuration> getConfig()
  {
    return Optional.ofNullable(config);
  }

  public Optional<String[]> getHeaders()
  {
    return Optional.ofNullable(headers);
  }

  @Value
  @AllArgsConstructor
  @EqualsAndHashCode(callSuper=true)
  private static class SeparatorConverter extends StringConverter<Character>
  {
    char defaultChar;
    String defaultDesc;

    private SeparatorConverter(char defaultChar)
    {
      this(defaultChar, SEPARATOR_MAP.getOrDefault(defaultChar,"" + defaultChar));
    }

    @Override
    public String toString(Character c)
    {
      if(c == getDefaultChar())
      {
        return getDefaultDesc() + " (Default)";
      }
      else
      {
        return SEPARATOR_MAP.getOrDefault(c,c + " (Unknown)");
      }
    }

    @Override
    public Character fromString(String s)
    {
      if(getDefaultDesc().equals(s))
      {
        return getDefaultChar();
      }
      else
      {
        return SEPARATOR_MAP.entrySet().stream()
            .filter(e -> e.getValue().equals(s))
            .map(Map.Entry::getKey)
            .findAny()
            .orElse(getDefaultChar());
      }
    }
  }
}
