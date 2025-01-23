DROP TABLE IF EXISTS `batch_job_execution_seq`;
DROP TABLE IF EXISTS `batch_job_seq`;
DROP TABLE IF EXISTS `batch_step_execution_seq`;
DROP TABLE IF EXISTS `batch_job_execution_context`;
DROP TABLE IF EXISTS `batch_job_execution_params`;
DROP TABLE IF EXISTS `batch_step_execution_context`;
DROP TABLE IF EXISTS `batch_step_execution`;
DROP TABLE IF EXISTS `batch_job_execution`;
DROP TABLE IF EXISTS `batch_job_instance`;

DROP TABLE IF EXISTS `BATCH_JOB_EXECUTION_SEQ`;
DROP TABLE IF EXISTS `BATCH_JOB_SEQ`;
DROP TABLE IF EXISTS `BATCH_STEP_EXECUTION_SEQ`;
DROP TABLE IF EXISTS `BATCH_JOB_EXECUTION_CONTEXT`;
DROP TABLE IF EXISTS `BATCH_JOB_EXECUTION_PARAMS`;
DROP TABLE IF EXISTS `BATCH_STEP_EXECUTION_CONTEXT`;
DROP TABLE IF EXISTS `BATCH_STEP_EXECUTION`;
DROP TABLE IF EXISTS `BATCH_JOB_EXECUTION`;
DROP TABLE IF EXISTS `BATCH_JOB_INSTANCE`;

DROP TABLE IF EXISTS `domain_batch_vacation`;
DROP TABLE IF EXISTS `domain_reception_contrat`;
DROP TABLE IF EXISTS `domain_detail_integration`;
DROP TABLE IF EXISTS `domain_integration_contrat`;
DROP TABLE IF EXISTS `domain_vacation`;
DROP TABLE IF EXISTS `key_sequence`;
DROP TABLE IF EXISTS `cfg_parametrage`;
DROP TABLE IF EXISTS `cfg_dependance_batch`;
DROP TABLE IF EXISTS `cfg_trt_batch`;

	
-- LOBOT.BATCH_JOB_EXECUTION_SEQ DEFINITION
CREATE TABLE `BATCH_JOB_EXECUTION_SEQ` (
  `ID` BIGINT NOT NULL,
  `UNIQUE_KEY` CHAR(1) NOT NULL,
  UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`)
);

-- LOBOT.BATCH_JOB_INSTANCE DEFINITION
CREATE TABLE `BATCH_JOB_INSTANCE` (
  `JOB_INSTANCE_ID` BIGINT NOT NULL,
  `VERSION` BIGINT DEFAULT NULL,
  `JOB_NAME` VARCHAR(100) NOT NULL,
  `JOB_KEY` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`JOB_INSTANCE_ID`),
  UNIQUE KEY `JOB_INST_UN` (`JOB_NAME`,`JOB_KEY`)
);


-- LOBOT.BATCH_JOB_SEQ DEFINITION
CREATE TABLE `BATCH_JOB_SEQ` (
  `ID` BIGINT NOT NULL,
  `UNIQUE_KEY` CHAR(1) NOT NULL,
  UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`)
);


-- LOBOT.BATCH_STEP_EXECUTION_SEQ DEFINITION
CREATE TABLE `BATCH_STEP_EXECUTION_SEQ` (
  `ID` BIGINT NOT NULL,
  `UNIQUE_KEY` CHAR(1) NOT NULL,
  UNIQUE KEY `UNIQUE_KEY_UN` (`UNIQUE_KEY`)
);

-- LOBOT.BATCH_JOB_EXECUTION DEFINITION
CREATE TABLE `BATCH_JOB_EXECUTION` (
  `JOB_EXECUTION_ID` BIGINT NOT NULL,
  `VERSION` BIGINT DEFAULT NULL,
  `JOB_INSTANCE_ID` BIGINT NOT NULL,
  `CREATE_TIME` DATETIME(6) NOT NULL,
  `START_TIME` DATETIME(6) DEFAULT NULL,
  `END_TIME` DATETIME(6) DEFAULT NULL,
  `STATUS` VARCHAR(10) DEFAULT NULL,
  `EXIT_CODE` VARCHAR(2500) DEFAULT NULL,
  `EXIT_MESSAGE` VARCHAR(2500) DEFAULT NULL,
  `LAST_UPDATED` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`JOB_EXECUTION_ID`),
  KEY `JOB_INST_EXEC_FK` (`JOB_INSTANCE_ID`),
  CONSTRAINT `JOB_INST_EXEC_FK` FOREIGN KEY (`JOB_INSTANCE_ID`) REFERENCES `BATCH_JOB_INSTANCE` (`JOB_INSTANCE_ID`) ON DELETE CASCADE
);


