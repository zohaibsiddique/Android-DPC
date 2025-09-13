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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.google.firebase.messaging.FirebaseMessaging
import devtros.zohaib.dpc.ui.theme.DPCTheme

class MainActivity : ComponentActivity() {

    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //request for permissions
        requestNeededPermissions()

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", "Current token: $token")
                } else {
                    Log.w("FCM", "Fetching FCM token failed", task.exception)
                }
            }


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
                    Greeting(
                        name = isOwner,
                        modifier = Modifier.padding(innerPadding)
                    )
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
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DPCTheme {
        Greeting("Android")
    }
}