# BioStudies CLI

BioStudies CLI is a tool that allows interacting with the BioStudies web application to perform operations over 
submissions.

## Build
To build the CLI, execute the command `gradle :client:bio-commandline:shadowJar`. This will generate the command line 
jar under `build/libs` folder.

## Usage

### Submit
Make a submission to BioStudies

`java -jar build/libs/BioStudiesCLI-2.0.jar submit -s <server> -u <user> -p <password> -i <page tab input> -a <files>`

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** User that will perform the submission.
* **p or --password:** The user password.
* **b or --onBehalf:** Allows performing a submission on behalf of the user with the given e-mail.
* **i or --input:** Path to the file containing the submission page tab.
* **a or --attach:** Comma separated list of paths to the files referenced in the submission.
* **ps or --preferredSource:** Comma separated list of file sources. Valid values are FIRE, SUBMISSION and USER_SPACE.
  The order of the list indicates the priority in which the sources will be used
* **sm or --storageMode:** Submission storage mode. Determines where the submission needs to be saved FIRE/NFS
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
Delete a list of submissions. The accession numbers should be the last parameter in the command separated by a space.

`java -jar build/libs/BioStudiesCLI-2.0.jar delete -s <server> -u <user> -p <password> <accNo1> <accNo2> <accNon>`

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to delete the submission.
* **p or --password:** The user password.
* **b or --onBehalf:** Allows deleting a submission on behalf of the user with the given e-mail.

### Migrate
Migrates a submission to the given target storage mode

`java -jar build/libs/BioStudiesCLI-2.0.jar migrate -s <server> -u <user> -p <password> -ac <accNo> -t <target>`

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to delete the submission.
* **p or --password:** The user password.
* **ac or --accNo:** Accession number of the submission to be transferred.
* **t or --target:** Determines where the submission needs to be transferred to. Valid values are: FIRE/NFS

### Transfer
Transfers the submission ownership from the current user to the given target user

```
java -jar build/libs/BioStudiesCLI-2.0.jar transfer \
-s <server> \
-u <user> \
-p <password> \
-o <owner> \
-to <target owner> \
<accNo1> <accNo2> <accNon>`
```
> Note: The accession list is optional. If not provided, all submissions owned by the current user will be transferred.

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to transfer submissions.
* **p or --password:** The user password.
* **o or --owner:** User that owns the submissions to be transferred.
* **to or --targetOwner:** User that will own the transferred submissions.

### Generate DOI
Allows generating a DOI for the given submission.

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** BioStudies user with privileges to generate a DOI for the submission.
* **p or --password:** The user password.
* **ac or --accNo:** Accession number of the submission to generate DOI.

```
java -jar build/libs/BioStudiesCLI-2.0.jar generateDoi \
-s <server> \
-u <user> \
-p <password> \
-ac <accNo>
```

### Validate File List
Validates the given file list contains valid pagetab, and the referenced files exist either in the user folder or in the
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
* **b or --onBehalf:** Allows deleting a submission on behalf of the user with the given e-mail.
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

### Upload User Files
Upload files to the user folder.

```
java -jar BioStudiesCLI-2.0.jar uploadUserFiles \
-s <server> \
-u <user> \
-p <password> \
-f <files> \
-rp <relative path>
```

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** User that will upload the files.
* **p or --password:** The user password.
* **f or --file:** Path of the file to be uploaded.
* **rp or --relPath:** Relative user folder path where the file will be located.

### Delete User Files
Delete files from the user folder.

```
java -jar BioStudiesCLI-2.0.jar deleteUserFiles \
-s <server> \
-u <user> \
-p <password> \
-f <file name> \
-rp <relative path>
```

#### Arguments
* **s or --server:** BioStudies instance URL.
* **u or --user:** User that will upload the files.
* **p or --password:** The user password.
* **f or --file:** Name of the file to be deleted.
* **rp or --relPath:** Relative user folder path where the file to delete is located.
