#include "PGMFile.h"
#include <android/log.h>

#include <ctype.h>
#include <stdlib.h>

#include <iostream>
#include <sstream>

PGMFile::PGMFile()
	: MemoryFile()
{
}

PGMFile::PGMFile(const char * fileData, int fileLength)
	: MemoryFile(fileData, fileLength)
{
}

#if defined (_MSC_VER)
PGMFile::PGMFile(std::string fileName)
	: MemoryFile(fileName)
{
}
#endif // defined (_MSC_VER)

int PGMFile::readPGMB_header(int * rows, int * cols, int * max_color)
{
	size_t hlen;
	char signature[3];

	this->seek(0, SEEK_SET);

	this->gets(signature, sizeof(signature));
	if (signature[0] != 'P' || signature[1] != '6')
	{
		close(); return 0;
	}	//probably not pgm binary file...
	//return std::string(signature);
	skipcomments();
	*cols = this->getInt();
	skipcomments();
	*rows = this->getInt();
	skipcomments();
	*max_color = this->getInt();
	this->getc();

	hlen = this->tellg(); //header lenght
	//this->close();
	/*if ((*rows) * 3 * (*cols) != (flen - hlen))	//we assume only one picture in the file
		return 0;*/
	if ((*rows) * 3 * (*cols) != (this->getFileLength() - hlen))	//we assume only one picture in the file
		return 0;

	return hlen;
}

int PGMFile::readPGMB_data(unsigned char * image, int hlen, int rows, int cols, int max_color)
{
	/*FILE *fp;
	if ((fp = fopen(fname, "rb")) == NULL)
		return 0;*/

	this->seek(hlen, SEEK_SET);
	int from = this->tellg();
	for (size_t i = 0; i < this->getFileLength() - from; ++i) {
		unsigned char pixel = this->getc();
		if (i > 614390)
		if (i % (1) == 0) {
			int x = pixel;
			x = pixel;
		}
		image[i] = static_cast<unsigned char>(pixel);
	}
	//int readedrows = fread(image, cols, rows, fp);
	//fclose(fp);

	/*if (rows != readedrows)
		return 0;*/

	return 1;
}

int PGMFile::readPPMB_header(int * rows, int * cols, int * max_color)
{
	size_t hlen;
	char signature[3];

	this->seek(0, SEEK_SET);

	this->gets(signature, sizeof(signature));
	if (signature[0] != 'P' || signature[1] != '6')
	{
		close(); return 0;
	}	//probably not pgm binary file...

	skipcomments();
	*cols = this->getInt();
	skipcomments();
	*rows = this->getInt();
	skipcomments();
	*max_color = this->getInt();
	this->getc();

	hlen = this->tellg(); //header lenght
	//this->close();
	/*if ((*rows) * 3 * (*cols) != (flen - hlen))	//we assume only one picture in the file
    return 0;*/
	if ((*rows) * 3 * (*cols) != (this->getFileLength() - hlen))	//we assume only one picture in the file
		return 0;

	return hlen;
}

int PGMFile::writePGMB_image_to_string(std::string &dataDestination, unsigned char * image, int rows, int cols, int max_color)
{
	if (this->getDataPointer() == nullptr) {
		mError = NO_DATA_FILE_IN_MEMORY;
		return mError;
	}

	std::stringstream ss;
	//ss << "P5\n" << cols << " " << rows << "\n# eyetom.com\n" << max_color << "\n";
	ss.write(reinterpret_cast<const char*>(image), cols * rows);
	//for (size_t i = 0; i < cols * rows; ++i)
	//ss.write(reinterpret_cast<char*>(image[i]), 1);
	//ss << image[i];
	//fp.write(reinterpret_cast<const char*>(image), cols * rows);

	dataDestination = ss.str();
	return E_OK;
}

int PGMFile::writePGMB_image_to_tableInt(int* table, unsigned char * image, int rows, int cols, int max_color)
{
	if (this->getDataPointer() == nullptr) {
		mError = NO_DATA_FILE_IN_MEMORY;
		return mError;
	}

	//std::stringstream ss;
	//ss << "P5\n" << cols << " " << rows << "\n# eyetom.com\n" << max_color << "\n";
	//ss.write(reinterpret_cast<const char*>(image), cols * rows);
	//for (size_t i = 0; i < cols * rows; ++i)
	//ss.write(reinterpret_cast<char*>(image[i]), 1);
	//ss << image[i];
	//fp.write(reinterpret_cast<const char*>(image), cols * rows);
	int ileBialych = 0;
	for (int i = 0; i < rows * cols; ++i) {
		int argb = 0xFF;
		((char*)&argb)[1] |= image[i];
		((char*)&argb)[2] |= image[i];
		((char*)&argb)[3] |= image[i];
		table[i] = argb;

		if (image[i] != 0) {
			++ileBialych;
		}
	}
	//__android_log_print(ANDROID_LOG_DEBUG, "LOG_TEST", "Białych jest" + itoa(ileBialych));
	//dataDestination = ss.str();
	return E_OK;
}

#if defined(_MSC_VER)
int PGMFile::writePGMB_image_to_file(std::string fileName, unsigned char * image, int rows, int cols, int max_color)
{
	if (this->getDataPointer() == nullptr) {
		mError = NO_DATA_FILE_IN_MEMORY;
		return mError;
	}

	/*if ((fp = fopen(fname, "wb")) == NULL)
		return(0);*/

	std::ofstream fp;
	fp.open(fileName, std::ios_base::binary);
	if (!fp.is_open()) {
		mError = CANNOT_OPEN_FILE;
		return mError;
	}

	std::stringstream ss;
	ss << "P5\n" << cols << " " << rows << "\n# eyetom.com\n" << max_color << "\n";
	//fprintf(fp, "P5\n%d %d\n# eyetom.com\n%d\n", cols, rows, max_color);

	fp.write(ss.str().c_str(), ss.str().size());

	/* dane zapusujemy wierszami, bo tak dane sa alokowane w pami�ci(**image),
	wiec nie koniecznie te fragmety pamieci musza byc ko�os siebie, cho� tak zazwyczaj jest*/
	// to jest ryzykowne -> 
	fp.write(reinterpret_cast<const char*>(image), cols * rows);
	/*for (size_t i = 0; i < rows; ++i) {
		fp.write(reinterpret_cast<const char*>(image[i]), cols * rows);
	}*/

	fp.close();
	return E_OK;

	/*if (rows != fwrite(image, cols, rows, fp)) {
		fclose(fp);
		return(0);
	}

	fclose(fp);
	return (1);*/
}
#endif // defined (_MSC_VER)

int PGMFile::readPPMB_data(unsigned char * imageR, unsigned char * imageG, unsigned char * imageB, int hlen, int rows, int cols, int max_color)
{
	long i, wxh;
	//FILE *fp;

	if (max_color>255) return 0;	//for now only 1 byte color values

	/*if ((fp = fopen(fname, "rb")) == NULL)
		return 0;*/

	this->seek(hlen, SEEK_SET);
	//fseek(fp, hlen, SEEK_SET);

	wxh = rows*cols;
	for (i = 0; i<wxh; i++) {
		imageR[i] = (unsigned char)this->getc();
		imageG[i] = (unsigned char)this->getc();
		imageB[i] = (unsigned char)this->getc();
	}

	return 1;
}

void PGMFile::skipcomments()
{
	int ch;
	char line[256];

	while ((ch = this->getc()) != EOF && isspace(ch));

	if (ch == '#') {
		this->gets(line, sizeof(line));
		this->skipcomments();
	}
	else
		this->seek(-1, SEEK_CUR);
}
