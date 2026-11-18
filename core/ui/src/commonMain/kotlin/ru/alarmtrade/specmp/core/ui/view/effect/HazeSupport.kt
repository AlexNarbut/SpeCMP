package ru.alarmtrade.specmp.core.ui.view.effect

// Windows: шейдер блюра haze вешает рендер Skiko (DirectX) при скролле — там блюр выключен, остаётся тинт
internal expect val isHazeBlurSupported: Boolean
