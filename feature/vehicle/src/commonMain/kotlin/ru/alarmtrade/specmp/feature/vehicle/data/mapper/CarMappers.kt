package ru.alarmtrade.specmp.feature.vehicle.data.mapper

import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specialist.domain.cars.Car as SdkCar
import ru.alarmtrade.specialist.domain.cars.CarBrand as SdkCarBrand
import ru.alarmtrade.specialist.domain.cars.CarBusGroup
import ru.alarmtrade.specialist.domain.cars.CarFeature as SdkCarFeature
import ru.alarmtrade.specialist.domain.cars.CarFeatureGroup as SdkCarFeatureGroup
import ru.alarmtrade.specialist.domain.cars.CarInfo
import ru.alarmtrade.specialist.domain.cars.CarModel as SdkCarModel
import ru.alarmtrade.specialist.domain.cars.EngineBlockByCanFeature
import ru.alarmtrade.specialist.domain.cars.ImmobilizerBypassFeature
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBus
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCapability
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarDocument
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeature
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim
import ru.alarmtrade.specmp.feature.vehicle.domain.model.MountPhoto
import ru.alarmtrade.specmp.feature.vehicle.domain.model.MountPoint
import ru.alarmtrade.specmp.feature.vehicle.domain.model.UserManualRef
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType

internal fun SdkCarBrand.toDomain() = CarBrand(id = id, name = name, logoUrl = imageUrl.ifBlank { null })

internal fun SdkCarModel.toDomain() =
    CarModel(id = id, name = name, imageUrl = imageUrl.ifBlank { null }, years = productionYears)

internal fun SdkCar.toDomain() = CarTrim(id = id, name = name)

internal fun CarInfo.toSummary() = CarSummary(
    id = carId,
    brand = carBrand,
    model = carModel,
    name = carName,
    years = productionYears,
    canCode = canCode,
    imageUrl = carImageUrl?.ifBlank { null },
)

internal fun CarInfo.toCard() = CarCard(
    summary = toSummary(),
    engineBlockByCan = when (val feature = engineBlockByCan) {
        EngineBlockByCanFeature.NotAvailable -> CarCapability.NoData
        is EngineBlockByCanFeature.Available -> capability(feature.isSupported, feature.description)
    },
    keylessBypass = when (val feature = immobilizerBypass) {
        ImmobilizerBypassFeature.NotAvailable -> CarCapability.NoData
        is ImmobilizerBypassFeature.Available -> capability(feature.isSupported, feature.description)
    },
    notes = listOfNotNull(description1, description2).filter { it.isNotBlank() },
    features = features.sortedBy { it.order }.map { it.toDomain() },
    mountPoints = mountingGuides.map { guide ->
        MountPoint(
            title = guide.title,
            type = guide.type,
            description = guide.description?.ifBlank { null },
            photos = guide.images.sortedBy { it.order }.map { MountPhoto(it.name, it.url) },
        )
    },
    schemes = mountingSchemes.sortedBy {
        it.order
    }.map { CarDocument(it.name, it.description?.ifBlank { null }, it.url) },
    mountingMaps = mountingMaps.sortedBy {
        it.order
    }.map { CarDocument(it.name, it.description?.ifBlank { null }, it.url) },
    userManuals = userManuals.map { UserManualRef(it.id, it.title, it.description.ifBlank { null }) },
    hasUserManuals = userManualsExist || userManuals.isNotEmpty(),
)

private fun capability(isSupported: Boolean, description: String?): CarCapability {
    val note = description?.ifBlank { null }
    return if (isSupported) CarCapability.Supported(note) else CarCapability.NotSupported(note)
}

private fun SdkCarFeature.toDomain() = CarFeature(
    name = name,
    description = description?.ifBlank { null },
    group = group.toDomain(),
    reads = statuses.map { it.toDomain() },
    controls = commands.map { it.toDomain() },
)

internal fun SdkCarFeatureGroup.toDomain(): CarFeatureGroup = when (this) {
    SdkCarFeatureGroup.MAIN -> CarFeatureGroup.MAIN
    SdkCarFeatureGroup.EXTENDED -> CarFeatureGroup.EXTENDED
    SdkCarFeatureGroup.ELECTRIC -> CarFeatureGroup.ELECTRIC
    SdkCarFeatureGroup.TRAILER -> CarFeatureGroup.TRAILER
}

internal fun CarFeatureGroup.toSdk(): SdkCarFeatureGroup = when (this) {
    CarFeatureGroup.MAIN -> SdkCarFeatureGroup.MAIN
    CarFeatureGroup.EXTENDED -> SdkCarFeatureGroup.EXTENDED
    CarFeatureGroup.ELECTRIC -> SdkCarFeatureGroup.ELECTRIC
    CarFeatureGroup.TRAILER -> SdkCarFeatureGroup.TRAILER
}

/** Пустой фильтр групп — `null` (без фильтра), а не пустой список. */
internal fun Set<CarFeatureGroup>.toSdkFilter(): List<SdkCarFeatureGroup>? =
    takeIf { it.isNotEmpty() }?.sortedBy { it.ordinal }?.map { it.toSdk() }

@Suppress("CyclomaticComplexMethod") // плоская таблица соответствия
internal fun CarBusGroup.toDomain(): CarBus = when (this) {
    CarBusGroup.CAN1 -> CarBus.CAN1
    CarBusGroup.CAN2 -> CarBus.CAN2
    CarBusGroup.CAN3 -> CarBus.CAN3
    CarBusGroup.CAN4 -> CarBus.CAN4
    CarBusGroup.CAN1_FD -> CarBus.CAN1_FD
    CarBusGroup.CAN2_FD -> CarBus.CAN2_FD
    CarBusGroup.CAN3_FD -> CarBus.CAN3_FD
    CarBusGroup.CAN4_FD -> CarBus.CAN4_FD
    CarBusGroup.CAN_FD1_OR_CAN_FD3 -> CarBus.CAN_FD1_OR_CAN_FD3
    CarBusGroup.CAN_FD2_OR_CAN_FD4 -> CarBus.CAN_FD2_OR_CAN_FD4
    CarBusGroup.LIN -> CarBus.LIN
    CarBusGroup.LIN_IN_OUT -> CarBus.LIN_IN_OUT
    CarBusGroup.BEAN -> CarBus.BEAN
    CarBusGroup.IMO_KEY1 -> CarBus.IMO_KEY1
    CarBusGroup.IMO_KEY2 -> CarBus.IMO_KEY2
    CarBusGroup.IMO_KEY1_PLUS_IMO_KEY2 -> CarBus.IMO_KEY1_PLUS_IMO_KEY2
    CarBusGroup.UNKNOWN -> CarBus.UNKNOWN
}

internal fun VehicleDeviceType.toSubsystemType(): SubsystemType = when (this) {
    VehicleDeviceType.ALARM -> SubsystemType.ALARM_MAIN
    VehicleDeviceType.MICROBOT -> SubsystemType.MICROBOT_MAIN
}
