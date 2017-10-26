#pragma once

#include <string>

struct AEException : public std::exception
{
   std::string s;
   AEException(const std::string& ss) : s(ss) {}
   virtual ~AEException() throw() {}
   const char* what() const throw()
   {
       return s.c_str();
   }
};
