package com.dhc.plugin.util

import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import java.io.FileOutputStream
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

 fun getNodeOrCreate(doc: Document, node: String): Node {
    val applicationList = doc.getElementsByTagName(node)
    var application = applicationList.item(0)
    if ( application==null)
        application = doc.createElement(node)
    return application
}

 fun isHave(doc: Document, node: String):  Boolean{
    return doc.getElementsByTagName(node).length>0
}
 fun saveXml(file: File, doc: Document) {
    val transFactory = TransformerFactory.newInstance()
    try {
        val transformer = transFactory.newTransformer()
        transformer.setOutputProperty("indent", "yes")
        val source = DOMSource()
        source.node = doc
        val result = StreamResult()
        result.outputStream = FileOutputStream(file)
        transformer.transform(source, result)
    } catch (e: Exception) {
        e.printStackTrace()
    }finally {
    }

}