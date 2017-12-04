package com.kikatech.voice.util.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.UUID;

public class DeviceUtils {
    private static final String TAG = "DeviceUtils";
    private static final String KIKA_FONT_PREFIX = "kikafont_";
    private static final int BITMAP_WIDTH = 50;
    private static final int BITMAP_HEIGHT = 50;
    private static String sDeviceId;
    private static String sGAId;
    private static long sTotalMemory = -1;
    private static long sAppTotalMemory = -1;
    private static String sCpuName = null;
    private static long sCpuMaxFreq = -1;
    private static long sCpuMinFreq = -1;
    private static long sLastTotalCpuTime = -1;
    private static long sLastIdleCpuTime = -1;
    private static long sLastAppCpuTime = -1;
    private static long sLastTotalCpuTimeDiff = -1;

//    /**
//     * 获取手机的基本信息
//     */
//    public static final String getDeviceBaseInfo() {
//        //BOARD 主板
//        String phoneInfo = "BOARD: " + Build.BOARD;
//        phoneInfo += "\nBOOTLOADER: " + Build.BOOTLOADER;
//        //BRAND 运营商
//        phoneInfo += "\nBRAND: " + Build.BRAND;
//        //DEVICE 驱动
//        phoneInfo += "\nDEVICE: " + Build.DEVICE;
//        //DISPLAY 显示
//        phoneInfo += "\nDISPLAY: " + Build.DISPLAY;
//        //指纹
//        phoneInfo += "\nFINGERPRINT: " + Build.FINGERPRINT;
//        //HARDWARE 硬件
//        phoneInfo += "\nHARDWARE: " + Build.HARDWARE;
//        phoneInfo += "\nHOST: " + Build.HOST;
//        phoneInfo += "\nID: " + Build.ID;
//        //MANUFACTURER 生产厂家
//        phoneInfo += "\nMANUFACTURER: " + Build.MANUFACTURER;
//        //MODEL 机型
//        phoneInfo += "\nMODEL: " + Build.MODEL;
//        phoneInfo += "\nPRODUCT: " + Build.PRODUCT;
//        phoneInfo += "\nRADITAGSO: " + Build.TAGS;
//        phoneInfo += "\nTIME: " + Build.TIME;
//        phoneInfo += "\nTYPE: " + Build.TYPE;
//        phoneInfo += "\nUSER: " + Build.USER;
//        //VERSION.RELEASE 固件版本
//        phoneInfo += "\nVERSION.RELEASE: " + Build.VERSION.RELEASE;
//        phoneInfo += "\nVERSION.CODENAME: " + Build.VERSION.CODENAME;
//        //VERSION.INCREMENTAL 基带版本
//        phoneInfo += "\nVERSION.INCREMENTAL: " + Build.VERSION.INCREMENTAL;
//        //VERSION.SDK SDK版本
//        phoneInfo += "\nVERSION.SDK_INT: " + Build.VERSION.SDK_INT;
//        return phoneInfo;
//    }
//
//    /**
//     * 网络是否已经连接
//     *
//     * @param context
//     * @return
//     */
//    public static boolean isNetworkConnected(Context context) {
//        if (context == null) {
//            return false;
//        }
//        ConnectivityManager manager = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkinfo = null;
//        try {
//            networkinfo = manager.getActiveNetworkInfo();
//        } catch (Exception e) {
//            LogUtils.error(e);
//        }
//
//        boolean isConnected =  networkinfo != null && networkinfo.isConnected();
//        if (LogUtils.verbose(TAG)) {
//            Log.v(TAG, "is network connected?" + isConnected);
//        }
//        return isConnected;
//    }
//
//    /**
//     * 获取网络连接类型
//     */
//    public static int getNetworkConnectedType(Context context) {
//        if (context != null) {
//            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
//            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
//                return mNetworkInfo.getType();
//            }
//        }
//        return -1;
//    }
//
//    public static String getNetworkConnectedTypeName(Context context) {
//        if (context != null) {
//            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
//            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
//                return mNetworkInfo.getTypeName();
//            }
//        }
//        return "";
//    }

