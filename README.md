REST-API Security Demo

Eine Spring-Boot-REST-API zur praktischen Demonstration von API-Sicherheitsschwachstellen und deren Gegenmaßnahmen. Entwickelt im Rahmen einer Schwachstellenanalyse auf Basis der OWASP API Security Top 10.


Technologie-Stack

KomponenteTechnologieFrameworkSpring Boot 4.0.6SpracheJava 25SicherheitSpring Security + JJWT 0.11.5DatenbankH2 (In-Memory)BuildMaven


Voraussetzungen


Java 25
Maven 3.x



Starten

bashmvn spring-boot:run

Die API ist erreichbar unter http://localhost:8080.

Die H2-Konsole ist erreichbar unter http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:testdb).


Vordefinierte Benutzer

Beim Start werden automatisch folgende Testnutzer angelegt:

UsernamePasswortRolleUserpasswortUSERuser2passwort2USERadminadminpasswortADMIN


Endpunkte

Auth (offen, kein Token erforderlich)

MethodePfadBeschreibungPOST/auth/loginLogin, gibt Access- und Refresh-Token zurückPOST/auth/registerNeuen User anlegen (Rolle USER), gibt direkt Tokens zurückPOST/auth/refreshNeuen Access-Token per Refresh-Token holen

Personen (JWT erforderlich)

MethodePfadBeschreibungGET/api/personenEigene Personen abrufen (bei BOLA-Schutz aktiv)GET/api/personen/{id}Einzelne Person abrufenPOST/api/personenNeue Person anlegen (Owner = eingeloggter User)DELETE/api/personen/{id}Person löschenGET/api/safetyTest-Endpoint

Admin (JWT mit Rolle ADMIN erforderlich)

MethodePfadBeschreibungGET/api/admin/personenAlle Personen aller User abrufen


Beispiel-Request (Postman / curl)

Login:

bashcurl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"User","password":"passwort"}'

Gesicherter Endpoint:

bashcurl http://localhost:8080/api/personen \
  -H "Authorization: Bearer <access-token>"

Refresh:

bashcurl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: text/plain" \
  -d '<refresh-token>'


Security-Demo-Toggles

Das Projekt enthält drei togglebare Sicherheitsmechanismen zur Demonstration von Schwachstellen vs. Schutzmaßnahmen. Die Konfiguration erfolgt in src/main/resources/application.properties:

4.2 / 5.1 – Broken Authentication (JWT-Signaturprüfung)

properties# true  → sicher:     Signatur und Ablauf werden geprüft
# false → verwundbar: Token wird ohne Signaturprüfung akzeptiert (Privilege Escalation möglich)
app.security.jwt-secure-validation=true

4.3 / 5.2 – Broken Object Level Authorization (BOLA)

properties# true  → sicher:     Jeder User sieht nur seine eigenen Personen
# false → verwundbar: Jeder eingeloggte User kann alle Personen lesen/löschen
app.security.bola-protection=true

4.4 / 5.3 – Rate Limiting (Brute-Force-Schutz)

properties# true  → sicher:     Max. 5 Login-Versuche pro Minute pro IP, danach HTTP 429
# false → verwundbar: Unbegrenzte Login-Versuche möglich
app.security.rate-limiting=true
app.security.rate-limit.max-requests=5
app.security.rate-limit.window-ms=60000


JWT-Konfiguration

properties# Secret (min. 32 Byte, in Produktion als Umgebungsvariable JWT_SECRET setzen)
jwt.secret=${JWT_SECRET:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcd}

# Access-Token-Gültigkeit: 15 Minuten
jwt.access-expiration-ms=900000

# Refresh-Token-Gültigkeit: 7 Tage
jwt.refresh-expiration-ms=604800000


Wichtig: In Produktion das JWT_SECRET immer als Umgebungsvariable setzen, niemals hartcodiert lassen.




Hinweis

Dieses Projekt dient ausschließlich Lehr- und Demonstrationszwecken. Die bewusst eingebauten Schwachstellen (togglebar über application.properties) dürfen nicht in produktiven Systemen verwendet werden.
