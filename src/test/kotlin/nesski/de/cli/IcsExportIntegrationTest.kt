package nesski.de.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import nesski.de.config.EmailConfig
import nesski.de.ics.IcsFileWriter

class IcsExportIntegrationTest {

    @Test
    fun `EmailConfig defaults have email disabled`() {
        val config = EmailConfig()
        assertFalse(config.enabled)
        assertEquals("", config.smtpHost)
        assertEquals(587, config.smtpPort)
        assertTrue(config.useTls)
        assertEquals("Wahoo SYSTM Workout Plan", config.subject)
    }

    @Test
    fun `EmailConfig can be constructed with all fields`() {
        val config =
            EmailConfig(
                enabled = true,
                smtpHost = "smtp.gmail.com",
                smtpPort = 465,
                smtpUsername = "user@gmail.com",
                smtpPassword = "app-password",
                fromAddress = "user@gmail.com",
                toAddress = "dest@gmail.com",
                subject = "Custom Subject",
                useTls = true,
            )
        assertTrue(config.enabled)
        assertEquals("smtp.gmail.com", config.smtpHost)
        assertEquals(465, config.smtpPort)
        assertEquals("user@gmail.com", config.smtpUsername)
        assertEquals("app-password", config.smtpPassword)
        assertEquals("user@gmail.com", config.fromAddress)
        assertEquals("dest@gmail.com", config.toAddress)
        assertEquals("Custom Subject", config.subject)
        assertTrue(config.useTls)
    }

    @Test
    fun `IcsFileWriter creates file at specified path`() {
        val tempDir = kotlin.io.path.createTempDirectory("wahoo-test").toFile()
        try {
            val icsContent = "BEGIN:VCALENDAR\nVERSION:2.0\nEND:VCALENDAR"
            val filename = "test_export.ics"
            val targetFile = java.io.File(tempDir, filename)

            val savedPath = IcsFileWriter.write(targetFile, icsContent)

            val savedFile = java.io.File(savedPath)
            assertTrue(savedFile.exists(), "ICS file should exist on disk")
            assertEquals(icsContent, savedFile.readText())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `IcsFileWriter auto-creates missing directories`() {
        val tempDir = kotlin.io.path.createTempDirectory("wahoo-test").toFile()
        val nestedPath = java.io.File(tempDir, "nested/sub/dir")
        try {
            val icsContent = "BEGIN:VCALENDAR\nVERSION:2.0\nEND:VCALENDAR"
            val filename = "nested_test.ics"
            nestedPath.mkdirs()
            val targetFile = java.io.File(nestedPath, filename)

            val savedPath = IcsFileWriter.write(targetFile, icsContent)

            val savedFile = java.io.File(savedPath)
            assertTrue(savedFile.exists(), "ICS file should exist in nested directory")
            assertEquals(icsContent, savedFile.readText())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `IcsFileWriter writes UTF-8 content correctly`() {
        val tempDir = kotlin.io.path.createTempDirectory("wahoo-test").toFile()
        try {
            val icsContent = "BEGIN:VCALENDAR\nSUMMARY:\uD83D\uDEB4 Cycling Workout\nEND:VCALENDAR"
            val filename = "emoji_test.ics"
            val targetFile = java.io.File(tempDir, filename)

            val savedPath = IcsFileWriter.write(targetFile, icsContent)

            val savedFile = java.io.File(savedPath)
            assertTrue(savedFile.exists())
            val readContent = savedFile.readText(Charsets.UTF_8)
            assertTrue(readContent.contains("\uD83D\uDEB4"), "Should preserve emoji characters")
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `IcsFileWriter returns absolute path of saved file`() {
        val tempDir = kotlin.io.path.createTempDirectory("wahoo-test").toFile()
        try {
            val icsContent = "BEGIN:VCALENDAR\nVERSION:2.0\nEND:VCALENDAR"
            val filename = "path_test.ics"
            val targetFile = java.io.File(tempDir, filename)

            val savedPath = IcsFileWriter.write(targetFile, icsContent)

            assertTrue(savedPath.endsWith(filename), "Should end with the filename")
            assertTrue(java.io.File(savedPath).isAbsolute, "Should be absolute path")
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
