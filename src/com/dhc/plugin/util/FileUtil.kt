package com.dhc.plugin.util

import java.io.*

class FileUtil {


    public fun readFile(filename: String): String {
        var `in`: InputStream? = null
        `in` = this.javaClass.getResourceAsStream("../code/$filename")
        var content = ""
        try {
            content = String(readStream(`in`!!))
        } catch (e: Exception) {
        }

        return content
    }

    public fun writetoFile(content: String, filepath: String, filename: String) {
        try {
            val floder = File(filepath)
            // if file doesnt exists, then create it
            if (!floder.exists()) {
                floder.mkdirs()
            }
            val file = File("$filepath/$filename")
            if (!file.exists()) {
                file.createNewFile()
            }

            val fw = FileWriter(file.absoluteFile)
            val bw = BufferedWriter(fw)
            bw.write(content)
            bw.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    public fun readStream(inStream: InputStream): ByteArray {
        val outSteam = ByteArrayOutputStream()
        try {
            val buffer = ByteArray(1024)
            var read: Int = -1
            `inStream`.use { input ->
                outSteam.use {
                    while (input.read(buffer).also { read = it } != -1) {
                        it.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: IOException) {
        } finally {
            outSteam.close()
            inStream.close()
        }
        return outSteam.toByteArray()
    }
}