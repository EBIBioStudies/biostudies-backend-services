## Permissions Test Suite

### Delete Permission Test Suite

Contains test related to submission deletion.

| Class                | Test No | Test name                                                                 |
|----------------------|---------|---------------------------------------------------------------------------|
| DeletePermissionTest | 1-1     | Superuser deletes private submission                                      |
| DeletePermissionTest | 1-2     | Superuser deletes public submission                                       |
| DeletePermissionTest | 1-3     | Superuser delete own private submissions                                  |
| DeletePermissionTest | 1-4     | Superuser resubmit deleted submission                                     |
| DeletePermissionTest | 1-5     | Regular user deletes private submission                                   |
| DeletePermissionTest | 1-6     | Regular user deletes public submission                                    |
| DeletePermissionTest | 1-7     | Regular user deletes their own public submission                          |
| DeletePermissionTest | 1-8     | Regular user deletes their own private submission                         |
| DeletePermissionTest | 1-9     | Regular user deletes only their own public subsmissions                   |
| DeletePermissionTest | 1-10    | Regular user with DELETE access tag permission deletes private submission |
| DeletePermissionTest | 1-11    | Regular user with DELETE access tag permission deletes public submission  |
| DeletePermissionTest | 1-12    | Regular user with ADMIN access tag deletes private submission             |
| DeletePermissionTest | 1-13    | Regular user with ADMIN access tag deletes public submission              |

### Delete Files Test Suite

Contains test related to submission file deletion.

| Class                     | Test No | Test name                                                                                   |
|---------------------------|---------|---------------------------------------------------------------------------------------------|
| DeleteFilesPermissionTest | 1-14    | Regular user deletes their own public submission files                                      |
| DeleteFilesPermissionTest | 1-15    | Regular user with DELETE_FILES permission delete their own public submission files          |
| DeleteFilesPermissionTest | 1-16    | Regular user deletes their own public submission filelist files                             |
| DeleteFilesPermissionTest | 1-16    | Regular user deletes their own public submission filelist files                             |
| DeleteFilesPermissionTest | 1-17    | Collection ADMIN user deletes public submission files                                       |
| DeleteFilesPermissionTest | 1-18    | Regular user deletes their own public submission files when preventFileDeletion is disabled |

### Submit Permission Test Suite

Contains test related to Submission and collection manipulation.

| Class                | Test No | Test name                                                                       |
|----------------------|---------|---------------------------------------------------------------------------------|
| SubmitPermissionTest | 4-1     | Superuser creates a collection                                                  |
| SubmitPermissionTest | 4-2     | Regular user can not create a collection                                        |
| SubmitPermissionTest | 4-3     | Regular user submits a collection submision without attach permission           |
| SubmitPermissionTest | 4-4     | Regular user submits a collection submision attach permission                   |
| SubmitPermissionTest | 4-5     | Regular user register and submits to default project                            |
| SubmitPermissionTest | 4-6     | Regular user submits with collection admin permission                           |
| SubmitPermissionTest | 4-7     | Regular user resubmits another user submission with collection admin permission |
| SubmitPermissionTest | 4-8     | Regular user resubmits its own submission                                       |
| SubmitPermissionTest | 4-9     | Regular user resubmits another user submission                                  |
| SubmitPermissionTest | 4-10    | Regular user resubmits another user submission with UPDATE permission           |

### Permission API Test Suite

Contains permissions API test.

| Class             | Test No | Test name                                          |
|-------------------|---------|----------------------------------------------------|
| PermissionApiTest | 21-1    | Grant permission to a Regular user by Superuser    |
| PermissionApiTest | 21-2    | Grant permission to a Regular user by Regular user |
| PermissionApiTest | 21-3    | Grant permission to non-existing user              |
| PermissionApiTest | 21-4    | Grant permission to non-existing submission        |

## File List Test Suite

Contains test related to file list.

