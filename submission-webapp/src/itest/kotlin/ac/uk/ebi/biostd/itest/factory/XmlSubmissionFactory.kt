package ac.uk.ebi.biostd.itest.factory

import org.redundent.kotlin.xml.xml

fun allInOneSubmissionXml() = xml("submission") {
    attribute("accNo", "S-EPMC126")
    "attributes" {
        "attribute" {
            "name" { -"Title" }
            "value" { -"venous blood, Monocyte" }
        }
    }

    "section" {
        attribute("accNo", "SECT-001")
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
                "namequal" {
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
                "path" { -"LibraryFile1.txt" }
                "attributes" {
                    "attribute" {
                        "name" { -"Description" }
                        "value" { -"Library File 1" }
                    }
                }
            }
            "table" {
                "file" {
                    "path" { -"LibraryFile2.txt" }
                    "attributes" {
                        "attribute" {
                            "name" { -"Description" }
                            "value" { -"Library File 2" }
                        }
                        "attribute" {
                            "name" { -"Type" }
                            "value" { -"Lib" }
                        }
                    }
                }
            }
        }
        "subsections" {
            "section" {
                attribute("accNo", "SUBSECT-001")
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
                    attribute("accNo", "DT-1")
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
