package devtros.zohaib.dpc

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.messaging.FirebaseMessaging
import devtros.zohaib.dpc.ui.theme.DPCTheme

class MainActivity : ComponentActivity() {

    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //request for permissions
        requestNeededPermissions()


//        val extras = intent?.extras
//        val serverUrl = extras?.getString("server_url")
//        val enrollToken = extras?.getString("enrollment_token")
//        val deviceSerial: String =
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                Build.getSerial()
//            } else {
//                "PermissionNotGranted"
//            }

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val isOwner = if (dpm.isDeviceOwnerApp(packageName)) "Yes, I am Device Owner" else "No, not Device Owner"


        // Register this device with your Firebase backend:
//        postDeviceRegistration(serverUrl, enrollToken, deviceSerial)

        //Factory reset protection (owner only can do factory reset)
        //DPM APIs let your DPC set FRP accounts and other policies. (Device owner only.)
        val admin = ComponentName(this, DPCReceiver::class.java)
        val frpPolicy = FactoryResetProtectionPolicy.Builder()
            .setFactoryResetProtectionAccounts(listOf("admin@company.com"))
            .build()
        try {
//            dpm.setFactoryResetProtectionPolicy(admin, frpPolicy)
        } catch (e: UnsupportedOperationException) {
            Log.w("DPC", "FRP policy not supported on this device")
        }

        enableEdgeToEdge()
        setContent {
            DPCTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private val requestPermissionLauncher =
        this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                println("Notification permission granted")
            } else {
                println("Notification permission denied")
            }
        }

    // Call this when you need to request permissions
    private fun requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires POST_NOTIFICATIONS
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // Request READ_PHONE_STATE for all versions that need it
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            return
        }

        // ✅ If we reach here, all required permissions are already granted
    }

}



@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var fcmToken by remember { mutableStateOf("Tap to generate token") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = fcmToken, modifier = Modifier.padding(bottom = 16.dp))

        Button(onClick = {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    fcmToken = token
                    clipboardManager.setText(AnnotatedString(token))
                    Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to get token", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("Generate & Copy FCM Token")
        }
    }
}
