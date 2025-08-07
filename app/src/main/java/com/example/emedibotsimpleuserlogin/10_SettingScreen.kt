package com.example.emedibotsimpleuserlogin

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogoutDone: () -> Unit
) {
    val context = LocalContext.current
    val account = GoogleSignIn.getLastSignedInAccount(context)
    val auth = FirebaseAuth.getInstance()

    var isNotificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        account?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(it.photoUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 8.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
                Column {
                    Text(text = it.displayName ?: "", fontWeight = FontWeight.Bold)
                    Text(text = it.email ?: "")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingToggle(
            label = "Dark Mode (experimental)",
            isChecked = isDarkMode,
            onCheckedChange = onToggleDarkMode
        )

        SettingToggle(
            label = "Enable Notifications",
            isChecked = isNotificationsEnabled,
            onCheckedChange = { isNotificationsEnabled = !isNotificationsEnabled }
        )

        Spacer(modifier = Modifier.height(24.dp))

        FilledTonalButton(
            onClick = {
                signOut(context) {
                    onLogoutDone()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun SettingToggle(label: String, isChecked: Boolean, onCheckedChange: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Switch(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() }
        )
    }
}


fun signOut(context: Context, onComplete: () -> Unit) {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    googleSignInClient.signOut().addOnCompleteListener {
        FirebaseAuth.getInstance().signOut()
        onComplete()
    }
}
