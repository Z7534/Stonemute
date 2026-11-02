# 🔇 Stonemute – LiteBans to SimpleVoiceChat Mute Sync

**Stonemute** ist ein hochperformantes, eigenständiges Paper-Plugin (für Paper, Purpur und Leaf ab Minecraft 26.2 / Java 25), das Mutes von **LiteBans** automatisch und in Echtzeit auf **SimpleVoiceChat** überträgt.

---

## 📑 Inhaltsverzeichnis
1. [Funktionen & Highlights](#-funktionen--highlights)
2. [Voraussetzungen](#-voraussetzungen)
3. [Befehle & Berechtigungen](#-befehle--berechtigungen)
4. [Konfiguration (`config.yml`)](#-konfiguration-configyml)
5. [Architektur & Performance](#-architektur--performance)
6. [Kompilierung & Entwicklung](#-kompilierung--entwicklung)
7. [Changelog](#-changelog)

---

## ⚡ Funktionen & Highlights

* **Echtzeit-Synchronisation:** Reagiert über die LiteBans Events-API (`entryAdded`, `entryRemoved`) ohne Verzögerung auf neue oder aufgehobene Mutes.
* **Netzwerkweite Mute-Unterstützung:** Erkennt auch Mutes, die auf anderen BungeeCord-/Velocity-Unterservern vergeben wurden oder mit zeitlicher Befristung ablaufen (`check-all-server-scopes`).
* **0-Lag VoiceChat-Thread-Architektur:** Das Abfangen der Mikrofonpakete (`MicrophonePacketEvent`) erfolgt ohne blockierende Datenbankzugriffe direkt aus einem atomaren In-Memory-Cache (`ConcurrentHashMap`).
* **Actionbar-Hinweis mit Spam-Schutz:** Gemutete Spieler erhalten beim Sprechversuch eine formatierte Meldung in der Actionbar mit anpassbarem Cooldown (`notify-cooldown-seconds`).
* **LuckPerms Bypass-Unterstützung:** Spieler mit der Berechtigung `stonemute.bypass` können trotz LiteBans-Mute im Voicechat sprechen.
* **Laufzeit-Reload:** Mit `/stonemute reload` können Konfigurationsänderungen (inkl. Intervall-Anpassungen) direkt ohne Server-Neustart übernommen werden.

---

## 📦 Voraussetzungen

| Plugin | Pflicht / Optional | Beschreibung |
| :--- | :--- | :--- |
| **SimpleVoiceChat** (`voicechat`) | **Erforderlich** | Das Voicechat-System auf dem Server. |
| **LiteBans** | **Erforderlich** | Das Bestrafungs- & Mute-System. |
| **LuckPerms** | *Optional* | Für erweiterte Bypass-Berechtigungsprüfungen. |

> **Systemvoraussetzung:** Minecraft Server 26.2+ (Paper/Purpur/Leaf) mit **Java 25**.

---

## 🛠️ Befehle & Berechtigungen

### Befehle

| Befehl | Beschreibung | Benötigte Berechtigung |
| :--- | :--- | :--- |
| `/stonemute` | Zeigt Plugin-Version und Entwickler-Informationen | *Keine* |
| `/stonemute reload` | Lädt die `config.yml` im laufenden Betrieb neu | `stonemute.admin` |

### Berechtigungen

| Berechtigung | Standard | Beschreibung |
| :--- | :--- | :--- |
| `stonemute.admin` | `op` | Erlaubt die Nutzung des Reload-Befehls. |
| `stonemute.bypass` | `false` | Erlaubt das Sprechen im Voicechat trotz aktivem LiteBans-Mute. |

---

## ⚙️ Konfiguration (`config.yml`)

Die Datei befindet sich im Ordner `plugins/Stonemute/config.yml`:

```yaml
settings:
  # Wie oft (in Sekunden) der Mute-Status aller Online-Spieler gegen LiteBans geprueft wird.
  # Sorgt dafuer, dass Mutes von anderen Servern im Netzwerk und ablaufende Mutes erkannt werden.
  refresh-interval-seconds: 10

  # true  = Mutes aus allen LiteBans-Server-Scopes zaehlen (empfohlen fuer netzwerkweiten Mute)
  # false = nur globale Mutes bzw. der Standard-Scope dieses Servers
  check-all-server-scopes: true

  # Permission, die den Voicechat-Mute umgeht
  bypass-permission: "stonemute.bypass"

messages:
  # MiniMessage-Format (unterstützt Small-Caps und Farb-Codes)
  muted-actionbar: "<red>ᴅᴜ ʙɪꜱᴛ ɢᴇᴍᴜᴛᴇᴛ ᴜɴᴅ ᴋᴀɴɴꜱᴛ ɪᴍ ᴠᴏɪᴄᴇᴄʜᴀᴛ ɴɪᴄʜᴛ ꜱᴘʀᴇᴄʜᴇɴ."
  # Mindestabstand zwischen zwei Hinweisen an denselben Spieler (in Sekunden)
  notify-cooldown-seconds: 5
```

---

## 🧠 Architektur & Performance

1. **Kein blockierender I/O:** SimpleVoiceChat verarbeitet Audio-Pakete auf einem separaten Audio-Netzwerkthread. Direkte SQL-Abfragen auf diesem Thread würden sofort zu Audio-Glitches führen. Stonemute cacht alle Mute-Zustände asynchron im Arbeitsspeicher.
2. **Event-Driven:** Sobald LiteBans einen Spieler mutet oder entmutet, wird der Cache auf dem Hauptthread sofort aktualisiert.
3. **Periodischer Backup-Sweep:** Ein asynchroner Bukkit-Timer gleicht in konfigurierbaren Intervallen alle verbundenen Spieler ab, um externe Änderungen aus der LiteBans-Datenbank aufzugreifen.

---

## 🔨 Kompilierung & Entwicklung

Das Projekt nutzt Gradle mit ShadowJar:

```powershell
# In das Stonemute-Verzeichnis wechseln:
cd C:\Stonemute

# Plugin kompilieren und Jar erzeugen:
.\gradlew.bat build
```

Die fertige Datei befindet sich anschließend unter `build/libs/Stonemute-1.0.0.jar`.

---

## 📜 Changelog

### v1.0.0
* Erstveröffentlichung als eigenständiges Plugin nach Auslagerung aus dem Ztone-Kern.
* Vollständige Entkopplung für minimale Serverlast und maximale Wartbarkeit.
* `/stonemute reload`-Befehl mit dynamischer Intervall-Aktualisierung implementiert.
