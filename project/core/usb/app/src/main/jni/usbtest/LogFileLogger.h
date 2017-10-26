#pragma once

#include "ILogger.h"

/**
 * Logging class for logging to a file. The actual logging happens on the Java side using the
 * appendLog() call in ToJava.
 */
class LogFileLogger : public ILogger
{
public:
	LogFileLogger();
	~LogFileLogger();

    void logMessage(const std::string& i_message);
};
