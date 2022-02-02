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
import java.util.function.*;
import java.util.stream.*;

@Getter(AccessLevel.PRIVATE)
public class FileConverterController implements Initializable
{
  private final StringProperty status = new SimpleStringProperty("No configuration loaded.");
  private final ObjectProperty<Parser<String, String>> parser = new SimpleObjectProperty<>();

  private final Parser<String, String> defaultEnrollmentParser;
  private final Parser<String, String> defaultAnamnesisParser;
  private final Parser<String, String> defaultQuestionnaireParser;
  private final Parser<String, String> defaultMedicationParser;
  private final Parser<String, String> defaultWeeklyParser;
  private final Parser<String, String> defaultMonthlyParser;

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
  @FXML
  @Getter(AccessLevel.NONE)
  private MenuItem loadEnrollment;
  @FXML
  @Getter(AccessLevel.NONE)
  private MenuItem loadAnamnesis;
  @FXML
  @Getter(AccessLevel.NONE)
  private MenuItem loadQuestionnaire;
  @FXML
  @Getter(AccessLevel.NONE)
  private MenuItem loadMedication;
  @FXML
  @Getter(AccessLevel.NONE)
  private MenuItem loadWeekly;
  @FXML
  @Getter(AccessLevel.NONE)
  private MenuItem loadMonthly;

