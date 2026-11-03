package com.pawsmedic.shared.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

private const val LOGO_CROSS_PATH = "M56.875 10.9375C56.875 4.9 61.775 0 67.8125 0H107.188C113.225 0 118.125 4.9 118.125 10.9375V56.875H164.062C170.1 56.875 175 61.775 175 67.8125V107.188C175 113.225 170.1 118.125 164.062 118.125H118.125V164.062C118.125 170.1 113.225 175 107.188 175H67.8125C61.775 175 56.875 170.1 56.875 164.062V118.125H10.9375C4.9 118.125 0 113.225 0 107.188V67.8125C0 61.775 4.9 56.875 10.9375 56.875H56.875V10.9375Z"
private const val LOGO_SHIELD_BG_PATH = "M29 40.5765C68.0109 17.1412 107.344 17.1412 147 40.5765V85.8418C147 120.513 125.238 145.554 88 160C50.2787 145.554 29 120.513 29 85.8418V40.5765Z"
private const val LOGO_SHIELD_OUTLINE_PATH = "M31 42.0633C68.6885 19.3122 106.689 19.3122 145 42.0633V86.007C145 119.666 123.975 143.975 88 158C51.5574 143.975 31 119.666 31 86.007V42.0633Z"
private const val LOGO_TOE_1 = "M83.5917 55.2809C84.7633 62.6775 81.1653 69.394 75.5554 70.2825C69.9456 71.171 64.4482 65.8951 63.2767 58.4985C62.1052 51.1019 65.7032 44.3854 71.313 43.4969C76.9229 42.6084 82.4202 47.8843 83.5917 55.2809Z"
private const val LOGO_TOE_2 = "M113.451 58.8498C112.023 66.1974 106.345 71.2753 100.77 70.1915C95.1943 69.1078 91.8324 62.2728 93.2606 54.9252C94.6889 47.5775 100.366 42.4996 105.942 43.5834C111.517 44.6671 114.879 51.5021 113.451 58.8498Z"
private const val LOGO_TOE_3 = "M61.49 75.4364C64.1162 81.335 62.804 87.6488 58.5593 89.5387C54.3145 91.4286 48.7444 88.1789 46.1182 82.2804C43.492 76.3818 44.8041 70.068 49.0489 68.1781C53.2937 66.2882 58.8638 69.5379 61.49 75.4364Z"
private const val LOGO_TOE_4 = "M131.095 81.9505C128.368 87.799 122.741 90.9472 118.527 88.9821C114.313 87.0171 113.107 80.683 115.835 74.8345C118.562 68.986 124.189 65.8378 128.403 67.8029C132.617 69.7679 133.822 76.102 131.095 81.9505Z"
private const val LOGO_MAIN_PAD = "M64.5535 98.4945C71.5661 91.0143 72.0336 76.5212 87.9289 76.5212C101.954 76.5212 103.824 90.5467 111.304 98.962C118.317 105.975 126.265 114.858 118.784 124.675C112.239 134.026 100.552 127.948 91.669 126.078C84.1888 124.208 77.1762 128.883 69.2286 129.351C58.9434 130.286 53.3333 123.273 55.2034 113.923C56.1384 108.78 60.3459 103.637 64.5535 98.4945Z"

