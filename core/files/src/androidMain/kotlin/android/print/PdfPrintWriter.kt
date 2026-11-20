// Пакет платформы, а не модуля: только отсюда доступны конструкторы колбэков печати (см. writePdf).
@file:Suppress("konsist.module-package")

package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import java.io.File
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Печать адаптера в файл без системного диалога. Конструкторы `LayoutResultCallback`/`WriteResultCallback`
 * package-private — поэтому файл в пакете `android.print`. Вызывать на main (адаптер WebView).
 */
internal suspend fun PrintDocumentAdapter.writePdf(attributes: PrintAttributes, target: File): Boolean =
    suspendCancellableCoroutine { cont ->
        val signal = CancellationSignal()
        cont.invokeOnCancellation { signal.cancel() }
        val finish = { ok: Boolean ->
            onFinish()
            if (cont.isActive) cont.resumeWith(Result.success(ok))
        }
        onStart()
        onLayout(
            null,
            attributes,
            signal,
            object : PrintDocumentAdapter.LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
                    writeAll(target, signal, finish)
                }

                override fun onLayoutFailed(error: CharSequence?) = finish(false)

                override fun onLayoutCancelled() = finish(false)
            },
            null,
        )
    }

private fun PrintDocumentAdapter.writeAll(target: File, signal: CancellationSignal, finish: (Boolean) -> Unit) {
    val descriptor = ParcelFileDescriptor.open(
        target,
        ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE or ParcelFileDescriptor.MODE_READ_WRITE,
    )
    val done = { ok: Boolean ->
        descriptor.close()
        finish(ok)
    }
    onWrite(
        arrayOf(PageRange.ALL_PAGES),
        descriptor,
        signal,
        object : PrintDocumentAdapter.WriteResultCallback() {
            override fun onWriteFinished(pages: Array<out PageRange>?) = done(!pages.isNullOrEmpty())

            override fun onWriteFailed(error: CharSequence?) = done(false)

            override fun onWriteCancelled() = done(false)
        },
    )
}
