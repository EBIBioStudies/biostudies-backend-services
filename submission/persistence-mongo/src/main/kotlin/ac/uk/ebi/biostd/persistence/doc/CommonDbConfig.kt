package ac.uk.ebi.biostd.persistence.doc

import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocAttributeConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileListConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileListDocFileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocLinkConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocLinkListConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocLinkTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocSectionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocSubmissionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocSubmissionFileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.AttributeConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileListConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileListDocFileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.LinkConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.LinkListConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.LinkTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.SectionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.SubmissionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.SubmissionFileConverter
import org.springframework.core.convert.converter.Converter

fun converters(): MutableList<Converter<*, *>> {
    val converters = mutableListOf<Converter<*, *>>()
    converters.add(docSubmissionConverter())
    converters.add(submissionConverter())
    converters.add(FileConverter(AttributeConverter()))
    converters.add(DocFileConverter(DocAttributeConverter()))
    converters.add(FileListDocFileConverter(FileConverter(AttributeConverter())))
    converters.add(SubmissionFileConverter(FileConverter(AttributeConverter())))
    converters.add(DocFileListDocFileConverter(DocFileConverter(DocAttributeConverter())))
    converters.add(DocSubmissionFileConverter(DocFileConverter(DocAttributeConverter())))
    return converters
}

private fun docSubmissionConverter(): DocSubmissionConverter {
    val docAttributeConverter = DocAttributeConverter()
    val docFileConverter = DocFileConverter(docAttributeConverter)
    val docFileListConverter = DocFileListConverter(docFileConverter)
    val docLinkListConverter = DocLinkListConverter(docFileConverter)
    val docFileTableConverter = DocFileTableConverter(docFileConverter)
    val docLinkConverter = DocLinkConverter(docAttributeConverter)
    val docLinksTableConverter = DocLinkTableConverter(docLinkConverter)
    val docSectionConverter =
        DocSectionConverter(
            docAttributeConverter,
            docLinkConverter,
            docLinksTableConverter,
            docFileConverter,
            docFileTableConverter,
            docFileListConverter,
            docLinkListConverter,
        )
    return DocSubmissionConverter(docFileConverter, docSectionConverter, docAttributeConverter)
}

private fun submissionConverter(): SubmissionConverter {
    val attributeConverter = AttributeConverter()
    val fileConverter = FileConverter(attributeConverter)
    val fileListConverter = FileListConverter(fileConverter)
    val linkListConverter = LinkListConverter(fileConverter)
    val fileTableConverter = FileTableConverter(fileConverter)
    val linkConverter = LinkConverter(attributeConverter)
    val linksTableConverter = LinkTableConverter(linkConverter)
    val sectionConverter =
        SectionConverter(
            attributeConverter,
            linkConverter,
            linksTableConverter,
            fileConverter,
            fileTableConverter,
            fileListConverter,
            linkListConverter,
        )
    return SubmissionConverter(sectionConverter, attributeConverter, fileConverter)
}
