package com.pkmkcub.spectragrow.view.ui.detailsitem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.pkmkcub.spectragrow.R

@Composable
fun DetailsItemScreen() {
    var phVisible by remember { mutableStateOf(false) }
    var natriumVisible by remember { mutableStateOf(false) }
    var fosfatVisible by remember { mutableStateOf(false) }
    var kaliumVisible by remember { mutableStateOf(false) }
    var kaVisible by remember { mutableStateOf(false) }
    var ttVisible by remember { mutableStateOf(false) }
    var sklVisible by remember { mutableStateOf(false) }
    var tempVisible by remember { mutableStateOf(false) }
    var highVisible by remember { mutableStateOf(false) }

    Image(
        painter = painterResource(id = R.drawable.ob_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AccordionCard(
            title = stringResource(id = R.string.ph),
            content = stringResource(id = R.string.ph_details),
            isVisible = phVisible,
            onToggle = { phVisible = !phVisible }
        )

        AccordionCard(
            title = stringResource(id = R.string.natrium),
            content = stringResource(id = R.string.n_details),
            isVisible = natriumVisible,
            onToggle = { natriumVisible = !natriumVisible }
        )

        AccordionCard(
            title = stringResource(id = R.string.fosfor),
            content = stringResource(id = R.string.p_details),
            isVisible = fosfatVisible,
            onToggle = { fosfatVisible = !fosfatVisible }
        )

        AccordionCard(
            title = stringResource(id = R.string.kalium),
            content = stringResource(id = R.string.k_details),
            isVisible = kaliumVisible,
            onToggle = { kaliumVisible = !kaliumVisible }
        )

        AccordionCard(
            title = stringResource(id = R.string.air),
            content = stringResource(id = R.string.ka_details),
            isVisible = kaVisible,
            onToggle = { kaVisible = !kaVisible }
        )

        AccordionCard(
            title = stringResource(id = R.string.tekstur_tanah),
            content = stringResource(id = R.string.tt_details),
            isVisible = ttVisible,
            onToggle = { ttVisible = !ttVisible }
        )

        AccordionCard(
            title = stringResource(id = R.string.skl),
            content = stringResource(id = R.string.skl_details),
            isVisible = sklVisible,
            onToggle = { sklVisible = !sklVisible }
        )

        AccordionCard(
            title = stringResource(id = R.string.temp),
            content = stringResource(id = R.string.temp_details),
            isVisible = tempVisible,
            onToggle = { tempVisible = !tempVisible }
        )

        AccordionCard(
            title = stringResource(id = R.string.high),
            content = stringResource(id = R.string.high_details),
            isVisible = highVisible,
            onToggle = { highVisible = !highVisible }
        )
    }
}

@Composable
fun AccordionCard(title: String, content: String, isVisible: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(5.dp),
        colors = CardDefaults.cardColors(colorResource(id = R.color.white))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                modifier = Modifier.padding(bottom = 8.dp),
                color = colorResource(id = R.color.black),
                fontFamily = FontFamily(Font(R.font.semibold)))
            AnimatedVisibility(visible = isVisible) {
                Text(text = content)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isVisible) "Hide" else "Show",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onToggle() },
                color = colorResource(id = R.color.black),
                fontFamily = FontFamily(
                    Font(R.font.medium))
            )
        }
    }
}
