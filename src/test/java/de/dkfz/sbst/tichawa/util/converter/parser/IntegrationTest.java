package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.stream.*;

class IntegrationTest
{
  private static final String PATIENT_LABELS = "record_id\tredcap_event_name\tredcap_repeat_instrument\tredcap_repeat_instance\tredcap_data_access_group\tpatient_id\topenbis_id\tpatient_type\tgender_enroll\tgender_divers_enroll\tvisit_date\tconsent_date\tversion_number\tconsent_data_protection\tconsent_data_other_counties\tconsent_biomaterial\tconsent_renewed_contact\tquestionnaire\tquestionnaire_date\tcomments\ttest_enrollment_and_consent_complete";
  private static final String PATIENT_DATA = "1\tenrollment_arm_1\t\t\t\tN1-000\t\t\t2\t\t18.05.2021\t11.05.2021\t1\t1\t1\t1\t1\t2\t18.05.2021\t\t2";
  private static final Configuration PATIENT_CONFIG = Configuration
      .fromFile(Configuration.class.getResourceAsStream("/patient.cfg"))
      .orElse(new Configuration(new LinkedList<>()));

  private static final String WEEKLY_LABELS = "record_id\tredcap_event_name\tredcap_repeat_instrument\tredcap_repeat_instance\tredcap_data_access_group\tpatient_id_w\tvisit_date_w\tdialysis_time_point_w\tdialysis_day_w\tpl_sample_taken_w\ttime_dialysis_start_w\ttime_blood_drawn_w\ttime_blood_stored_w\ttime_blood_pickup_w\ttime_blood_arrival_w\ttime_blood_processing_w\ttotalpl_volume_w\tuh_plasma_stfr_w\tuh_plasma_ntbi_w\tuh_aliquots_number_w\tuh_totalpl_volume_w\td_aliquots_number_w\td_totalpl_volume_w\tdkfz_plasma_ms_w\tdkfz_plasma_msd_w\tdkfz_aliquots_number_w\tdkfz_totalpl_volume_w\tadd_plasma_w\thh_add_plasma_w\thh_add_aliquot_volume_w\thh_add_totalpl_volume_w\thh_add_aliquots_number_w\tcomment_plasma_w\teryth_sample_taken_w\ttotaleryth_volume_w\teryth_number_aliquots_w\tcomment_eryth_w\tdialysis_date_w\tvacular_access_w\tdialysis_access_w\tdialysis_access_shunt_w\tdialysis_access_cath_w\tdialysis_type_w\tfirst_dialysis_w\ttime_prior_to_hd_yes_w\trr_prior_hd_yes_w\tpuls_prior_hd_yes_w\tweight_prior_hd_yes_w\tti_during_hd_w_1\tti_during_hd_w_2\tti_during_hd_w_3\tti_during_hd_w_4\trr_during_hd_w_1\trr_during_hd_w_2\trr_during_hd_w_3\trr_during_hd_w_4\tp_during_hd_w_1\tp_during_hd_w_2\tp_during_hd_w_3\tp_during_hd_w_4\tw_during_hd_w_1\tw_during_hd_w_2\tw_during_hd_w_3\tw_during_hd_w_4\tbf_during_hd_w_1\tbf_during_hd_w_2\tbf_during_hd_w_3\tbf_during_hd_w_4\tpa_during_hd_w_1\tpa_during_hd_w_2\tpa_during_hd_w_3\tpa_during_hd_w_4\tpv_during_hd_w_1\tpv_during_hd_w_2\tpv_during_hd_w_3\tpv_during_hd_w_4\ttmp_during_hd_w_1\ttmp_during_hd_w_2\ttmp_during_hd_w_3\ttmp_during_hd_w_4\tufr_during_hd_w_1\tufr_during_hd_w_2\tufr_during_hd_w_3\tufr_during_hd_w_4\ttime_post_hd_w\trr_post_hd_w\tpuls_post_hd_w\tweight_post_hd_w\tvolume_withdrawal_hd_w\tsubv_hd_w\tbv_hd_w\ttotal_time_hd_w\tacg_bolus_hd_w\tacg_drug_bolus_hd_w\tacg_dose_bolus_hd_w\tacg_iu_hd_w\tacg_iu_drug_hd_w\tacg_iu_dose_hd_w\tacg_total_hd_w\tesa_drug_hd_w\tesa_drug_name_hd_w\tesa_dose_hd_w\tiron_suppl_hd_w\tiron_drug_name_hd_w\tiron_dose_hd_w\tbloodtrans_hd_w\tbloodtrans_volume_hd_w\tmed_iv_drug_hd_w_1\tmed_iv_dose_hd_w_1\tmed_iv_drug_hd_w_2\tmed_iv_dose_hd_w_2\tmed_iv_drug_hd_w_3\tmed_iv_dose_hd_w_3\tmed_iv_drug_hd_w_4\tmed_iv_dose_hd_w_4\tmed_iv_drug_hd_w_5\tmed_iv_dose_hd_w_5\tmed_iv_drug_hd_w_6\tmed_iv_dose_hd_w_6\tcomplications_hd_w\tactions_hd_w\tallergies_hd_w\trisks_hd_w\thb_w\terythrocytes_w\thaematocrit_w\tmchc_w\tmcv_w\tmch_w\tevb_w\tleucocytes_w\tthrombocytes_w\tph_w\tpco2_w\tpo2_w\tcthb_w\tchbc_w\thct_w\tso2_w\tfo2hb_w\tfcohb_w\tfhhb_w\tfmethb_w\tck_w\tcna_w\tcca2_w\tccl_w\tcglu_w\tclac_w\tctbil_w\thco3_current_w\thco3_standard_w\tbe_w\tferritin_w\tph_w_2\tpco2_w_2\tpo2_w_2\tcthb_w_2\tchbc_w_2\thct_w_2\tfo2hb_w_2\tso2_w_2\tfcohb_w_2\tfhhb_w_2\tfmethb_w_2\tck_w_2\tcna_w_2\tcca2_w_2\tccl_w_2\tcglu_w_2\tclac_w_2\tctbil_w_2\thco3_current_w_2\thco3_standard_w_2\tbe_w_2\tferritin_w_2\tiron_w\ttransferrin_w\ttransferrin_saturation_w\treticulocytes_w\ttromboticevents_w___1\ttromboticevents_w___2\ttromboticevents_w___3\ttromboticevents_w___4\ttromboticevents_w___5\tpulm_embol_ae_date_w\tang_pect_compl_date_w\tmyocardinfarc_ae_date_w\tapopl_ae_date_w\tapopl_ae_type_w\trhythm_disturbances_hd_w___1\trhythm_disturbances_hd_w___2\trhythm_disturbances_hd_w___3\trhythm_disturbances_hd_w___4\trhythm_disturbances_hd_w___5\tatrial_fibrill_ae_date_w\textrasystoles_ae_date_w\ttachycardia_ae_date_w\tarrhythmia_ae_date_w\tcardiac_insuff_type_hd_w___1\tcardiac_insuff_type_hd_w___2\tcardiac_insuff_type_hd_w___3\tcardiac_insuff_type_hd_w___4\tcardiac_insuff_type_hd_w___5\tcardiac_insuff_date_ae_w\thypotension_ae_date_w\tsyncope_ae_date_w\thypertenderail_ae_date_w\tbloodtrans_ae_w\tbloodtrans_ae_date_w\tcomments_3_w\ttest_plasma_dialysis_weekly_complete";
  private static final String WEEKLY_DATA = "1\tweekly_plasma_and_arm_1\t\t1\t\tN1-000\t25.05.2021\t1\t3\t2\t07:30\t07:33\t07:50\t00:00\t09:20\t09:45\t4.5\t3\t2\t6\t2.1\t4\t0.4\t2\t4\t6\t1.2\t2\t1\t400\t1.2\t3\t\t2\t3\t\t\t25.05.2021\t3\t2\t2\t1\t\t\t07:20\t120/80\t70\t87\t07:35\t09:55\t11:00\t\t110/70\t106/74\t125/88\t\t59\t76\t73\t\t\t\t\t\t220\t\t220\t\t80\t\t80\t\t70\t\t70\t\t20\t\t20\t\t115\t\t302\t\t12:30\t118/78\t85\t\t\t\t\t\t\t\t\t\t\t\t\t5\tMircera\t\t2\tFer Abseamed\t2000 IU/ml\t1\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t8.1\tND\tND\tND\tND\tND\tND\tND\tND\t7.4\t39\t23\tND\tND\tND\t50.3\t49.5\t0.5\t49\t1\t4.1\t135\t1.15\t106\t84\t0.6\tND\tND\t23.2\t-0.6\tND\t7.5\t40\t25\tND\tND\tND\t50\t51\t0.5\t48\t0.5\t4\t110\t1.2\t100\t82\t0.7\tND\tND\t23\t-0.5\tND\tND\tND\tND\tND\t1\t0\t0\t0\t0\t\t\t\t\t\t0\t0\t1\t0\t0\t\t24.05.2021\t\t\t1\t0\t0\t0\t0\t\t\t\t\t1\t\t\t1";
  private static final Configuration WEEKLY_CONFIG = Configuration
      .fromFile(Configuration.class.getResourceAsStream("/weekly.cfg"))
      .orElse(new Configuration(new LinkedList<>()));

