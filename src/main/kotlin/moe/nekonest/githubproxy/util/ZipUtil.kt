package moe.nekonest.githubproxy.util

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipUtil {
    fun compress(path: String): String{
        val file = File(path)
        if (!file.exists()){
            throw FileNotFoundException()
        }
        val fileName = file.name
        val archiveDir = File(ARCHIVE_DIR)
        if (!archiveDir.exists()){
            archiveDir.mkdir()
        }
        val obj = File(ARCHIVE_DIR, "$fileName.zip")
        if (obj.exists()){
            obj.delete()
        }
        obj.createNewFile()
        val zout = ZipOutputStream(FileOutputStream(obj))
        val bout = zout.buffered()
        compress(file,null,zout,bout)
        bout.flush()
        bout.close()
        zout.close()               // 一定要记得关闭流！！！！！
        return obj.name
    }

    private fun compress(
            file: File,
            parentName: String?,
            zout: ZipOutputStream,
            bout: BufferedOutputStream){
        if (file.isDirectory){
            if (parentName == null){
                val children = file.listFiles() ?:return
                children.forEach {
                    compress(it,file.name,zout,bout)
                }
            }else{
                val children = file.listFiles() ?:return
                children.forEach {
                    compress(it,"$parentName/${file.name}",zout,bout)
                }
            }
        }else{
            if (parentName == null){
                doCompress(file,file.name,zout, bout)
            }else{
                doCompress(file,"$parentName/${file.name}",zout, bout)
            }
        }
    }

    private fun doCompress(
            file: File,
            entryName: String,
            zout: ZipOutputStream,
            bout: BufferedOutputStream
    ){
        val entry = ZipEntry(entryName)
        zout.putNextEntry(entry)
        val input = FileInputStream(file).buffered()
        var i = input.read()
        while (i != -1){
            bout.write(i)
            i = input.read()
        }
        input.close()
        bout.flush()
        zout.closeEntry()
    }
}