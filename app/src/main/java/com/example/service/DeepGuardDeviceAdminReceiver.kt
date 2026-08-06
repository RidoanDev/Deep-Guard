package com.example.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DeepGuardDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "DeepGuard আনইনস্টল সুরক্ষা সক্রিয় হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "DeepGuard ডিভাইস অ্যাডমিন নিষ্ক্রিয় করলে টাইমার সুরক্ষা এবং আনইনস্টল ডিফেন্স বন্ধ হয়ে যাবে।"
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "DeepGuard ডিভাইস অ্যাডমিন নিষ্ক্রিয় হয়েছে।", Toast.LENGTH_SHORT).show()
    }
}
