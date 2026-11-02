package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountChecklist

/**
 * Экраны других фич, которые мастер показывает на своих шагах. Подставляет composition root (`composeApp`): у
 * `feature/fast-mount` нет зависимостей на `firmware`/`autostart`/`state`/`vehicle-ui`/`configurator-ui`.
 *
 * @property firmwareUpdate обновление прошивки подключённого блока («Обновить прошивку» на проверке блока / CAN).
 * @property vehiclePicker каталог автомобилей с конфигурациями под модель блока.
 * @property configurationPicker облачные конфигурации выбранного автомобиля; карточка — «Выбрать» вместо загрузки
 *   в редактор, несовпадение CAN блокирует выбор и ведёт в [firmwareUpdate].
 * @property bypass обучение бесключевому обходу в режиме шага (контент и VM фичи автозапуска в шапке и панели мастера):
 *   без перехода в Clone (его ведёт SDK); на успехе и «нужен Clone» — «Далее» → `onNext(cloneRequired)`.
 * @property clone процедура Clone в режиме шага: на успехе — «Далее» → `onNext`.
 * @property systemCheck проверка системы с чек-листом мастера (строгий порядок заданий) в шапке и панели мастера.
 * @property fileOpener открыть файл системным приложением (карта монтажа).
 */
@Immutable
class FastMountSlots(
    val firmwareUpdate: @Composable (onBack: () -> Unit) -> Unit,
    val vehiclePicker: @Composable (
        request: MountVehicleRequest,
        onPick: (MountCar) -> Unit,
        onBack: () -> Unit
    ) -> Unit,
    val configurationPicker: @Composable (
        request: MountConfigurationRequest,
        onPick: (configurationId: Int) -> Unit,
        onOpenFirmwareUpdate: () -> Unit,
        onBack: () -> Unit
    ) -> Unit,
    val bypass: @Composable (step: MountStep, onNext: (cloneRequired: Boolean) -> Unit) -> Unit,
    val clone: @Composable (step: MountStep, onNext: () -> Unit) -> Unit,
    val systemCheck: @Composable (step: MountStep, checklist: MountChecklist, actions: MountChecklistActions) -> Unit,
    val fileOpener: MountFileOpener
)

/** Выбор автомобиля под модель блока ([deviceName] — `systemName`), [canCode] — CAN-код прошивки блока. */
@Immutable
data class MountVehicleRequest(val deviceName: String, val canCode: String?)

/** Выбор облачной конфигурации для [car] и модели блока; [canCode] — CAN блока (несовпадение блокирует выбор). */
@Immutable
data class MountConfigurationRequest(val deviceName: String, val canCode: String?, val car: MountCar)

/** Действия чек-листа проверки системы (id задания — id вопроса SDK). */
@Immutable
class MountChecklistActions(
    val onAnswer: (taskId: Int, option: Int) -> Unit,
    val onSkip: (taskId: Int) -> Unit,
    val onNext: () -> Unit,
    val onBack: () -> Unit
)

/** Открыть файл системным приложением: `true` — открыт, `false` — нет приложения, `null` — ошибка. */
fun interface MountFileOpener {
    suspend fun open(name: String, bytes: ByteArray): Boolean?
}

/**
 * Шаг мастера, который рисует другая фича (обход, Clone): шапка и нижняя панель мастера ([frame]), «Далее» мастера
 * ([next]) и выход с шага ([onBack] — «Вернуться к монтажу?»).
 *
 * @property key ключ VM экрана шага: меняется, когда шаг надо пройти заново (вернулись к монтажу).
 */
@Immutable
class MountStep(val key: String, val frame: MountStepFrame, val next: MountStepNext, val onBack: () -> Unit)

/** Шапка («шаг · Модель · ID», «Назад» или замок) и нижняя панель мастера для экрана другой фичи. */
@Immutable
class MountStepFrame(
    val topBar: @Composable (locked: Boolean, onBack: () -> Unit) -> Unit,
    val dock: @Composable (content: @Composable ColumnScope.() -> Unit) -> Unit
)

/** Переход мастера: [busy] — ждём SDK (спиннер на «Далее»), [error] — причина, по которой SDK не пустил дальше. */
@Immutable
class MountStepNext(val busy: Boolean, val error: String?)
