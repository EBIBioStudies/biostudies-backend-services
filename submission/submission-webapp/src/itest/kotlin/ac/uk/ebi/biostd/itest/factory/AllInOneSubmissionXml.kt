package ac.uk.ebi.biostd.itest.factory

import ebi.ac.uk.io.ext.createNewFile
import java.io.File
import org.redundent.kotlin.xml.xml

fun submissionSpecXml(tempFolder: File, accNo: String): SubmissionSpec = SubmissionSpec(
    submission = tempFolder.createNewFile("submission.xml", allInOneSubmissionXml(accNo).toString()),
    fileList = tempFolder.createNewFile("file-list.xml", fileList().toString()),
    files = submissionsFiles(tempFolder),
    subFileList = SubmissionFile(tempFolder.createNewFile("file-list2.xml", fileList2().toString()), "sub-folder")
)

fun allInOneSubmissionXml(accNo: String) = xml("submission") {
    attribute("accno", accNo)
    "attributes" {
        "attribute" {
            "name" { -"Title" }
            "value" { -"venous blood, ∆Monocyte" }
        }
        "attribute" {
            "name" { -"ReleaseDate" }
            "value" { -"2021-02-12" }
        }
    }

    "section" {
        attribute("accno", "SECT-001")
        attribute("type", "Study")
        "attributes" {
            "attribute" {
                "name" { -"Project" }
                "value" { -"CEEHRC (McGill)" }
            }
            "attribute" {
                attribute("reference", true)
                "name" { -"Organization" }
                "value" { -"Org1" }
            }
            "attribute" {
                "name" { -"Tissue type" }
                "value" { -"venous blood" }
                "valqual" {
                    "name" { -"Ontology" }
                    "value" { -"UBERON" }
                }
                "nmqual" {
                    "name" { -"Tissue" }
                    "value" { -"Blood" }
                }
            }
            "attribute" {
                "name" { -"File List" }
                "value" { -"file-list.xml" }
            }
        }
        "links" {
            "link" {
                "url" { -"AF069309" }
                "attributes" {
                    "attribute" {
                        "name" { -"type" }
                        "value" { -"gen" }
                    }
                }
            }
        }
        "files" {
            "file" {
                "path" { -"DataFile1.txt" }
                "attributes" {
                    "attribute" {
                        "name" { -"Description" }
                        "value" { -"Data File 1" }
                    }
                }
            }
            "table" {
                "file" {
                    "path" { -"DataFile2.txt" }
                    "attributes" {
                        "attribute" {
                            "name" { -"Description" }
                            "value" { -"Data File 2" }
                        }
                        "attribute" {
                            "name" { -"Type" }
                            "value" { -"Data" }
                        }
                    }
                }
                "file" {
                    "path" { -"Folder1/DataFile3.txt" }
                    "attributes" {
                        "attribute" {
                            "name" { -"Description" }
                            "value" { -"Data File 3" }
                        }
                        "attribute" {
                            "name" { -"Type" }
                            "value" { -"Data" }
                        }
                    }
                }
                "file" {
                    "path" { -"Folder1/Folder2/DataFile4.txt" }
                    "attributes" {
                        "attribute" {
                            "name" { -"Description" }
                            "value" { -"Data File 4" }
                        }
                        "attribute" {
                            "name" { -"Type" }
                            "value" { -"Data" }
                        }
                    }
                }
            }
        }
        "subsections" {
            "section" {
                attribute("accno", "SUBSECT-001")
                attribute("type", "Stranded Total RNA-Seq")
                "links" {
                    "table" {
                        "link" {
                            "url" { -"EGAD00001001282" }
                            "attributes" {
                                "attribute" {
                                    "name" { -"Type" }
                                    "value" { -"EGA" }
                                }
                                "attribute" {
                                    "name" { -"Assay type" }
                                    "value" { -"RNA-Seq" }
                                    "valqual" {
                                        "name" { -"Ontology" }
                                        "value" { -"EFO" }
                                    }
                                    "nmqual" {
                                        "name" { -"TermId" }
                                        "value" { -"EFO_0002768" }
                                    }
                                }
                            }
                        }
                    }
                }
                "attributes" {
                    "attribute" {
                        "name" { -"File List" }
                        "value" { -"sub-folder/file-list2.xml" }
                    }
                }
            }
            "table" {
                "section" {
                    attribute("accno", "DT-1")
                    attribute("type", "Data")
                    "attributes" {
                        "attribute" {
                            "name" { -"Title" }
                            "value" { -"Group 1 Transcription Data" }
                        }
                        "attribute" {
                            "name" { -"Description" }
                            "value" { -"The data for zygotic transcription in mammals group 1" }
                        }
                    }
                }
            }
        }
    }
}

private fun fileList() = xml("table") {
    "file" {
        "path" { -"DataFile5.txt" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced" }
            }
        }
    }
    "file" {
        "path" { -"Folder1/DataFile6.txt" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced" }
            }
        }
    }
}

private fun fileList2() = xml("table") {
    "file" {
        "path" { -"DataFile7.txt" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced" }
            }
        }
    }
    "file" {
        "path" { -"Folder1/DataFile8.txt" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced" }
            }
        }
    }
}
