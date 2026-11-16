# ExpenseTracker – Brevo SMTP OTP Setup

## Local Run (IntelliJ / Eclipse / Maven)

1. Copy `.env.example` to `.env` in the project root (the same folder as `pom.xml`).
2. Create a Brevo account at https://www.brevo.com/.
3. Open **Settings → SMTP & API → SMTP**.
4. Copy your Brevo SMTP login and generate an SMTP key.
5. Verify your sender email address in Brevo.
6. Configure the following values in your `.env` file:

```properties
# Brevo SMTP Configuration
BREVO_SMTP_LOGIN=your_brevo_smtp_login
BREVO_SMTP_KEY=your_brevo_smtp_key
MAIL_FROM=your_verified_sender_email

# Email Settings
MAIL_ENABLED=true
MAIL_CONSOLE_FALLBACK=false
```

7. The application uses the following SMTP configuration:

```properties
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=2525
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

8. Stop the running application completely and restart it so the new environment/configuration is loaded.

**Important:**
- `BREVO_SMTP_LOGIN` is your Brevo SMTP login, not necessarily your sender email.
- `BREVO_SMTP_KEY` must be the generated SMTP key, not the Brevo API key.
- `MAIL_FROM` must be an email address verified in Brevo.
- Do not use your Gmail password or Gmail App Password.

The application imports the root `.env` as properties for local runs. Real process environment variables take precedence over values in `.env`.

## Docker Compose

The Docker Compose configuration must pass the following environment variables to Spring Boot:

- `BREVO_SMTP_LOGIN`
- `BREVO_SMTP_KEY`
- `MAIL_FROM`
- `MAIL_ENABLED`
- `MAIL_CONSOLE_FALLBACK`

Example Docker Compose environment configuration:

```yaml
environment:
  BREVO_SMTP_LOGIN: ${BREVO_SMTP_LOGIN}
  BREVO_SMTP_KEY: ${BREVO_SMTP_KEY}
  MAIL_FROM: ${MAIL_FROM}
  MAIL_ENABLED: ${MAIL_ENABLED:-true}
  MAIL_CONSOLE_FALLBACK: ${MAIL_CONSOLE_FALLBACK:-false}
```

Recreate the application container after changing credentials:

```bash
docker compose up -d --build --force-recreate app
```

## Render Deployment

1. Open your Render Dashboard.
2. Select your ExpenseTracker Web Service.
3. Navigate to **Environment**.
4. Add the following environment variables:

| Variable | Value |
|---|---|
| `BREVO_SMTP_LOGIN` | Your Brevo SMTP login |
| `BREVO_SMTP_KEY` | Your generated Brevo SMTP key |
| `MAIL_FROM` | Your verified sender email |
| `MAIL_ENABLED` | `true` |
| `MAIL_CONSOLE_FALLBACK` | `false` |

5. Save the environment variables.
6. Redeploy your application.

**SMTP Configuration:**

| Setting | Value |
|---|---|
| SMTP Host | `smtp-relay.brevo.com` |
| SMTP Port | `2525` |
| Authentication | Enabled |
| STARTTLS | Enabled |

Port `2525` is used as an alternative to the standard SMTP ports blocked on Render Free services.

## Expected Logs

Successful SMTP authentication:

```text
MAIL CHECK OK - SMTP authentication succeeded
for x***@gmail.com at smtp-relay.brevo.com:2525.
Email delivery connection is ready.
```

Authentication failure:

```text
MAIL CHECK FAILED ...
SMTP authentication failed.
```

Connection failure:

```text
MailConnectException:
Couldn't connect to host, port:
smtp-relay.brevo.com, 2525
```

**Troubleshooting:**

- Verify your Brevo SMTP login.
- Generate a new SMTP key if authentication fails.
- Confirm that your sender email is verified in Brevo.
- Check SMTP host and port configuration.
- Check Render environment variables.
- Check Brevo transactional email logs for delivery status.

Successful SMTP authentication confirms that the application can connect and authenticate. It does not guarantee inbox delivery.

## Security

- Never commit or share `.env`.
- Keep `.env` excluded through `.gitignore`.
- Do not paste SMTP keys, JWT secrets, database passwords, or API keys into source code or logs.
- Never expose SMTP credentials in frontend code.
- If a real `.env` file was shared or uploaded, rotate its credentials.
- Keep `MAIL_CONSOLE_FALLBACK=false` in production.
- Do not print OTPs or sensitive email content in production logs.
- Use a verified sender address in Brevo.
- Keep production credentials in Render Environment Variables.

For local development without SMTP, explicitly set:

```properties
MAIL_ENABLED=false
```

This uses the existing development behavior instead of attempting real email delivery.