| Class                  | Test No | Test name                                                         |
|------------------------|---------|-------------------------------------------------------------------|
| FileListSubmissionTest | 3-1     | JSON submission with TSV file list                                |
| FileListSubmissionTest | 3-2     | JSON submission with XLS file list                                |
| FileListSubmissionTest | 3-3     | JSON submission with invalid file list format                     |
| FileListSubmissionTest | 3-4     | Filelist Submission with files inside a folder                    |
| FileListSubmissionTest | 3-5     | Filelist Submission with files reusing previous version file list |
| FileListSubmissionTest | 3-6     | Filelist Submission with an empty file list                       |
| FileListSubmissionTest | 3-7     | Filelist Submission with a file list with an empty attribute name |
| FileListSubmissionTest | 3-8     | Filelist Submission with empty accNo                              |
| FileListValidationTest | 11-1    | Filelist validation when blank file list                          |
| FileListValidationTest | 11-2    | Filelist validation when empty file list                          |
| FileListValidationTest | 11-3    | Filelist validation when unsupported file list format             |
| FileListValidationTest | 11-4    | Filelist when missing files                                       |
| FileListValidationTest | 11-5    | Filelist validation when valid filelist                           |
| FileListValidationTest | 11-6    | Filelist validation when valid filelist with root path            |
| FileListValidationTest | 11-7    | Filelist validation when valid filelist on behalf another user    |

## Submission/Resubmission Test Suite

### Submission Test suite

Contains test related to submission

| Class                  | Test No | Test name                                                                 |
|------------------------|---------|---------------------------------------------------------------------------|
| SubmissionApiTest      | 16-1    | Submit study with submission object                                       |
| SubmissionApiTest      | 16-2    | Submit study with empty accNo                                             |
| SubmissionApiTest      | 16-3    | Submit study using root path                                              |
| SubmissionApiTest      | 16-4    | Submit study with generic root section                                    |
| SubmissionApiTest      | 16-5    | Submit study with invalid link Url                                        |
| SubmissionApiTest      | 16-6    | Submit study with validation error                                        |
| SubmissionApiTest      | 16-7    | Submit public study with folder make files public                         |
| SubmissionApiTest      | 16-8    | Submit public study with file make files public                           |
| SubmissionApiTest      | 16-9    | Submit study not released makes files private                             |
| SubmissionApiTest      | 16-10   | Submit study with invalid characters file path                            |
| SubmissionApiTest      | 16-10-1 | Submit study with invalid characters file path in file list               |
| SubmissionApiTest      | 16-11   | Submit study containing folder with trailing slash                        |
| SubmissionApiTest      | 16-12   | Submit study containing filelist with invalid name                        |
| SubmissionApiTest      | 16-13   | Submit study by Regular user with Ftp home directory                      |
| SubmissionApiTest      | 16-14   | Submit study when the system has the basePath property configured         |
| SubmissionApiTest      | 16-15   | Submit study publish SubmissionSubmitted message                          |
| SubmissionApiTest      | 16-16   | Submit study with silentMode does not publish SubmissionSubmitted message |
| SubmissionApiTest      | 16-17   | Submit study with singleJobMode                                           |
| AllInOneSubmissionTest | 2-1     | Submit all in one TSV study                                               |
| AllInOneSubmissionTest | 2-2     | Submit all in one JSON study                                              |

### Resubmission Test suite

Contains test related to resubmission

| Class               | Test No | Test name                                      |
|---------------------|---------|------------------------------------------------|
| ResubmissionApiTest | 5-1     | Resubmit study updating a file content         |
| ResubmissionApiTest | 5-2     | Resubmit study with the same files             |
| ResubmissionApiTest | 5-3     | Resubmit study with rootPath                   |
| ResubmissionApiTest | 5-4     | Resubmit study updating only metadata          |
| ResubmissionApiTest | 5-5     | Resubmit study adding new files                |
| ResubmissionApiTest | 5-6     | Resubmit study currenlty being flag as invalid |

