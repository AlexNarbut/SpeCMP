package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

/** Разовые действия мастера, которые выполняет UI. */
sealed interface FastMountEffect {
    /** Выйти из монтажа (сессия закрывается обёрткой). */
    data object Exit : FastMountEffect

    /** Выйти с возвратом к поиску блока. */
    data object ChooseAnother : FastMountEffect

    /** Выйти в расширенный монтаж (новый поиск). */
    data object AdvancedMount : FastMountEffect

    /** Открыть файл системным приложением. */
    class OpenFile(val name: String, val bytes: ByteArray) : FastMountEffect
}
