# BioStudies CLI

BioStudies CLI is a tool that allows interacting with the BioStudies web application in order to perform operations over
submissions.

## Build
In order to build the CLI, execute the command `gradle :client:bio-commandline:shadowJar`. This will generate the
command line jar under `build/libs` folder.

## Usage

### Submit
Make a submission to BioStudies

`java -jar build/libs/BioStudiesCLI-2.0.jar submit -s <server> -u <user> -p <password> -i <page tab input> -a <files>`

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** User that will perform the submission.
* **p or --password:** The user password.
* **b or --onBehalf:** Allows to perform a submission on behalf of the user with the given e-mail.
* **i or --input:** Path to the file containing the submission page tab.
* **a or --attach:** Comma separated list of paths to the files referenced in the submission.
* **ps or --preferredSource:** Comma separated list of file sources. Valid values are FIRE, SUBMISSION and USER_SPACE.
  The order of the list indicates the priority in which the sources will be used
* **sm or --storageMode:** Submission storage mode. Determines where submission need to be saved FIRE/NFS
* **aw or --await:** Indicates whether to wait for the submission processing
* **sj or --splitJobs:** Indicates whether the submission should be processed in individual jobs per each stage

**Notes:**

* The parameter to attach files is optional. If no files are attached, the files referenced in the submission page tab
  will be retrieved from the user folder.
* If one of the paths provided to the attached argument is a directory, all files inside it will be used as attachments.
* The submission format doesn't need to be specified since it'll be inferred from the input file's extension.
* TSV submissions can be submitted using a XLSX file.

### Submission Request Status
Get the status of a submission request

```
java -jar build/libs/BioStudiesCLI-2.0.jar requestStatus \
-s <server> \
-u <user> \
-p <password> \
-ac <accNo> \
-v <version>
```

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to get the submission request.
* **p or --password:** The user password.
* **ac or --accNo:** Accession number of the submission request to check the status.
* **v or --version:** Version of the submission request to check the status.

### Delete
Delete a submission

`java -jar build/libs/BioStudiesCLI-2.0.jar delete -s <server> -u <user> -p <password> -ac <accNo>`

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to delete the submission.
* **p or --password:** The user password.
* **ac or --accNo:** Accession number of the submission to delete.
* **b or --onBehalf:** Allows to delete a submission on behalf of the user with the given e-mail.

### Migrate
Migrate a submission from one environment to another

```
java -jar build/libs/BioStudiesCLI-2.0.jar migrate \
-ac <accNo> \
-s <source> \
-su <source user> \
-sp <source password> \
-t <target> \
-tu <target user> \
-tp <target password> \
-as <true / false>
```

#### Arguments
* **ac or --accNo:** Accession number of the submission to migrate.
* **s or --server:** BioStudies environment to take the submission from.
* **su or --sourceUser:** BioStudies user in the source environment. (Only superusers can perform this operation)
* **sp or --sourcePassword:** Password for the BioStudies user in the source environment.
* **t or --target:** BioStudies environment to migrate the submission to.
* **tu or --targetUser:** BioStudies user in the target environment. (Only superusers can perform this operation)
* **tp or --targetPassword:** Password for the BioStudies user in the target environment.
* **to or --targetOwner:** New owner for the submission in the target environment. This is an optional parameter. If it
  isn't provided, the current submission owner should exist in the target environment.
* **as or --async:** Indicates whether the migration should be processed in async mode.

### Transfer
Transfers a submission to the given target storage mode

`java -jar build/libs/BioStudiesCLI-2.0.jar transfer -s <server> -u <user> -p <password> -ac <accNo> -t <target>`

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to delete the submission.
* **p or --password:** The user password.
* **ac or --accNo:** Accession number of the submission to be transferred.
* **t or --target:** Determines where submission need to be transferred to. Valid values are: FIRE/NFS

### Validate File List
Validates the given file list contains valid pagetab and the referenced files exist either in the user folder or in the
submission files of the given accession.

```
java -jar build/libs/BioStudiesCLI-2.0.jar validateFileList \
-s <server> \
-u <user> \
-p <password> \
-b <onBehalf> \
-f <fileListPath> \
-ac <accNo> \
-rp <rootPath>
```

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to delete the submission.
* **p or --password:** The user password.
* **b or --onBehalf:** Allows to delete a submission on behalf of the user with the given e-mail.
* **f or --fileListPath:** Path to the file list to be validated. The path is relative to the user folder.
* **ac or --accNo:** The accNo for the submission which files will be included in the search.
* **rp or --rootPath:** Base path to search for the files in the user folder.

### Grant Permission
Grants the specified permissions for the given user in the given collection.

```
java -jar BioStudiesCLI-2.0.jar grantPermission \
-s <server> \
-u <user> \
-p <password> \
-at <access type> \
-tu <target user> \
-ac <collection accNo>
```

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to delete the submission.
* **p or --password:** The user password.
* **at or --accessType:** Access type to be granted to the user. Valid values are: *ADMIN, ATTACH, UPDATE, READ, DELETE*
* **tu or --targetUser:** User to whom the access permission will be granted.
* **ac or --accNo:** The accession to grant the permission to. The accession must exist.

### Revoke Permission
Revokes the specified permissions for the given user in the given collection.

```
java -jar BioStudiesCLI-2.0.jar revokePermission \
-s <server> \
-u <user> \
-p <password> \
-at <access type> \
-tu <target user> \
-ac <collection accNo>
```

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to delete the submission.
* **p or --password:** The user password.
* **at or --accessType:** Access type to be revoked to the user. Valid values are: *ADMIN, ATTACH, UPDATE, READ, DELETE*
* **tu or --targetUser:** User to whom the access permission will be revoked.
* **ac or --accNo:** The accession to revoke the permission to. The accession must exist.
