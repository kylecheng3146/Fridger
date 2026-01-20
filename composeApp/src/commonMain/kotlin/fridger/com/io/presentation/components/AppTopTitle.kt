package fridger.com.io.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fridger.com.io.ui.theme.sizing
import fridger.com.io.ui.theme.spacing

@Composable
fun AppTopTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    top = MaterialTheme.sizing.contentPaddingTop,
                    bottom = MaterialTheme.spacing.extraSmall,
                    start = MaterialTheme.sizing.contentPaddingHorizontal,
                    end = MaterialTheme.sizing.contentPaddingHorizontal
                )
    ) {
        Text(
            text = title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
