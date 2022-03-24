/*
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ezteam.windowslauncher.utils.swiftp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ezteam.windowslauncher.R;
import com.ezteam.windowslauncher.utils.Config;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;


public class FTPServerService extends Service implements Runnable {
    protected static Thread serverThread = null;
    protected boolean shouldExit = false;

    public static final int BACKLOG = 21;
    public static final String WAKE_LOCK_TAG = "SwiFTP";

    //protected ServerSocketChannel wifiSocket;
    protected ServerSocket listenSocket;
    protected static WifiLock wifiLock = null;

    protected static List<String> sessionMonitor = new ArrayList<String>();
    protected static List<String> serverLog = new ArrayList<String>();
    protected static int uiLogLevel = Defaults.getUiLogLevel();

    // The server thread will check this often to look for incoming
    // connections. We are forced to use non-blocking accept() and polling
    // because we cannot wait forever in accept() if we want to be able
    // to receive an exit signal and cleanly exit.
    public static final int WAKE_INTERVAL_MS = 1000; // milliseconds

    protected static int port;
    protected static boolean acceptWifi;
    protected static boolean acceptNet;
    protected static boolean fullWake;

    private TcpListener wifiListener = null;
    private ProxyConnector proxyConnector = null;
    private List<SessionThread> sessionThreads = new ArrayList<SessionThread>();

    NotificationManager notificationMgr = null;
    PowerManager.WakeLock wakeLock;

    public FTPServerService() {
    }

    public IBinder onBind(Intent intent) {
        // We don't implement this functionality, so ignore it
        return null;
    }

    public void onCreate() {
        // Set the application-wide context global, if not already set
        Context myContext = Globals.getContext();
        if (myContext == null) {
            myContext = getApplicationContext();
            if (myContext != null) {
                Globals.setContext(myContext);
            }
        }
        return;
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        shouldExit = false;
        int attempts = 10;
        // The previous server thread may still be cleaning up, wait for it
        // to finish.
        while (serverThread != null) {
            if (attempts > 0) {
                attempts--;
                Util.sleepIgnoreInterupt(1000);
            } else {
                return;
            }
        }
        serverThread = new Thread(this);
        serverThread.start();

        // todo: we should broadcast an intent to inform anyone who cares
    }

    public static boolean isRunning() {
        // return true if and only if a server Thread is running
        if (serverThread == null) {
            return false;
        }
        if (!serverThread.isAlive()) {
        } else {
        }
        return true;
    }

    public void onDestroy() {
        shouldExit = true;
        if (serverThread == null) {
            return;
        } else {
            serverThread.interrupt();
            try {
                serverThread.join(10000);  // wait 10 sec for server thread to finish
            } catch (InterruptedException e) {
            }
            if (serverThread.isAlive()) {
                // it may still exit eventually if we just leave the
                // shouldExit flag set
            } else {
                serverThread = null;
            }
        }
        try {
            if (listenSocket != null) {
                listenSocket.close();
            }
        } catch (IOException e) {
        }

        UiUpdater.updateClients();
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }
        clearNotification();
    }

    private boolean loadSettings() {
        port = 2121;

        acceptNet = false;
        acceptWifi = true;
        fullWake = false;
        // The username, password, and chrootDir are just checked for sanity
        String username = "admin";
        String password = "123456";
        String chrootDir = "/storage/emulated/0";

        validateBlock:
        {
            File chrootDirAsFile = new File(chrootDir);
            if (!chrootDirAsFile.isDirectory()) {
                break validateBlock;
            }
            Globals.setChrootDir(chrootDirAsFile);
            Globals.setUsername(username);
            return true;
        }
        // We reach here if the settings were not sane
        return false;
    }

    // This opens a listening socket on all interfaces.
    void setupListener() throws IOException {
        listenSocket = new ServerSocket();
        listenSocket.setReuseAddress(true);
        listenSocket.bind(new InetSocketAddress(port));
    }

    private final BroadcastDisconnectFtp broadCastListener = new BroadcastDisconnectFtp();

    private void setupNotification() {
        Intent contentIntent = new Intent("action click notification");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 124, contentIntent, 0);
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "123")
                .setSmallIcon(R.drawable.ic_folder_ftp)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_text))
                .setTicker(getString(R.string.notif_server_starting))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());

        try {
            unregisterReceiver(broadCastListener);
        } catch (Exception e) {

        }
        broadCastListener.setDisconnectListener(unit -> {
            clearNotification();
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(it);
            stopSelf();
            return null;
        });
        IntentFilter intentFilterControl = new IntentFilter();
        intentFilterControl.addAction(Config.Notification.ActionClickNotification);
        registerReceiver(broadCastListener, intentFilterControl);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.start_ftp);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("123", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void clearNotification() {
        if (notificationMgr == null) {
            // Get NotificationManager reference
            String ns = Context.NOTIFICATION_SERVICE;
            notificationMgr = (NotificationManager) getSystemService(ns);
        }
        notificationMgr.cancelAll();
    }

    public void run() {
        // The UI will want to check the server status to update its
        // start/stop server button
        int consecutiveProxyStartFailures = 0;
        long proxyStartMillis = 0;

        UiUpdater.updateClients();


        // set our members according to user preferences
        if (!loadSettings()) {
            // loadSettings returns false if settings are not sane
            cleanupAndStopService();
            return;
        }


        // Initialization of wifi
        if (acceptWifi) {
            // If configured to accept connections via wifi, then set up the socket
            try {
                setupListener();
            } catch (IOException e) {
//				serverAddress = null;
                cleanupAndStopService();
                return;
            }
            takeWifiLock();
        }
        takeWakeLock();

        setupNotification();

        // We should update the UI now that we have a socket open, so the UI
        // can present the URL
        UiUpdater.updateClients();

        while (!shouldExit) {
            if (acceptWifi) {
                if (wifiListener != null) {
                    if (!wifiListener.isAlive()) {
                        try {
                            wifiListener.join();
                        } catch (InterruptedException e) {
                        }
                        wifiListener = null;
                    }
                }
                if (wifiListener == null) {
                    // Either our wifi listener hasn't been created yet, or has crashed,
                    // so spawn it
                    wifiListener = new TcpListener(listenSocket, this);
                    wifiListener.start();
                }
            }
            try {
                // todo: think about using ServerSocket, and just closing
                // the main socket to send an exit signal
                Thread.sleep(WAKE_INTERVAL_MS);
            } catch (InterruptedException e) {
            }
        }

        terminateAllSessions();

        if (proxyConnector != null) {
            proxyConnector.quit();
            proxyConnector = null;
        }
        if (wifiListener != null) {
            wifiListener.quit();
            wifiListener = null;
        }
        shouldExit = false; // we handled the exit flag, so reset it to acknowledge
        clearNotification();
        releaseWakeLock();
        releaseWifiLock();
    }

    private void terminateAllSessions() {
        synchronized (this) {
            for (SessionThread sessionThread : sessionThreads) {
                if (sessionThread != null) {
                    sessionThread.closeDataSocket();
                    sessionThread.closeSocket();
                }
            }
        }
    }

    public void cleanupAndStopService() {
        // Call the Android Service shutdown function
        Context context = getApplicationContext();
        Intent intent = new Intent(context, FTPServerService.class);
        context.stopService(intent);
        releaseWifiLock();
        releaseWakeLock();
        clearNotification();
    }

    @SuppressLint("InvalidWakeLockTag")
    private void takeWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            // Many (all?) devices seem to not properly honor a PARTIAL_WAKE_LOCK,
            // which should prevent CPU throttling. This has been
            // well-complained-about on android-developers.
            // For these devices, we have a config option to force the phone into a
            // full wake lock.
            if (fullWake) {
                wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                        WAKE_LOCK_TAG);
            } else {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        WAKE_LOCK_TAG);
            }
            wakeLock.setReferenceCounted(false);
        }
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        } else {
        }
    }

    private void takeWifiLock() {
        if (wifiLock == null) {
            WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiLock = manager.createWifiLock("SwiFTP");
            wifiLock.setReferenceCounted(false);
        }
        wifiLock.acquire();
    }

    private void releaseWifiLock() {
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }
    }

    public void errorShutdown() {
        cleanupAndStopService();
    }

    /**
     * Gets the IP address of the wifi connection.
     *
     * @return The integer IP address if wifi enabled, or null if not.
     */
    public static InetAddress getWifiIp() {
        Context myContext = Globals.getContext();
        if (myContext == null) {
            throw new NullPointerException("Global context is null");
        }
        WifiManager wifiMgr = (WifiManager) myContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (isWifiEnabled()) {
            int ipAsInt = wifiMgr.getConnectionInfo().getIpAddress();
            if (ipAsInt == 0) {
                return null;
            } else {
                return Util.intToInet(ipAsInt);
            }
        } else {
            return null;
        }
    }

    public static boolean isWifiEnabled() {
        Context myContext = Globals.getContext();
        if (myContext == null) {
            throw new NullPointerException("Global context is null");
        }
        WifiManager wifiMgr = (WifiManager) myContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            return true;
        } else {
            return false;
        }
    }

    public static List<String> getSessionMonitorContents() {
        return new ArrayList<String>(sessionMonitor);
    }

    public static List<String> getServerLogContents() {
        return new ArrayList<String>(serverLog);
    }

    public static void log(int msgLevel, String s) {
        serverLog.add(s);
        int maxSize = Defaults.getServerLogScrollBack();
        while (serverLog.size() > maxSize) {
            serverLog.remove(0);
        }
        //updateClients();
    }

    public static void updateClients() {
        UiUpdater.updateClients();
    }

    public static void writeMonitor(boolean incoming, String s) {
    }