  private static final String ANAM_LABELS = "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id_enroll_fu;weight_enroll_fu;height_enroll_fu;bmi_enroll_fu;visit_type_enroll_fu;visit_date_enroll_fu;basic_renal_disease_enroll_fu;dkd_enroll_fu;htn_enroll_fu;gn_enroll_fu;gn_type_enroll_fu___1;gn_type_enroll_fu___2;gn_type_enroll_fu___3;gn_type_enroll_fu___4;gn_type_enroll_fu___5;gn_type_enroll_fu___6;gn_type_enroll_fu___7;gn_type_enroll_fu___8;gn_type_enroll_fu___9;gn_type_enroll_fu___10;gn_type_enroll_fu___11;gn_type_enroll_fu___12;gn_other_enroll_fu;fsgs_enroll_fu;geneticdis_enroll_fu;geneticdis_type_enroll_fu___1;geneticdis_type_enroll_fu___2;geneticdis_type_enroll_fu___3;geneticdis_type_enroll_fu___4;geneticdis_type_enroll_fu___5;geneticdis_type_enroll_fu___6;geneticdis_other_enroll_fu;intnephr_enroll_fu;intnephr_type_enroll_fu;intnephr_other_enroll_fu;amyloidosis_enroll_fu;other_kiddis_enroll_fu;other_kiddis_type_enroll_fu;cvd_enroll_fu;cvd_type_enroll_fu___1;cvd_type_enroll_fu___2;cvd_type_enroll_fu___3;cvd_type_enroll_fu___4;cvd_type_enroll_fu___5;cvd_type_enroll_fu___6;cvd_other_enroll_fu;thrombev_enroll_fu;thrombev_type_enroll_fu___1;thrombev_type_enroll_fu___2;thrombev_type_enroll_fu___3;thrombev_other_enroll_fu;diab_enroll_fu;thyroiddys_enroll_fu;thyroiddys_type_enroll_fu;rheumdiseas_enroll_fu;rheumdis_type_enroll_fu___1;rheumdis_type_enroll_fu___2;maligdiseas_enroll_fu;maligdiseas_type_enroll_fu;infectdis_enroll_fu;infectdis_type_enroll_fu___1;infectdis_type_enroll_fu___2;infectdis_type_enroll_fu___3;infectdis_type_enroll_fu___4;infectdis_type_enroll_fu___5;infectdis_type_enroll_fu___6;infectdis_type_enroll_fu___7;hepatitis_type_enroll_fu;hiv_infect_type_enroll_fu;bactinfect_type_enroll_fu;infectdis_other_enroll_fu;ibd_enroll_fu;ibd_type_enroll_fu;allergies_enroll_fu;allergies_type_enroll_fu;hearinglo_enroll_fu;otherdis_enroll_fu;otherdis_type_enroll_fu;dialysisobl_enroll_fu;conddialy_enroll_fu;conddialy_year_enroll_fu;conddialys_npw_enroll_fu;condialys_dpw_enroll_fu;typdialys_enroll_fu;pretranspl_enroll_fu;transpl_type_enroll_fu___1;transpl_type_enroll_fu___2;transpl_type_enroll_fu___3;transpl_type_enroll_fu___4;transpl_type_enroll_fu___5;transpl_type_enroll_fu___6;transpl_type_enroll_fu___7;kidtrans_date_enroll_fu;primarydis_kid_enroll_fu;livertrans_date_enroll_fu;primarydis_liv_enroll_fu;lungtrans_date_enroll_fu;primarydis_lung_enroll_fu;hearttrans_date_enroll_fu;primarydis_heart_enroll_fu;panctrans_date_enroll_fu;primarydis_panc_enroll_fu;bmtrans_date_enroll_fu;primarydis_bm_enroll_fu;othertrans_date_enroll_fu;primarydis_other_enroll_fu;comments_anam_enroll_fu;test_anamnesis_diagnosis_enroll_fu_complete";
  private static final String ANAM_DATA = "1;enrollment_arm_1;;;;N1-000;65;165;24;1;18.05.2021;2;2;1;1;0;0;0;0;0;0;0;0;0;0;0;0;;1;1;0;0;0;0;0;0;;1;;;1;1;;1;0;0;0;0;0;0;;1;0;0;0;;2;1;;1;0;0;1;;1;0;0;0;0;0;0;0;;;;;1;;2;penicillin;1;1;;2;2;2010;3;4;1;1;0;0;0;0;0;0;0;;;;;;;;;;;;;;;;2";
  private static final Configuration ANAM_CONFIG = Configuration
      .fromFile(Configuration.class.getResourceAsStream("/anamnesis.cfg"))
      .orElse(new Configuration(new LinkedList<>()));

