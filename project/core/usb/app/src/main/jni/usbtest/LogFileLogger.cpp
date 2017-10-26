#include "LogFileLogger.h"
#include "ToJava.h"

LogFileLogger::LogFileLogger()
{
}


LogFileLogger::~LogFileLogger()
{
}


void LogFileLogger::logMessage(const std::string& i_message)
{
    appendLog(i_message.c_str());
}

