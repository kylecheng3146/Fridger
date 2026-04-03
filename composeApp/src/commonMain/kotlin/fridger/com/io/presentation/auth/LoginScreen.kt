package fridger.com.io.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import fridger.com.io.presentation.settings.components.LocalGoogleSignInLauncher
import fridger.com.io.presentation.settings.components.LocalGoogleSignInStatus
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.login_bg
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen() {
    val launcher = LocalGoogleSignInLauncher.current
    val status = LocalGoogleSignInStatus.current

    val cardColor = Color(0xFFFDFBF7)
    val accent = Color(0xFF2E3A2E)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE6E0D7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("歡迎回來", style = MaterialTheme.typography.titleLarge, color = accent)
                    }
                    Text(
                        "使用 Google 帳號登入以同步你的清單與設定。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A4A4A)
                    )

                    Spacer(Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = { launcher?.invoke() },
                        enabled = launcher != null && status?.isSigningIn != true,
                        border = BorderStroke(1.dp, Color(0xFFD7D1C8)),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        GoogleIcon()
                        Spacer(Modifier.width(10.dp))
                        Text(if (status?.isSigningIn == true) "登入中…" else "使用 Google 登入")
                    }

                    status?.errorMessage?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    status?.successMessage?.let { message ->
                        Text(message, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleIcon() {
    val blue = Color(0xFF4285F4)
    val red = Color(0xFFEA4335)
    val yellow = Color(0xFFFBBC05)
    val green = Color(0xFF34A853)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Dot(red)
        Spacer(Modifier.width(2.dp))
        Dot(blue)
        Spacer(Modifier.width(2.dp))
        Dot(yellow)
        Spacer(Modifier.width(2.dp))
        Dot(green)
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier.size(6.dp).background(color, shape = RoundedCornerShape(50))
    )
}
