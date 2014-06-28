package edu.bupt.netstat.pcap;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import edu.bupt.netstat.R;
import edu.bupt.netstat.R.raw;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * DumpHelper
 * 
 * @author zzz
 * 
 */
public class DumpHelper {
    private final static String TAG = "DumpHelper";
    public static final String fileOutPath = Environment
            .getExternalStorageDirectory().getPath() + "/";
    public static final String fileName = "capture.pcap";
    public static final String fileTcpdump = "/data/local/tcpdump";
    public static final String cmdTcpdump = fileTcpdump + " -p -vv -U -s 0 -w "
            + fileOutPath + fileName + "\n";

    private Context context;
    private String uid;
    private String localIP;
    private File captureFile;
    private Process rootProcess;
    private DataOutputStream os;
    private boolean capFlag = false;

    private HashSet<String> ipSet;

    /**
     * @author zzz
     * 
     */
    public DumpHelper(Context context) {
        this.context = context;
        this.localIP = getLocalIPAddress();
        this.uid = "";
        this.captureFile = new File(fileOutPath + fileName);
        if (captureFile.exists()) {
            captureFile.delete();
        }
        initTcpdump();
    }

    /**
     * @author zzz
     * 
     */
    public DumpHelper(Context context, String pkgname) {
        this.context = context;
        this.localIP = getLocalIPAddress();
        this.captureFile = new File(fileOutPath + fileName);
        setUidWithPkgName(pkgname);
        initTcpdump();
    }

    /**
     * @author xiang
     * 
     */
    public boolean setUidWithPkgName(String pkgName) {
        Log.i(TAG, "setUidWithPkgName - " + pkgName);
        final List<PackageInfo> packageInfos = context.getPackageManager()
                .getInstalledPackages(
                        PackageManager.GET_PERMISSIONS
                                | PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pi : packageInfos) {
            if (pi.packageName.contains(pkgName) && isNetworkNeeded(pi)) {
                this.uid = Integer.toString(pi.applicationInfo.uid);
                Log.i(TAG, "uid - " + this.uid);
            }
        }
        return true;
    }

    /**
     * @author xiang
     * 
     */
    public void updateIpSet() {
        if (localIP == null) {
            Log.w(TAG, "localIP == null");
            return;
        }
        String ip = null;
        ipSet = new HashSet<String>();
        ArrayList<Connection> connections = getConnections();
        for (Connection c : connections) {
            if (this.uid.equals(c.uid)) {
                ip = this.localIP.equals(c.src) ? c.dst : c.src;
                ipSet.add(ip);
            }
        }
        ipSet.remove("0.0.0.0");
        ipSet.remove("-1.-1.-1.-1");
        ipSet.remove("-2.-2.-2.-2");
    }

    /**
     * @author zzz
     * 
     */
    public HashSet<String> getIpSet() {
        return ipSet;
    }

    /**
     * @author zzz
     * 
     */
    public String getLocalIp() {
        return localIP;
    }

    /**
     * @author zzz
     * 
     */
    public long getCaptureFileLength() {
        return captureFile.length();
    }

    /**
     * @author xiang
     * 
     */
    public void startCapture() {
        Log.v(TAG, "startCapture");
        if (rootProcess == null && capFlag == false) {
            capFlag = true;
            if (this.localIP == null) {
                Toast.makeText(context, "Network is unavailable!",
                        Toast.LENGTH_LONG).show();
            }
            Log.i(TAG, "localIP - " + this.localIP);
            try {
                rootProcess = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(rootProcess.getOutputStream());
                os.writeBytes(cmdTcpdump);
                os.flush();
                Toast.makeText(context, "get stream...", Toast.LENGTH_LONG)
                        .show();
            } catch (IOException e) {
                Log.e("run", "io: " + e.getMessage());
            }
        }
    }

    /**
     * @author xiang
     * 
     */
    public void stopCapture() {
        Log.v(TAG, "stopCapture");
        if (rootProcess != null && capFlag == true) {
            capFlag = false;
            try {
                os.writeBytes("exit\n");
                os.close();
                // rootProcess.waitFor();
                os = null;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "failed to stop tcpdump!");
            } finally {
                rootProcess.destroy();
                rootProcess = null;
            }
        }
    }

    /**
     * @author xiang
     * 
     */
    private boolean isNetworkNeeded(PackageInfo pi) {
        String[] permissions = pi.requestedPermissions;
        if (permissions != null && permissions.length > 0) {
            for (String p : permissions) {
                if (p.equals("android.permission.INTERNET")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @author xiang
     * 
     */
    private ArrayList<Connection> getConnections() {
        ArrayList<Connection> connections = new ArrayList<Connection>();
        connections.addAll(getConnections("tcp"));
        connections.addAll(getConnections("udp"));
        connections.addAll(getConnections("tcp6"));
        connections.addAll(getConnections("udp6"));
        return connections;
    }

    /**
     * @author xiang
     * 
     */
    private ArrayList<Connection> getConnections(String type) {
        ArrayList<Connection> connections = new ArrayList<Connection>();
        try {
            String line;
            BufferedReader in;
            in = new BufferedReader(new FileReader("/proc/"
                    + android.os.Process.myPid() + "/net/" + type));
            while ((line = in.readLine()) != null) {
                if (line.trim().startsWith("sl")) {
                    continue;
                }
                Connection connection = new Connection(line, type);
                connections.add(connection);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connections;
    }

    /**
     * @author xiang
     * 
     */
    private String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> mEnumeration = NetworkInterface
                    .getNetworkInterfaces(); mEnumeration.hasMoreElements();) {
                NetworkInterface intf = mEnumeration.nextElement();
                for (Enumeration<InetAddress> enumIPAddr = intf
                        .getInetAddresses(); enumIPAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIPAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(inetAddress
                                    .getHostAddress())) {
                        Log.i(TAG,
                                "getLocalIPAddress - "
                                        + inetAddress.getHostAddress());
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Error", ex.toString());
        }
        return null;
    }

    /**
     * @author xiang
     * 
     */
    private void initTcpdump() {
        File file = new File(fileTcpdump);
        if (!file.exists()) {
            Log.v("tcpdump", "no tcpdump file");
            InputStream is = context.getResources().openRawResource(
                    R.raw.tcpdump);
            Log.v("tcpdump", "reading file from res/raw/ ...");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                Log.v("tcpdump", "writing file to /data/local/ ...");
                byte[] buffer = new byte[8192];
                int cnt = 0;
                while ((cnt = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, cnt);
                }
                Log.v("tcpdump", "file is created!");
                fos.close();
                is.close();
                Runtime.getRuntime().exec("su");
                Runtime.getRuntime().exec("chmod 755 " + fileTcpdump);
                Runtime.getRuntime().exec("exit");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