-- LOBOT.BATCH_JOB_EXECUTION_CONTEXT DEFINITION
CREATE TABLE `BATCH_JOB_EXECUTION_CONTEXT` (
  `JOB_EXECUTION_ID` BIGINT NOT NULL,
  `SHORT_CONTEXT` VARCHAR(2500) NOT NULL,
  `SERIALIZED_CONTEXT` TEXT,
  PRIMARY KEY (`JOB_EXECUTION_ID`),
  CONSTRAINT `JOB_EXEC_CTX_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`) ON DELETE CASCADE
);


-- LOBOT.BATCH_JOB_EXECUTION_PARAMS DEFINITION
CREATE TABLE `BATCH_JOB_EXECUTION_PARAMS` (
  `JOB_EXECUTION_ID` BIGINT NOT NULL,
  `PARAMETER_NAME` VARCHAR(100) NOT NULL,
  `PARAMETER_TYPE` VARCHAR(100) NOT NULL,
  `PARAMETER_VALUE` VARCHAR(2500) DEFAULT NULL,
  `IDENTIFYING` CHAR(1) NOT NULL,
  KEY `JOB_EXEC_PARAMS_FK` (`JOB_EXECUTION_ID`),
  CONSTRAINT `JOB_EXEC_PARAMS_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`) ON DELETE CASCADE
);


-- LOBOT.BATCH_STEP_EXECUTION DEFINITION
CREATE TABLE `BATCH_STEP_EXECUTION` (
  `STEP_EXECUTION_ID` BIGINT NOT NULL,
  `VERSION` BIGINT NOT NULL,
  `STEP_NAME` VARCHAR(100) NOT NULL,
  `JOB_EXECUTION_ID` BIGINT NOT NULL,
  `CREATE_TIME` DATETIME(6) NOT NULL,
  `START_TIME` DATETIME(6) DEFAULT NULL,
  `END_TIME` DATETIME(6) DEFAULT NULL,
  `STATUS` VARCHAR(10) DEFAULT NULL,
  `COMMIT_COUNT` BIGINT DEFAULT NULL,
  `READ_COUNT` BIGINT DEFAULT NULL,
  `FILTER_COUNT` BIGINT DEFAULT NULL,
  `WRITE_COUNT` BIGINT DEFAULT NULL,
  `READ_SKIP_COUNT` BIGINT DEFAULT NULL,
  `WRITE_SKIP_COUNT` BIGINT DEFAULT NULL,
  `PROCESS_SKIP_COUNT` BIGINT DEFAULT NULL,
  `ROLLBACK_COUNT` BIGINT DEFAULT NULL,
  `EXIT_CODE` VARCHAR(2500) DEFAULT NULL,
  `EXIT_MESSAGE` VARCHAR(2500) DEFAULT NULL,
  `LAST_UPDATED` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`STEP_EXECUTION_ID`),
  KEY `JOB_EXEC_STEP_FK` (`JOB_EXECUTION_ID`),
  CONSTRAINT `JOB_EXEC_STEP_FK` FOREIGN KEY (`JOB_EXECUTION_ID`) REFERENCES `BATCH_JOB_EXECUTION` (`JOB_EXECUTION_ID`) ON DELETE CASCADE
);


-- LOBOT.BATCH_STEP_EXECUTION_CONTEXT DEFINITION
CREATE TABLE `BATCH_STEP_EXECUTION_CONTEXT` (
  `STEP_EXECUTION_ID` BIGINT NOT NULL,
  `SHORT_CONTEXT` VARCHAR(2500) NOT NULL,
  `SERIALIZED_CONTEXT` TEXT,
  PRIMARY KEY (`STEP_EXECUTION_ID`),
  CONSTRAINT `STEP_EXEC_CTX_FK` FOREIGN KEY (`STEP_EXECUTION_ID`) REFERENCES `BATCH_STEP_EXECUTION` (`STEP_EXECUTION_ID`) ON DELETE CASCADE
);



-- lobot.cfg_parametrage definition
CREATE TABLE `cfg_parametrage` (
  `code_param` varchar(100) NOT NULL,
  `contexte` varchar(100) NOT NULL DEFAULT '' COMMENT 'contexte : élement différenciant',
  `date_debut` date NOT NULL DEFAULT '1900-01-01',
  `date_fin` date DEFAULT NULL,
  `valeur_param` varchar(1000) NOT NULL,
  `libelle` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`code_param`,`contexte`,`date_debut`)
);


-- lobot.cfg_trt_batch definition
CREATE TABLE `cfg_trt_batch` (
  `code` varchar(25) NOT NULL,
  `intitule` varchar(100) NOT NULL,
  `type_champ_id` varchar(100) NOT NULL,
  `ordre` int NOT NULL DEFAULT '0',
  `nom_fichier_cr` varchar(50) NOT NULL,
  `nom_fichier_ano` varchar(50) DEFAULT NULL,
  `charset` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`code`)
);


