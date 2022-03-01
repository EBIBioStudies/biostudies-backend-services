# BioStudies Backend Services

Project containing the BioStudies backend services. Below you'll find a description of each main component in the
project:

* **Bio Admin**:
Spring boot administration module.

* **CI**:
It contains the definitions of the CI/CD GitLab jobs as well as the required scripts.

* **Client**:
Contains the web client used to interact with the submitter and the code for the BioStudies CLI

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
Execute the command `gradle setUpTestDatabase` in the [infrastructure](infrastructure) folder. This will deploy a
custom MySql image loaded with the application's schema and some initial test data and the following configuration:
* Container name: biostudies-mysql
* User: root
* Password: admin
* Port: 3306


#### RabbitMQ Setup
Execute the command `gradle setUpRabbitMQ` in the [infrastructure](infrastructure) folder. This will deploy a
RabbitMQ image with the following configuration:
* Container name: biostudies-rabbitmq
* User: manager
* Password: manager-local
* Port: 5672
* Admin console: http://localhost:15672

#### FireMock Setup (EBI only)

* Generate a GitLab access token [here](https://gitlab.ebi.ac.uk/-/profile/personal_access_tokens) with at least `read_registry` scope
* Execute `docker login dockerhub.ebi.ac.uk` and enter your GitLab username when prompted for username, and the access token when prompted for a password
* Execute `gradle setUpFireMock` in the [infrastructure](infrastructure) folder

The steps above will deploy a FIREMock image listening on localhost, with the following configurations

* Container name: biostudies-firemock
* User: user
* Password: pass
* Port: 8092

#### Configure WebApp
1. Move to [Submission WebApp](submission/submission-webapp) folder
2. Edit the [Submission WebApp Config File](submission/submission-webapp/src/main/resources/application.yml) to match
the [Local Submission WebApp Config File](submission/submission-webapp/src/main/resources/application-local.yml)
3. Choose a folder in your file system to be used as the application storage. It's recommended that this is an empty
folder for fresh instances. Set this folder path in the **basepath** property of the configuration file
4. Execute the command `gradle bootRun`

>Note: In order for the messages in the rabbit queues to be processed you need to start the Handlers application

#### Make A Submission
In order to make a submission, you can use the BioStudies CLI. Find more information
[here](client/bio-commandline/README.md).

#### Page Tab
Page tab is the specification used to define studies in BioStudies. In order to get a better understanding you can refer
to:
* [Page Tab Specification](https://ebibiostudies.github.io/page-tab-specification)
* [All In One Example](https://ebibiostudies.github.io/page-tab-specification/examples/AllInOneExample.html)
* [File List Example](https://ebibiostudies.github.io/page-tab-specification/examples/FileListExample.html)

## Development Process
In this section there're useful documents related to the development process
- [Coding Conventions](/docs/Coding_Conventions.md)
