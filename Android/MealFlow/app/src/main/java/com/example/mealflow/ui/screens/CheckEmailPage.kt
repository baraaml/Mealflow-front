package com.example.mealflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mealflow.R

@Composable
fun CheckEmailPage(
    navController: NavController,
    email: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Email Icon
            Image(
                painter = painterResource(id = R.drawable.google_icon_icons_com_62736), // تأكد من وجود الأيقونة
                contentDescription = "Email Icon",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = stringResource(id = R.string.CheckYourEmail), // "تحقق من إيميلك"
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.sf_pro_rounded_heavy)),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = stringResource(id = R.string.ResetPasswordEmailSent), // "تم إرسال رابط إعادة تعيين كلمة المرور إلى إيميلك"
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.sfmed)),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (email.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = email,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.sfmed)),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Instructions
            Text(
                text = stringResource(id = R.string.CheckEmailInstructions), // "يرجى فتح إيميلك والضغط على الرابط لإعادة تعيين كلمة المرور"
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.sf_reg_ita)),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Back to Login Button
            Button(
                onClick = {
                    // العودة إلى شاشة تسجيل الدخول
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.BackToLogin), // "العودة لتسجيل الدخول"
                    fontFamily = FontFamily(Font(R.font.sfmed)),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resend Button (Optional)
            Button(
                onClick = {
                    navController.popBackStack() // العودة لصفحة نسيان كلمة المرور
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.ResendEmail), // "إعادة إرسال الإيميل"
                    fontFamily = FontFamily(Font(R.font.sfmed)),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun CheckEmailScreenPreview() {
    CheckEmailPage(
        navController = rememberNavController(),
        email = "user@example.com"
    )
}