#include "StringUtils.h"
#include <stdlib.h>

namespace ESDUtils
{
    std::string format(const char *fmt, ...)
    {
        std::string result;

        va_list ap;
        va_start(ap, fmt);

        char *tmp = 0;
        vasprintf(&tmp, fmt, ap);
        va_end(ap);

        result = tmp;
        free(tmp);

        return result;
    }
}