    private static final String PREF_DEVICE_ID = "pref_device_id";
    /**
     * 获取手机UID
     */
    public synchronized static String getUID(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (TextUtils.isEmpty(sDeviceId)) {

            sDeviceId = preferences.getString(PREF_DEVICE_ID, "");
        }
        if (TextUtils.isEmpty(sDeviceId)) {
            sDeviceId = UUID.randomUUID().toString().replace("-", "");
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString(PREF_DEVICE_ID, sDeviceId);
            edit.apply();
        }
        return sDeviceId;
    }
//
//    public synchronized static String getSavedGAID(Context context) {
//        if (TextUtils.isEmpty(sGAId)) {
//            sGAId = SharedPreferencesUtils.getString(context, SharedPreferencesUtils.PREF_GA_ID, "");
//        }
//        return sGAId;
//    }
//
//    public synchronized static String getGAID(final Context context) {
//        if (TextUtils.isEmpty(sGAId)) {
//            new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
//                        if (adInfo != null) {
//                            String id = adInfo.getId();
//                            if (id != null) {
//                                sGAId = id;
//                                Agent.setGAId(id, context);
//                                SharedPreferencesUtils.setString(context, SharedPreferencesUtils.PREF_GA_ID, id);
//                            }
//                        }
//                    } catch (Throwable e) {
//                    }
//                }
//            }.start();
//        }
//        return sGAId;
//    }
//
//    public static boolean isLGnotNexus() {
//        return Build.MANUFACTURER.toLowerCase().startsWith("lg")
//                && !Build.MODEL.toLowerCase().startsWith("nexus");
//    }
//
//    public static boolean isHTC() {
//        return Build.MANUFACTURER.toLowerCase().startsWith("htc");
//    }
//
//    /**
//     * 判断手机是否是三星
//     */
//    public static boolean isSAMSUNG() {
//        return Build.MANUFACTURER.toLowerCase().startsWith("samsung") && !Build.MODEL.toLowerCase().startsWith("nexus");
//    }
//
//    public static boolean isShowEmojiFont() {
//        return isSAMSUNG() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
//    }
//
//
//    /**
//     * 判断手机是否使用了EmojiFont
//     */
//    public static boolean isUsingEmojiFont() {
//        File file = new File("/data/data/com.android.settings/app_fonts/sans.loc");
//        InputStreamReader read = null;
//        try {
//            if (file.isFile() && file.exists()) { //判断文件是否存在
//                read = new InputStreamReader(
//                        new FileInputStream(file));//考虑到编码格式
//                BufferedReader bufferedReader = new BufferedReader(read);
//                String lineTxt;
//                boolean re = false;
//                while ((lineTxt = bufferedReader.readLine()) != null) {
//                    // for EmojiOne, lineTxt is /data/data/com.android.settings/app_fonts/kikafont_emoji_one#EmojiOne
//                    re = lineTxt.endsWith("coolemoji#Emojifont")
//                            || lineTxt.indexOf(KIKA_FONT_PREFIX) != -1;
//                    if (re) {
//                        break;
//                    }
//                }
//                return re;
//            } else {
//                return false;
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            FileUtils2.closeQuietly(read);
//        }
//        return false;
//    }
//
//    public static boolean canShowUnicodeEightEmoji() {
//        return canShowUnicodeEightEmoji("\uD83E\uDDC0");
//    }
//
//    public static boolean canShowUnicode9Emoji() {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
//            return false;
//        }
//        // 黑人boy
//        return new Paint().hasGlyph("\uD83D\uDC66\uD83C\uDFFF");
//    }
//
//    public static boolean canShowUnicodeEightEmoji(String code) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//            return false;
//        }
//        return isSupportGlyph(code);
//    }
//
//    public static boolean supportLanguageFont(String text) {
//        if (TextUtils.isEmpty(text)) {
//            return false;
//        }
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return !isCharacterMissingInFont(String.valueOf(text.charAt(0)));
//        } else {
//            return new Paint().hasGlyph(String.valueOf(text.charAt(0)));
//        }
//    }
//
//    private static Bitmap drawBitmap(String text) {
//        Paint paint = new Paint();
//        paint.setColor(Color.BLACK);
//        Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ALPHA_8);
//        Canvas c = new Canvas(b);
//        c.drawText(text, 0, BITMAP_HEIGHT / 2, paint);
//        return b;
//    }
//
//    private static byte[] getPixels(Bitmap b) {
//        ByteBuffer buffer = ByteBuffer.allocate(b.getByteCount());
//        b.copyPixelsToBuffer(buffer);
//        return buffer.array();
//    }
//
//    private static boolean isCharacterMissingInFont(String ch) {
//        String missingCharacter = "\uFFFE";
//        byte[] b1 = getPixels(drawBitmap(ch));
//        byte[] b2 = getPixels(drawBitmap(missingCharacter));
//        return Arrays.equals(b1, b2);
//    }
//
//    public static boolean isSupportGlyph(String glyph) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return glyphSupport(glyph);
//        } else {
//            return new Paint().hasGlyph(glyph);
//        }
//    }
//
//    private static boolean glyphSupport(String glyph) {
//        if (TextUtils.isEmpty(glyph)) {
//            return false;
//        }
//        TextPaint paint = new TextPaint();
//        float sampleWidth = Layout.getDesiredWidth(glyph, paint);
//        float tofuWidth = Layout.getDesiredWidth("\uFFFE", paint);
//        return sampleWidth > 0 && sampleWidth != tofuWidth;
//    }
//
//    public static int getScreenHeight(Context context) {
//        DisplayMetrics metric = new DisplayMetrics();
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        wm.getDefaultDisplay().getMetrics(metric);
//        return metric.heightPixels;
//    }
//
//    public static int getScreenWidth(Context context) {
//        DisplayMetrics metric = new DisplayMetrics();
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        wm.getDefaultDisplay().getMetrics(metric);
//        return metric.widthPixels;
//    }
//
//    public static int getStatusBarHeight(View view) {
//        Rect r = new Rect();
//        view.getWindowVisibleDisplayFrame(r);
//        return r.top;
//    }
//
//    public static boolean verifyApk(Context context, String packageName) {
//        if (context == null || TextUtils.isEmpty(packageName)) {
//            return false;
//        }
//        PackageManager pm = context.getPackageManager();
//        try {
//            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);
//            if (pInfo == null) {
//                return false;
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            return false;
//        }
//        return true;
//    }
//
//    @RequiresPermission(Manifest.permission.GET_ACCOUNTS)
//    public static List<String> getDeviceUserEmail(@NonNull Context context) {
//        List<String> emailList = new ArrayList<>();
//        try {
//            if (ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS)
//                    == PackageManager.PERMISSION_GRANTED) {
//                Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
//                Account[] accounts = AccountManager.get(context).getAccounts();
//                for (Account account : accounts) {
//                    if (BuildConfig.DEBUG) {
//                        Log.d("Accounts", account.toString());
//                    }
//                    if (emailPattern.matcher(account.name).matches()) {
//                        emailList.add(account.name);
//                    }
//                }
//            }
//        }catch (Exception e){
//            LogUtils.error(TAG, e, true);
//        }
//        return emailList;
//    }
//
//    public static long getSystemTotalMemory() {
//        if (sTotalMemory == -1) {
//            String memInfoPath = "/proc/meminfo";
//            FileReader fileReader = null;
//            try {
//                fileReader = new FileReader(memInfoPath);
//                BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
//                String[] arrayOfString = bufferedReader.readLine().split("\\s+");
//                sTotalMemory = Long.parseLong(arrayOfString[1]);
//            } catch (IOException e) {
//            } finally {
//                if (fileReader != null) {
//                    try {
//                        fileReader.close();
//                    } catch (IOException e) {
//                    }
//                }
//            }
//        }
//        return sTotalMemory;
//    }
//
//    public static long getSystemAvailMemory() {
//        ActivityManager am = (ActivityManager) ApplicationHelper.getContext().getSystemService(Context.ACTIVITY_SERVICE);
//        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
//        am.getMemoryInfo(mi);
//        return mi.availMem;
//    }
//
//    public static long getAppTotalMemory() {
//        if (sAppTotalMemory == -1) {
//            sAppTotalMemory = Runtime.getRuntime().maxMemory();
//        }
//        return sAppTotalMemory;
//    }
//
//    public static long getAppUsedMemory() {
//        return Runtime.getRuntime().totalMemory();
//    }
//
//    public static long getAppFreeMemory() {
//        return Runtime.getRuntime().freeMemory();
//    }
//
//    public static String getCpuName() {
//        if (sCpuName == null) {
//            String cpuInfoPath = "/proc/cpuinfo";
//            String lineStr = "";
//            FileReader fileReader = null;
//            try {
//                fileReader = new FileReader(cpuInfoPath);
//                BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
//                while ((lineStr = bufferedReader.readLine()) != null) {
//                    if (lineStr.contains("Hardware")) {
//                        sCpuName = lineStr.split(":")[1];
//                        break;
//                    }
//                }
//                if (sCpuName == null) {
//                    sCpuName = "No Name";
//                }
//            } catch (IOException e) {
//            } finally {
//                if (fileReader != null) {
//                    try {
//                        fileReader.close();
//                    } catch (IOException e) {
//                    }
//                }
//            }
//        }
//        return sCpuName;
//    }
//
//    public static long getCpuMaxFreq() {
//        if (sCpuMaxFreq == -1) {
//            FileReader fileReader = null;
//            try {
//                fileReader = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
//                BufferedReader bufferedReader = new BufferedReader(fileReader);
//                sCpuMaxFreq = Long.parseLong(bufferedReader.readLine().trim());
//            } catch (Exception ex) {
//            } finally {
//                if (fileReader != null) {
//                    try {
//                        fileReader.close();
//                    } catch (IOException e) {
//                    }
//                }
//            }
//        }
//        return sCpuMaxFreq;
//    }
//
//    public static long getCpuMinFreq() {
//        if (sCpuMinFreq == -1) {
//            FileReader fileReader = null;
//            try {
//                fileReader = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq");
//                BufferedReader bufferedReader = new BufferedReader(fileReader);
//                sCpuMinFreq = Long.parseLong(bufferedReader.readLine().trim());
//            } catch (Exception ex) {
//            } finally {
//                if (fileReader != null) {
//                    try {
//                        fileReader.close();
//                    } catch (IOException e) {
//                    }
//                }
//            }
//        }
//        return sCpuMinFreq;
//    }
//
//    public static long getCpuCurFreq() {
//        long cpuCurFreq = -1;
//        FileReader fileReader = null;
//        try {
//            fileReader = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
//            BufferedReader bufferedReader = new BufferedReader(fileReader);
//            cpuCurFreq = Long.parseLong(bufferedReader.readLine().trim());
//        } catch (IOException ex) {
//        } finally {
//            if (fileReader != null) {
//                try {
//                    fileReader.close();
//                } catch (IOException e) {
//                }
//            }
//        }
//        return cpuCurFreq;
//    }
//
//    public static int getTotalCpuRate() {
//        String cpuStatPath = "/proc/stat";
//        FileReader fileReader = null;
//        String[] cpuInfos = null;
//        try {
//            fileReader = new FileReader(cpuStatPath);
//            BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
//            cpuInfos = bufferedReader.readLine().split("\\s+");
//        } catch (IOException e) {
//        } finally {
//            if (fileReader != null) {
//                try {
//                    fileReader.close();
//                } catch (IOException e) {
//                }
//            }
//        }
//        long totalCpuTime = 0;
//        long idleCpuTime = 0;
//        if (cpuInfos != null) {
//            try {
//                totalCpuTime = Long.parseLong(cpuInfos[1])
//                        + Long.parseLong(cpuInfos[2]) + Long.parseLong(cpuInfos[3])
//                        + Long.parseLong(cpuInfos[4]) + Long.parseLong(cpuInfos[5])
//                        + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[7]);
//                idleCpuTime = Long.parseLong(cpuInfos[4]);
//            } catch (Exception e) {
//            }
//        }
//        if (sLastTotalCpuTime == -1 || sLastIdleCpuTime == -1) {
//            sLastTotalCpuTime = totalCpuTime;
//            sLastIdleCpuTime = idleCpuTime;
//            return -1;
//        }
//        long totalCpuTimeDiff = totalCpuTime - sLastTotalCpuTime;
//        if (totalCpuTimeDiff <= 0) {
//            sLastTotalCpuTime = totalCpuTime;
//            sLastIdleCpuTime = idleCpuTime;
//            return -1;
//        }
//        long idleCpuTimeDiff = idleCpuTime - sLastIdleCpuTime;
//        if (idleCpuTimeDiff <= 0) {
//            sLastTotalCpuTime = totalCpuTime;
//            sLastIdleCpuTime = idleCpuTime;
//            return -1;
//        }
//        float totalCpuRate = (totalCpuTimeDiff - idleCpuTimeDiff) / (float) totalCpuTimeDiff;
//        sLastTotalCpuTime = totalCpuTime;
//        sLastIdleCpuTime = idleCpuTime;
//        sLastTotalCpuTimeDiff = totalCpuTimeDiff;
//        int retVal = (int)(totalCpuRate * 10000);
//        if (retVal < 0) {
//            return -1;
//        }
//        return retVal;
//    }
//
//    /**
//     * it can only be used after {@link #getTotalCpuRate()}
//     */
//    public static int getAppCpuRate() {
//        FileReader fileReader = null;
//        String[] cpuInfos = null;
//        try {
//            int pid = android.os.Process.myPid();
//            fileReader = new FileReader("/proc/" + pid + "/stat");
//            BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
//            cpuInfos = bufferedReader.readLine().split("\\s+");
//        } catch (IOException e) {
//        } finally {
//            if (fileReader != null) {
//                try {
//                    fileReader.close();
//                } catch (IOException e) {
//                }
//            }
//        }
//        long appCpuTime = 0;
//        if (cpuInfos != null) {
//            try {
//                appCpuTime = Long.parseLong(cpuInfos[13])
//                        + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
//                        + Long.parseLong(cpuInfos[16]);
//            } catch (Exception e) {
//            }
//        }
//        if (sLastAppCpuTime == -1) {
//            sLastAppCpuTime = appCpuTime;
//            return -1;
//        }
//        if (sLastTotalCpuTimeDiff <= 0) {
//            sLastAppCpuTime = appCpuTime;
//            return -1;
//        }
//        long appCpuTimeDiff = appCpuTime - sLastAppCpuTime;
//        if (appCpuTimeDiff <= 0) {
//            return -1;
//        }
//        float appCpuRate = (appCpuTime - sLastAppCpuTime) / (float) sLastTotalCpuTimeDiff;
//        sLastAppCpuTime = appCpuTime;
//        int retVal = (int)(appCpuRate * 10000);
//        if (retVal < 0) {
//            return -1;
//        }
//        return retVal;
//    }
//
//
//    /**
//     * Koala Ad id 规则：只允许由字母、数字组成（此处因id目前均为小写字母，所以统一使用小写），不能有空格、其它符号等字符
//     *
//     * @return
//     */
//    private static String getModelInfoPostfixForKoalaAd() {
//        try {
//            // 表示非小写字母、数字
//            final String regex = "[^a-z0-9]";
//
//            String modelName = Build.MODEL.toLowerCase();
//            Pattern pattern = Pattern.compile(regex);
//            Matcher matcher = pattern.matcher(modelName);
//            // 将所有非小写字母、数字的字符删掉
//            return "_" + matcher.replaceAll("");
//        } catch (Exception e) {
//            return "";
//        }
//    }
//
//    public static String getKoalaAdIdWithModelInfo(String id) {
//        return id + getModelInfoPostfixForKoalaAd();
//    }
//
//    public static String getAndroidId(Context context) {
//        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//    }
}