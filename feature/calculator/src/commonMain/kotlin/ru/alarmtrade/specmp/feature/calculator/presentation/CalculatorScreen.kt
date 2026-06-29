package ru.alarmtrade.specmp.feature.calculator.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.calculator.domain.model.CalculatorOperation
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import specmp.feature.calculator.generated.resources.Res
import specmp.feature.calculator.generated.resources.calculator_common_a11y_back
import specmp.feature.calculator.generated.resources.calculator_main_error_divzero
import specmp.feature.calculator.generated.resources.calculator_main_title
import specmp.feature.calculator.generated.resources.calculator_timer_btn_reset
import specmp.feature.calculator.generated.resources.calculator_timer_btn_start
import specmp.feature.calculator.generated.resources.calculator_timer_btn_stop

@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<CalculatorViewModel>()
    val state = viewModel.state.collectAsState()
    val colors = cmpThemeColors()
    val snackbarHostState = rememberCmpSnackbarHostState()

    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            CalculatorEffect.DivideByZero ->
                snackbarHostState.showSnackbar(getString(Res.string.calculator_main_error_divzero))
        }
    }

    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = {
                    CmpText(
                        text = stringResource(Res.string.calculator_main_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.calculator_common_a11y_back),
                    )
                },
            )
        },
        snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            TimerSection(
                state = state,
                colors = colors,
                onStart = viewModel::onTimerStart,
                onStop = viewModel::onTimerStop,
                onReset = viewModel::onTimerReset,
            )

            Spacer(modifier = Modifier.height(8.dp))

            CalculatorDisplay(state = state, colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            CalculatorGrid(
                colors = colors,
                onDigit = viewModel::onDigit,
                onOperation = viewModel::onOperation,
                onEqual = viewModel::onEqual,
                onClear = viewModel::onClear,
                onDelete = viewModel::onDelete,
                onDecimal = viewModel::onDecimal,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun TimerSection(
    state: State<CalculatorState>,
    colors: CmpThemeColors,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
) {
    val timerSeconds = state.value.timerSeconds
    val timerRunning = state.value.timerRunning
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpText(
            text = formatTimer(timerSeconds),
            fontSize = 36.sp,
            fontWeight = FontWeight.Light,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!timerRunning) {
                CmpClickableSurface(
                    onClick = onStart,
                    modifier = Modifier.height(40.dp).weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primaryContainer,
                    contentColor = colors.onPrimaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CmpText(text = stringResource(Res.string.calculator_timer_btn_start), fontSize = 14.sp)
                    }
                }
            }
            if (timerRunning) {
                CmpClickableSurface(
                    onClick = onStop,
                    modifier = Modifier.height(40.dp).weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CmpText(text = stringResource(Res.string.calculator_timer_btn_stop), fontSize = 14.sp)
                    }
                }
            }
            CmpClickableSurface(
                onClick = onReset,
                modifier = Modifier.height(40.dp).weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceVariant,
                contentColor = colors.onSurfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CmpText(text = stringResource(Res.string.calculator_timer_btn_reset), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun CalculatorDisplay(
    state: State<CalculatorState>,
    colors: CmpThemeColors,
) {
    val value = state.value
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.End
    ) {
        value.operation?.let { op ->
            CmpText(
                text = "${value.firstOperand?.let { formatNumber(it) } ?: ""} ${op.symbol}",
                fontSize = 16.sp,
                color = colors.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.End,
            )
        }
        val displayFontSize by remember { derivedStateOf { if (state.value.display.length > 9) 40.sp else 64.sp } }
        CmpText(
            text = value.display,
            fontSize = displayFontSize,
            fontWeight = FontWeight.Light,
            color = colors.onBackground,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CalculatorGrid(
    colors: CmpThemeColors,
    onDigit: (String) -> Unit,
    onOperation: (CalculatorOperation) -> Unit,
    onEqual: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onDecimal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcButton("C", colors.surfaceVariant, colors.onSurfaceVariant, Modifier.weight(1f), onClear)
            CalcButton("⌫", colors.surfaceVariant, colors.onSurfaceVariant, Modifier.weight(1f), onDelete)
            CalcButton("÷", colors.secondaryContainer, colors.onSecondaryContainer, Modifier.weight(1f)) { onOperation(CalculatorOperation.Divide) }
            CalcButton("×", colors.secondaryContainer, colors.onSecondaryContainer, Modifier.weight(1f)) { onOperation(CalculatorOperation.Multiply) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcButton("7", colors.surface, colors.onSurface, Modifier.weight(1f)) { onDigit("7") }
            CalcButton("8", colors.surface, colors.onSurface, Modifier.weight(1f)) { onDigit("8") }
            CalcButton("9", colors.surface, colors.onSurface, Modifier.weight(1f)) { onDigit("9") }
            CalcButton("-", colors.secondaryContainer, colors.onSecondaryContainer, Modifier.weight(1f)) { onOperation(CalculatorOperation.Subtract) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcButton("4", colors.surface, colors.onSurface, Modifier.weight(1f)) { onDigit("4") }
            CalcButton("5", colors.surface, colors.onSurface, Modifier.weight(1f)) { onDigit("5") }
            CalcButton("6", colors.surface, colors.onSurface, Modifier.weight(1f)) { onDigit("6") }
            CalcButton("+", colors.secondaryContainer, colors.onSecondaryContainer, Modifier.weight(1f)) { onOperation(CalculatorOperation.Add) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcButton("1", colors.surface, colors.onSurface, Modifier.weight(1f)) { onDigit("1") }
            CalcButton("2", colors.surface, colors.onSurface, Modifier.weight(1f)) { onDigit("2") }
            CalcButton("3", colors.surface, colors.onSurface, Modifier.weight(1f)) { onDigit("3") }
            CalcButton("=", colors.primaryContainer, colors.onPrimaryContainer, Modifier.weight(1f), onEqual)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcButton("0", colors.surface, colors.onSurface, Modifier.weight(2f)) { onDigit("0") }
            CalcButton(".", colors.surface, colors.onSurface, Modifier.weight(1f), onDecimal)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CmpClickableSurface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            CmpText(
                text = label,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

private fun formatTimer(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}
