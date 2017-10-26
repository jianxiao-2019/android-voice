#include "ToJava.h"
#include "PortDefs.h"
#include <pthread.h>
#include <signal.h>
#include <stdarg.h>
#include <sys/time.h>
#include <math.h>
#include "AEException.h"

#include <libusb/libusb.h>

static JavaVM *gJavaVM = NULL;
static bool isAttached = false;
static jclass s_progressClass = NULL;

static std::string s_logFileName;
static std::string s_allErrorLogs;
std::string s_packageName = "";


std::string getPackageName()
{
    return s_packageName;
}


void getGlobalRef(JNIEnv* i_env, const char* clazz, jclass* globalClass)
{
    JNIEnv* env = GetEnv();

    jclass local = i_env->FindClass(clazz);
    if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
    {
        env->ExceptionClear();
        return;
    } 
    
    if (local)
    {
      *globalClass = (jclass) env->NewGlobalRef(local);
      env->DeleteLocalRef(local);
    }
}


static struct sigaction old_sa[NSIG];

void android_sigaction(int signal, siginfo_t *info, void *reserved)
{
    JNIEnv* env = GetEnv();
    if (env)
    {
        std::string ProgressString = s_packageName + "/Progress";

        if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
        {
            wxLogErrorMain("exception in android_sigaction find");
            env->ExceptionClear();
        }

        if (s_progressClass != 0)
        {
            jmethodID method = env->GetStaticMethodID(s_progressClass, "nativeCrashed", "()V");
            
            if (method != 0)
            {
                //logIt("Found openProgressWindow!");
                env->CallStaticVoidMethod(s_progressClass, method);
                //logIt("Called openProgressWindow!\n");
            }
            else
            {
                logIt("Did NOT find nativeCrashed!");
            }
        }
    }

    old_sa[signal].sa_handler(signal);
    //wxLogErrorMain("Crashed in native code! Signal = %d, code = %d, errno = %d, signo = %d", signal, info->si_code, info->si_errno, info->si_signo);
}


jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv *env;
    gJavaVM = vm;

    s_progressClass = NULL;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
    {
        logIt("Failed to get the environment using GetEnv()");
        return -1;
    }

    struct sigaction handler;
    memset(&handler, 0, sizeof(struct sigaction));
    handler.sa_sigaction = android_sigaction;
    handler.sa_flags = SA_RESETHAND;
#define CATCHSIG(X) sigaction(X, &handler, &old_sa[X])
    CATCHSIG(SIGILL);
    CATCHSIG(SIGABRT);
    CATCHSIG(SIGBUS);
    CATCHSIG(SIGFPE);
    CATCHSIG(SIGSEGV);
    CATCHSIG(SIGSTKFLT);
    CATCHSIG(SIGPIPE);

    s_allErrorLogs = "";

    return JNI_VERSION_1_4;
}


void removeSignalHandler()
{
    struct sigaction handler;
    memset(&handler, 0, sizeof(struct sigaction));
    handler.sa_sigaction = android_sigaction;
    handler.sa_flags = SA_RESETHAND;
#define CATCHSIG2(X) sigaction(X, &old_sa[X], NULL)
    CATCHSIG2(SIGILL);
    CATCHSIG2(SIGABRT);
    CATCHSIG2(SIGBUS);
    CATCHSIG2(SIGFPE);
    CATCHSIG2(SIGSEGV);
    CATCHSIG2(SIGSTKFLT);
    CATCHSIG2(SIGPIPE);
}


JavaVM *getJavaVM()
{
    return gJavaVM;
}


static pthread_key_t current_env_key;

static void detach(void *i_env)
{
    if (gJavaVM)
    {
        JNIEnv *env;

        int status = gJavaVM->GetEnv((void **) &env, JNI_VERSION_1_4);

        if (env && env->ExceptionOccurred())
        {
            wxLogErrorMain("exception still occurred in detach");
            env->ExceptionClear();
            wxLogErrorMain("exception still occurred in detach cleared");
        }

        gJavaVM->DetachCurrentThread();
    }
}


