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