//	public static void writeMonitor(boolean incoming, String s) {
//		if(incoming) {
//			s = "> " + s;
//		} else {
//			s = "< " + s;
//		}
//		sessionMonitor.add(s.trim());
//		int maxSize = Defaults.getSessionMonitorScrollBack();
//		while(sessionMonitor.size() > maxSize) {
//			sessionMonitor.remove(0);
//		}
//		updateClients();
//	}

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        FTPServerService.port = port;
    }

    /**
     * The FTPServerService must know about all running session threads so they
     * can be terminated on exit. Called when a new session is created.
     */
    public void registerSessionThread(SessionThread newSession) {
        // Before adding the new session thread, clean up any finished session
        // threads that are present in the list.

        // Since we're not allowed to modify the list while iterating over
        // it, we construct a list in toBeRemoved of threads to remove
        // later from the sessionThreads list.
        synchronized (this) {
            List<SessionThread> toBeRemoved = new ArrayList<SessionThread>();
            for (SessionThread sessionThread : sessionThreads) {
                if (!sessionThread.isAlive()) {
                    try {
                        sessionThread.join();
                        toBeRemoved.add(sessionThread);
                        sessionThread.closeSocket(); // make sure socket closed
                    } catch (InterruptedException e) {
                        // We will try again in the next loop iteration
                    }
                }
            }
            for (SessionThread removeThread : toBeRemoved) {
                sessionThreads.remove(removeThread);
            }

            // Cleanup is complete. Now actually add the new thread to the list.
            sessionThreads.add(newSession);
        }
    }

    /**
     * Get the ProxyConnector, may return null if proxying is disabled.
     */
    public ProxyConnector getProxyConnector() {
        return proxyConnector;
    }

}