  private static final String MONTHLY_LABELS = "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id_labmon_m;blood_collection_date_m;blood_timepoint_m;clinical_chemist_m;creatinine_m;egfr_m;urea_m;albumin_m;total_protein_m;uric_acid_m;hba1c_m;urea_reduction_rate_m;hematology_m;hemoglobin_m;platelets_m;leucocytes_m;reticulocytes_m;rpi_m;ironstatus_m;transferrin_saturation_m;ferritin_m;transferrin_m;iron_m;bone_metabolism_m;ipht_m;calcium_m;calcium_total_m;phosphate_m;oh_vitamin_d_m;electrolytes_m;sodium_m;potassium_m;magnesium_m;chloride_m;glucose_m;infect_parameter_m;crp_m;coagulation;quick_m;inr_m;aptt_m;factor_vlll_m;factor_vll_m;protein_s_m;protein_c_m;antithrombin_lll_m;fibrinogen_m;d_dimere_m;pfa_m;bga_m;ph_m;po2_m;pco2_m;hco_3_current_m;hco_3_standard_m;be_m;spo_2_m;fmethb_m;cthb_m;hct_m;so2_m;fo2hb_m;fcohb_m;fhhb_m;ck_bga_m;cna_m;cca_m;ccl_m;cglu_m;clac_m;ctbil_m;vitamins_m;folic_acid_m;vitamin_b_12_m;vitamin_b6_m;liver_parameter_m;got_m;gpt_m;ggt_m;billirubin_total_m;billirubin_direct_m;billirubin_indirect_m;ldh_m;lipase_m;heart_parameter_m;ck_m;ck_mb_m;thyroid_m;ft_3_m;ft_4_m;tsh_m;blood_lipids_m;ldl_m;hdl_m;triglyceride_m;cholesterol_m;infect_serology_m;hepatitis_b_m;hepatitis_c_m;hiv_m;tumor_marker_m;psa_m;immunisation_m;hla_ak_m;differential_blood_count_m;neutrophils_m;lypmhocytes_m;monocytes_m;eosinophil_m;basophils;comments_lab_values_m;test_lab_values_monthly_complete";
  private static final String MONTHLY_DATA = "001;monthly_collection_arm_1;;1;;N1-000;2021-05-20;1;2;2;56;35;26;55;ND;7.1;ND;2;8.1;122;7.5;0.6;ND;2;16;166;ND;ND;2;455;2.4;ND;1.9;ND;2;144;4.9;ND;ND;ND;2;7;2;80;1.1;33;ND;ND;ND;ND;ND;3.4;ND;ND;2;7.35;35;67;17;18;\" -4\";76;ND;ND;35;ND;ND;ND;ND;5.3;147;1.5;ND;180;1.6;6;1;;;;2;77;65;30;3;2;ND;ND;ND;1;;;;;;;1;;;;;1;;;;1;;1;;1;;;;;;;1";
  private static final Configuration MONTHLY_CONFIG = Configuration
      .fromFile(Configuration.class.getResourceAsStream("/monthly.cfg"))
      .orElse(new Configuration(new LinkedList<>()));

