#pragma once

#if !defined(DEF_T_UTILS)
#define DEF_T_UTILS

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <vector>

using namespace std;

//klasa pomocnicza do tworzenia bufora o zadanej wielko�ci w przypadku np. gdy istniej�cy bufor zostanie przepe�niony
class TClass_Uint_Buf
{
public:
	unsigned int size;
	unsigned int *data;		//space is allocated in the constructors

							// constructors
	TClass_Uint_Buf();
	TClass_Uint_Buf(unsigned int N);
	// routines
	unsigned int *Create_and_copy_data(unsigned int N, unsigned int *old_data, unsigned int old_data_N);
	// destructors
	~TClass_Uint_Buf();
};


//rozne pomocnicze funkcje np. do alokacji pamieci
//bool replace(std::string& str, const std::string& from, const std::string& to);
unsigned char **new_char_image(int rows, int cols);
int **new_int_image(int rows, int cols);
unsigned int **new_uint_image(int rows, int cols);
double **new_double_image(int rows, int cols);

/* macro example
#define ASSERT_RETURN(condition, ret_val) \
if (!(condition)) { \
assert(false && #condition); \
return ret_val; }
*/


#endif