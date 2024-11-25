package com.pkmkcub.spectragrow.view.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.view.ui.auth.AuthViewModel

@Composable
fun OnboardingScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ob_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.testTag("OnboardingPager")
        ) { pageIndex ->
            OnboardingPage(
                pageIndex,
                pagerState.currentPage == 2
            )
        }
        if (pagerState.currentPage == 2) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    elevation = CardDefaults.elevatedCardElevation(20.dp),
                    colors = CardDefaults.cardColors(colorResource(id = R.color.white))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 30.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.handleGoogleAuth(
                                    context = context,
                                    navCallback = {
                                        navController.navigate("home")
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(horizontal = 20.dp),
                            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.white)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.google),
                                    contentDescription = "Google",
                                    modifier = Modifier.padding(end = 8.dp),
                                    tint = Color.Black
                                )
                                Text(
                                    text = stringResource(id = R.string.btn_google),
                                    fontFamily = FontFamily(Font(R.font.bold)),
                                    color = Color.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {navController.navigate("register")},
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .padding(start = 20.dp),
                                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.white_base)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.reg_btn),
                                    fontFamily = FontFamily(Font(R.font.semibold)),
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = {navController.navigate("login")},
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .padding(end = 20.dp)
                                    .testTag("LoginButton"),
                                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.white_base)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.log_btn),
                                    fontFamily = FontFamily(Font(R.font.semibold)),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(pageIndex: Int, isLastPage: Boolean) {
    val title = when (pageIndex) {
        0 -> stringResource(id = R.string.title_ob1)
        1 -> stringResource(id = R.string.title_ob2)
        else -> stringResource(id = R.string.title_ob3)
    }

    val subtitle = when (pageIndex) {
        0 -> stringResource(id = R.string.subtitle_ob1)
        1 -> stringResource(id = R.string.subtitle_ob2)
        else -> stringResource(id = R.string.subtitle_ob3)
    }

    val image = when (pageIndex) {
        0 -> painterResource(id = R.drawable.ob1)
        1 -> painterResource(id = R.drawable.ob2)
        else -> painterResource(id = R.drawable.ob3)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .padding(bottom = if (isLastPage) 200.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = image, contentDescription = null)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            color = colorResource(id = R.color.black),
            fontFamily = FontFamily(Font(R.font.bold)),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = colorResource(id = R.color.black),
            fontFamily = FontFamily(Font(R.font.semibold)),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
    }
}