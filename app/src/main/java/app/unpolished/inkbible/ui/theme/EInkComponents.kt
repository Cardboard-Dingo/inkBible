package app.unpolished.inkbible.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EInkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 38.dp, max = 48.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EInkBlack,
            contentColor = EInkWhite,
            disabledContainerColor = EInkWhite,
            disabledContentColor = EInkBlack.copy(alpha = 0.5f)
        ),
        border = if (enabled) BorderStroke(2.dp, EInkBlack) else BorderStroke(1.dp, EInkBlack.copy(alpha = 0.5f)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = contentPadding
    ) {
        content()
    }
}

@Composable
fun EInkOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 38.dp, max = 48.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = EInkWhite,
            contentColor = EInkBlack,
            disabledContainerColor = EInkWhite,
            disabledContentColor = EInkBlack.copy(alpha = 0.5f)
        ),
        border = if (enabled) BorderStroke(2.dp, EInkBlack) else BorderStroke(1.dp, EInkBlack.copy(alpha = 0.5f)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = contentPadding
    ) {
        content()
    }
}

@Composable
fun EInkCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    borderWidth: Dp = 1.5.dp,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EInkBlack else EInkWhite,
            contentColor = if (isSelected) EInkWhite else EInkBlack
        ),
        border = BorderStroke(if (isSelected) 2.5.dp else borderWidth, EInkBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
fun EInkChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 6.dp,
    fontSize: TextUnit = 13.sp
) {
    Surface(
        modifier = modifier
            .heightIn(min = 36.dp, max = 42.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) EInkBlack else EInkWhite,
        contentColor = if (isSelected) EInkWhite else EInkBlack,
        border = BorderStroke(if (isSelected) 2.dp else 1.5.dp, EInkBlack)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
    }
}

@Composable
fun EInkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(EInkWhite, shape = RoundedCornerShape(4.dp))
            .border(2.dp, EInkBlack, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = TextStyle(
                    color = EInkBlack.copy(alpha = 0.6f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = EInkBlack,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(EInkBlack),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}

@Composable
fun EInkDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = EInkBlack
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}
