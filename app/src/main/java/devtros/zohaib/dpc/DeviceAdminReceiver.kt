package devtros.zohaib.dpc

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity

class DPCReceiver : DeviceAdminReceiver() {
    private val TAG = "DeviceOwnerReceiver"

    @Override
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val manager = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context.applicationContext, DPCReceiver::class.java)

        manager.setProfileName(componentName, context.getString(R.string.app_name))

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @Override
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // TODO update composable UI message to "App is device owner"
        Log.d(TAG, "App is device owner")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        return super.onDisableRequested(context, intent)
        // TODO - this doesn't seem to be getting called when a seemingly successful
        // `adb shell dpm remove-active-admin co.coburglabs.mdm/.DeviceOwnerReceiver`
        // command is issued. See logcat. Could be a "testOnly" flag issue?
    }
}
