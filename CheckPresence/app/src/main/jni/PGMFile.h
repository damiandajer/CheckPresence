#ifndef PGMFILE_H
#define PGMFILE_H

#include "MemoryFile.h"


class PGMFile : public MemoryFile {
public:
	PGMFile();
	PGMFile(const char * fileData, int fileLength);

	int readPGMB_data(unsigned char *image, int hlen, int rows, int cols, int max_color);
	int readPPMB_data(unsigned char *imageR, unsigned char *imageG, unsigned char *imageB, int hlen, int rows, int cols, int max_color);
	int writePGMB_image_to_tableInt(int* table, unsigned char * image, int rows, int cols, int max_color);
private:
	void skipcomments();
};

#endif // PGMFILE_H