  private static final String MED_LABELS = "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id_allph;visittimepoint_allph;medication_allph1;medication_type_allph___1;medication_type_allph___2;medication_type_allph___3;medication_type_allph___4;medication_type_allph___5;medication_type_allph___6;medication_type_allph___7;medication_type_allph___8;medication_type_allph___9;medication_type_allph___10;medication_type_allph___11;medication_type_allph___12;medication_type_allph___13;medication_type_allph___14;drugname_antib_allph;drugdose_antib_allph;start_antib_allph;indic_antib_allph;drugname_ironsup_allph;start_ironsup_allph;drugdose_ironsup_allph;wdrugdose_ironsup_allph;drugname_diuret_allph;start_diuret_allph;drugdose_diuret_allph;wdrugdose_diuret_allph;drugname_epo_allph;start_epo_allph;drugdose_epo_allph;wdrugdose_epo_allph;drugname_analg_allph;start_analg_allph;drugdose_analg_allph;wdrugdose_analg_allph;drugname_anticoag_allph;drugdose_anticoag_allph;start_anticoag_allph;indic_anticoag_allph;drugname_aceinh_allph;drugdose_aceinh_allph;start_aceinh_allph;indic_aceinh_allph;drugname_calciumant_allph;drugdose_calciumant_allph;start_calciumant_allph;indic_calciumant_allph;drugname_betablo_allph;drugdose_betablo_allph;start_betablo_allph;indic_betablo_allph;drugname_statin_allph;drugdose_statin_allph;start_statin_allph;indic_statin_allph;drugname_antdiab_allph;drugdose_antdiab_allph;start_antdiab_allph;indic_antdiab_allph;drugname_insul_allph;drugdose_insul_allph;start_insul_allph;indic_insul_allph;drugname_chemo_allph;drugdose_chemo_allph;start_chemo_allph;indic_chemo_allph;immunsupp_allph;immunsupp_type_allph___1;immunsupp_type_allph___2;immunsupp_type_allph___3;immunsupp_type_allph___4;immunsupp_type_allph___5;immunsupp_type_allph___6;immunsupp_type_allph___7;drugdose_steroids_allph;drugdose_mycoph_allph;drugdose_tacrol_allph;drugdose_azath_allph;drugdose_sirol_allph;drugdose_everol_allph;drugdose_other_allphenter;test_medication_all_phases_complete";
  private static final String MED_DATA = "001;medication_all_pha_arm_1;;1;;N1-000;7;1;0;1;1;1;1;0;1;0;0;0;0;0;0;0;;;;;Ferrlecit;2010-01-01;;\"187.5 mg\";Torasemid;2009-01-01;;\"100 mg\";\"Epo alfa\";2010-01-01;;\"3000 I.E.\";Metamizol;2020-01-01;;\"7 g\";;;;;;;2010-01-01;;;;;;;;;;;;;;;;;;;;;;;;;;2;1;0;0;0;0;0;0;5;;;;;;;1";
  private static final Configuration MED_CONFIG = Configuration
      .fromFile(Configuration.class.getResourceAsStream("/medication.cfg"))
      .orElse(new Configuration(new LinkedList<>()));

