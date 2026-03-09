package nesski.de.ics

import org.slf4j.LoggerFactory
import java.io.File

private val log = LoggerFactory.getLogger("IcsFileWriter")

/**
 * Writes ICS content to disk files with directory auto-creation.
 */
object IcsFileWriter {

    /**
     * Write .ics content to a file at the given save path.
     * Auto-creates directories if they don't exist.
     *
     * @param savePath The directory to save the file in (~ is expanded to user home)
     * @param filename The filename (e.g. "workouts_2w_2026-03-08.ics")
     * @param icsContent The RFC 5545 VCALENDAR string to write
     * @return The absolute path of the written file
     */
    fun write(savePath: String, filename: String, icsContent: String): String {
        val resolvedPath = savePath.replaceFirst("~", System.getProperty("user.home"))
        val dir = File(resolvedPath)
        if (!dir.exists()) {
            dir.mkdirs()
            log.info("Created directory: ${dir.absolutePath}")
        }
        val file = File(dir, filename)
        file.writeText(icsContent, Charsets.UTF_8)
        log.info("ICS file written: ${file.absolutePath} (${icsContent.length} bytes)")
        return file.absolutePath
    }
}
