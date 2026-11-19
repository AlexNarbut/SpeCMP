package ru.alarmtrade.specmp.feature.productinfo.data.mapper

import ru.alarmtrade.specialist.domain.alarmDevice.information.DevicePackageComponent
import ru.alarmtrade.specialist.domain.alarmDevice.information.DevicePackageConfigurationInfo
import ru.alarmtrade.specialist.domain.alarmDevice.information.DevicePackageFirmwareInfo
import ru.alarmtrade.specialist.domain.alarmDevice.information.DevicePackageInformation
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductFirmware
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductInfo
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductKit
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductUnit

// SDK превращает отсутствующие name/serial сервера в пустые строки — для нас это «нет значения».
internal fun DevicePackageInformation.toDomain(): ProductInfo =
    ProductInfo(product = main?.toDomain(), kit = configuration?.toDomain())

internal fun DevicePackageComponent.toDomain(): ProductUnit = ProductUnit(
    id = deviceId,
    name = name.trim(),
    serial = serial.orNullIfBlank(),
    factoryDate = factoryDate?.date,
    firmwares = firmwares.map { it.toDomain() },
)

internal fun DevicePackageFirmwareInfo.toDomain(): ProductFirmware =
    ProductFirmware(module = type.trim(), version = name.orNullIfBlank(), date = factoryDate?.date)

internal fun DevicePackageConfigurationInfo.toDomain(): ProductKit = ProductKit(
    name = name.orNullIfBlank(),
    serial = serial.orNullIfBlank(),
    packageDate = packageDate?.date,
    components = components.map { it.toDomain() },
)

private fun String.orNullIfBlank(): String? = trim().takeIf { it.isNotEmpty() }
