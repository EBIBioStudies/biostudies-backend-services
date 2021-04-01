# BioStudies CLI

The BioStudies CLI is a tool that allows interacting with the BioStudies web application in order to perform
operations over submissions. 

## Build
In order to build the CLI, execute the command `gradle clean build shadowJar`. This will generate the command line jar
under `build/libs` folder.

## Usage

### Submit
Make a submission to BioStudies

`java -jar build/libs/BioStudiesCLI-2.0.jar submit -s <server> -u <user> -p <password> -i <page tab input> -a <files>`

### Arguments
* **s:** BioStudies instance URL.
* **u:** User that will perform the submission.
* **p:** The user password.
* **i:** Path to the file containing the submission page tab.
* **a:** Comma separated list of paths to the files referenced in the submission.
* **b:** Allows to perform a submission on behalf of the user with the given e-mail.
 
**Notes:**

* The parameter to attach files is optional. If no files are attached, the files referenced in the submission page tab
will be retrieved from the user folder.
* If one of the paths provided to the attached argument is a directory, all files inside it will be used as attachments.
* The submission format doesn't need to be specified since it'll be inferred from the input file's extension.
* TSV submissions can be submitted using a XLSX file.

### Delete
Delete a submission

`java -jar build/libs/BioStudiesCLI-2.0.jar delete -s <server> -u <user> -p <password> -ac <accNo>`

### Arguments
* **s:** BioStudies instance URL.
* **u:** BioStudies user with privileges to delete the submission.
* **p:** The user password.
* **ac:** Accession number of the submission to delete.
* **b:** Allows to delete a submission on behalf of the user with the given e-mail.

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
-tp <target password>
```

### Arguments
* **ac:** Accession number of the submission to migrate.
* **s:** BioStudies environment to take the submission from.
* **su:** BioStudies user in the source environment. (Only superusers can perform this operation)
* **sp:** Password for the BioStudies user in the source environment.
* **t:** BioStudies environment to migrate the submission to.
* **tu:** BioStudies user in the target environment. (Only superusers can perform this operation)
* **tp:** Password for the BioStudies user in the target environment.
