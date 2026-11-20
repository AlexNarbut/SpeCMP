package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.files.ImagePickResult
import ru.alarmtrade.specmp.core.files.ImagePicker
import ru.alarmtrade.specmp.core.files.ImagePrepareResult
import ru.alarmtrade.specmp.core.files.ImageSource
import ru.alarmtrade.specmp.core.files.PickedImage
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.settingsFileName
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportConfigurationsRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

/**
 * Предотчёт: правки ручных полей пишутся в SDK одной записью после паузы ввода ([SAVE_DEBOUNCE_MS]); «Назад»
 * дописывает несохранённое и закрывает экран без подтверждения.
 */
@Suppress("LongParameterList", "TooManyFunctions")
internal class PassportEditorViewModel(
    args: PassportEditorArgs,
    deviceSession: DeviceSession,
    private val repository: MountPassportRepository,
    private val configurations: PassportConfigurationsRepository,
    private val permissions: PermissionsProvider,
    private val imagePicker: ImagePicker,
    private val settingsFileSaver: SettingsFileSaver,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<PassportEditorState, PassportEditorEffect>(
    deviceSession,
    PassportEditorState(passportId = args.passportId, canTakePhoto = imagePicker.canTakePhoto),
) {
    private val passportId = args.passportId

    override suspend fun IntentScope.onStart() {
        connectedDevice
            .map { it?.systemId?.toString() }
            .collectIntoState { id -> copy(connectedSystemId = id) }
        load()
    }

    fun onRetry() {
        load()
    }

    fun onFieldChange(field: PassportField, value: String) {
        intent {
            if (currentState.passport.content == null) return@intent
            reduce { copy(form = form.with(field, value), save = SaveStatus.PENDING) }
            scheduleSave()
        }
    }

    fun onWorkToggle(work: PassportWork, checked: Boolean) {
        intent {
            if (currentState.passport.content == null || !currentState.worksEditable) return@intent
            reduce { copy(form = form.copy(works = form.works.with(work, checked)), save = SaveStatus.PENDING) }
            scheduleSave()
        }
    }

    fun onBack() {
        intent {
            if (currentState.save != SaveStatus.SAVED) {
                cancelLaunched(SAVE)
                save()
            }
            postSideEffect(PassportEditorEffect.Close)
        }
    }

    fun onAddPhoto() {
        intent {
            val passport = currentState.passport.content ?: return@intent
            when {
                passport.photos.size >= MountPassport.MAX_PHOTOS ->
                    reduce { copy(photoError = PassportError.PhotoLimit) }

                imagePicker.canTakePhoto -> reduce { copy(photoSheet = PhotoSheet.Pick, photoError = null) }

                else -> pickPhoto(ImageSource.Gallery)
            }
        }
    }

    fun onPhotoSource(source: PhotoSource) {
        intent {
            when (source) {
                PhotoSource.GALLERY -> pickPhoto(ImageSource.Gallery)

                PhotoSource.CAMERA -> when (val access = permissions.requestCameraPermission()) {
                    PermissionResult.Granted -> pickPhoto(ImageSource.Camera)

                    is PermissionResult.Denied -> reduce {
                        copy(photoSheet = PhotoSheet.CameraDenied(access.canOpenSettings))
                    }
                }
            }
        }
    }

    fun onPhotoSheetDismiss() {
        intent { reduce { copy(photoSheet = null) } }
    }

    fun onOpenAppSettings() {
        intent {
            permissions.openAppSettings()
            reduce { copy(photoSheet = null) }
        }
    }

    fun onPhotoErrorDismiss() {
        intent { reduce { copy(photoError = null) } }
    }

    fun onPhotoOpen(index: Int) {
        intent { reduce { copy(viewer = index) } }
    }

    fun onViewerIndex(index: Int) {
        intent { reduce { if (viewer != null) copy(viewer = index) else this } }
    }

    fun onViewerClose() {
        intent { reduce { copy(viewer = null, photoToDelete = null) } }
    }

    fun onPhotoCaptionChange(photoId: String, caption: String) {
        intent {
            val text = caption.replace('\n', ' ').take(MAX_CAPTION_LENGTH)
            reduce { copy(passport = passport.withCaption(photoId, text)) }
            launchReplacing(CAPTION + photoId) {
                delay(SAVE_DEBOUNCE_MS)
                val stored = text.trim().takeIf { it.isNotEmpty() }
                withContext(dispatchers.io) { repository.setPhotoCaption(passportId, photoId, stored) }
                    .onError { postSideEffect(PassportEditorEffect.PhotoFailed(PassportError.from(it))) }
            }
        }
    }

    fun onPhotoDeleteRequest(photoId: String) {
        intent { reduce { copy(photoToDelete = photoId) } }
    }

    fun onPhotoDeleteDismiss() {
        intent { reduce { copy(photoToDelete = null) } }
    }

    fun onPhotoDeleteConfirm() {
        intent {
            val photoId = currentState.photoToDelete ?: return@intent
            reduce { copy(photoToDelete = null) }
            when (val response = withContext(dispatchers.io) { repository.deletePhoto(passportId, photoId) }) {
                is Response.Success -> reduce {
                    val left = response.value.photos.size
                    copy(
                        passport = passport.replacedWith(response.value),
                        viewer = viewer?.takeIf { left > 0 }?.coerceAtMost(left - 1),
                    )
                }

                is Response.Error -> postSideEffect(
                    PassportEditorEffect.PhotoFailed(PassportError.from(response.exception)),
                )
            }
        }
    }

    fun onSaveSettingsFile() {
        intent {
            val passport = currentState.passport.content ?: return@intent
            val path = withContext(dispatchers.io) { repository.settingsFile(passportId) }
            if (path !is Response.Success) {
                postSideEffect(PassportEditorEffect.SettingsFileFailed)
                return@intent
            }
            val model = passport.settingsSnapshot?.deviceModel ?: passport.unit.systemName
            val title = passport.vehicleTitle() ?: passport.number
            when (val result = settingsFileSaver.save(path.value, settingsFileName(model, title))) {
                is SettingsFileSaveResult.Saved -> postSideEffect(PassportEditorEffect.SettingsFileSaved(result.name))
                SettingsFileSaveResult.Failed -> postSideEffect(PassportEditorEffect.SettingsFileFailed)
                SettingsFileSaveResult.Cancelled -> Unit
            }
        }
    }

    fun onSaveSettingsToConfigurations() {
        intent {
            val passport = currentState.passport.content ?: return@intent
            val model = passport.settingsSnapshot?.deviceModel ?: passport.unit.systemName
            val name = passport.vehicleTitle() ?: passport.number
            val response = withContext(dispatchers.io) {
                when (val path = repository.settingsFile(passportId)) {
                    is Response.Success -> configurations.saveSettings(name, model, path.value)
                    is Response.Error -> path
                }
            }
            when (response) {
                is Response.Success -> postSideEffect(PassportEditorEffect.ConfigurationSaved)

                is Response.Error ->
                    postSideEffect(PassportEditorEffect.ConfigurationFailed(PassportError.from(response.exception)))
            }
        }
    }

    private fun load() {
        launchReplacing(LOAD) {
            reduce { copy(passport = LCEState.Loading) }
            val response = withContext(dispatchers.io) { repository.get(passportId) }
            reduce {
                val loaded = response.asLCEState(PassportError::from)
                copy(passport = loaded, form = loaded.content?.toForm() ?: form, save = SaveStatus.SAVED)
            }
        }
    }

    private fun scheduleSave() {
        launchReplacing(SAVE) {
            delay(SAVE_DEBOUNCE_MS)
            save()
        }
    }

    private suspend fun save() = subIntent {
        val passport = currentState.passport.content ?: return@subIntent
        val edit = currentState.form.toEdit(passport)
        val response = withContext(dispatchers.io) { repository.edit(passportId, edit) }
        reduce {
            when (response) {
                is Response.Success -> copy(
                    passport = this.passport.replacedWith(response.value),
                    save = SaveStatus.SAVED,
                )

                is Response.Error -> copy(save = SaveStatus.FAILED)
            }
        }
    }

    private suspend fun pickPhoto(source: ImageSource) = subIntent {
        reduce { copy(photoSheet = null, photoError = null) }
        val image = when (val picked = imagePicker.pick(source)) {
            is ImagePickResult.Picked -> picked.image

            ImagePickResult.Cancelled -> return@subIntent

            ImagePickResult.Failed -> {
                reduce { copy(photoError = PassportError.Unknown) }
                return@subIntent
            }
        }
        reduce { copy(addingPhoto = true) }
        val response = addPhoto(image)
        reduce {
            when (response) {
                is Response.Success -> copy(passport = passport.replacedWith(response.value), addingPhoto = false)
                is Response.Error -> copy(photoError = PassportError.from(response.exception), addingPhoto = false)
            }
        }
    }

    private suspend fun addPhoto(image: PickedImage): Response<MountPassport> {
        val thumbnail = imagePicker.prepare(image, crop = null, maxSide = THUMBNAIL_SIDE)
        if (thumbnail !is ImagePrepareResult.Prepared) return Response.Error(IllegalStateException("Thumbnail failed"))
        return withContext(dispatchers.io) { repository.addPhoto(passportId, image.bytes, thumbnail.bytes) }
    }

    private companion object {
        const val LOAD = "load"
        const val SAVE = "save"
        const val CAPTION = "caption-"
        const val SAVE_DEBOUNCE_MS = 700L
        const val THUMBNAIL_SIDE = 320
        const val MAX_CAPTION_LENGTH = 60
    }
}
