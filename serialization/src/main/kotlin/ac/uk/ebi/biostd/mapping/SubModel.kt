package ac.uk.ebi.biostd.mapping

import ebi.ac.uk.model.IAttribute
import ebi.ac.uk.model.IFile
import ebi.ac.uk.model.ILink
import ebi.ac.uk.model.ISimpleAttribute
import ebi.ac.uk.model.NO_TABLE_INDEX

class SerializationFile(
        override var name: String,
        override var size: Int,
        override var order: Int,
        override var attributes: MutableList<IAttribute>,
        override var tableIndex: Int) : IFile

class SerializationLink(
        override var url: String,
        override var attributes: MutableList<IAttribute>,
        override var order: Int,
        override var tableIndex: Int = NO_TABLE_INDEX) : ILink


class SerializationAttribute(
        override var name: String,
        override var value: String,
        override var reference: Boolean,
        override var nameAttributes: MutableList<ISimpleAttribute>,
        override var valueAttributes: MutableList<ISimpleAttribute>) : IAttribute

class SerializationSimpleAttribute(
        override var name: String,
        override var value: String) : ISimpleAttribute