-- lobot.domain_vacation definition
CREATE TABLE `domain_vacation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'identifiant unique vacation',
  `repertoire` varchar(100) DEFAULT NULL COMMENT 'chemin vers répertoire sauvegarde',
  `flux_in` varchar(100) NOT NULL COMMENT 'chemin vers flux en entrée',
  `nb_contrats` int NOT NULL COMMENT 'nombre de contrats de la vacation',
  `nb_contrats_KO` int NOT NULL DEFAULT '0' COMMENT 'nombre de contrats en anomalie',
  `date_trt` datetime DEFAULT NULL COMMENT 'date de traitement du flux',
  `flux_ko` varchar(100) DEFAULT NULL COMMENT 'chemin vers fichier flux en anomalie',
  `date_vacation` datetime DEFAULT NULL,
  `date_flux` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `domain_vacation_date_vacation_IDX` (`date_vacation` DESC) USING BTREE,
  KEY `vacation_date_nbKO_IDX` (`date_vacation` DESC,`nb_contrats_KO` DESC) USING BTREE,
  KEY `domain_vacation_date_flux_IDX` (`date_flux` DESC) USING BTREE
);


-- lobot.key_sequence definition
CREATE TABLE `key_sequence` (
  `sequence_name` varchar(100) NOT NULL,
  `next_val` bigint NOT NULL,
  `increment_size` bigint NOT NULL DEFAULT '10',
  PRIMARY KEY (`sequence_name`)
);


-- lobot.cfg_dependance_batch definition
CREATE TABLE `cfg_dependance_batch` (
  `trt_maitre` varchar(25) NOT NULL,
  `trt_dependant` varchar(25) NOT NULL,
  PRIMARY KEY (`trt_maitre`,`trt_dependant`),
  KEY `dependance_batch_trt_batch_FK_1` (`trt_dependant`),
  CONSTRAINT `dependance_batch_trt_batch_FK` FOREIGN KEY (`trt_maitre`) REFERENCES `cfg_trt_batch` (`code`),
  CONSTRAINT `dependance_batch_trt_batch_FK_1` FOREIGN KEY (`trt_dependant`) REFERENCES `cfg_trt_batch` (`code`)
);


-- lobot.domain_batch_vacation definition
CREATE TABLE `domain_batch_vacation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `id_vacation` bigint NOT NULL,
  `code_batch` varchar(25) NOT NULL,
  `nb_anos` int DEFAULT '0',
  `fichier_archive` varchar(250) DEFAULT NULL,
  `type_fichier` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `domain_batch_vacation_domain_vacation_FK` (`id_vacation`),
  KEY `domain_batch_vacation_cfg_trt_batch_FK` (`code_batch`),
  CONSTRAINT `domain_batch_vacation_cfg_trt_batch_FK` FOREIGN KEY (`code_batch`) REFERENCES `cfg_trt_batch` (`code`),
  CONSTRAINT `domain_batch_vacation_domain_vacation_FK` FOREIGN KEY (`id_vacation`) REFERENCES `domain_vacation` (`id`) ON DELETE CASCADE
);


-- lobot.domain_integration_contrat definition
CREATE TABLE `domain_integration_contrat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'identifiant unique intégration',
  `id_vacation` bigint NOT NULL COMMENT 'identifiant unique vacation',
  `num_contrat` varchar(25) NOT NULL COMMENT 'n° contrat collectif',
  `siret` char(14) NOT NULL COMMENT 'n° SIRET de l''établissement',
  `offre_collective` varchar(25) NOT NULL COMMENT 'code offre collective',
  `msg_ano_niv1` varchar(100) DEFAULT NULL COMMENT 'message en cas d''anomalie de niveau 1',
  `trt_ano_niv2` varchar(100) DEFAULT NULL COMMENT 'traitement en erreur si anomalie de niveau 2',
  `nom_fichier_ano` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `integration_contrat_vacation_FK` (`id_vacation`),
  KEY `integration_contrat_trt_batch_FK` (`trt_ano_niv2`),
  KEY `integration_contrat_num_contrat_IDX` (`num_contrat`,`id_vacation` DESC) USING BTREE,
  KEY `integration_contrat_siret_IDX` (`siret`,`id_vacation` DESC) USING BTREE,
  KEY `integration_contrat_offre_collective_IDX` (`offre_collective`,`id_vacation` DESC) USING BTREE,
  KEY `integration_contrat_trt_ano_niv2_IDX` (`trt_ano_niv2`,`id_vacation` DESC) USING BTREE,
  CONSTRAINT `integration_contrat_vacation_FK` FOREIGN KEY (`id_vacation`) REFERENCES `domain_vacation` (`id`) ON DELETE CASCADE
);


-- lobot.domain_reception_contrat definition
CREATE TABLE `domain_reception_contrat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'identifiant item du flux reçu',
  `id_vacation` bigint NOT NULL COMMENT 'identifiant vacation correspondante',
  `num_contrat` varchar(25) NOT NULL COMMENT 'n° contrat collectif',
  PRIMARY KEY (`id`),
  KEY `reception_vacation_FK` (`id_vacation`),
  KEY `reception_num_contrat_IDX` (`num_contrat`,`id_vacation` DESC) USING BTREE,
  CONSTRAINT `reception_vacation_FK` FOREIGN KEY (`id_vacation`) REFERENCES `domain_vacation` (`id`) ON DELETE CASCADE
);