@Composable
fun PawMedicLogoIcon(
    size: Dp = 120.dp,
    containerColor: Color = PawMedicColors.Teal600,
    pawColor: Color = PawMedicColors.White,
    modifier: Modifier = Modifier,
    animated: Boolean = false,
    onAnimationEnd: () -> Unit = {}
) {
    val animationProgress = remember { Animatable(if (animated) 0f else 1f) }

    if (animated) {
        LaunchedEffect(Unit) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing)
            )
            onAnimationEnd()
        }
    }

    val crossPath = remember { PathParser().parsePathString(LOGO_CROSS_PATH).toPath().apply { fillType = PathFillType.EvenOdd } }
    val shieldBgPath = remember { PathParser().parsePathString(LOGO_SHIELD_BG_PATH).toPath() }
    val shieldOutlinePath = remember { PathParser().parsePathString(LOGO_SHIELD_OUTLINE_PATH).toPath() }
    val toe1Path = remember { PathParser().parsePathString(LOGO_TOE_1).toPath() }
    val toe2Path = remember { PathParser().parsePathString(LOGO_TOE_2).toPath() }
    val toe3Path = remember { PathParser().parsePathString(LOGO_TOE_3).toPath() }
    val toe4Path = remember { PathParser().parsePathString(LOGO_TOE_4).toPath() }
    val mainPadPath = remember { PathParser().parsePathString(LOGO_MAIN_PAD).toPath() }

    val greenCrossColor = Color(0xFF28AA80)
    val tealGradient = remember { Brush.linearGradient(colors = listOf(Color(0xFF30B5D8), Color(0xFF28AA80))) }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val progress = animationProgress.value
            val scaleFactor = this.size.width / 175f
            val pivotPoint = center

            // Phase 1: Green Cross background
            val crossProgress = (progress / 0.4f).coerceIn(0f, 1f)
            if (crossProgress > 0f) {
                withTransform({
                    scale(scaleFactor * (0.3f + 0.7f * crossProgress), scaleFactor * (0.3f + 0.7f * crossProgress), pivotPoint)
                }) {
                    drawPath(crossPath, color = greenCrossColor.copy(alpha = crossProgress))
                }
            }

            // Phase 2: White Shield
            val shieldProgress = ((progress - 0.25f) / 0.45f).coerceIn(0f, 1f)
            if (shieldProgress > 0f) {
                withTransform({
                    scale(scaleFactor * (0.5f + 0.5f * shieldProgress), scaleFactor * (0.5f + 0.5f * shieldProgress), pivotPoint)
                }) {
                    drawPath(shieldBgPath, color = Color.White.copy(alpha = shieldProgress))
                    drawPath(shieldOutlinePath, color = Color.White.copy(alpha = shieldProgress))
                    drawPath(shieldOutlinePath, brush = tealGradient, alpha = shieldProgress, style = Stroke(width = 5.61f))
                }
            }

            // Phase 3: Paw Elements
            val pawProgress = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)
            if (pawProgress > 0f) {
                withTransform({
                    scale(scaleFactor * (0.4f + 0.6f * pawProgress), scaleFactor * (0.4f + 0.6f * pawProgress), pivotPoint)
                }) {
                    listOf(toe1Path, toe2Path, toe3Path, toe4Path, mainPadPath).forEach { path ->
                        drawPath(path, brush = tealGradient, alpha = pawProgress, style = Stroke(width = 5.61f))
                    }
                }
            }
        }
    }
}

@Composable
fun PawMedicPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = PawMedicColors.Teal600,
    contentColor: Color = PawMedicColors.White
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = contentColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PawMedicSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = PawMedicColors.Gray300,
    textColor: Color = PawMedicColors.Gray700
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = textColor
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun GoogleSocialButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String = "Continuar con Google"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(PawMedicColors.Gray300)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = PawMedicColors.White,
            contentColor = PawMedicColors.Gray800
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = PawMedicColors.Teal600,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "G",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4285F4)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PawMedicColors.Gray800
                )
            }
        }
    }
}

@Composable
fun PawMedicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PawMedicColors.Gray800,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = PawMedicColors.Gray400,
                    fontSize = 14.sp
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            modifier = inputModifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PawMedicColors.Teal600,
                unfocusedBorderColor = PawMedicColors.Gray300,
                errorBorderColor = PawMedicColors.Error,
                focusedContainerColor = PawMedicColors.White,
                unfocusedContainerColor = PawMedicColors.White,
                focusedTextColor = PawMedicColors.Gray900,
                unfocusedTextColor = PawMedicColors.Gray900
            )
        )
        AnimatedVisibility(visible = isError && errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = PawMedicColors.Error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun PawMedicTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PawMedicColors.Gray100)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "‹",
                    fontSize = 28.sp,
                    color = PawMedicColors.Gray800,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray900
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = PawMedicColors.Gray500
            )
        }
    }
}
