package ru.alarmtrade.specmp.core.files

import androidx.core.content.FileProvider

/** Свой класс: два `<provider>` с одним `FileProvider` в одном манифесте склейка не допускает. */
class ImageCaptureFileProvider : FileProvider()

/** Совпадает с `android:authorities` провайдера в манифесте модуля. */
internal const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".core.files.images"