### Stats Test suite

| Class               | Test No | Test name                                       |
|---------------------|---------|-------------------------------------------------|
| SubmissionStatsTest | 26-1    | files size stat calculation on submit over FIRE | 
| SubmissionStatsTest | 26-2    | files size stat calculation on submit over NFS  | 
| SubmissionStatsTest | 26-3    | find stats by accNo                             |
| SubmissionStatsTest | 26-4    | find stats by type                              |
| SubmissionStatsTest | 26-5    | find stats by type and AccNo                    |
| SubmissionStatsTest | 26-6    | register stats by file                          |
| SubmissionStatsTest | 26-7    | increment stats by file                         |
| SubmissionStatsTest | 26-8    | refresh submission stats                        |
| SubmissionStatsTest | 26-9    | refresh all submissions stats                   |

### Async submission Test suite

| Class               | Test No | Test name                                   |
|---------------------|---------|---------------------------------------------|
| SubmissionAsyncTest | 19-1    | Simple submit async                         |
| SubmissionAsyncTest | 19-2    | Check submission stages                     |
| SubmissionAsyncTest | 19-3    | Multiple async submissions with files       |
| SubmissionAsyncTest | 19-4    | Multiple async submissions when one invalid |

### File operations Test suite

| Class           | Test No | Test name                                                          |
|-----------------|---------|--------------------------------------------------------------------|
| UserFileApiTest | 17-1    | upload download delete file and retrieve in user root folder       | 
| UserFileApiTest | 17-2    | upload download delete file and retrieve in user folder            |
| UserFileApiTest | 17-3    | upload download delete file and retrieve in user folder with space |
| UserFileApiTest | 17-4    | download a binary file                                             |
| UserFileApiTest | 17-5    | download a text file                                               |