JNIEnv* GetEnv()
{
    int status;
    JNIEnv *env;

    if ((env = (JNIEnv *) pthread_getspecific(current_env_key)) == NULL)
    {
        //wxLogDebugMain("getspec ret null, create one");
        status = gJavaVM->GetEnv((void **) &env, JNI_VERSION_1_4);

        if (status != JNI_OK)
        {
            //wxLogDebugMain("callback_handler: failed to get JNI environment, assuming native thread");
            status = gJavaVM->AttachCurrentThread(&env, NULL);
            if (status < 0)
            {
                logIt("callback_handler: failed to attach current thread");
                return NULL;
            }
            else
            {
                pthread_key_create(&current_env_key, detach);
                pthread_setspecific(current_env_key, env);
                //wxLogDebugMain("created key, tid = %d", gettid());
            }

            isAttached = true;
        }
    }
    else
    {
        //wxLogDebugMain("Cached key!");
    }

    return env;
}


void DoMessage(const std::string &i_Message)
{
	jmethodID method;
    JNIEnv* env = GetEnv();

    if (env)
    {
        if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
        {
            wxLogErrorMain("exception before DoMessage find");
            env->ExceptionClear();
            return;
        }

        if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
        {
            wxLogErrorMain("exception in DoMessage find");
            env->ExceptionDescribe();
            env->ExceptionClear();
            return;
        }

        if (s_progressClass != 0)
        {
            method = env->GetStaticMethodID(s_progressClass, "showMessage", "(Ljava/lang/String;)V");
            
            if (method != 0)
            {
                jstring str = env->NewStringUTF(i_Message.c_str());  
                env->CallStaticVoidMethod(s_progressClass, method, str);
                env->DeleteLocalRef(str);
            }
            else
            {
                logIt("Did NOT find showMessage!");
            }

            if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
            {
                wxLogErrorMain("exception after DoMessage");
                env->ExceptionClear();
                return;
            }
        }
    }
    else
    {
        logIt("env is NULL!");
    }
}


void DoMessageShort(const std::string &i_Message)
{
	jmethodID method;
    JNIEnv* env = GetEnv();

    if (env)
    {
        if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
        {
            wxLogErrorMain("exception occurred in DoMessageShort: FindClass");
            env->ExceptionClear();
            return;
        }

        if (s_progressClass != 0)
        {
            method = env->GetStaticMethodID(s_progressClass, "showMessageShort", "(Ljava/lang/String;)V");
            
            if (method != 0)
            {
                jstring str = env->NewStringUTF(i_Message.c_str());  
                env->CallStaticVoidMethod(s_progressClass, method, str);
                env->DeleteLocalRef(str);
            }
            else
            {
                logIt("Did NOT find showMessageShort!");
            }
        }
    }
    else
    {
        logIt("env is NULL!");
    }
}


void DoMessageShortLocalized(int i_messageID)
{
	jmethodID method;
    JNIEnv* env = GetEnv();

    if (env)
    {
        if (s_progressClass != 0)
        {
            method = env->GetStaticMethodID(s_progressClass, "showMessageShortLocalized", "(I)V");
            
            if (method != 0)
            {
                env->CallStaticVoidMethod(s_progressClass, method, i_messageID);
            }
            else
            {
                logIt("Did NOT find DoMessageShortLocalized!");
            }
        }
    }
    else
    {
        logIt("env is NULL!");
    }
}


void DoMessage2(const std::string &i_Message)
{
	jmethodID method;
    JNIEnv* env = GetEnv();

    if (env)
    {
        if (s_progressClass != 0)
        {
            method = env->GetStaticMethodID(s_progressClass, "showMessage2", "(Ljava/lang/String;)V");
            
            if (method != 0)
            {
                jstring str = env->NewStringUTF(i_Message.c_str());  
                env->CallStaticVoidMethod(s_progressClass, method, str);
                env->DeleteLocalRef(str);
            }
            else
            {
                logIt("Did NOT find showMessage2!");
            }
        }
    }
    else
    {
        logIt("env is NULL!");
    }
}


