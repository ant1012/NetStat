package edu.bupt.netstat.utils;

import java.io.DataOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Utils {
	private static final String TAG = "Utils";

	public static void clearCache(Context context, String pkgName) {
		Log.v(TAG, "clearCache");
		String cmdClearCacheInData = "rm -r /data/data/" + pkgName
				+ "/cache/*\n";
		String cmdClearCacheInSdcard = "rm -r /sdcard/Android/data/" + pkgName
				+ "/cache/*\n";

		try {
			Process rootProcess = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(
					rootProcess.getOutputStream());
			os.writeBytes(cmdClearCacheInData);
			os.writeBytes(cmdClearCacheInSdcard);
			os.flush();
			Toast.makeText(context, "clear cache", Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Log.e("run", "io: " + e.getMessage());
		}
	}
}
