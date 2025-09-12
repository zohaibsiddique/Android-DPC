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
import devtros.zohaib.dpc.ui.theme.DPCTheme

class MainActivity : ComponentActivity() {
    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent?.extras
        val serverUrl = extras?.getString("server_url")
        val enrollToken = extras?.getString("enrollment_token")
        val deviceSerial: String =
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                Build.getSerial()
            } else {
                "PermissionNotGranted"
            }

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
            dpm.setFactoryResetProtectionPolicy(admin, frpPolicy)
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