package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.core.common.AppDispatchers

@Composable
expect fun rememberBinaryFileExporter(dispatchers: AppDispatchers): BinaryFileExporter

@Composable
expect fun rememberHtmlPdfRenderer(dispatchers: AppDispatchers): HtmlPdfRenderer

@Composable
expect fun rememberPdfPageRasterizer(dispatchers: AppDispatchers): PdfPageRasterizer
