package nesski.de.email

import nesski.de.config.EmailConfig
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("EmailService")

/**
 * Result of attempting to send an email with .ics attachment.
 *
 * @property success Whether the email was sent successfully
 * @property errorMessage Error details if sending failed, null on success
 */
data class EmailResult(
    val success: Boolean,
    val errorMessage: String? = null
)

/**
 * Sends .ics calendar content as an email attachment via SMTP.
 *
 * Uses Simple Java Mail for SMTP transport. Configuration is driven by
 * [EmailConfig] from the TOML config file, with env var overrides for
 * sensitive values (SMTP_USERNAME, SMTP_PASSWORD).
 */
object EmailService {

    /**
     * Send an email with the given .ics content as an attachment.
     *
     * @param config Email configuration (SMTP host, port, credentials, addresses)
     * @param icsContent The RFC 5545 VCALENDAR string to attach
     * @param filename The attachment filename (e.g. "workouts_2w_2026-03-08.ics")
     * @param bodyText Optional plain-text body for the email
     * @return [EmailResult] indicating success or failure with error details
     */
    fun send(
        config: EmailConfig,
        icsContent: String,
        filename: String,
        bodyText: String = "Your Wahoo SYSTM workout plan is attached as an .ics file.\n\nImport it into Apple Reminders or any calendar application."
    ): EmailResult {
        if (!config.enabled) {
            log.info("Email sending is disabled in config")
            return EmailResult(success = false, errorMessage = "Email sending is disabled in config")
        }

        // Resolve credentials with env var overrides
        val smtpUsername = System.getenv("SMTP_USERNAME") ?: config.smtpUsername
        val smtpPassword = System.getenv("SMTP_PASSWORD") ?: config.smtpPassword
        val fromAddress = System.getenv("SMTP_FROM") ?: config.fromAddress
        val toAddress = System.getenv("SMTP_TO") ?: config.toAddress

        if (config.smtpHost.isBlank()) {
            return EmailResult(success = false, errorMessage = "SMTP host is not configured")
        }
        if (fromAddress.isBlank()) {
            return EmailResult(success = false, errorMessage = "From address is not configured")
        }
        if (toAddress.isBlank()) {
            return EmailResult(success = false, errorMessage = "To address is not configured")
        }

        return try {
            val email = EmailBuilder.startingBlank()
                .from(fromAddress)
                .to(toAddress)
                .withSubject(config.subject)
                .withPlainText(bodyText)
                .withAttachment(
                    filename,
                    icsContent.toByteArray(Charsets.UTF_8),
                    "text/calendar; charset=UTF-8"
                )
                .buildEmail()

            val transportStrategy = if (config.useTls) {
                TransportStrategy.SMTP_TLS
            } else {
                TransportStrategy.SMTP
            }

            val mailerBuilder = MailerBuilder
                .withSMTPServer(config.smtpHost, config.smtpPort)
                .withTransportStrategy(transportStrategy)

            // Only add credentials if provided
            val mailer = if (smtpUsername.isNotBlank() && smtpPassword.isNotBlank()) {
                mailerBuilder
                    .withSMTPServerUsername(smtpUsername)
                    .withSMTPServerPassword(smtpPassword)
                    .buildMailer()
            } else {
                mailerBuilder.buildMailer()
            }

            mailer.sendMail(email)

            log.info("Email sent successfully to $toAddress with attachment $filename")
            EmailResult(success = true)
        } catch (e: Exception) {
            val errorMsg = "Failed to send email: ${e.message}"
            log.error(errorMsg, e)
            EmailResult(success = false, errorMessage = errorMsg)
        }
    }
}
