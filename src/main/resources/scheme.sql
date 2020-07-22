CREATE TABLE IF NOT EXISTS DATA_ASSET(
	ASSET_ID NUMBER PRIMARY KEY,
	ASSET_NAME VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS DATA_GROUP(
	GROUP_ID NUMBER PRIMARY KEY,
	GROUP_NAME VARCHAR(32),
	GROUP_CREAT_DATE TIMESTAMP,
	GROUP_ASSET_ID NUMBER,
	FOREIGN KEY (GROUP_ASSET_ID) REFERENCES DATA_ASSET(ASSET_ID)
);

CREATE TABLE IF NOT EXISTS DATA_ITEM(
	ITEM_ID NUMBER PRIMARY KEY,
	PROSPECT_EMAIL VARCHAR(64),
	EMAIL_ISP VARCHAR(16),
	ITEM_GROUP_ID NUMBER,
	FOREIGN KEY(ITEM_GROUP_ID) REFERENCES DATA_GROUP(GROUP_ID)
);

CREATE TABLE IF NOT EXISTS FILTERED_GROUP_INFO(
	FGI_ID NUMBER PRIMARY KEY,
	SUPPRESSION_ID NUMBER,
	SUPPRESSION_LOCATION VARCHAR(64),
	SUPPRESSION_PLATFORM VARCHAR(8),
	FILTERED_DATA_COUNT NUMBER,
	FGI_GROUP_ID NUMBER,
	FOREIGN KEY(FGI_GROUP_ID) REFERENCES DATA_GROUP(GROUP_ID)
);

CREATE SEQUENCE IF NOT EXISTS DATA_ASSET_PK_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS DATA_GROUP_PK_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS DATA_ITEM_PK_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS FILTERED_GROUP_INFO_PK_SEQ START WITH 1 INCREMENT BY 1;