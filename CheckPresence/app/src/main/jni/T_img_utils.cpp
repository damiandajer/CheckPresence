#include "T_img_utils.h"

bool replace(std::string& str, const std::string& from, const std::string& to)
{
	size_t start_pos = str.find(from);
	if (start_pos == std::string::npos)
		return false;
	str.replace(start_pos, from.length(), to);
	return true;
}

void integral_image(unsigned char **g, unsigned int **I, unsigned int w, unsigned int h)
{
	unsigned int x, y, suma;
	for (y = 0; y<h; ++y) {
		suma = 0;
		for (x = 0; x<w; ++x) {
			suma += g[y][x];
			if (y == 0) I[y][x] = suma;
			else I[y][x] = I[y - 1][x] + suma;
		}
	}
}

void integral_image_sqr(unsigned char **g, double  **I, unsigned int w, unsigned int h)
{
	unsigned int x, y, suma;

	for (y = 0; y<h; ++y) {
		suma = 0;
		for (x = 0; x<w; ++x) {
			suma += (g[y][x] * g[y][x]);
			if (y == 0) I[y][x] = suma;
			else I[y][x] = I[y - 1][x] + suma;
		}
	}
}

double get_Window_Variance(unsigned int **A_M, double **A_S, int y_1, int y_2, int x_1, int x_2)
{
	int x1 = x_1 - 1;
	int x2 = x_2;
	int y1 = y_1 - 1;
	int y2 = y_2;

	double count = (x2 - x1)*(y2 - y1);

	double g = A_S[y2][x2] + A_S[y1][x1] - A_S[y1][x2] - A_S[y2][x1];
	g = g / count;

	double m = A_M[y2][x2] + A_M[y1][x1] - A_M[y1][x2] - A_M[y2][x1];

	m = m / count;
	m *= m;

	//return sqrt( g - m );
	return (g - m);
}

int get_Window_Sum(unsigned int **A_M, int y_1, int y_2, int x_1, int x_2)
{
	int x1 = x_1 - 1;
	int x2 = x_2;
	int y1 = y_1 - 1;
	int y2 = y_2;

	int m = A_M[y2][x2] + A_M[y1][x1] - A_M[y1][x2] - A_M[y2][x1];

	return m;
}

double get_Window_Mean(unsigned int **A_M, int y_1, int y_2, int x_1, int x_2)
{
	int x1 = x_1 - 1;
	int x2 = x_2;
	int y1 = y_1 - 1;
	int y2 = y_2;

	int m = A_M[y2][x2] + A_M[y1][x1] - A_M[y1][x2] - A_M[y2][x1];

	return ((double)m / ((x2 - x1)*(y2 - y1)));
}

unsigned char **new_char_image(int rows, int cols)
{
	int i;
	unsigned char **a = new unsigned char*[rows];
	a[0] = new unsigned char[rows*cols];
	for (i = 1; i < rows; i++)
		a[i] = a[i - 1] + cols;
	return a;
}

int **new_int_image(int rows, int cols)
{
	int i;
	int **a = new int*[rows];
	a[0] = new int[rows*cols];
	for (i = 1; i < rows; i++)
		a[i] = a[i - 1] + cols;
	return a;
}

unsigned int **new_uint_image(int rows, int cols)
{
	int i;
	unsigned int **a = new unsigned int*[rows];
	a[0] = new unsigned int[rows*cols];
	for (i = 1; i < rows; i++)
		a[i] = a[i - 1] + cols;
	return a;
}

double **new_double_image(int rows, int cols)
{
	int i;
	double **a = new double*[rows];
	a[0] = new double[rows*cols];
	for (i = 1; i < rows; i++)
		a[i] = a[i - 1] + cols;
	return a;
}

unsigned char **get_copy_char_image(unsigned char **a, int h, int w)
{
	int i;
	unsigned char **b = new_char_image(h, w);
	for (int y = 0; y<h; y++) for (int x = 0; x<w; x++) b[y][x] = a[y][x];
	return b;
}


TClass_Uint_Buf::TClass_Uint_Buf()
{
	size = 0;
	data = NULL;
}

TClass_Uint_Buf::TClass_Uint_Buf(unsigned int N)
{
	size = N;
	data = new unsigned int[size];
}

TClass_Uint_Buf::~TClass_Uint_Buf()
{
	if (data != NULL) delete[] data;
}

unsigned int * TClass_Uint_Buf::Create_and_copy_data(unsigned int N, unsigned int *old_data, unsigned int old_data_N)
{
	size = N;
	data = new unsigned int[size];

	memcpy(data, old_data, old_data_N*sizeof(unsigned int));
	return data;
}