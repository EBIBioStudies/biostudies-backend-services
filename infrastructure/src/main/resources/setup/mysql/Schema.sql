CREATE TABLE AccessTag
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NULL,
    name        VARCHAR(255) NULL,
    CONSTRAINT AccessTag_name_idx UNIQUE (name)
);

CREATE TABLE Counter
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    maxCount BIGINT NOT NULL,
    name     VARCHAR(255) NULL,
    CONSTRAINT Counter_name_idx UNIQUE (name)
);

CREATE TABLE FileAttribute
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NOT NULL,
    valueQualifierString LONGTEXT NULL,
    file_id              BIGINT NULL,
    ord                  INT NULL
);

CREATE INDEX FileAttribute_file_id_IDX ON FileAttribute (file_id);

CREATE TABLE FileRef
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NULL,
    size       BIGINT NOT NULL,
    tableIndex INT    NOT NULL,
    sectionId  BIGINT NULL,
    ord        INT NULL,
    path       VARCHAR(255) NULL,
    directory  BIT    NOT NULL
);

CREATE INDEX FileRef_sectionId_IDX ON FileRef (sectionId);

ALTER TABLE FileAttribute
    ADD CONSTRAINT FileRef_FileAttribute_FRG_KEY FOREIGN KEY (file_id) REFERENCES FileRef (id) ON DELETE CASCADE;

CREATE TABLE ReferencedFileAttribute
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NOT NULL,
    valueQualifierString LONGTEXT NULL,
    referenced_file_id   BIGINT NULL,
    ord                  INT NULL
);

CREATE INDEX ReferencedFileAttrFileId_IDX ON ReferencedFileAttribute (referenced_file_id);

CREATE TABLE ReferencedFile
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NULL,
    size       BIGINT NOT NULL,
    fileListId BIGINT NULL,
    path       VARCHAR(255) NULL,
    ord        INT NULL
);

CREATE INDEX ReferencedFile_FileList_IDX ON ReferencedFile (fileListId);

ALTER TABLE ReferencedFileAttribute
    ADD CONSTRAINT ReferencedFile_ReferencedFileAttr_FRG_KEY FOREIGN KEY (referenced_file_id) REFERENCES ReferencedFile (id);

CREATE TABLE FileList
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

ALTER TABLE ReferencedFile
    ADD CONSTRAINT ReferencedFile_FileList_FRG_KEY FOREIGN KEY (fileListId) REFERENCES FileList (id);

CREATE TABLE IdGen
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    prefix     VARCHAR(255) NULL,
    suffix     VARCHAR(255) NULL,
    counter_id BIGINT NULL,
    CONSTRAINT pfxsfx_idx UNIQUE (prefix, suffix),
    CONSTRAINT IdGen_Counter_FRG_KEY FOREIGN KEY (counter_id) REFERENCES Counter (id)
);

CREATE INDEX FKjndkokb5qh9p1af7cim617rvv ON IdGen (counter_id);

CREATE TABLE Link
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tableIndex INT NOT NULL,
    url        VARCHAR(255) NULL,
    section_id BIGINT NULL,
    ord        INT NULL
);

CREATE INDEX Link_Section_IDX ON Link (section_id);

CREATE TABLE LinkAttribute
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NOT NULL,
    valueQualifierString LONGTEXT NULL,
    link_id              BIGINT NULL,
    ord                  INT NULL,
    CONSTRAINT Link_LinkAttribute_FRG_KEY FOREIGN KEY (link_id) REFERENCES Link (id) ON DELETE CASCADE
);

CREATE INDEX LinkAttribute_Link_IDX ON LinkAttribute (link_id);

CREATE TABLE Section
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    accNo         VARCHAR(255) NULL,
    parentAccNo   VARCHAR(255) NULL,
    tableIndex    INT NOT NULL,
    type          VARCHAR(255) NULL,
    parent_id     BIGINT NULL,
    submission_id BIGINT NULL,
    fileListId    BIGINT NULL,
    ord           INT NULL,
    CONSTRAINT Section_SectionParent_FRG_KEY FOREIGN KEY (parent_id) REFERENCES Section (id) ON DELETE SET NULL,
    CONSTRAINT FileList_Section_FRG_KEY FOREIGN KEY (fileListId) REFERENCES FileList (id) ON DELETE CASCADE
);

CREATE INDEX Section_type_IDX ON Section (type);
CREATE INDEX Section_accNo_IDX ON Section (accNo);
CREATE INDEX Section_ParentSection_IDX ON Section (parent_id);
CREATE INDEX Section_Submission_IDX ON Section (submission_id);

