package ac.uk.ebi.biostd.itest.factory

import org.redundent.kotlin.xml.xml

fun allInOneSubmissionXml() = xml("submission") {
    attribute("accno", "S-EPMC126")
    "attributes" {
        "attribute" {
            "name" { -"Title" }
            "value" { -"venous blood, Monocyte" }
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
                "name" { -"Tissue Type" }
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
        }
        "links" {
            "link" {
                "url" { -"AF069309" }
                "attributes" {
                    "attribute" {
                        "name" { -"Type" }
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
                                }
                            }
                        }
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
