package com.coolingcare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC

@Composable
private fun FieldLabel(label: String?, optional: String?) {
    if (label == null) return
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        T(label, type.labelM, CC.text)
        if (optional != null) T(optional, type.caption, CC.text3)
    }
}

@Composable
private fun FieldMessage(error: String?, help: String?) {
    if (error != null) Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) { Ic(CcIcons.CircleAlert, 14.dp, CC.danger); T(error, type.caption, CC.danger) }
    else if (help != null) T(help, type.caption, CC.text3)
}

private fun Modifier.box(focused: Boolean, error: Boolean, minHeight: Dp) = this.fillMaxWidth().defaultMinSize(minHeight = minHeight).clip(RoundedCornerShape(12.dp)).background(CC.surface)
    .border(if (focused || error) 1.5.dp else 1.dp, if (error) CC.danger else if (focused) CC.primary else CC.border, RoundedCornerShape(12.dp))

/** Editable text field styled like the design (label, leading icon, error / help line). */
@Composable
fun Field(
    value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, label: String? = null, placeholder: String = "", icon: ImageVector? = null,
    optional: String? = null, error: String? = null, help: String? = null, multiline: Boolean = false, keyboard: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences, ime: ImeAction = ImeAction.Next, trailing: (@Composable () -> Unit)? = null, latin: Boolean = false,
) {
    val src = remember { MutableInteractionSource() }
    val focused by src.collectIsFocusedAsState()
    val asLatin = latin || (type.latin != null && isLatinText(value))
    val base = if (asLatin) (type.latin?.bodyM ?: type.bodyM).copy(textDirection = TextDirection.Ltr) else type.bodyM
    val style = base.copy(color = CC.text)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label, optional)
        Row(
            Modifier.box(focused, error != null, if (multiline) 64.dp else 46.dp).padding(start = 14.dp, end = 12.dp, top = if (multiline) 10.dp else 0.dp, bottom = if (multiline) 10.dp else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically,
        ) {
            if (icon != null) Ic(icon, 18.dp, if (focused) CC.primary else CC.text2)
            BasicTextField(
                value, onChange, Modifier.weight(1f).padding(vertical = if (multiline) 0.dp else 12.dp), textStyle = style, singleLine = !multiline, minLines = if (multiline) 2 else 1, maxLines = if (multiline) 4 else 1,
                cursorBrush = SolidColor(CC.primary), interactionSource = src,
                keyboardOptions = KeyboardOptions(capitalization = capitalization, keyboardType = keyboard, imeAction = if (multiline) ImeAction.Default else ime),
                keyboardActions = KeyboardActions.Default,
                decorationBox = { inner -> Box { if (value.isEmpty()) T(placeholder, type.bodyM, CC.placeholder, maxLines = if (multiline) 3 else 1, latin = latin); inner() } },
            )
            trailing?.invoke()
        }
        FieldMessage(error, help)
    }
}

/** Read-only field that opens a picker (dropdowns, date, time slot). */
@Composable
fun SelectField(
    value: String?, placeholder: String, onClick: () -> Unit, modifier: Modifier = Modifier, label: String? = null, icon: ImageVector? = null,
    trailingIcon: ImageVector? = CcIcons.ChevronDown, error: String? = null, latin: Boolean = false,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label, null)
        Row(
            Modifier.box(false, error != null, 46.dp).tap(onClick = onClick).padding(start = 14.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) Ic(icon, 18.dp, CC.text2)
            T(value ?: placeholder, type.bodyM, if (value == null) CC.placeholder else CC.text, Modifier.weight(1f).padding(vertical = 12.dp), maxLines = 1, latin = latin)
            if (trailingIcon != null) Ic(trailingIcon, 18.dp, if (trailingIcon == CcIcons.Pencil) CC.primary else CC.text2)
        }
        FieldMessage(error, null)
    }
}

@Composable
fun SearchBox(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) =
    Field(value, onChange, modifier, placeholder = placeholder, icon = CcIcons.Search, ime = ImeAction.Search,
        trailing = if (value.isNotEmpty()) ({ IconBtn(CcIcons.X, { onChange("") }, size = 28.dp, iconSize = 16.dp, tint = CC.text3) }) else null)
