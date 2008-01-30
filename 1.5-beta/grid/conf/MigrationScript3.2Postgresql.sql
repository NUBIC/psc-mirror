UPDATE csm_application SET DECLARATIVE_FLAG='0';

ALTER TABLE CSM_PROTECTION_ELEMENT DROP COLUMN PROTECTION_ELEMENT_TYPE_ID;
ALTER TABLE CSM_PROTECTION_ELEMENT ADD COLUMN PROTECTION_ELEMENT_TYPE VARCHAR(100);
ALTER TABLE CSM_PG_PE alter COLUMN UPDATE_DATE type date;
ALTER TABLE CSM_APPLICATION alter COLUMN APPLICATION_NAME type VARCHAR(255);
ALTER TABLE CSM_APPLICATION alter COLUMN APPLICATION_NAME set  NOT NULL;
ALTER TABLE CSM_GROUP alter COLUMN GROUP_NAME type VARCHAR(255);
ALTER TABLE CSM_GROUP alter COLUMN GROUP_NAME set NOT NULL;
ALTER TABLE CSM_PROTECTION_ELEMENT ADD constraint  UQ_PE_OBJECT_ID_ATTRIBUTE_APP_ID UNIQUE(PROTECTION_ELEMENT_NAME, ATTRIBUTE, APPLICATION_ID);

ALTER TABLE CSM_APPLICATION ADD COLUMN DATABASE_URL VARCHAR(100);
ALTER TABLE CSM_APPLICATION ADD COLUMN DATABASE_USER_NAME VARCHAR(100);
ALTER TABLE CSM_APPLICATION ADD COLUMN DATABASE_PASSWORD VARCHAR(100);
ALTER TABLE CSM_APPLICATION ADD COLUMN DATABASE_DIALECT VARCHAR(100);
ALTER TABLE CSM_APPLICATION ADD COLUMN DATABASE_DRIVER VARCHAR(100);

UPDATE csm_pg_pe SET UPDATE_DATE=NULL;

