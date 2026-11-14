package ru.alarmtrade.specmp.appupdate

import co.touchlab.kermit.Logger
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.Base64
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.file.safeFileName
import ru.alarmtrade.specmp.core.common.file.uniqueFileName
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateInstallResult
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePackage
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateInstaller

internal class DesktopAppUpdateInstaller(
    private val ioDispatcher: CoroutineDispatcher,
    private val os: DesktopOs? = DesktopOs.current(),
    private val updateDir: File = File(System.getProperty("java.io.tmpdir"), UPDATE_DIR_NAME),
) : AppUpdateInstaller {
    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override val platform: AppUpdatePlatform = when (os) {
        DesktopOs.WINDOWS -> AppUpdatePlatform.WINDOWS
        DesktopOs.MAC -> AppUpdatePlatform.MACOS
        null -> AppUpdatePlatform.OTHER_DESKTOP
    }

    override val storeUrls: List<String> = emptyList()

    override fun accepts(fileName: String): Boolean = os != null && InstallerKind.of(fileName)?.os == os

    override suspend fun prepare(fileName: String): Response<AppUpdatePackage> = io {
        updateDir.deleteRecursively()
        val file = File(updateDir, safeFileName(fileName))
        if (!(updateDir.isDirectory || updateDir.mkdirs()) || !file.createNewFile()) {
            throw IOException("Update file is not created: $file")
        }
        AppUpdatePackage(file.absolutePath, file.name)
    }

    override suspend fun append(target: AppUpdatePackage, chunk: ByteArray): Response<Unit> = io {
        FileOutputStream(target.path, true).use { it.write(chunk) }
    }

    override suspend fun install(target: AppUpdatePackage): Response<AppUpdateInstallResult> {
        val file = File(target.path)
        val kind = InstallerKind.of(file.name)
        if (kind == null || kind.os != os ||
            !kind.matches(file)
        ) {
            return Response.Success(AppUpdateInstallResult.WRONG_FILE)
        }
        val started = try {
            when (kind.os) {
                DesktopOs.WINDOWS -> startWindows(file, kind)
                DesktopOs.MAC -> startMac(file)
            }
        } catch (e: IOException) {
            logger.e(e) { "Installer is not started" }
            false
        }
        if (!started) return Response.Success(AppUpdateInstallResult.FAILED)
        delay(EXIT_DELAY_MS)
        exitProcess(0)
    }

    override suspend fun export(target: AppUpdatePackage): Response<Unit> = io {
        val dir = File(System.getProperty("user.home"), EXPORT_DIR)
        if (!dir.exists() && !dir.mkdirs()) throw IOException("Export folder is not created: $dir")
        val copy = File(dir, uniqueFileName(target.fileName) { File(dir, it).exists() })
        File(target.path).copyTo(copy)
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(dir)
        }
    }

    private suspend fun <T> io(block: () -> T): Response<T> = withContext(ioDispatcher) { safeRun(block) }

    // PowerShell, а не .cmd: -EncodedCommand (UTF-16) не ломает кириллицу в путях профиля Windows.
    private fun startWindows(file: File, kind: InstallerKind): Boolean {
        val install = when (kind) {
            InstallerKind.MSI ->
                "Start-Process msiexec.exe -ArgumentList '/i \"${file.ps()}\" /passive /norestart' -Wait"

            else -> "Start-Process -FilePath '${file.ps()}' -Wait"
        }
        val relaunch = currentLauncher()?.let { "Start-Process -FilePath '${it.ps()}'" }.orEmpty()
        val script = """
            ${'$'}p = ${ProcessHandle.current().pid()}
            while (Get-Process -Id ${'$'}p -ErrorAction SilentlyContinue) { Start-Sleep -Milliseconds 500 }
            $install
            $relaunch
        """.trimIndent()
        val encoded = Base64.getEncoder().encodeToString(script.toByteArray(Charsets.UTF_16LE))
        ProcessBuilder(
            "powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass",
            "-WindowStyle", "Hidden", "-EncodedCommand", encoded,
        ).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectErrorStream(true).start()
        return true
    }

    private fun startMac(file: File): Boolean {
        val bundle = currentLauncher()?.let(::enclosingBundle) ?: return false
        val script = File(updateDir, MAC_SCRIPT_NAME).apply {
            writeText(MAC_SCRIPT)
            setExecutable(true)
        }
        ProcessBuilder(
            "/bin/zsh",
            script.absolutePath,
            ProcessHandle.current().pid().toString(),
            file.absolutePath,
            bundle.absolutePath,
        ).redirectOutput(File(updateDir, MAC_LOG_NAME)).redirectErrorStream(true).start()
        return true
    }

    // Лаунчер jpackage; из IDE/gradle это java — тогда перезапускать и заменять нечего.
    private fun currentLauncher(): File? {
        val path = System.getProperty("jpackage.app-path")
            ?: ProcessHandle.current().info().command().orElse(null)
            ?: return null
        val file = File(path)
        return file.takeIf { it.isFile && !it.nameWithoutExtension.equals("java", ignoreCase = true) }
    }

    private fun enclosingBundle(launcher: File): File? =
        generateSequence(launcher.parentFile) { it.parentFile }.firstOrNull { it.name.endsWith(".app") }

    private fun File.ps(): String = absolutePath.replace("'", "''")

    private companion object {
        const val UPDATE_DIR_NAME = "PandoraSpecUpdate"
        const val EXPORT_DIR = "Downloads/Pandora Spec"
        const val MAC_SCRIPT_NAME = "install.zsh"
        const val MAC_LOG_NAME = "install.log"
        const val EXIT_DELAY_MS = 500L

        // Ждёт выхода приложения, ставит .app из dmg на место текущего бандла целиком и открывает его.
        val MAC_SCRIPT = """
            #!/bin/zsh
            pid=${'$'}1; dmg=${'$'}2; target=${'$'}3
            while kill -0 "${'$'}pid" 2>/dev/null; do sleep 0.5; done
            mount=${'$'}(mktemp -d /tmp/pandoraspec.XXXXXX)
            if hdiutil attach -nobrowse -noautoopen -mountpoint "${'$'}mount" "${'$'}dmg"; then
              app=${'$'}(ls -d "${'$'}mount"/*.app 2>/dev/null | head -1)
              staging="${'$'}{target:h}/.${'$'}{target:t}.update"
              rm -rf "${'$'}staging"
              if [[ -n "${'$'}app" ]] && ditto "${'$'}app" "${'$'}staging"; then
                rm -rf "${'$'}target" && mv "${'$'}staging" "${'$'}target"
              else
                rm -rf "${'$'}staging"
              fi
              hdiutil detach "${'$'}mount" -quiet
            fi
            rmdir "${'$'}mount" 2>/dev/null
            open "${'$'}target"
        """.trimIndent() + "\n"
    }
}

