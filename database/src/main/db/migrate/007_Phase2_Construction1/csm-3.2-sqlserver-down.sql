ALTER TABLE CSM_PROTECTION_ELEMENT DROP COLUMN PROTECTION_ELEMENT_TYPE;
ALTER TABLE CSM_PROTECTION_ELEMENT DROP CONSTRAINT  UQ_PE_OBJECT_ID_ATTRIBUTE_APP_ID;
ALTER TABLE CSM_APPLICATION DROP COLUMN DATABASE_URL;
ALTER TABLE CSM_APPLICATION DROP COLUMN DATABASE_USER_NAME;
ALTER TABLE CSM_APPLICATION DROP COLUMN DATABASE_PASSWORD;
ALTER TABLE CSM_APPLICATION DROP COLUMN DATABASE_DIALECT;
ALTER TABLE CSM_APPLICATION DROP COLUMN DATABASE_DRIVER;