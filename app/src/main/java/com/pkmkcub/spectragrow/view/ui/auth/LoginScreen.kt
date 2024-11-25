package com.pkmkcub.spectragrow.view.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pkmkcub.spectragrow.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(nav2: NavController, viewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.white_base))
    ) {
        Image(
            painter = painterResource(id = R.drawable.ob_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.login_fragment_title),
                fontSize = 26.sp,
                color = colorResource(id = R.color.black),
                fontFamily = FontFamily(Font(R.font.bold))
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = stringResource(id = R.string.email_form), fontFamily = FontFamily(Font(R.font.semibold))) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.white_base), shape = RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = stringResource(id = R.string.password_form), fontFamily = FontFamily(Font(R.font.semibold))) },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.white_base), shape = RoundedCornerShape(8.dp))
            )

            // Forgot password button and logic
            TextButton(
                onClick = {
                    if (email.isEmpty()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Field email cannot be empty")
                        }
                    } else {
                        viewModel.forgotPassword(email) { success ->
                            coroutineScope.launch {
                                val message = if (success) {
                                    "Password change request successfully sent to your registered email."
                                } else {
                                    "Password change request failed."
                                }
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
            ) {
                Text(text = stringResource(id = R.string.forgot_pw), color = colorResource(id = R.color.black), fontFamily = FontFamily(Font(R.font.semibold)))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    Modifier.testTag("TermsCheckbox"),
                    colors = CheckboxDefaults.colors(checkedColor = colorResource(id = R.color.yellow_pattern))
                )
                Text(
                    text = stringResource(id = R.string.snk),
                    color = colorResource(id = R.color.black),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.semibold))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        email.isEmpty() || password.isEmpty() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Field cannot be empty")
                            }
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Incorrect email format")
                            }
                        }
                        password.length < 6 -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Password must be at least 6 characters")
                            }
                        }
                        !isChecked -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please accept the terms and conditions")
                            }
                        }
                        else -> {
                            isLoading = true
                            viewModel.login(email, password) { success ->
                                isLoading = false
                                coroutineScope.launch {
                                    if (success) {
                                        snackbarHostState.showSnackbar("Login successful")
                                        nav2.navigate("home")
                                    } else {
                                        snackbarHostState.showSnackbar("Email or password is incorrect")
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("LoginSubmitButton"),
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.yellow_pattern)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(text = stringResource(id = R.string.log_btn), color = colorResource(id = R.color.white))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.dont_have_an_account),
                    color = colorResource(id = R.color.black),
                    fontFamily = FontFamily(Font(R.font.semibold))
                )
                TextButton(onClick = { nav2.navigate("register") }) {
                    Text(text = stringResource(id = R.string.tv_register), color = colorResource(id = R.color.black), fontFamily = FontFamily(Font(R.font.bold)))
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White,
                contentColor = Color.Black,
                actionColor = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}