-- lobot.domain_detail_integration definition
CREATE TABLE `domain_detail_integration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'identifiant unique détail contrat',
  `id_integ_contrat` bigint NOT NULL COMMENT 'identifiant unique intégration contrat',
  `code_trt` varchar(25) NOT NULL COMMENT 'code traitement batch',
  `type_champ_id` varchar(100) NOT NULL COMMENT 'nom du champ identifiant',
  `valeur_champ_id` varchar(100) NOT NULL COMMENT 'valeur du champ identifiant',
  `donnee_ano` varchar(250) DEFAULT NULL COMMENT 'valeur de la donnée en anomalie',
  `code_ano` varchar(25) DEFAULT NULL COMMENT 'code anomalie',
  `libelle_ano` varchar(250) DEFAULT NULL COMMENT 'message d''anomalie',
  `etat_integration` varchar(25) NOT NULL DEFAULT 'ATTENTE' COMMENT 'état ''ATTENTE'', ''OK'' ou ''KO''',
  PRIMARY KEY (`id`),
  KEY `detail_integration_integration_contrat_FK` (`id_integ_contrat`),
  KEY `detail_integration_trt_batch_FK` (`code_trt`),
  CONSTRAINT `detail_integration_integration_contrat_FK` FOREIGN KEY (`id_integ_contrat`) REFERENCES `domain_integration_contrat` (`id`) ON DELETE CASCADE,
  CONSTRAINT `detail_integration_trt_batch_FK` FOREIGN KEY (`code_trt`) REFERENCES `cfg_trt_batch` (`code`)
);



-- données 

INSERT INTO cfg_trt_batch (code,intitule,type_champ_id,ordre,nom_fichier_cr,nom_fichier_ano,charset) VALUES
	 ('BS800','Eclatement des flux pour intégration des contrats','N° contrat',10,'BS800_synthese_contrat_*.csv','BS800_synthese_contrat_*.csv','ISO-8859-1'),
	 ('BZ104','Intégration offre collective & garantie technique','Code offre collective',20,'BZ104_*.log','BZ104_ano_fonc_*.csv, BZ104_ano_tech_*.csv',NULL),
	 ('FLUX_CC_IN','Réception du flux des contrats collectifs','N° contrat',0,'BS800_flux_in*.txt',NULL,NULL),
	 ('MSP20','Intégration contrat collectif','N° contrat',60,'MSP20C_synthese*.cr','MSP20C_synthese*.ano',NULL),
	 ('MSP21','Intégration payeur cotisations (interlocution)','N° SIRET',50,'MSP21C_INPUT*','MSP21C_synthese*.ano',NULL),
	 ('MSP23','Intégration interlocuteur entreprise','N° SIREN',40,'MSP23C_synthese*.ano','MSP23C_synthese*.ano',NULL),
	 ('PE023','Intégration entreprise et établissement','N° SIREN',30,'PE023.*.log','PE023.*.ano,PE023.*.cr','ISO-8859-1');


INSERT INTO cfg_parametrage (code_param,contexte,date_debut,date_fin,valeur_param,libelle) VALUES
	 ('flux.archivage.directory','suivi_contrats','1900-01-01',NULL,'/xchange/gdapp/lobot/biuh01/archive','Répertoire pour archivage des fichiers sauvegardés'),
	 ('flux.input.directory','suivi_contrats','1900-01-01',NULL,'/xchange/inputfile/lobot/biuh01/allianz/oav/sante','Répertoire fichiers compte rendu vacation batch');

INSERT INTO cfg_dependance_batch (trt_maitre,trt_dependant) VALUES
	 ('BS800','BZ104'),
	 ('BS800','MSP20'),
	 ('BZ104','MSP20'),
	 ('PE023','MSP20'),
	 ('BS800','MSP21'),
	 ('PE023','MSP21'),
	 ('BS800','MSP23'),
	 ('PE023','MSP23'),
	 ('BS800','PE023');

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID,UNIQUE_KEY) VALUES (1,'0');
INSERT INTO BATCH_JOB_SEQ (ID,UNIQUE_KEY) VALUES (1,'0');
INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID,UNIQUE_KEY) VALUES (1,'0');