ALTER TABLE Link
    ADD CONSTRAINT Link_Section_FRG_KEY FOREIGN KEY (section_id) REFERENCES Section (id) ON DELETE CASCADE;
ALTER TABLE FileRef
    ADD CONSTRAINT FileRef_Section_FRG_KEY FOREIGN KEY (sectionId) REFERENCES Section (id) ON DELETE CASCADE;

CREATE TABLE SectionAttribute
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NOT NULL,
    valueQualifierString LONGTEXT NULL,
    section_id           BIGINT NULL,
    ord                  INT NULL,
    CONSTRAINT Section_SectionAttribute_FRG_KEY FOREIGN KEY (section_id) REFERENCES Section (id) ON DELETE CASCADE
);

CREATE INDEX SectionAttribute_IDX ON SectionAttribute (section_id);

CREATE TABLE Submission
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    CTime          BIGINT       NOT NULL,
    MTime          BIGINT       NOT NULL,
    RTime          BIGINT       NOT NULL,
    accNo          VARCHAR(255) NOT NULL,
    relPath        LONGTEXT NULL,
    method         LONGTEXT NULL,
    released       BIT          NOT NULL,
    rootPath       LONGTEXT NULL,
    title          LONGTEXT NULL,
    version        INT          NOT NULL,
    owner_id       BIGINT NULL,
    submitter_id   BIGINT NULL,
    rootSection_id BIGINT NULL,
    secretKey      VARCHAR(255) NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PROCESSED',
    CONSTRAINT Submission_accNo_version_UNQ UNIQUE (accNo, version),
    CONSTRAINT Submission_RootSection_FRG_KEY FOREIGN KEY (rootSection_id) REFERENCES Section (id) ON DELETE SET NULL
);

CREATE INDEX Submission_Root_Section_IDX ON Submission (rootSection_id);
CREATE INDEX Submission_Owner_IDX ON Submission (owner_id);
CREATE INDEX Submission_Status_IDX ON Submission (status);
CREATE INDEX version_idx ON Submission (version);
CREATE INDEX released_idx ON Submission (released);
CREATE INDEX rtime_idx ON Submission (RTime);

ALTER TABLE Section
    ADD CONSTRAINT Submission_Section_FRG_KEY
        FOREIGN KEY (submission_id) REFERENCES Submission (id) ON DELETE CASCADE;

CREATE TABLE SubmissionAttribute
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NOT NULL,
    valueQualifierString LONGTEXT NULL,
    submission_id        BIGINT NULL,
    ord                  INT NULL,
    CONSTRAINT Submission_SubmissionAttr_FRG_KEY FOREIGN KEY (submission_id) REFERENCES Submission (id) ON DELETE CASCADE
);

CREATE INDEX SubmissionAtrribute_IDX ON SubmissionAttribute (submission_id);

CREATE TABLE Submission_AccessTag
(
    Submission_id BIGINT NOT NULL,
    accessTags_id BIGINT NOT NULL,
    CONSTRAINT Submission_AccessTag_FRG_KEY FOREIGN KEY (Submission_id) REFERENCES Submission (id),
    CONSTRAINT AccessTag_Submission_FRG_KEY FOREIGN KEY (accessTags_id) REFERENCES AccessTag (id)
);

CREATE INDEX Submission_AccessTag_AccesTag_IDX ON Submission_AccessTag (accessTags_id);
CREATE INDEX Submission_AccessTag_Submission_IDX ON Submission_AccessTag (Submission_id);

CREATE TABLE ElementTag
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    classifier VARCHAR(255) NOT NULL,
    CONSTRAINT tag_name UNIQUE (classifier, name)
);

CREATE TABLE Submission_ElementTag
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_id        BIGINT NULL,
    submission_id BIGINT NULL,
    CONSTRAINT Tag_Submission_FRG_KEY FOREIGN KEY (tag_id) REFERENCES ElementTag (id),
    CONSTRAINT Submission_ElementTag_FRG_KEY FOREIGN KEY (submission_id) REFERENCES Submission (id)
);

CREATE INDEX Submission_ElementTag_Tag_Id_IDX ON Submission_ElementTag (tag_id);
CREATE INDEX Submission_ElementTag_Submission_Id_IDX ON Submission_ElementTag (submission_id);

CREATE TABLE SubmissionRT
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    accNo    LONGTEXT NOT NULL,
    ticketId LONGTEXT NOT NULL
);

