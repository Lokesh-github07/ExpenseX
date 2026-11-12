# ExpenseTracker – Gmail OTP setup

## Local run (IntelliJ / Eclipse / Maven)

1. Copy `.env.example` to `.env` in the project root (the same folder as `pom.xml`).
2. Set `MAIL_USERNAME` to the Gmail account that will send messages.
3. Enable Google 2-Step Verification, then create an App Password at
   https://myaccount.google.com/apppasswords.
4. Put the newly generated 16-character value in `MAIL_APP_PASSWORD`.
   Do not use the normal Gmail account password. Spaces shown by Google may be
   omitted; the application removes whitespace before SMTP authentication.
5. Keep `MAIL_ENABLED=true`.
6. Stop the running application completely and start it again so the new
   environment/configuration is loaded.

The application imports the root `.env` as properties for local runs. Real
process environment variables take precedence over values in `.env`.

## Docker Compose

The compose file passes `MAIL_USERNAME` and `MAIL_APP_PASSWORD` from the
project-root `.env` into Spring Boot as `SPRING_MAIL_USERNAME` and
`SPRING_MAIL_PASSWORD`. Recreate the app container after changing credentials:

```bash
docker compose up -d --build --force-recreate app
```

## Expected logs

Successful authentication:

```text
MAIL CHECK OK - SMTP authentication succeeded ...
```

Credential rejection:

```text
MAIL CHECK FAILED ... 535-5.7.8 Username and Password not accepted
```

A `535-5.7.8` response comes from Google. It means Google rejected the
account/password pair; application code cannot bypass that decision. Generate
a fresh App Password for the exact account in `MAIL_USERNAME`, update `.env`,
and restart the application.

## Security

- Never commit or share `.env`.
- `.env` is excluded from this delivered project archive.
- Do not paste App Passwords, JWT secrets, database passwords, or API keys
  into source code or logs.
- If a real `.env` file was shared or uploaded, rotate its credentials.
- `MAIL_CONSOLE_FALLBACK=false` keeps failed SMTP sends from being reported as
  successful console-only deliveries. For local development without SMTP,
  explicitly set `MAIL_ENABLED=false` to use the existing development console
  behavior.