  public FileConverterController()
  {
    this.defaultEnrollmentParser = new SimpleStringParser("Enrollment",null,
        ";","\t");
    Configuration.fromFile(getClass().getResourceAsStream("/patient.cfg"))
        .ifPresent(conf -> defaultEnrollmentParser.configure(conf, "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id;openbis_id;patient_type;gender_enroll;gender_divers_enroll;visit_date;consent_date;version_number;consent_data_protection;consent_data_other_counties;consent_biomaterial;consent_renewed_contact;questionnaire;questionnaire_date;comments;test_enrollment_and_consent_complete".split(";")));

    this.defaultAnamnesisParser = new SimpleStringParser("Anamnesis",null,
        ";","\t");
    Configuration.fromFile(Configuration.class.getResourceAsStream("/anamnesis.cfg"))
        .ifPresent(conf -> defaultAnamnesisParser.configure(conf, "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id_enroll_fu;weight_enroll_fu;height_enroll_fu;bmi_enroll_fu;visit_type_enroll_fu;visit_date_enroll_fu;basic_renal_disease_enroll_fu;dkd_enroll_fu;htn_enroll_fu;gn_enroll_fu;gn_type_enroll_fu___1;gn_type_enroll_fu___2;gn_type_enroll_fu___3;gn_type_enroll_fu___4;gn_type_enroll_fu___5;gn_type_enroll_fu___6;gn_type_enroll_fu___7;gn_type_enroll_fu___8;gn_type_enroll_fu___9;gn_type_enroll_fu___10;gn_type_enroll_fu___11;gn_type_enroll_fu___12;gn_other_enroll_fu;fsgs_enroll_fu;geneticdis_enroll_fu;geneticdis_type_enroll_fu___1;geneticdis_type_enroll_fu___2;geneticdis_type_enroll_fu___3;geneticdis_type_enroll_fu___4;geneticdis_type_enroll_fu___5;geneticdis_type_enroll_fu___6;geneticdis_other_enroll_fu;intnephr_enroll_fu;intnephr_type_enroll_fu;intnephr_other_enroll_fu;amyloidosis_enroll_fu;other_kiddis_enroll_fu;other_kiddis_type_enroll_fu;cvd_enroll_fu;cvd_type_enroll_fu___1;cvd_type_enroll_fu___2;cvd_type_enroll_fu___3;cvd_type_enroll_fu___4;cvd_type_enroll_fu___5;cvd_type_enroll_fu___6;cvd_other_enroll_fu;thrombev_enroll_fu;thrombev_type_enroll_fu___1;thrombev_type_enroll_fu___2;thrombev_type_enroll_fu___3;thrombev_other_enroll_fu;diab_enroll_fu;thyroiddys_enroll_fu;thyroiddys_type_enroll_fu;rheumdiseas_enroll_fu;rheumdis_type_enroll_fu___1;rheumdis_type_enroll_fu___2;maligdiseas_enroll_fu;maligdiseas_type_enroll_fu;infectdis_enroll_fu;infectdis_type_enroll_fu___1;infectdis_type_enroll_fu___2;infectdis_type_enroll_fu___3;infectdis_type_enroll_fu___4;infectdis_type_enroll_fu___5;infectdis_type_enroll_fu___6;infectdis_type_enroll_fu___7;hepatitis_type_enroll_fu;hiv_infect_type_enroll_fu;bactinfect_type_enroll_fu;infectdis_other_enroll_fu;ibd_enroll_fu;ibd_type_enroll_fu;allergies_enroll_fu;allergies_type_enroll_fu;hearinglo_enroll_fu;otherdis_enroll_fu;otherdis_type_enroll_fu;dialysisobl_enroll_fu;conddialy_enroll_fu;conddialy_year_enroll_fu;conddialys_npw_enroll_fu;condialys_dpw_enroll_fu;typdialys_enroll_fu;pretranspl_enroll_fu;transpl_type_enroll_fu___1;transpl_type_enroll_fu___2;transpl_type_enroll_fu___3;transpl_type_enroll_fu___4;transpl_type_enroll_fu___5;transpl_type_enroll_fu___6;transpl_type_enroll_fu___7;kidtrans_date_enroll_fu;primarydis_kid_enroll_fu;livertrans_date_enroll_fu;primarydis_liv_enroll_fu;lungtrans_date_enroll_fu;primarydis_lung_enroll_fu;hearttrans_date_enroll_fu;primarydis_heart_enroll_fu;panctrans_date_enroll_fu;primarydis_panc_enroll_fu;bmtrans_date_enroll_fu;primarydis_bm_enroll_fu;othertrans_date_enroll_fu;primarydis_other_enroll_fu;comments_anam_enroll_fu;test_anamnesis_diagnosis_enroll_fu_complete".split(";")));

    this.defaultQuestionnaireParser = new SimpleStringParser("Questionnaire",null,
        ";","\t");
    Configuration.fromFile(Configuration.class.getResourceAsStream("/questionnaire.cfg"))
        .ifPresent(conf -> defaultQuestionnaireParser.configure(conf, "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id_question;ethnicity_question;ethn_other_question;visit_date_question;visit_timepoint_question;weight_question;body_height_question;famstroke_question;famstroke_yes_question;famstroke_year;eating_habits_question;eatinghabits_other_question;meat_meal_question;smokinghab_question;cigarette_yes_question;ecigarette_yes_question;cigar_yes_question;ex_smoker_yes_question;tobacco_start_question;tobacco_stop_question;alcohol_question;diuresis_question;chronic_wounds_question;leg_edema_question;occupation_question;occuppation_other_question;firststart_dial_question;frequency_dial_question;timframe_dial_question;sporting_question;allergies_question;fatigue_question;nyha_question;ccs_score_question;famkidney_question;famkidney_yes_question;famkidneytype_yes_question;test_questionnaire_enroll_fu_complete".split(";")));

    this.defaultMedicationParser = new SimpleStringParser("Medication",null,
        ";","\t");
    Configuration.fromFile(Configuration.class.getResourceAsStream("/medication.cfg"))
        .ifPresent(conf -> defaultMedicationParser.configure(conf, "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id_allph;visittimepoint_allph;medication_allph1;medication_type_allph___1;medication_type_allph___2;medication_type_allph___3;medication_type_allph___4;medication_type_allph___5;medication_type_allph___6;medication_type_allph___7;medication_type_allph___8;medication_type_allph___9;medication_type_allph___10;medication_type_allph___11;medication_type_allph___12;medication_type_allph___13;medication_type_allph___14;drugname_antib_allph;drugdose_antib_allph;start_antib_allph;indic_antib_allph;drugname_ironsup_allph;start_ironsup_allph;drugdose_ironsup_allph;wdrugdose_ironsup_allph;drugname_diuret_allph;start_diuret_allph;drugdose_diuret_allph;wdrugdose_diuret_allph;drugname_epo_allph;start_epo_allph;drugdose_epo_allph;wdrugdose_epo_allph;drugname_analg_allph;start_analg_allph;drugdose_analg_allph;wdrugdose_analg_allph;drugname_anticoag_allph;drugdose_anticoag_allph;start_anticoag_allph;indic_anticoag_allph;drugname_aceinh_allph;drugdose_aceinh_allph;start_aceinh_allph;indic_aceinh_allph;drugname_calciumant_allph;drugdose_calciumant_allph;start_calciumant_allph;indic_calciumant_allph;drugname_betablo_allph;drugdose_betablo_allph;start_betablo_allph;indic_betablo_allph;drugname_statin_allph;drugdose_statin_allph;start_statin_allph;indic_statin_allph;drugname_antdiab_allph;drugdose_antdiab_allph;start_antdiab_allph;indic_antdiab_allph;drugname_insul_allph;drugdose_insul_allph;start_insul_allph;indic_insul_allph;drugname_chemo_allph;drugdose_chemo_allph;start_chemo_allph;indic_chemo_allph;immunsupp_allph;immunsupp_type_allph___1;immunsupp_type_allph___2;immunsupp_type_allph___3;immunsupp_type_allph___4;immunsupp_type_allph___5;immunsupp_type_allph___6;immunsupp_type_allph___7;drugdose_steroids_allph;drugdose_mycoph_allph;drugdose_tacrol_allph;drugdose_azath_allph;drugdose_sirol_allph;drugdose_everol_allph;drugdose_other_allphenter;test_medication_all_phases_complete".split(";")));

    this.defaultWeeklyParser = new SimpleStringParser("Weekly",null,
        ";","\t");
    Configuration.fromFile(Configuration.class.getResourceAsStream("/weekly.cfg"))
        .ifPresent(conf -> defaultWeeklyParser.configure(conf, "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id_w;visit_date_w;dialysis_time_point_w;dialysis_day_w;pl_sample_taken_w;time_dialysis_start_w;time_blood_drawn_w;time_blood_stored_w;time_blood_pickup_w;time_blood_arrival_w;time_blood_processing_w;totalpl_volume_w;uh_plasma_stfr_w;uh_plasma_ntbi_w;uh_aliquots_number_w;uh_totalpl_volume_w;d_aliquots_number_w;d_totalpl_volume_w;dkfz_plasma_ms_w;dkfz_plasma_msd_w;dkfz_aliquots_number_w;dkfz_totalpl_volume_w;add_plasma_w;hh_add_plasma_w;hh_add_aliquot_volume_w;hh_add_totalpl_volume_w;hh_add_aliquots_number_w;comment_plasma_w;eryth_sample_taken_w;totaleryth_volume_w;eryth_number_aliquots_w;comment_eryth_w;dialysis_date_w;vacular_access_w;dialysis_access_w;dialysis_access_shunt_w;dialysis_access_cath_w;dialysis_type_w;first_dialysis_w;time_prior_to_hd_yes_w;rr_prior_hd_yes_w;puls_prior_hd_yes_w;weight_prior_hd_yes_w;ti_during_hd_w_1;ti_during_hd_w_2;ti_during_hd_w_3;ti_during_hd_w_4;rr_during_hd_w_1;rr_during_hd_w_2;rr_during_hd_w_3;rr_during_hd_w_4;p_during_hd_w_1;p_during_hd_w_2;p_during_hd_w_3;p_during_hd_w_4;w_during_hd_w_1;w_during_hd_w_2;w_during_hd_w_3;w_during_hd_w_4;bf_during_hd_w_1;bf_during_hd_w_2;bf_during_hd_w_3;bf_during_hd_w_4;pa_during_hd_w_1;pa_during_hd_w_2;pa_during_hd_w_3;pa_during_hd_w_4;pv_during_hd_w_1;pv_during_hd_w_2;pv_during_hd_w_3;pv_during_hd_w_4;tmp_during_hd_w_1;tmp_during_hd_w_2;tmp_during_hd_w_3;tmp_during_hd_w_4;ufr_during_hd_w_1;ufr_during_hd_w_2;ufr_during_hd_w_3;ufr_during_hd_w_4;time_post_hd_w;rr_post_hd_w;puls_post_hd_w;weight_post_hd_w;volume_withdrawal_hd_w;subv_hd_w;bv_hd_w;total_time_hd_w;acg_bolus_hd_w;acg_drug_bolus_hd_w;acg_dose_bolus_hd_w;acg_iu_hd_w;acg_iu_drug_hd_w;acg_iu_dose_hd_w;acg_total_hd_w;esa_drug_hd_w;esa_drug_name_hd_w;esa_dose_hd_w;iron_suppl_hd_w;iron_drug_name_hd_w;iron_dose_hd_w;bloodtrans_hd_w;bloodtrans_volume_hd_w;med_iv_drug_hd_w_1;med_iv_dose_hd_w_1;med_iv_drug_hd_w_2;med_iv_dose_hd_w_2;med_iv_drug_hd_w_3;med_iv_dose_hd_w_3;med_iv_drug_hd_w_4;med_iv_dose_hd_w_4;med_iv_drug_hd_w_5;med_iv_dose_hd_w_5;med_iv_drug_hd_w_6;med_iv_dose_hd_w_6;complications_hd_w;actions_hd_w;allergies_hd_w;risks_hd_w;hb_w;erythrocytes_w;haematocrit_w;mchc_w;mcv_w;mch_w;evb_w;leucocytes_w;thrombocytes_w;ph_w;pco2_w;po2_w;cthb_w;chbc_w;hct_w;so2_w;fo2hb_w;fcohb_w;fhhb_w;fmethb_w;ck_w;cna_w;cca2_w;ccl_w;cglu_w;clac_w;ctbil_w;hco3_current_w;hco3_standard_w;be_w;ferritin_w;ph_w_2;pco2_w_2;po2_w_2;cthb_w_2;chbc_w_2;hct_w_2;fo2hb_w_2;so2_w_2;fcohb_w_2;fhhb_w_2;fmethb_w_2;ck_w_2;cna_w_2;cca2_w_2;ccl_w_2;cglu_w_2;clac_w_2;ctbil_w_2;hco3_current_w_2;hco3_standard_w_2;be_w_2;ferritin_w_2;iron_w;transferrin_w;transferrin_saturation_w;reticulocytes_w;tromboticevents_w___1;tromboticevents_w___2;tromboticevents_w___3;tromboticevents_w___4;tromboticevents_w___5;pulm_embol_ae_date_w;ang_pect_compl_date_w;myocardinfarc_ae_date_w;apopl_ae_date_w;apopl_ae_type_w;rhythm_disturbances_hd_w___1;rhythm_disturbances_hd_w___2;rhythm_disturbances_hd_w___3;rhythm_disturbances_hd_w___4;rhythm_disturbances_hd_w___5;atrial_fibrill_ae_date_w;extrasystoles_ae_date_w;tachycardia_ae_date_w;arrhythmia_ae_date_w;cardiac_insuff_type_hd_w___1;cardiac_insuff_type_hd_w___2;cardiac_insuff_type_hd_w___3;cardiac_insuff_type_hd_w___4;cardiac_insuff_type_hd_w___5;cardiac_insuff_date_ae_w;hypotension_ae_date_w;syncope_ae_date_w;hypertenderail_ae_date_w;bloodtrans_ae_w;bloodtrans_ae_date_w;comments_3_w;test_plasma_dialysis_weekly_complete".split(";")));

    this.defaultMonthlyParser = new SimpleStringParser("Monthly",null,
        ";","\t");
    Configuration.fromFile(Configuration.class.getResourceAsStream("/monthly.cfg"))
        .ifPresent(conf -> defaultMonthlyParser.configure(conf, "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id_labmon_m;blood_collection_date_m;blood_timepoint_m;clinical_chemist_m;creatinine_m;egfr_m;urea_m;albumin_m;total_protein_m;uric_acid_m;hba1c_m;urea_reduction_rate_m;hematology_m;hemoglobin_m;platelets_m;leucocytes_m;reticulocytes_m;rpi_m;ironstatus_m;transferrin_saturation_m;ferritin_m;transferrin_m;iron_m;bone_metabolism_m;ipht_m;calcium_m;calcium_total_m;phosphate_m;oh_vitamin_d_m;electrolytes_m;sodium_m;potassium_m;magnesium_m;chloride_m;glucose_m;infect_parameter_m;crp_m;coagulation;quick_m;inr_m;aptt_m;factor_vlll_m;factor_vll_m;protein_s_m;protein_c_m;antithrombin_lll_m;fibrinogen_m;d_dimere_m;pfa_m;bga_m;ph_m;po2_m;pco2_m;hco_3_current_m;hco_3_standard_m;be_m;spo_2_m;fmethb_m;cthb_m;hct_m;so2_m;fo2hb_m;fcohb_m;fhhb_m;ck_bga_m;cna_m;cca_m;ccl_m;cglu_m;clac_m;ctbil_m;vitamins_m;folic_acid_m;vitamin_b_12_m;vitamin_b6_m;liver_parameter_m;got_m;gpt_m;ggt_m;billirubin_total_m;billirubin_direct_m;billirubin_indirect_m;ldh_m;lipase_m;heart_parameter_m;ck_m;ck_mb_m;thyroid_m;ft_3_m;ft_4_m;tsh_m;blood_lipids_m;ldl_m;hdl_m;triglyceride_m;cholesterol_m;infect_serology_m;hepatitis_b_m;hepatitis_c_m;hiv_m;tumor_marker_m;psa_m;immunisation_m;hla_ak_m;differential_blood_count_m;neutrophils_m;lypmhocytes_m;monocytes_m;eosinophil_m;basophils;comments_lab_values_m;test_lab_values_monthly_complete".split(";")));
  }

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
        Parser<String, String> innerParser = getParser().get();
        dropArea.getStyleClass().add("working");
        Map<Path, List<String>> parsed = db.getFiles().stream()
            .filter(File::isFile)
            .filter(File::canRead)
            .map(File::toPath)
            .collect(Collectors.toMap(Function.identity(), this::parseLines));

