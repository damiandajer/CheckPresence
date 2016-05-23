#ifndef PGMFILE_H
#define PGMFILE_H

#include "MemoryFile.h"

class PGMFile : public MemoryFile {
public:
	PGMFile();
	PGMFile(const char * fileData, int fileLength);
#if defined (_MSC_VER)
	PGMFile(std::string fileName);
#endif // defined (_MSC_VER)

	int readPGMB_header(int *rows, int *cols, int *max_color);
	int readPGMB_data(unsigned char *image, int hlen, int rows, int cols, int max_color);
	int readPPMB_header(int * rows, int * cols, int * max_color);
	/*int readPGMB_data(unsigned char *image, const  char *fname, int hlen, int rows, int cols, int max_color);*/
	int writePGMB_image_to_string(std::string& dataDestination, unsigned char *image, int rows, int cols, int max_color);
	int writePGMB_image_to_tableInt(int* table, unsigned char * image, int rows, int cols, int max_color);
#if defined (_MSC_VER)
	int writePGMB_image_to_file(std::string fileName, unsigned char *image, int rows, int cols, int max_color);
#endif // defined (_MSC_VER)
	/*int readPPMB_header(const char *fname, int *rows, int *cols, int *max_color);
	//	readPPMB_data*/
	int readPPMB_data(unsigned char *imageR, unsigned char *imageG, unsigned char *imageB, int hlen, int rows, int cols, int max_color);
	/*
	int writePPMB_image(const char *fname, unsigned char *imageR, unsigned char *imageG, unsigned char *imageB, int rows, int cols, int max_color);
	*/
private:
	void skipcomments();
};

#endif // PGMFILE_H