| Class                           | Test No | Test name                                                                        | Description                                                                                   |
|---------------------------------|---------|----------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------
| SubmissionFileSourceTest        | 6-1     | resubmission with SUBMISSION file source as priority over USER_SPACE             | Considers submission with different source files, user space, fire, bypassing files with fire |
| SubmissionFileSourceTest        | 6-3-1   | submission with directory with files on FIRE                                     |                                                                                               |
| SubmissionFileSourceTest        | 6-3-2-1 | re-submission with directory with files on FIRE                                  |                                                                                               |
| SubmissionFileSourceTest        | 6-3-2-1 | re-submission with directory with files on FIRE using submission source only     |                                                                                               |
| SubmissionFileSourceTest        | 6-3-3   | re-submission on FIRE, User folder should be prioritized                         |                                                                                               |
| SubmissionFileSourceTest        | 6-3-4   | submission with directories with the same name on FIRE                           |                                                                                               |
| SubmissionFileSourceTest        | 6-3-5   | submission with directory with files on NFS                                      |                                                                                               |
| SubmissionFileSourceTest        | 6-3-6   | submission with directories with the same name on NFS                            |                                                                                               |
| SubmissionFileSourceTest        | 6-4     | multiple file references                                                         |                                                                                               |
| SubmissionFileSourceTest        | 6-5     | submission with group file                                                       |                                                                                               |
| SubmissionFileSourceTest        | 6-6     | Submission bypassing fire                                                        |                                                                                               |
| SubmissionFileSourceTest        | 6-7     | resubmission with SUBMISSION source ONLY                                         |                                                                                               |
| SubmissionFileSourceTest        | 6-8     | submission with files with the same md5 and different path                       |                                                                                               |
| ExtCollectionSubmitTest         | 7-1     | submit private project                                                           | Creates public and private collection project                                                 |
| ExtCollectionSubmitTest         | 7-2     | submit public project                                                            |                                                                                               |
| ExtCollectionSubmitTest         | 7-3     | submit duplicated accNo template                                                 |                                                                                               |
| SubmissionToCollectionsTest     | 8-1     | accNo generation from collection template                                        | Considers different test cases on submission attached to project                              |
| SubmissionToCollectionsTest     | 8-2     | direct submission overriding collection                                          |                                                                                               |
| SubmissionToCollectionsTest     | 8-3     | no release date to private collection                                            |                                                                                               |
| SubmissionToCollectionsTest     | 8-4     | public submission to private collection                                          |                                                                                               |
| SubmissionToCollectionsTest     | 8-5     | private submission to public collection                                          |                                                                                               |
| SubmissionToCollectionsTest     | 8-6     | no release date to public collection                                             |                                                                                               |
| SubmissionToCollectionsTest     | 8-7     | submit to collection with validator                                              |                                                                                               |
| SubmissionToCollectionsTest     | 8-8     | submit to collection with failling validator                                     |                                                                                               |
| SubmissionToCollectionsTest     | 8-9     | admin user provides accNo                                                        |                                                                                               |
| SubmissionToCollectionsTest     | 8-10    | regular user provides accNo                                                      |                                                                                               |
| MultipartFileSubmissionApiTest  | 9-1     | XLS submission                                                                   | Submits submissions and files at the same time                                                |
| MultipartFileSubmissionApiTest  | 9-2     | TSV submission                                                                   |                                                                                               |
| MultipartFileSubmissionApiTest  | 9-3     | JSON submission                                                                  |                                                                                               |
| MultipartFileSubmissionApiTest  | 9-4     | direct submission with overriden attributes                                      |                                                                                               |
| MultipartFileSubmissionApiTest  | 9-5     | invalid format file                                                              |                                                                                               |
| SubmissionStorageModeTest       | 10-1    | Fire to Nfs                                                                      | Submit in Fire storage mode, resubmit in Nfs mode, and vice versa.                            |
| SubmissionStorageModeTest       | 10-2    | Nfs to Fire                                                                      |                                                                                               |
| SubmissionStorageModeTest       | 10-3    | transfer from NFS to FIRE                                                        |                                                                                               |
| SubmissionStorageModeTest       | 10-4    | transfer from FIRE to NFS                                                        |                                                                                               |
| SubmissionStorageModeTest       | 10-5    | previous version keeps storage mode                                              |                                                                                               |
| SubmissionDraftApiTest          | 12-1    | get draft submission when draft does not exist but submission does               | How the system behaves with respect on drafts                                                 |
| SubmissionDraftApiTest          | 12-2    | create and get submission draft                                                  |                                                                                               |
| SubmissionDraftApiTest          | 12-3    | create and update submission draft                                               |                                                                                               |
| SubmissionDraftApiTest          | 12-4    | delete submission draft after submission                                         |                                                                                               |
| SubmissionDraftApiTest          | 12-5    | get draft submission when neither draft nor submission exists                    |                                                                                               |
| SubmissionDraftApiTest          | 12-6    | delete a draft directly                                                          |                                                                                               |
| SubmissionDraftApiTest          | 12-7    | re submit from draft                                                             |                                                                                               |
| SubmissionDraftApiTest          | 12-8    | update a submission already submitted draft                                      |                                                                                               |
| SubmissionDraftApiTest          | 12-9    | submit json when a draft already exists                                          |                                                                                               |
| SubmissionDraftApiTest          | 12-10   | update a draft with an processing request                                        |                                                                                               |
| SubmissionListApiTest           | 13-1    | get submission list                                                              | Get all submissions that satisfies filters                                                    |
| SubmissionListApiTest           | 13-2    | get submission list by accession                                                 |                                                                                               |
| SubmissionListApiTest           | 13-3    | get direct submission list by accession                                          |                                                                                               |
| SubmissionListApiTest           | 13-4    | get submission list by keywords                                                  |                                                                                               |
| SubmissionListApiTest           | 13-5    | get submission list by release date                                              |                                                                                               |
| SubmissionListApiTest           | 13-6    | get submission list pagination                                                   |                                                                                               |
| SubmissionListApiTest           | 13-7    | get submissions with submission title                                            |                                                                                               |
| SubmissionListApiTest           | 13-8    | get submissions with section title                                               |                                                                                               |
| SubmissionListApiTest           | 13-9    | submission with spaces                                                           |                                                                                               |
| SubmissionListApiTest           | 13-10   | latest updated submission should appear first                                    |                                                                                               |
| SubmissionListSubmittedTest     | 13-11   | list submission request in SUBMITTED stage                                       |                                                                                               |
| SubmissionOnBehalfTest          | 14-1    | submission on behalf another user                                                | Performs differents submissions on behalf other user                                          |
| SubmissionOnBehalfTest          | 14-2    | submission on behalf new user                                                    |                                                                                               |
| SubmissionOnBehalfTest          | 14-3    | submission on behalf created user with files in his folder                       |                                                                                               |
| SubmissionOnBehalfTest          | 14-4    | submission on behalf when owner and submitter has the same file                  |                                                                                               |
| SubmissionOnBehalfTest          | 14- 5   | On behalf with manager with another user submission update user Owner            |                                                                                               |
| SpecialSubmissionAttributesTest | 15-2    | submission with tags                                                             |                                                                                               |
| SpecialSubmissionAttributesTest | 15-3    | new submission with sections table without elements                              |                                                                                               |
| SpecialSubmissionAttributesTest | 15-4    | new submission with empty-null attributes                                        |                                                                                               |
| SpecialSubmissionAttributesTest | 15-5    | new submission with empty-null table attributes                                  |                                                                                               |
| SpecialSubmissionAttributesTest | 15-6    | submission with DOI                                                              |                                                                                               |
| SpecialSubmissionAttributesTest | 15-7    | submission with DOI and incomplete name                                          | The DOI is registered without contributors when the author name is incomplete                 |
| SpecialSubmissionAttributesTest | 15-8    | submission with DOI and no name                                                  | The DOI is registered without contributors when the author name is not present                |
| SpecialSubmissionAttributesTest | 15-9    | private submission with double blind review                                      | Any author or organizations are hidden in the generated pagetab                               |
| SpecialSubmissionAttributesTest | 15-10   | private submission with different review type                                    | All the fields are included in the generated pagetab                                          |
| SpecialSubmissionAttributesTest | 15-11   | public submission with double blind review                                       | All the fields are included in the generated pagetab                                          |
| SpecialSubmissionAttributesTest | 15-12   | submission with empty sections table.                                            |                                                                                               |
| GroupFilesApiTest               | 18-1    | upload download delete file and retrieve in user root folder                     | Shows behaviour of filesApi for groups considering different folder names                     |
| GroupFilesApiTest               | 18-2    | upload download delete file and retrieve in user folder                          |                                                                                               |
| CollectionsListTest             | 20-1    | list collections for super user                                                  | Shows which collections users can see                                                         |
| CollectionsListTest             | 20-2    | list collections for regular user                                                |                                                                                               |
| CollectionsListTest             | 20-3    | list collections for default user                                                |                                                                                               |
| CollectionsListTest             | 20-4    | list collections for collection admin user                                       |                                                                                               |
| SecurityApiTest                 | 22-1    | register with invalid email                                                      | Shows registration user behaviour                                                             |
| SecurityApiTest                 | 22-2    | register when activation is not enable                                           |                                                                                               |
| SecurityApiTest                 | 22-3    | login when inactive                                                              |                                                                                               |
| SecurityApiTest                 | 22-4    | case insensitive user registration                                               |                                                                                               |
| SecurityApiTest                 | 22-5    | case insensitive inactive registration                                           |                                                                                               |
| SecurityApiTest                 | 22-6    | check ftp home type user                                                         |                                                                                               |
| SecurityApiTest                 | 22-7    | check Nfs home type user                                                         |                                                                                               |
| SubmissionDraftListApiTest      | 23-1    | get draft by key                                                                 | Shows how to get drafts, paginated or not.                                                    |
| SubmissionDraftListApiTest      | 23-2    | get drafts without pagination                                                    |                                                                                               |
| SubmissionDraftListApiTest      | 23-3    | get drafts with pagination                                                       |                                                                                               |
| UserGroupsApiTest               | 24-1    | get user groups                                                                  | Shows belonging behaviour of users in groups, and exceptions                                  |
| UserGroupsApiTest               | 24-2    | trying to add a user to un-existing group                                        |                                                                                               |
| UserGroupsApiTest               | 24-3    | trying to add a user that does not exist                                         |                                                                                               |
| UserGroupsApiTest               | 24-4    | trying to add a user by regularUser                                              |                                                                                               |
| SubmissionRefreshApiTest        | 25-1    | Refresh when submission title is updated                                         |                                                                                               |
| SubmissionRefreshApiTest        | 25-2    | Refresh when submission release date is updated                                  |                                                                                               |
| SubmissionRefreshApiTest        | 25-3    | Refresh when submission attribute is updated                                     |                                                                                               |
| SubmissionRefreshApiTest        | 25-4    | Refresh when submission fileListFile attribute is updated                        |                                                                                               |
| SubmissionReleaseTest           | 27-1    | public submission without secret key and HARD_LINKS release mode                 |                                                                                               |
| SubmissionReleaseTest           | 27-2    | private submission without secret key and HARD_LINKS release mode                |                                                                                               |
| SubmissionReleaseTest           | 27-3    | public submission with secret key and MOVE release mode                          |                                                                                               |
| SubmissionReleaseTest           | 27-4    | private submission with secret key and MOVE release mode                         |                                                                                               |
| SubmissionReleaseTest           | 27-5    | release already submitted submission using release operation                     |                                                                                               |
| SubmissionDatesTest             | 28-1    | Creation date is not changed beetween re submissions                             |                                                                                               |
| SubmissionDatesTest             | 28-2    | Modification date is changed beetween re submissions                             |                                                                                               |
| SubmissionDatesTest             | 28-3    | Regular user submit with release date in the past                                |                                                                                               |
| SubmissionDatesTest             | 28-4    | Regular user re-submit a public submission with a new release date in the future |                                                                                               |
| SubmissionDatesTest             | 28-5    | Regular user re-submit a private submission with a new release date in the past  |                                                                                               |
| SubmissionDatesTest             | 28-6    | Admin submit and re Submit in the past                                           |                                                                                               |
| SubmissionDatesTest             | 28-7    | Admin make a public submission private                                           |                                                                                               |
| SubmissionDatesTest             | 28-8    | Admin make a Regular user public submission private                              |                                                                                               |
| SubmissionDatesTest             | 28-9    | Collection Admin submit and re Submit in the past                                |                                                                                               |
| SubmissionDatesTest             | 28-10   | Collection Admin make a public submission private                                |                                                                                               |
| SubmissionDatesTest             | 28-11   | Collection Admin make a Regular user public submission private                   |                                                                                               |
| SubmissionRequestApiTest        | 29-1    | Get submission request status                                                    |                                                                                               |
| SubmissionRequestApiTest        | 29-2    | Archive submission request                                                       |                                                                                               |

### Admin Operations Test suite

Contains test related to resubmission

| Class            | Test No | Test name                                                  |
|------------------|---------|------------------------------------------------------------|
| UserAdminApiTest | 30-1    | 30-1 Get ext user                                          |
| UserAdminApiTest | 30-2    | 30-2 Get user home stats                                   |
| UserAdminApiTest | 30-3    | 30-3 Migrate user folder when not empty folder and disable |
| UserAdminApiTest | 30-4    | 30-4 Migrate user folder when not empty folder and enable  |
| UserAdminApiTest | 30-5    | 30-5 Migrate user folder when empty folder                 |
