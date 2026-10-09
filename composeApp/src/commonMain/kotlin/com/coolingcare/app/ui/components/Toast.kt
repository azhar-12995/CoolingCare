package com.coolingcare.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Problem
import com.coolingcare.app.data.problem
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.model.Txt
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

/** Short confirmation / error line at the bottom of the screen. */
object Toasts {
    data class Msg(val text: Txt, val error: Boolean, val at: Long)
    val current = MutableStateFlow<Msg?>(null)
    private var n = 0L
    fun show(text: Txt) { current.value = Msg(text, false, ++n) }
    fun error(text: Txt) { current.value = Msg(text, true, ++n) }
    fun error(e: Throwable) = error(e.problem().text())
}

fun Problem.text(): Txt = when (this) {
    Problem.OFFLINE -> S.errOffline
    Problem.INVALID_NUMBER -> S.errInvalidNumber
    Problem.INVALID_CODE -> S.errInvalidCode
    Problem.CODE_EXPIRED -> S.errCodeExpired
    Problem.TOO_MANY -> S.errTooMany
    Problem.SERVICE_CLOSED -> S.errServiceClosed
    Problem.NOT_ALLOWED -> S.errNotAllowed
    Problem.RELOGIN -> S.errRelogin
    Problem.OTHER -> S.errGeneric
}

@Composable
fun BoxScope.ToastHost() {
    val msg by Toasts.current.collectAsState()
    LaunchedEffect(msg?.at) { if (msg != null) { delay(if (msg!!.error) 4200 else 2600); Toasts.current.value = null } }
    AnimatedVisibility(msg != null, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 96.dp),
        enter = fadeIn() + slideInVertically { it / 2 }, exit = fadeOut() + slideOutVertically { it / 2 }) {
        val m = msg ?: return@AnimatedVisibility
        Row(
            Modifier.widthIn(max = 480.dp).shadow(12.dp, RoundedCornerShape(14.dp)).clip(RoundedCornerShape(14.dp)).background(if (m.error) CC.danger else CC.text).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
        ) {
            Ic(if (m.error) CcIcons.CircleAlert else CcIcons.CircleCheck, 18.dp, CC.onPrimary)
            T(m.text.s, type.bodyS, CC.onPrimary)
        }
    }
}

/** Placeholder while live data loads. */
@Composable
fun LoadingBox(modifier: Modifier = Modifier) = Box(modifier.padding(vertical = 60.dp), contentAlignment = Alignment.Center) { T(S.loading.s, type.bodyM, CC.text3) }