  private static final String QUEST_LABELS = "record_id;redcap_event_name;redcap_repeat_instrument;redcap_repeat_instance;redcap_data_access_group;patient_id_question;ethnicity_question;ethn_other_question;visit_date_question;visit_timepoint_question;weight_question;body_height_question;famstroke_question;famstroke_yes_question;famstroke_year;eating_habits_question;eatinghabits_other_question;meat_meal_question;smokinghab_question;cigarette_yes_question;ecigarette_yes_question;cigar_yes_question;ex_smoker_yes_question;tobacco_start_question;tobacco_stop_question;alcohol_question;diuresis_question;chronic_wounds_question;leg_edema_question;occupation_question;occuppation_other_question;firststart_dial_question;frequency_dial_question;timframe_dial_question;sporting_question;allergies_question;fatigue_question;nyha_question;ccs_score_question;famkidney_question;famkidney_yes_question;famkidneytype_yes_question;test_questionnaire_enroll_fu_complete";
  private static final String QUEST_DATA = "001;questionnaire_enro_arm_1;;1;;N1-000;1;;2021-05-19;1;65;165;1;;;4;Frutarier;2;2;20;;;;;10/1990;200;100;\"right lower leg\";1;1;;04/2010;2;4;6;ND;3;1;6;2;sister;insufficiency;1";
  private static final Configuration QUEST_CONFIG = Configuration
      .fromFile(Configuration.class.getResourceAsStream("/questionnaire.cfg"))
      .orElse(new Configuration(new LinkedList<>()));