void DoStopJava(const std::string &i_Message)
{
	jmethodID method;
    JNIEnv* env = GetEnv();
    //gJavaVM->AttachCurrentThread(&env, NULL);

    if (env)
    {
        if (s_progressClass != 0)
        {
            method = env->GetStaticMethodID(s_progressClass, "stopJava", "(Ljava/lang/String;)V");
            
            if (method != 0)
            {
                jstring str = env->NewStringUTF(i_Message.c_str());  
                env->CallStaticVoidMethod(s_progressClass, method, str);
                env->DeleteLocalRef(str);
            }
            else
            {
                logIt("Did NOT find stopJava!");
            }
        }
        else
        {
            wxLogErrorMain("Did not find progress class!");
        }

        //gJavaVM->DetachCurrentThread();
    }
    else
    {
        logIt("env is NULL!");
    }
}


void DoStopJava2(int i_messageNr)
{
	jmethodID method;
    JNIEnv* env = GetEnv();
    //gJavaVM->AttachCurrentThread(&env, NULL);

    if (env)
    {
        if (s_progressClass != 0)
        {
            method = env->GetStaticMethodID(s_progressClass, "stopJava2", "(I)V");
            
            if (method != 0)
            {
                env->CallStaticVoidMethod(s_progressClass, method, i_messageNr);
            }
            else
            {
                logIt("Did NOT find stopJava2!");
            }
        }
        else
        {
            wxLogErrorMain("Did not find progress class!");
        }

        //gJavaVM->DetachCurrentThread();
    }
    else
    {
        logIt("env is NULL!");
    }
}


void ThrowJavaException(const AEException& i_exception)
{
    JNIEnv* env = GetEnv();

    if (env)
    {
        // You can put your own exception here
        jclass c = env->FindClass("java/lang/RuntimeException");

        if (c)
        {
            env->ThrowNew(c, i_exception.what());
        }
    }
}


void appendLog(const std::string& i_message)
{
#ifdef __ANDROID__
	jmethodID method;
    JNIEnv* env = GetEnv();

    if (env)
    {
        if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
        {
            wxLogErrorMain("exception in FindClass appendLog");
            env->ExceptionClear();
            return;
        }

        if (s_progressClass != 0)
        {
            method = env->GetStaticMethodID(s_progressClass, "logMessage", "(Ljava/lang/String;)V");
            
            if (method != 0)
            {
                jstring str = env->NewStringUTF(i_message.c_str());  
                env->CallStaticVoidMethod(s_progressClass, method, str);
                env->DeleteLocalRef(str);
            }
            else
            {
                logIt("Did NOT find logMessage!");
            }
        }
        else
        {
            logError("Did not find progress class!");
        }
    }
    else
    {
        logIt("env is NULL!");
    }
#endif
}


