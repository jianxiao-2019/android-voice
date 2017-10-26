#pragma once

#include <ToJava.h>

/**
 * Abstract class for logging
 */
class ILogger
{
public:
    ILogger() {};
    virtual ~ILogger() {};

    virtual void logMessage(const std::string& i_message) = 0;
};