  @SuppressWarnings("unused")
  private static Stream<Arguments> generateStringTestSets()
  {
    return Stream.of(
        Arguments.of(PATIENT_CONFIG, PATIENT_DATA, PATIENT_LABELS, "\t", "\t"),
        Arguments.of(WEEKLY_CONFIG, WEEKLY_DATA, WEEKLY_LABELS, "\t", "\t"),
        Arguments.of(ANAM_CONFIG, ANAM_DATA, ANAM_LABELS, ";", ";"),
        Arguments.of(MONTHLY_CONFIG, MONTHLY_DATA, MONTHLY_LABELS, ";", ";"),
        Arguments.of(MED_CONFIG, MED_DATA, MED_LABELS, ";", ";"),
        Arguments.of(QUEST_CONFIG, QUEST_DATA, QUEST_LABELS, ";", ";")
    );
  }

  // TODO: Add test cases
  @SuppressWarnings("unused")
  private static Stream<Arguments> generateExcelTestSets()
  {
    Workbook cache = new XSSFWorkbook();
    Sheet cacheSheet = cache.createSheet();
    Row header = cacheSheet.createRow(cacheSheet.getLastRowNum() + 1);
    header.createCell(0, CellType.STRING).setCellValue("record_id");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("redcap_event_name");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("redcap_repeat_instrument");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("redcap_repeat_instance");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("redcap_data_access_group");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("patient_id");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("openbis_id");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("patient_type");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("gender_enroll");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("gender_divers_enroll");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("visit_date");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("consent_date");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("version_number");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("consent_data_protection");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("consent_data_other_counties");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("consent_biomaterial");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("consent_renewed_contact");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("questionnaire");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("questionnaire_date");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("comments");
    header.createCell(header.getLastCellNum(), CellType.STRING).setCellValue("test_enrollment_and_consent_complete");

    // TODO: Fix date fields
    Row data = cacheSheet.createRow(cacheSheet.getLastRowNum() + 1);
    data.createCell(0, CellType.NUMERIC).setCellValue(1);
    data.createCell(data.getLastCellNum(), CellType.STRING).setCellValue("enrollment_arm_1");
    data.createCell(data.getLastCellNum(), CellType.BLANK);
    data.createCell(data.getLastCellNum(), CellType.BLANK);
    data.createCell(data.getLastCellNum(), CellType.BLANK);
    data.createCell(data.getLastCellNum(), CellType.STRING).setCellValue("N1-000");
    data.createCell(data.getLastCellNum(), CellType.BLANK);
    data.createCell(data.getLastCellNum(), CellType.BLANK);
    data.createCell(data.getLastCellNum(), CellType.NUMERIC).setCellValue(2);
    data.createCell(data.getLastCellNum(), CellType.BLANK);
    data.createCell(data.getLastCellNum(), CellType.STRING).setCellValue(LocalDate.of(2021,5,18));
    data.createCell(data.getLastCellNum(), CellType.STRING).setCellValue(LocalDate.of(2021,5,11));
    data.createCell(data.getLastCellNum(), CellType.NUMERIC).setCellValue(1);
    data.createCell(data.getLastCellNum(), CellType.NUMERIC).setCellValue(1);
    data.createCell(data.getLastCellNum(), CellType.NUMERIC).setCellValue(1);
    data.createCell(data.getLastCellNum(), CellType.NUMERIC).setCellValue(1);
    data.createCell(data.getLastCellNum(), CellType.NUMERIC).setCellValue(1);
    data.createCell(data.getLastCellNum(), CellType.NUMERIC).setCellValue(2);
    data.createCell(data.getLastCellNum(), CellType.STRING).setCellValue(LocalDate.of(2021,5,18));
    data.createCell(data.getLastCellNum(), CellType.BLANK);
    data.createCell(data.getLastCellNum(), CellType.NUMERIC).setCellValue(2);

    return Stream.of(
        Arguments.of(PATIENT_CONFIG, data, header)
    );
  }

