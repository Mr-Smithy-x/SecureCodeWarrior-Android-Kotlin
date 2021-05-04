package nyc.charlton.scw

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import nyc.charlton.scw.ui.login.LoginActivity
import java.io.IOException

class AppTamperingActivity : AppCompatActivity() {

    private val PACKAGE_NAME = "nyc.charlton.nyc"

    private val knownRootPackages = arrayOf(
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su"
    )

    private val knownRootCloakers = arrayOf(
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus",
        "de.robv.android.xposed.installer",
        "com.saurik.substrate",
        "com.zachspong.temprootremovejb",
        "com.amphoras.hidemyroot",
        "com.amphoras.hidemyrootadfree",
        "com.formyhm.hiderootPremium",
        "com.formyhm.hideroot"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isAppRooted() &&
            appNotTampered() &&
            isAppFromAStore()
            && appNotRunningInEmulator()
        ) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }

    private fun appNotTampered(): Boolean {
        if (packageName.compareTo(PACKAGE_NAME) != 0) {
            Toast.makeText(
                this, "This app has been tampered",
                Toast.LENGTH_LONG
            ).show()
            return false;
        }
        return true;
    }

    private fun isAppFromAStore(): Boolean {
        if (!BuildConfig.DEBUG) {
            val installer = packageManager.getInstallerPackageName(PACKAGE_NAME)

            if (installer == null) {
                Toast.makeText(
                    this, "App not from an app store",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }

            if (installer.contains("google") || installer.contains("amazon")) {
                return true
            }

            Toast.makeText(
                this, "App not from Play or Amazon stores",
                Toast.LENGTH_LONG
            ).show()
            return false;
        }
        return true
    }


    private fun isAppRooted(): Boolean {
        if (canExecuteCommand("su")) return true
        if (canExecuteCommand("busybox")) return true
        if (isPackageInstalled(knownRootPackages.toList())) return true
        return isPackageInstalled(knownRootCloakers.toList())
    }

    private fun canExecuteCommand(command: String): Boolean {
        return try {
            Runtime.getRuntime().exec(command)
            true
        } catch (localIOException: IOException) {
            //Can  not execute su
            false
        }
    }

    private fun isPackageInstalled(packages: List<String>): Boolean {
        val pm = packageManager
        for (p in packages) {
            try {
                // App detected
                pm.getPackageInfo(p, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                //Package not installed
            }
        }
        return false
    }

    private fun appNotRunningInEmulator(): Boolean {
        if (isEmulator()) {
            Toast.makeText(
                this, "This app can't run on an Emulator",
                Toast.LENGTH_LONG
            ).show()
            return false;
        }
        return true;
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || "goldfish".equals(Build.HARDWARE)
    }


}