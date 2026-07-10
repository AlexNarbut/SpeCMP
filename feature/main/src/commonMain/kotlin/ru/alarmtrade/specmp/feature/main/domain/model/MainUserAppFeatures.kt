package ru.alarmtrade.specmp.feature.main.domain.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specialist.domain.user.UserAppFeature

@Immutable
data class MainUserAppFeatures(
    val features: Set<UserAppFeature> = emptySet(),
) {
    val hasCanScanner: Boolean
        get() = UserAppFeature.CAN_SCANNER in features

    val hasOrdersRequest: Boolean
        get() = UserAppFeature.ORDERS_REQUEST in features

    val hasProductSearch: Boolean
        get() = UserAppFeature.PRODUCT_SEARCH in features
}