  @ParameterizedTest
  @MethodSource("generateStringTestSets")
  void parseStringData(Configuration config, String rawData, String rawLabels, String dataSeparator, String rawSeparator)
  {
    SimpleStringParser parser = new SimpleStringParser("Custom",null, dataSeparator, rawSeparator);
    parser.parseHeaderLine(rawLabels).ifPresent(inHeaders -> parser.configure(config, inHeaders));
    Map<String, Rule.Result<Object>> results = parser.parse(rawData);

    List<String> expectedLabels = new LinkedList<>(config.getOutLabels());
    Collections.sort(expectedLabels);
    List<String> actualLabels = new LinkedList<>(results.keySet());
    Collections.sort(actualLabels);

    Assertions.assertLinesMatch(expectedLabels, actualLabels);

    System.out.println(parser.encodeHeader());
    System.out.println(parser.encode(results));
  }

  @ParameterizedTest
  @MethodSource("generateExcelTestSets")
  void parseExcelData(Configuration config, Row rawData, Row header)
  {
    ExcelParser parser = new ExcelParser("Custom","CUSTOM_OBJECT",null);
    parser.parseHeaderLine(header).ifPresent(inHeaders -> parser.configure(config, inHeaders));
    Map<String, Rule.Result<Object>> results = parser.parse(rawData);

    List<String> expectedLabels = new LinkedList<>(config.getOutLabels());
    Collections.sort(expectedLabels);
    List<String> actualLabels = new LinkedList<>(results.keySet());
    Collections.sort(actualLabels);

    Assertions.assertLinesMatch(expectedLabels, actualLabels);

    Workbook output = new XSSFWorkbook();
    Sheet outSheet = output.createSheet(parser.getSheetName());
    parser.encodeHeader().forEach(headerRow ->
        ExcelParser.copyRow(headerRow, outSheet,outSheet.getLastRowNum() + 1));
    ExcelParser.copyRow(parser.encode(results), outSheet,outSheet.getLastRowNum() + 1);
    Assertions.assertDoesNotThrow(() -> output.write(Files.newOutputStream(Path.of("test.xlsx"))));
  }
}