internal enum class DesktopOs {
    WINDOWS,
    MAC,
    ;

    companion object {
        fun current(): DesktopOs? {
            val name = System.getProperty("os.name").orEmpty()
            return when {
                name.contains("win", ignoreCase = true) -> WINDOWS
                name.contains("mac", ignoreCase = true) -> MAC
                else -> null
            }
        }
    }
}

internal enum class InstallerKind(val os: DesktopOs, private val extension: String) {
    EXE(DesktopOs.WINDOWS, "exe"),
    MSI(DesktopOs.WINDOWS, "msi"),
    DMG(DesktopOs.MAC, "dmg"),
    ;

    fun matches(file: File): Boolean = try {
        when (this) {
            EXE -> file.startsWith(EXE_MAGIC)
            MSI -> file.startsWith(MSI_MAGIC)
            DMG -> file.endsWithDmgTrailer()
        }
    } catch (ignored: IOException) {
        false
    }

    companion object {
        fun of(fileName: String): InstallerKind? =
            entries.firstOrNull { fileName.substringAfterLast('.', "").equals(it.extension, ignoreCase = true) }
    }
}

private val EXE_MAGIC = bytes(0x4D, 0x5A)
private val MSI_MAGIC = bytes(0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1)
private val DMG_TRAILER_MAGIC = "koly".toByteArray(Charsets.US_ASCII)
private const val DMG_TRAILER_SIZE = 512L

private fun bytes(vararg values: Int): ByteArray = ByteArray(values.size) { values[it].toByte() }

private fun File.startsWith(magic: ByteArray): Boolean = inputStream().use { input ->
    input.readNBytes(magic.size).contentEquals(magic)
}

private fun File.endsWithDmgTrailer(): Boolean {
    if (length() < DMG_TRAILER_SIZE) return false
    return RandomAccessFile(this, "r").use { raf ->
        raf.seek(length() - DMG_TRAILER_SIZE)
        ByteArray(DMG_TRAILER_MAGIC.size).also(raf::readFully).contentEquals(DMG_TRAILER_MAGIC)
    }
}