        List<Integer> lineCounts = parsed.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .filter(e -> exportResult(innerParser.hasOutputPath() ?
                innerParser.getOutputPath().resolve(e.getKey().getFileName()) : e.getKey(), e.getValue()))
            .map(e -> e.getValue().size())
            .collect(Collectors.toList());

        success = parsed.size() == lineCounts.size();
        new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
            "Parsed " + lineCounts.stream().reduce(0, Integer::sum)
                + " lines over " + lineCounts.size() + " files. "
                + (parsed.size() - lineCounts.size()) + " files produced errors.")
            .showAndWait();

        dropArea.getStyleClass().remove("working");
      }

      dragEvent.setDropCompleted(success);
      dragEvent.consume();
    });

    configLabel.textProperty().bind(Bindings.createStringBinding(() ->
        "Configuration: " + (parser.isNull().get() ? "-/-" : parser.get().getName()), parser));
    statusLabel.textProperty().bind(status);
    parser.addListener((obs, oldVal, newVal) -> updateParser(newVal));
  }

  @FXML
  private void loadConfig(ActionEvent a)
  {
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
            getParser().set(newVal));
        stage.showAndWait();
      }
    }
    catch(IOException ex)
    {
      getParser().set(legacyLoadConfig(a).orElse(null));
    }
  }

  @FXML
  private void loadPreset(ActionEvent a)
  {
    if(a.getSource() == loadEnrollment)
    {
      getParser().set(defaultEnrollmentParser);
    }
    else if(a.getSource() == loadAnamnesis)
    {
      getParser().set(defaultAnamnesisParser);
    }
    else if(a.getSource() == loadQuestionnaire)
    {
      getParser().set(defaultQuestionnaireParser);
    }
      else if(a.getSource() == loadMedication)
    {
      getParser().set(defaultMedicationParser);
    }
      else if(a.getSource() == loadWeekly)
    {
      getParser().set(defaultWeeklyParser);
    }
      else if(a.getSource() == loadMonthly)
    {
      getParser().set(defaultMonthlyParser);
    }
  }

  private void updateParser(Parser<String, String> parser)
  {
    if(parser != null && parser.isReady())
    {
      getDropArea().getStyleClass().add("ready");
      getStatus().set("Configuration \"" + parser.getName() + "\" loaded.");
    }
    else
    {
      getDropArea().getStyleClass().remove("ready");
      getStatus().set("Error in configuration file.");
    }

    new Alert(getStatus().get().contains("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION,
        getStatus().get()).showAndWait();
  }

  @FXML
  @SuppressWarnings("unused")
  private Optional<Parser<String, String>> legacyLoadConfig(ActionEvent a)
  {
    FileChooser configChooser = new FileChooser();
    configChooser.getExtensionFilters().clear();
    configChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Configuration files","*.cfg"));
    configChooser.setTitle("Open Configuration File");

    File configFile = configChooser.showOpenDialog(getRootPane().getScene().getWindow());
    return Optional.ofNullable(configFile)
        .flatMap(Configuration::fromFile)
        .map(config ->
        {
          Parser<String, String> newParser = new SimpleStringParser("Custom",null,
              ";","\t");
          FileChooser templateChooser = new FileChooser();
          templateChooser.getExtensionFilters().clear();
          templateChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Template files","*.csv"));
          templateChooser.setTitle("Open Input Template File");

          Optional.ofNullable(templateChooser.showOpenDialog(getRootPane().getScene().getWindow()))
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
              .ifPresent(headers -> newParser.configure(config, headers));

          return newParser;
        });
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
