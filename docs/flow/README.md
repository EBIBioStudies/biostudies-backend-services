Submission Persistence Flow Diagram
==============

The diagram in *persistenceFlow.md* describes the high-level interaction between the backend and our database
and file persistence layers in both NFS and FIRE mode. The interpretation of the solid arrows in the diagram are shown 
in the following table.

| Action                          | NFS                                     | Fire                                                                                                                   |
|---------------------------------|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| Delete file                     | Deletes io file                         | Deletes file via http request DELETE - /objects/$fireId                                                                |
| Unpublish all previous files in | Deletes public files in ftp folder      | (Action not needed in Fire mode)                                                                                       |
| Persist the files in            | Copies io file                          | Saves file via http request POST - /objects, then sets the file path via http request POST - /objects/$fireId/firePath |
| Publish file using              | Creates hard link of file in ftp folder | If file is not published in Fire, Publishes file via http request PUT - /objects/$fireId/publish                       |

Confluence link: https://www.ebi.ac.uk/seqdb/confluence/display/BS/Submission+Persistence+Flow+Diagrams
