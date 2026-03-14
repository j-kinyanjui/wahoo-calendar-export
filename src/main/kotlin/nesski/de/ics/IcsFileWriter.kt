package nesski.de.ics

import java.io.File
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("IcsFileWriter")

/** Writes ICS content to disk files with directory auto-creation. */
object IcsFileWriter {

    /**
     * Write .ics content to a file at the given save path. Auto-creates directories if they don't
     * exist.
     *
     * @param file The file to save the file in
     * @param icsContent The RFC 5545 VCALENDAR string to write
     * @return The absolute path of the written file
     */
    fun write(file: File, icsContent: String): String {
        file.writeText(icsContent, Charsets.UTF_8)
        log.info("ICS file written: ${file.absolutePath} (${icsContent.length} bytes)")
        return file.absolutePath
    }
}
