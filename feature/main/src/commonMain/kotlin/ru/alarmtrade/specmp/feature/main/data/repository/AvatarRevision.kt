package ru.alarmtrade.specmp.feature.main.data.repository

internal fun String.withAvatarRevision(revision: Long): String =
    "$this${if ('?' in this) '&' else '?'}$AVATAR_REVISION_PARAM=$revision"

internal fun String.withoutAvatarRevision(): String = replace(AVATAR_REVISION_SUFFIX, "")

private const val AVATAR_REVISION_PARAM = "specmp_v"
private val AVATAR_REVISION_SUFFIX = Regex("[?&]$AVATAR_REVISION_PARAM=\\d+$")
