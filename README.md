# just-chat-mobile

Aplikasi Android (Kotlin + Jetpack Compose) untuk [just-chat](https://github.com/closofdev/just-chat) — room chat AI bergaya Claude web.

Bubble user di kanan, jawaban AI di kiri, respons di-stream kata per kata. Semua request dikirim ke server `just-chat` di Vercel, jadi **token CodeBuddy tidak pernah disimpan di dalam APK**.

## Status

- Chat streaming, stop, regenerate, like, tombol salin — jalan
- Markdown: heading, list, tabel, code block (dengan label bahasa + tombol salin), quote, `hr`
- Rumus LaTeX (jlatexmath-android)
- Layar awal dengan chip saran prompt
- Tool agent: badge status + form pertanyaan

Belum selesai:
- Tombol lampiran (`+`) masih placeholder, picker file belum dipasang
- Panel hasil websearch belum ditampilkan (datanya sudah diterima)

## Menjalankan

Butuh Android Studio (atau JDK 17+) dan Android SDK.

```bash
./gradlew assembleDebug      # APK debug
./gradlew assembleRelease    # APK release
./gradlew testDebugUnitTest  # unit test parser markdown
```

APK hasil build ada di `app/build/outputs/apk/`.

Kalau `JAVA_HOME` belum diset, arahkan ke JBR bawaan Android Studio:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

## Konfigurasi server

Endpoint ada di `data/ChatApi.kt`:

```kotlin
const val DEFAULT_BASE = "https://justchat.closof.site"
const val MODEL = "deepseek-v4.1-flash"
```

Ganti `DEFAULT_BASE` kalau mau menunjuk server lain. Server itu yang memegang kredensial AI — app hanya bicara lewat `/api/chat`.

## Struktur

```
app/src/main/java/com/example/just_chat_mobile/
  MainActivity.kt              entry point
  data/
    Models.kt                  ChatMessage, ToolInfo, ChatEvent
    ChatApi.kt                 klien SSE (OkHttp) ke /api/chat
  ui/
    ChatScreen.kt              layar utama
    ChatViewModel.kt           state: pesan, streaming, tool, pertanyaan
    components/                bubble, code block, tabel, rumus, composer, dll
    markdown/                  parser markdown & deteksi rumus (port dari versi web)
    theme/                     warna, ukuran, tipografi
  util/Time.kt                 waktu relatif & hitungan token/s
```

## Catatan teknis

**Markdown & rumus** ditulis ulang di Kotlin, bukan library. Logikanya diport dari versi web (`src/lib/markdown.js` dan `src/lib/math.js`) supaya perilakunya sama, termasuk aturan ketat agar teks seperti `harga $5` tidak dianggap rumus. Ada unit test untuk ini.

**Rumus pakai jlatexmath-android**, bukan KaTeX. Bentuk glyph-nya berbeda dari versi web — ini kompromi karena Android tidak punya KaTeX native.

**Minify (R8) dimatikan.** Sempat diaktifkan dan menyebabkan `IllegalAccessError` pada `kotlinx.coroutines` saat startup. Kalau mau dinyalakan lagi, perlu proguard rules yang benar dan wajib diuji langsung di perangkat.

**Animasi stream** berbeda pendekatan dari web. Web memberi fade per kata; di Compose itu memicu re-layout terus-menerus dan terasa tersendat, jadi diganti fade blok + kursor kedip.
