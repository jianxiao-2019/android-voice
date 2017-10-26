#pragma once

#include "AEException.h"
#include "StringUtils.h"

#ifdef __ANDROID__
#include <jni.h>
JavaVM *getJavaVM();
JNIEnv* GetEnv();
#endif

// cannot move to PortDefs.h because it is included in rt effects which cannot use wxWidgets
#define PRE(x) { if (!(x)) { ThrowJavaException(AEException(ESDUtils::format("Exception in file %s, line %d", __FILE__, __LINE__))); } }


void DoMessage(const std::string &i_Message);
void DoMessageShort(const std::string &i_Message);
void DoMessageShortLocalized(int i_messageID);
void DoMessage2(const std::string &i_Message);
void DoStopJava(const std::string &i_Message);
void DoStopJava2(int i_messageNr);
void ThrowJavaException(const AEException& i_exception);
void appendLog(const std::string& i_message);
void logIt(const char *fmt, ...);
void logError(const char *fmt, ...);
void logToFile(const char *fmt, ...);
void addToAcraErrorLog(const std::string &i_message);
void setLogMethod(int i_methodNr);
void setLogFileName(std::string i_logFileName);
int getLogMethod();
std::string getPackageName();
void setPackageName(std::string i_PackageName);
void removeSignalHandler();
void scanFile(const std::string& i_fileName, bool i_recursive);
std::string getAllErrorLogs();
void showErrorDialog(const std::string& i_message);
std::string generateNewEffectFileName(std::string i_directory,
                                      std::string i_fileName,
                                      std::string i_extension,
                                      bool i_alwaysAddEff);
timespec diff(timespec start, timespec end);

#ifdef __APPLE__
//clock_gettime is not implemented on OSX
#define CLOCK_MONOTONIC 0
int clock_gettime(int clk_id, struct timespec* t);
timespec diff(timespec start, timespec end);

#define PRE(x) { if (!(x)) { throw AEException(ESDUtils::format("Exception in file %s, line %d", __FILE__, __LINE__)); } }


#endif


