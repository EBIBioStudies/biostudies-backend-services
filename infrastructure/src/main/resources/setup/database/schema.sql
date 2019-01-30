CREATE TABLE AccessTag (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    description   VARCHAR(255) NULL,
    name          VARCHAR(255) NULL,
    CONSTRAINT access_tag_name_idx UNIQUE (name)
);

CREATE TABLE Classifier (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NULL,
    name        VARCHAR(255) NULL,
    CONSTRAINT classifier_name_idx UNIQUE (name)
);

CREATE TABLE Counter (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    maxCount BIGINT       NOT NULL,
    name     VARCHAR(255) NULL,
    CONSTRAINT counter_name_idx UNIQUE (name)
);

CREATE TABLE FileAttribute (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NULL,
    valueQualifierString LONGTEXT NULL,
    file_id              BIGINT   NULL,
    ord                  INT      NULL
);

CREATE INDEX FKek4om17ruuhrjo2gmirdxevay ON FileAttribute (file_id);

CREATE TABLE FileRef (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    directory  BIT          NOT NULL,
    name       VARCHAR(255) NULL,
    size       BIGINT       NOT NULL,
    tableIndex INT          NOT NULL,
    sectionId  BIGINT       NULL,
    ord        INT          NULL,
    path       VARCHAR(255) NULL
);

CREATE INDEX FK464kkuexjpycuic1n33q0yhe2 ON FileRef (sectionId);

ALTER TABLE FileAttribute ADD CONSTRAINT FKek4om17ruuhrjo2gmirdxevay FOREIGN KEY (file_id) REFERENCES FileRef (id);

CREATE TABLE IdGen (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    prefix     VARCHAR(255) NULL,
    suffix     VARCHAR(255) NULL,
    counter_id BIGINT       NULL,
    CONSTRAINT pfxsfx_idx
    UNIQUE (prefix, suffix),
    CONSTRAINT FKjndkokb5qh9p1af7cim617rvv
    FOREIGN KEY (counter_id) REFERENCES Counter (id)
);

CREATE INDEX FKjndkokb5qh9p1af7cim617rvv ON IdGen (counter_id);

CREATE TABLE Link (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tableIndex INT          NOT NULL,
    url        VARCHAR(255) NULL,
    section_id BIGINT       NULL,
    ord        INT          NULL
);

CREATE INDEX FKqhsnrwf0i6q08gt5l83fwchn8 ON Link (section_id);

CREATE TABLE LinkAttribute (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NULL,
    valueQualifierString LONGTEXT NULL,
    link_id              BIGINT   NULL,
    ord                  INT      NULL,
    CONSTRAINT FKiy7ig2d3ubfsc921qrarw4x5n
    FOREIGN KEY (link_id) REFERENCES Link (id)
);

CREATE INDEX FKiy7ig2d3ubfsc921qrarw4x5n ON LinkAttribute (link_id);

CREATE TABLE Section (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    accNo         VARCHAR(255) NULL,
    parentAccNo   VARCHAR(255) NULL,
    tableIndex    INT          NOT NULL,
    type          VARCHAR(255) NULL,
    parent_id     BIGINT       NULL,
    submission_id BIGINT       NULL,
    ord           INT          NULL,
    CONSTRAINT FKba6xolosvegauoq8xs1kj17ch
    FOREIGN KEY (parent_id) REFERENCES Section (id)
);

CREATE INDEX acc_idx ON Section (accNo);
CREATE INDEX FK4bi0ld27mvrinwk6gleu9phf4 ON Section (submission_id);
CREATE INDEX FKba6xolosvegauoq8xs1kj17ch ON Section (parent_id);
CREATE INDEX glob_idx ON Section (global);

CREATE INDEX section_type_index ON Section (type);
ALTER TABLE FileRef ADD CONSTRAINT FK464kkuexjpycuic1n33q0yhe2
FOREIGN KEY (sectionId) REFERENCES Section (id);
ALTER TABLE Link ADD CONSTRAINT FKqhsnrwf0i6q08gt5l83fwchn8
FOREIGN KEY (section_id) REFERENCES Section (id);

CREATE TABLE SectionAttribute (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NULL,
    valueQualifierString LONGTEXT NULL,
    section_id           BIGINT   NULL,
    ord                  INT      NULL,
    CONSTRAINT FK93fwpmt18ghb0hktnsljtlnhu
    FOREIGN KEY (section_id) REFERENCES Section (id)
);

CREATE INDEX FK93fwpmt18ghb0hktnsljtlnhu ON SectionAttribute (section_id);

CREATE TABLE Submission (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    CTime          BIGINT       NOT NULL,
    MTime          BIGINT       NOT NULL,
    RTime          BIGINT       NOT NULL,
    accNo          VARCHAR(255) NULL,
    relPath        LONGTEXT     NULL,
    released       BIT          NOT NULL,
    rootPath       LONGTEXT     NULL,
    title          LONGTEXT     NULL,
    version        INT          NOT NULL,
    owner_id       BIGINT       NULL,
    rootSection_id BIGINT       NULL,
    secretKey      VARCHAR(255) NULL,
    CONSTRAINT UKalkiyx9bg56ika8jw65r99fll
    UNIQUE (accNo, version),
    CONSTRAINT FKhsm5gtat31dkrft0was3a7gr7
    FOREIGN KEY (rootSection_id) REFERENCES Section (id)
);

CREATE INDEX FKhsm5gtat31dkrft0was3a7gr7 ON Submission (rootSection_id);
CREATE INDEX FKidqs3m2ntuqyuiophfwikw81a ON Submission (owner_id);
CREATE INDEX released_idx ON Submission (released);
CREATE INDEX rtime_idx ON Submission (RTime);

