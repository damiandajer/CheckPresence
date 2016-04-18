#ifndef MEMORYFILE_H
#define MEMORYFILE_H

#include <string>

#if defined (_MSC_VER)
	#include <fstream>
#endif // defined (IS_VS)

#include "Errors.h"

#define SEEK_SET 0
#define SEEK_CUR 1
#define SEEK_END 2
#define SEEK_COUNT 3

class MemoryFile {
public:
	MemoryFile();
	MemoryFile(const char* fileData, int fileLength);
#if defined (_MSC_VER)
	MemoryFile(std::string fileName);
#endif
	virtual ~MemoryFile();

	void close();

	bool setFile(const char* fileData, int fileLength);

	size_t getLenght() const;

	/* my of implementation to read from memmory
	the same as fgets from standars c library 
	Na koñcu fgets() dopisuje znak '\0' */
	char * gets(char *str, int size);
	int getc();

	/* Interpreting current reading bytes as int
	Parameters:
	bool binaryMode: ASCI or binary read mode from stream(memory file).
	return value:
	int number
	When error:
	When reach EOF
	member MemoryFile::mError has value EOF*/
	int getInt(bool binaryMode = false);

	int seek(long int offset, int origin);

	/*Get position in input sequence
	Return value:
	The current position in the stream */
	size_t tellg() const;
	/*Get position in output sequence
	Return value:
	Returns the position of the current character in the output stream*/
	size_t tellp() const;

	size_t getFileLength() const;
	const char* getDataPointer() const;

private:
	char* mFileData; // poiter to file data in memory
	size_t mFileLength;
	size_t mIndexRead; // current read position
	size_t mIndexWrite; // current read position

protected:
	int mError; // special member for some functions
};

#endif // MEMORYFILE_H