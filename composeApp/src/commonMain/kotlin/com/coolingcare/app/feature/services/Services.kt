package com.coolingcare.app.feature.services

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coolingcare.app.data.Catalog
import com.coolingcare.app.i18n.S
import com.coolingcare.app.i18n.s
import com.coolingcare.app.model.Service
import com.coolingcare.app.ui.components.AppBar
import com.coolingcare.app.ui.components.Btn
import com.coolingcare.app.ui.components.CheckCircle
import com.coolingcare.app.ui.components.Chip
import com.coolingcare.app.ui.components.Footer
import com.coolingcare.app.ui.components.HDivider
import com.coolingcare.app.ui.components.Ic
import com.coolingcare.app.ui.components.IconBtn
import com.coolingcare.app.ui.components.Img
import com.coolingcare.app.ui.components.Note
import com.coolingcare.app.ui.components.Pill
import com.coolingcare.app.ui.components.Screen
import com.coolingcare.app.ui.components.SearchBox
import com.coolingcare.app.ui.components.T
import com.coolingcare.app.ui.components.tap
import com.coolingcare.app.ui.components.type
import com.coolingcare.app.ui.icons.CcIcons
import com.coolingcare.app.ui.theme.CC
import com.coolingcare.app.ui.theme.LocalLang
import com.coolingcare.app.data.Graph
import com.coolingcare.app.ui.components.ServiceHeader
import com.coolingcare.app.ui.components.ServiceThumb
import androidx.compose.runtime.collectAsState

@Composable
fun ServiceListThumb(s: Service, w: Int = 96, h: Int = 76) = ServiceThumb(s, Modifier.size(w.dp, h.dp).clip(RoundedCornerShape(12.dp)))

/** 20–22 · Services list with category chips + search. */
@Composable
fun ServicesTab(filter: String, onFilter: (String) -> Unit, onService: (String) -> Unit) {
    var searching by rememberSaveable { mutableStateOf(false) }
    var q by rememberSaveable { mutableStateOf("") }
    val lang = LocalLang.current
    val services by Graph.cloud.services.collectAsState()
    val list = services.filter { it.active }.filter { (filter == "all" || it.category == filter) && (q.isBlank() || it.title.get(lang).contains(q.trim(), true) || it.title.en.contains(q.trim(), true) || it.short.get(lang).contains(q.trim(), true)) }
    Screen(
        top = {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    T(S.ourServices.s, type.titleL, modifier = Modifier.weight(1f))
                    IconBtn(if (searching) CcIcons.X else CcIcons.Search, { searching = !searching; if (!searching) q = "" }, tint = CC.primary, bg = CC.primaryTint, size = 38.dp, iconSize = 19.dp, description = S.searchServices.s)
                }
                T(S.chooseService.s, type.bodyS, CC.text2)
                if (searching) Box(Modifier.padding(top = 8.dp)) { SearchBox(q, { q = it }, S.searchServices.s) }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(S.all.s, filter == "all", { onFilter("all") }, height = 34.dp)
                for (c in Catalog.categories) Chip(c.name.s, filter == c.id, { onFilter(c.id) }, height = 34.dp)
            }
        },
        gap = 0.dp, bodyPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 12.dp),
    ) {
        if (list.isEmpty()) T(S.noResults.s, type.bodyM, CC.text2, Modifier.padding(vertical = 24.dp))
        for (s in list) {
            Row(Modifier.fillMaxWidth().tap { onService(s.id) }.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                ServiceListThumb(s)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    T(s.list.s, type.titleS)
                    T(s.short.s, type.bodyS, CC.text2, maxLines = 2)
                    Pill(S.inspectionQuote.s, Modifier.fillMaxWidth(), icon = CcIcons.Tag, radius = 8.dp, padding = PaddingValues(horizontal = 10.dp, vertical = 5.dp))
                }
                Ic(CcIcons.ChevronRight, 18.dp, CC.text3)
            }
            HDivider()
        }
        if (filter != "all" && filter != "ac" || q.isNotBlank()) Box(Modifier.padding(top = 12.dp)) {
            Note(CcIcons.Phone, S.notListed.s, Modifier.tap { Graph.launcher.whatsApp(com.coolingcare.app.domain.Messages.support(null, lang)) })
        }
    }
}

/** 23 · Service Details — one template for every service (content from catalog.json). */
@Composable
fun ServiceDetailScreen(id: String, onBack: () -> Unit, onBook: () -> Unit) {
    val services by Graph.cloud.services.collectAsState()
    val s = services.firstOrNull { it.id == id && it.active }
    if (s == null) {
        Screen(top = { AppBar(S.serviceDetails.s, onBack) }) { Note(CcIcons.CircleAlert, S.serviceClosedNote.s, Modifier.tap { Graph.launcher.dial() }, bg = CC.warningTint, tint = CC.warning) }
        return
    }
    Screen(
        top = { AppBar(S.serviceDetails.s, onBack) },
        footer = { Footer { Btn(S.bookVisit.s, onBook, Modifier.fillMaxWidth(), arrow = true) } },
        bodyPadding = PaddingValues(0.dp), gap = 0.dp,
    ) {
        ServiceHeader(s, Modifier.fillMaxWidth().height(180.dp))
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { T(s.title.s, type.displayM); T(s.about.s, type.bodyM, CC.text2) }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CC.primaryTint).padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) { CheckCircle(24.dp); T(S.whatsIncluded.s, type.titleS) }
                for (it in s.included) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Ic(CcIcons.Check, 18.dp, CC.green, Modifier.padding(top = 1.dp)); T(it.s, type.bodyS, modifier = Modifier.weight(1f)) }
            }
            Column {
                Row(Modifier.padding(horizontal = 4.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Ic(CcIcons.Clock, 20.dp, CC.text2); T(S.estimatedDuration.s, type.bodyM, CC.text2, Modifier.weight(1f)); T(s.duration.s, type.titleS)
                }
                HDivider()
                Row(Modifier.padding(horizontal = 4.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Ic(CcIcons.Settings, 20.dp, CC.text2); T(S.partsRepairs.s, type.bodyM, CC.text2); T(S.quotedSeparately.s, type.bodyS, CC.text2, Modifier.weight(1f), align = TextAlign.End)
                }
            }
        }
    }
}
