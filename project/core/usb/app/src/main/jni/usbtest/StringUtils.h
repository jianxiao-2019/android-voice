#pragma once

#include <string>

namespace ESDUtils
{
    std::string format(const char *fmt, ...) __attribute__((format(printf, 1, 2)));
}