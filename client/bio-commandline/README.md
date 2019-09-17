# BioStudies Command Line (PT Submit)

The BioStudies command line is a tool that allows to interact with the BioStudies web application in order to perform
operations over submissions. 

## Build
In order to build the CLI, execute the command `gradle clean build shadowJar`. This will generate the command line jar
under `build/libs` folder.

## Usage

`java -jar build/libs/PTSubmit-2.0.jar -s <server> -u <user> -p <password> -i <page tab input> -a <attached files>`

>For now, only the submit operation is supported. More operations will be added in the future.

### Arguments
* **s:** BioStudies instance URL.
* **u:** User that will perform the submission.
* **p:** The user password.
* **i:** Path to the file containing the submission page tab.
* **a:** Comma separated list of paths to the files referenced in the submission.
 
**Notes:**

* The parameter to attach files is optional. If no files are attached, the files referenced in the submission page tab
will be retrieved from the user folder.
* If one of the paths provided to the attach argument is a directory, all files inside it will be used as attachments.
* The submission format doesn't need to be specified since it'll be inferred from the input file's extension.
* TSV submissions can be submitted using a XLSX file.
