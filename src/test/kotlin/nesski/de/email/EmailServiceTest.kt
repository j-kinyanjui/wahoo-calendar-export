package nesski.de.email

import nesski.de.config.EmailConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EmailServiceTest {

    private val validIcsContent =
        """
        BEGIN:VCALENDAR
        VERSION:2.0
        PRODID:-//WahooCLI//SYSTM Plan Export//EN
        CALSCALE:GREGORIAN
        BEGIN:VEVENT
        UID:test-123@wahoo
        DTSTART;VALUE=DATE:20260310
        DTEND;VALUE=DATE:20260311
        SUMMARY:Test Workout (30 min)
        STATUS:CONFIRMED
        TRANSP:TRANSPARENT
        END:VEVENT
        END:VCALENDAR
        """
            .trimIndent()

    @Test
    fun `returns failure when email is disabled`() {
        val config = EmailConfig(enabled = false)
        val result = EmailService.send(config, validIcsContent, "test.ics")

        assertFalse(result.success)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage.contains("disabled"))
    }

    @Test
    fun `returns failure when SMTP host is blank`() {
        val config =
            EmailConfig(
                enabled = true,
                smtpHost = "",
                fromAddress = "from@test.com",
                toAddress = "to@test.com",
            )
        val result = EmailService.send(config, validIcsContent, "test.ics")

        assertFalse(result.success)
        assertEquals("SMTP host is not configured", result.errorMessage)
    }

    @Test
    fun `returns failure when from address is blank`() {
        val config =
            EmailConfig(
                enabled = true,
                smtpHost = "smtp.test.com",
                fromAddress = "",
                toAddress = "to@test.com",
            )
        val result = EmailService.send(config, validIcsContent, "test.ics")

        assertFalse(result.success)
        assertEquals("From address is not configured", result.errorMessage)
    }

    @Test
    fun `returns failure when to address is blank`() {
        val config =
            EmailConfig(
                enabled = true,
                smtpHost = "smtp.test.com",
                fromAddress = "from@test.com",
                toAddress = "",
            )
        val result = EmailService.send(config, validIcsContent, "test.ics")

        assertFalse(result.success)
        assertEquals("To address is not configured", result.errorMessage)
    }

    @Test
    fun `returns failure with error message on SMTP connection failure`() {
        // Use an unreachable SMTP server to trigger a connection failure
        val config =
            EmailConfig(
                enabled = true,
                smtpHost = "localhost",
                smtpPort = 19999, // unlikely to have anything listening
                fromAddress = "from@test.com",
                toAddress = "to@test.com",
                useTls = false,
            )
        val result = EmailService.send(config, validIcsContent, "test.ics")

        assertFalse(result.success)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage.startsWith("Failed to send email:"))
    }

    @Test
    fun `EmailResult success has no error message`() {
        val result = EmailResult(success = true)
        assertTrue(result.success)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `EmailResult failure has error message`() {
        val result = EmailResult(success = false, errorMessage = "test error")
        assertFalse(result.success)
        assertEquals("test error", result.errorMessage)
    }
}
