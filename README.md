# BioStudies Backend Services

Project containing the BioStudies backend services. Below you'll find a description of each main component in the
project:

* **Bio Admin**:
Spring boot administration module.

* **CI**:
It contains the definitions of the CI/CD GitLab jobs as well as the required scripts.

* **Client**:
Contains the web client used to interact with the submitter and the code for the BioStudies CLI (PTSubmit)

* **Commons**:
Contains common functionality used in all modules like the model definition, serialization, utils, etc. 

* **Infrastructure**:
Contains infrastructure related tooling to help deploying the application

* **Scheduler**:
Module that contains scheduled task to perform BioStudies related processes like submissions export to the UI or
Europe-PMC submissions processing.

* **Submission**:
Contains all the services that are specifically related to the submissions.


## Deploying The Submitter WebApp
This section will explain how to deploy a local instance of the submitter webapp from scratch. You'll need to have:
* Gradle
* Java (at least 8 version)
* Docker

#### Database Setup
1. Execute the command `gradle setUpTestDatabase` in the [infrastructure](infrastructure) folder. This will deploy a
custom MySql image loaded with the application's schema and some initial test data.
2. Execute the command `docker ps`. You should see the _biostudies-mysql_ image up and running.

#### Configure WebApp
1. Move to [Submission WebApp](submission/submission-webapp) folder
2. Edit the [Submission WebApp Config File](submission/submission-webapp/src/main/resources/application.yml) to match
the [Local Submission WebApp Config File](submission/submission-webapp/src/main/resources/application-local.yml)
3. Choose a folder in your file system to be used as the application storage. It's recommended that this is an empty
folder for fresh instances. Set this folder path in the **basepath** property of the configuration file
4. Execute the command `gradle bootRun`

#### Make A Submission
In order to make a submission, you can use the BioStudies CLI. Find more information
[here](client/bio-commandline/README.md).

## Development Process
In this section there're useful documents related to the development process
- [Coding Conventions](/docs/Coding_Conventions.md)