void setPackageName(std::string i_PackageName)
{
    s_packageName = i_PackageName;
    
    std::string ProgressString = s_packageName + "/Progress";
    jclass progressClass = GetEnv()->FindClass(ProgressString.c_str());
    
    if (GetEnv()->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
    {
        wxLogErrorMain("exception in FindClass setPackageName");
        GetEnv()->ExceptionClear();
        return;
    }

    if (progressClass)
    {
        /* Create a global reference */
        s_progressClass = (jclass) GetEnv()->NewGlobalRef(progressClass);
    
        /* The local reference is no longer useful */
        GetEnv()->DeleteLocalRef(progressClass);
    }
}


timespec diff(timespec start, timespec end)
{
	timespec temp;
	if ((end.tv_nsec-start.tv_nsec)<0) {
		temp.tv_sec = end.tv_sec-start.tv_sec-1;
		temp.tv_nsec = 1000000000+end.tv_nsec-start.tv_nsec;
	} else {
		temp.tv_sec = end.tv_sec-start.tv_sec;
		temp.tv_nsec = end.tv_nsec-start.tv_nsec;
	}
	return temp;
}


static int s_logMethod = 0;


int getLogMethod()
{
    return s_logMethod;
}


void setLogMethod(int i_methodNr)
{
    if (i_methodNr >=0 && i_methodNr <= 2)
    {
        s_logMethod = i_methodNr;
#ifdef __ANDROID__
#ifndef NO_LIBUSB
        libusb_setLogMethod(i_methodNr);
#endif
#endif
    }
}



void setLogFileName(std::string i_logFileName)
{
    s_logFileName = i_logFileName;
#ifdef __ANDROID__
#ifndef NO_LIBUSB
    libusb_setLogFileName(i_logFileName.c_str());
#endif
#endif
}


void logToFile(const char *fmt, ...)
{
    va_list argp;
    va_start(argp, fmt);

    extern std::string s_logFileName;

    FILE *logFile = fopen(s_logFileName.c_str(), "a");
    if (logFile)
    {
        vfprintf(logFile, fmt, argp);
        fprintf(logFile, "\r\n");
        fclose(logFile);
    }
    else
    {
#ifdef __ANDROID__
        __android_log_print(ANDROID_LOG_DEBUG, "Main", "couldn't open logfile %s!", s_logFileName.c_str());
#endif
    }

    va_end(argp);
}


void logIt(const char *fmt, ...)
{
    va_list argp;
    va_start(argp, fmt);

    if (s_logMethod == 2)
    {
#ifdef __ANDROID__
        __android_log_vprint(ANDROID_LOG_DEBUG, "Main", fmt, argp);
#else
        MyNSLog(fmt, argp);
#endif
    }
    else if (s_logMethod == 1)
    {
        extern std::string s_logFileName;

        FILE *logFile = fopen(s_logFileName.c_str(), "a");
        if (logFile)
        {
            timeval curTime;
            gettimeofday(&curTime, NULL);
            int milli = curTime.tv_usec / 1000;

            char buffer [80];
            strftime(buffer, 80, "%Y-%m-%d %H:%M:%S", localtime(&curTime.tv_sec));

            char currentTime[84] = "";
            sprintf(currentTime, "%s:%d", buffer, milli);

            fprintf(logFile, "%s  ", currentTime);
            vfprintf(logFile, fmt, argp);
            fprintf(logFile, "\r\n");
            fclose(logFile);
        }
        else
        {
#ifdef __ANDROID__
            __android_log_print(ANDROID_LOG_DEBUG, "Main", "couldn't open logfile %s!", s_logFileName.c_str());
#endif
        }
    }

    va_end(argp);
}


void logError(const char *fmt, ...)
{
    char msg[1024];
    
    va_list argp;
    va_start(argp, fmt);
    vsnprintf(msg, 1023, fmt, argp);
    va_end(argp);

    wxLogErrorMain("%s", msg);

    extern std::string s_logFileName;

    FILE *logFile = fopen(s_logFileName.c_str(), "a");
    if (logFile)
    {
        char buffer[26];
        int millisec;
        struct tm* tm_info;
        struct timeval tv;

        gettimeofday(&tv, NULL);

        millisec = lrint(tv.tv_usec / 1000.0); // Round to nearest millisec
        if (millisec >= 1000) { // Allow for rounding up to nearest second
            millisec -= 1000;
            tv.tv_sec++;
        }

        tm_info = localtime(&tv.tv_sec);

        strftime(buffer, 26, "%Y:%m:%d %H:%M:%S", tm_info);

        fprintf(logFile, "%s.%03d %s\r\n", buffer, millisec, msg);
        fclose(logFile);
    }
    else
    {
#ifdef __ANDROID__
        __android_log_print(ANDROID_LOG_DEBUG, "Main", "couldn't open logfile %s!", s_logFileName.c_str());
#endif
    }

    s_allErrorLogs += msg;
}


std::string getAllErrorLogs()
{
    return s_allErrorLogs;
}


void addToAcraErrorLog(const std::string &i_message)
{
#ifdef __ANDROID__
    jmethodID method;
    JNIEnv* env = GetEnv();

    if (env)
    {
        if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
        {
            wxLogErrorMain("exception in FindClass addToAcraErrorLog");
            env->ExceptionClear();
            return;
        }

        if (s_progressClass != 0)
        {
            method = env->GetStaticMethodID(s_progressClass, "addToACRAErrorLog", "(Ljava/lang/String;)V");
            
            if (method != 0)
            {
                jstring str1 = env->NewStringUTF(i_message.c_str());
                env->CallStaticVoidMethod(s_progressClass, method, str1);
                if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
                {
                    wxLogErrorMain("exception in addToAcraErrorLog");
                    env->ExceptionClear();
                    return;
                }
                env->DeleteLocalRef(str1);
            }
            else
            {
                logIt("Did NOT find addToAcraErrorLog!");
            }
        }
        
        if (env->ExceptionOccurred()) // FindClass does not return NULL but throws an exception, clear it
        {
            env->ExceptionDescribe();
            env->ExceptionClear();
        }
    }
    else
    {
        logIt("env is NULL!");
    }
#endif
}


void showErrorDialog(const std::string& i_message)
{
#ifdef __ANDROID__
    jmethodID method;
    JNIEnv* env = GetEnv();

    if (env)
    {
        if (s_progressClass != 0)
        {
            method = env->GetStaticMethodID(s_progressClass, "ShowErrorDialog", "(Ljava/lang/String;)V");
            
            if (method != 0)
            {
                jstring str = env->NewStringUTF(i_message.c_str());  
                env->CallStaticVoidMethod(s_progressClass, method, str);
                env->DeleteLocalRef(str);
            }
            else
            {
                logIt("Did NOT find ShowErrorDialog!");
            }
        }
    }
    else
    {
        logIt("env is NULL!");
    }
#endif
}


void scanFile(const std::string& i_fileName, bool i_recursive)
{
#ifdef __ANDROID__
    jmethodID method;
    JNIEnv* env = GetEnv();

    if (env)
    {
        std::string ae5ActivityString = getPackageName() + "/AE5MobileActivity";
        jclass ae5ActivityClass = GetEnv()->FindClass(ae5ActivityString.c_str());

        if (ae5ActivityClass != 0)
        {
            method = env->GetStaticMethodID(ae5ActivityClass, "scanFile", "(Ljava/lang/String;Z)V");
            
            if (method != 0)
            {
                jstring str = env->NewStringUTF(i_fileName.c_str());  
                env->CallStaticVoidMethod(ae5ActivityClass, method, str, i_recursive);
                env->DeleteLocalRef(str);
            }
            else
            {
                logIt("Did NOT find scanFile!");
            }

            env->DeleteLocalRef(ae5ActivityClass);
        }
    }
    else
    {
        logIt("env is NULL!");
    }
#endif
}


std::string generateNewEffectFileName(std::string i_directory,
                                      std::string i_fileName,
                                      std::string i_extension,
                                      bool i_alwaysAddEff)
{
#ifdef __ANDROID__
    jmethodID method;
    JNIEnv* env = GetEnv();
    std::string result = "";

    if (env)
    {
        std::string miscString = getPackageName() + "/Misc";
        jclass miscClass = GetEnv()->FindClass(miscString.c_str());

        if (miscClass != 0)
        {
            method = env->GetStaticMethodID(miscClass, "GenerateNewEffectFileName", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V");
            
            if (method != 0)
            {
                jstring str1 = env->NewStringUTF(i_directory.c_str());
                jstring str2 = env->NewStringUTF(i_fileName.c_str());
                jstring str3 = env->NewStringUTF(i_extension.c_str());  
                jstring obj = (jstring) env->CallStaticObjectMethod(miscClass, method, str1, str2, str3, i_alwaysAddEff);
                env->DeleteLocalRef(str1);
                env->DeleteLocalRef(str2);
                env->DeleteLocalRef(str3);

                if (obj)
                {
                    const char *str = env->GetStringUTFChars(obj, 0);
		            result = std::string(str);
                    env->ReleaseStringUTFChars(obj, str);
                }
            }
            else
            {
                logIt("Did NOT find GenerateNewEffectFileName!");
            }

            env->DeleteLocalRef(miscClass);
        }
    }
    else
    {
        logIt("env is NULL!");
    }

    return result;
#else
    return Misc::GenerateNewEffectFileName(i_directory,
                                    i_fileName,
                                    i_extension,
                                    i_alwaysAddEff).c_str();
#endif
}


#ifdef __APPLE__
//clock_gettime is not implemented on OSX
int clock_gettime(int clk_id, struct timespec* t)
{
    struct timeval now;
    int rv = gettimeofday(&now, NULL);
    if (rv) return rv;
    t->tv_sec  = now.tv_sec;
    t->tv_nsec = now.tv_usec * 1000;
    return 0;
}



#ifdef __APPLE__
void AE5ProjectIO_autoSave()
{
    ProjectIO::autoSave(false);
}
#endif

#endif

//#define logIt(...) \
//{ \
//    char *msg = (char *) malloc(1024); \
//    sprintf(msg, __VA_ARGS__); \
//    appendLog(msg); \
//    free(msg); \
//}