CREATE TABLE SubmissionStat
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    accNo VARCHAR(255) NOT NULL,
    value BIGINT       NOT NULL,
    type  LONGTEXT     NOT NULL,
    CONSTRAINT Submission_Stat_FRG_KEY FOREIGN KEY (accNo) REFERENCES Submission (accNo) ON DELETE CASCADE
);

-- Security
CREATE TABLE User
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    activationKey        VARCHAR(255) NULL,
    active               BIT    NOT NULL,
    auxProfileInfo       LONGTEXT NULL,
    email                VARCHAR(255) NULL,
    fullName             VARCHAR(255) NULL,
    keyTime              BIGINT NOT NULL,
    login                VARCHAR(255) NULL,
    passwordDigest       LONGBLOB NULL,
    secret               VARCHAR(255) NULL,
    superuser            BIT    NOT NULL,
    ssoSubject           VARCHAR(255) NULL,
    notificationsEnabled BIT    NOT NULL,
    CONSTRAINT email_index UNIQUE (email),
    CONSTRAINT login_index UNIQUE (login)
);

ALTER TABLE Submission
    ADD CONSTRAINT Submission_User_FRG_KEY FOREIGN KEY (owner_id) REFERENCES User (id);

CREATE TABLE UserData
(
    dataKey     VARCHAR(255) NOT NULL,
    userId      BIGINT       NOT NULL,
    data        LONGTEXT NULL,
    contentType VARCHAR(255) NULL,
    topic       VARCHAR(255) NULL,
    PRIMARY KEY (dataKey, userId)
);

CREATE TABLE UserGroup
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NULL,
    name        VARCHAR(255) NULL,
    owner_id    BIGINT NULL,
    secret      VARCHAR(255) NULL,
    CONSTRAINT UserGroup_name_IDX UNIQUE (name),
    CONSTRAINT UserGroup_OwnerId_FRG_KEY FOREIGN KEY (owner_id) REFERENCES User (id)
);

CREATE INDEX UserGroup_owner_id_IDX ON UserGroup (owner_id);

CREATE TABLE UserGroup_User
(
    groups_id BIGINT NOT NULL,
    users_id  BIGINT NOT NULL,
    CONSTRAINT UserGroup_User_FRG_KEY FOREIGN KEY (groups_id) REFERENCES UserGroup (id),
    CONSTRAINT User_UserGroup_FRG_KEY FOREIGN KEY (users_id) REFERENCES User (id)
);

CREATE INDEX UserGroup_User_UserId_IDX ON UserGroup_User (users_id);
CREATE INDEX UserGroup_User_GroupId_IDX ON UserGroup_User (groups_id);

CREATE TABLE UserGroup_UserGroup
(
    UserGroup_id BIGINT NOT NULL,
    groups_id    BIGINT NOT NULL,
    CONSTRAINT UserGroup_UserGroup_UserGroupId_FRG_KEY FOREIGN KEY (UserGroup_id) REFERENCES UserGroup (id),
    CONSTRAINT UserGroup_UserGroup_Group_id_FRG_KEY FOREIGN KEY (groups_id) REFERENCES UserGroup (id)
);

CREATE INDEX UserGroup_UserGroup_GroupId_IDX ON UserGroup_UserGroup (groups_id);
CREATE INDEX UserGroup_UserGroup_UserGroupId_IDX ON UserGroup_UserGroup (UserGroup_id);

CREATE TABLE AccessPermission
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    access_type   VARCHAR(255),
    user_id       BIGINT NOT NULL,
    access_tag_id BIGINT NOT NULL
);

ALTER TABLE AccessPermission
    ADD CONSTRAINT access_permission_user_fk FOREIGN KEY (user_id) REFERENCES User (id);
ALTER TABLE AccessPermission
    ADD CONSTRAINT access_permission_access_tag_fk
        FOREIGN KEY (access_tag_id) REFERENCES AccessTag (id);

CREATE UNIQUE INDEX access_permission_id_index ON AccessPermission (id);

CREATE TABLE SecurityToken
(
    id                VARCHAR(500) PRIMARY KEY NOT NULL,
    invalidation_date DATETIME                 NOT NULL
);
CREATE UNIQUE INDEX SecurityToken_id_uindex ON SecurityToken (id);

CREATE TABLE SubmissionRequest
(
    id       int auto_increment,
    accNo    nvarchar(200) not null,
    version  int      not null,
    draftKey VARCHAR(500) null,
    fileMode VARCHAR(500) null,
    request  longtext not null,
    constraint SubmissionRequest_pk primary key (id)
);

CREATE index SubmissionRequest_accNo_version_index on SubmissionRequest (accNo, version);
