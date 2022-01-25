package de.dkfz.sbst.tichawa.util.converter.parser;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

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

  // TODO: Add monthly test.
  @SuppressWarnings("unused")
  private static Stream<Arguments> generateTestSets()
  {
    return Stream.of(
        Arguments.of(PATIENT_CONFIG, PATIENT_DATA, PATIENT_LABELS, "\t", "\t"),
        Arguments.of(WEEKLY_CONFIG, WEEKLY_DATA, WEEKLY_LABELS, "\t", "\t"),
        Arguments.of(ANAM_CONFIG, ANAM_DATA, ANAM_LABELS, ";", ";")
    );
  }

  @ParameterizedTest
  @MethodSource("generateTestSets")
  void parseData(Configuration config, String rawData, String rawLabels, String dataSeparator, String rawSeparator)
  {
    SimpleStringParser parser = new SimpleStringParser("Custom", dataSeparator, rawSeparator);
    parser.parseHeaderLine(rawLabels).ifPresent(inHeaders -> parser.configure(config, inHeaders));
    Map<String, Rule.Result<Object>> results = parser.parse(rawData);

    List<String> expectedLabels = config.getOutLabels();
    Collections.sort(expectedLabels);
    List<String> actualLabels = new LinkedList<>(results.keySet());
    Collections.sort(actualLabels);

    Assertions.assertLinesMatch(expectedLabels, actualLabels);

    System.out.println(parser.encodeHeader());
    System.out.println(parser.encode(results));
  }
}