ALTER TABLE Section ADD CONSTRAINT FK4bi0ld27mvrinwk6gleu9phf4
FOREIGN KEY (submission_id) REFERENCES Submission (id);

CREATE TABLE SubmissionAttribute (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NULL,
    valueQualifierString LONGTEXT NULL,
    submission_id        BIGINT   NULL,
    ord                  INT      NULL,
    CONSTRAINT FKstek2rbmsk052iydxt2eamv15
    FOREIGN KEY (submission_id) REFERENCES Submission (id)
);

CREATE INDEX FKstek2rbmsk052iydxt2eamv15 ON SubmissionAttribute (submission_id);

CREATE TABLE Submission_AccessTag (
    Submission_id BIGINT NOT NULL,
    accessTags_id BIGINT NOT NULL,
    CONSTRAINT FK6kvcm7vgoutbie7um590vt5ev
    FOREIGN KEY (Submission_id) REFERENCES Submission (id),
    CONSTRAINT FKgsgxljia12i17av51pl5el3c0
    FOREIGN KEY (accessTags_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK6kvcm7vgoutbie7um590vt5ev ON Submission_AccessTag (Submission_id);
CREATE INDEX FKgsgxljia12i17av51pl5el3c0 ON Submission_AccessTag (accessTags_id);

CREATE TABLE Tag (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    description   VARCHAR(255) NULL,
    name          VARCHAR(255) NULL,
    classifier_id BIGINT       NULL,
    parent_tag_id BIGINT       NULL,
    CONSTRAINT name_idx
    UNIQUE (name),
    CONSTRAINT classifier_fk
    FOREIGN KEY (classifier_id) REFERENCES Classifier (id),
    CONSTRAINT parent_tag_fk
    FOREIGN KEY (parent_tag_id) REFERENCES Tag (id)
);

CREATE INDEX classifier_fk ON Tag (classifier_id);
CREATE INDEX parent_tag_fk ON Tag (parent_tag_id);

-- Security
CREATE TABLE User (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    activationKey  VARCHAR(255) NULL,
    active         BIT          NOT NULL,
    auxProfileInfo LONGTEXT     NULL,
    email          VARCHAR(255) NULL,
    fullName       VARCHAR(255) NULL,
    keyTime        BIGINT       NOT NULL,
    login          VARCHAR(255) NULL,
    passwordDigest LONGBLOB     NULL,
    secret         VARCHAR(255) NULL,
    superuser      BIT          NOT NULL,
    ssoSubject     VARCHAR(255) NULL,
    CONSTRAINT email_index
    UNIQUE (email),
    CONSTRAINT login_index
    UNIQUE (login)
);

ALTER TABLE Submission ADD CONSTRAINT FKidqs3m2ntuqyuiophfwikw81a
FOREIGN KEY (owner_id) REFERENCES User (id);

CREATE TABLE UserData (
    dataKey     VARCHAR(255) NOT NULL,
    userId      BIGINT       NOT NULL,
    data        LONGTEXT     NULL,
    contentType VARCHAR(255) NULL,
    topic       VARCHAR(255) NULL,
    PRIMARY KEY (dataKey, userId)
);

CREATE TABLE UserGroup (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NULL,
    name        VARCHAR(255) NULL,
    project     BIT          NOT NULL,
    owner_id    BIGINT       NULL,
    secret      VARCHAR(255) NULL,
    CONSTRAINT name_index
    UNIQUE (name),
    CONSTRAINT FKt6580c8mqsfigvlgbtepcdnnk
    FOREIGN KEY (owner_id) REFERENCES User (id)
);

CREATE INDEX FKt6580c8mqsfigvlgbtepcdnnk ON UserGroup (owner_id);

CREATE TABLE UserGroup_User (
    groups_id BIGINT NOT NULL,
    users_id  BIGINT NOT NULL,
    CONSTRAINT FK7t0wbkhu02mbvoxwt7np5h0xv
    FOREIGN KEY (groups_id) REFERENCES UserGroup (id),
    CONSTRAINT FK77fyj1avmh71l1dgqu5rl516l
    FOREIGN KEY (users_id) REFERENCES User (id)
);

CREATE INDEX FK77fyj1avmh71l1dgqu5rl516l ON UserGroup_User (users_id);
CREATE INDEX FK7t0wbkhu02mbvoxwt7np5h0xv ON UserGroup_User (groups_id);

CREATE TABLE UserGroup_UserGroup (
    UserGroup_id BIGINT NOT NULL,
    groups_id    BIGINT NOT NULL,
    CONSTRAINT FK2eixf3lpm38fj2ffey19uqnqg
    FOREIGN KEY (UserGroup_id) REFERENCES UserGroup (id),
    CONSTRAINT FKdm8ojg4ou9wj3j5r9s9mhp6me
    FOREIGN KEY (groups_id) REFERENCES UserGroup (id)
);

CREATE INDEX FK2eixf3lpm38fj2ffey19uqnqg ON UserGroup_UserGroup (UserGroup_id);
CREATE INDEX FKdm8ojg4ou9wj3j5r9s9mhp6me ON UserGroup_UserGroup (groups_id);

CREATE TABLE SecurityToken
(
    id VARCHAR(500) PRIMARY KEY NOT NULL,
    invalidation_date DATETIME NOT NULL
);
CREATE UNIQUE INDEX SecurityToken_id_uindex ON SecurityToken (id);
