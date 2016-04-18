#include "MemoryFile.h"

MemoryFile::MemoryFile()
	: mFileData(nullptr)
	, mFileLength(0)
	, mIndexRead(0)
	, mIndexWrite(0)
{
}

MemoryFile::MemoryFile(const char * fileData, int fileLength)
	: mFileData(nullptr)
	, mFileLength(fileLength)
	, mIndexRead(0)
	, mIndexWrite(0)
{
	setFile(fileData, fileLength);
}

#if defined (_MSC_VER)
MemoryFile::MemoryFile(std::string fileName)
	: mFileData(nullptr)
	, mFileLength(0)
	, mIndexRead(0)
	, mIndexWrite(0)
{
	std::fstream fp;
	fp.open(fileName, std::ios_base::in | std::ios_base::binary);
	if (!fp.is_open()) {
		mError = FILE_NOT_FOUND;
		return;
	}
	/*if ((fp = fopen(f.c_str(), "rb")) == NULL)
		return 0;*/

	fp.seekg(0, std::ios_base::end);
	//fseek(fp, 0, SEEK_END);
	this->mFileLength = fp.tellg();
	//flen = ftell(fp);	//file lenght
	//fileData = new char[flen];
	fp.seekg(0, std::ios_base::beg);
	mFileData = new char[mFileLength];
	fp.read(mFileData, mFileLength);
	//fseek(fp, 0, SEEK_SET);
	//fread(fileData, 1, flen, fp);
	//memoryFile.setFile(fileData, flen);
	mError = E_OK;
	fp.close();
}
#endif // defined (_MSC_VER)

MemoryFile::~MemoryFile()
{
	close();
}

void MemoryFile::close()
{
	if (mFileData != nullptr) {
		delete[] mFileData;
		mFileData = nullptr;
	}

	mFileLength = 0;
	mIndexRead = 0;
	mIndexWrite = 0;
}

bool MemoryFile::setFile(const char *fileData, int fileLength)
{
	if (fileData == nullptr || fileLength < 0)
		return false;

	close();

	mFileData = new char[fileLength];
	if (mFileData == nullptr)
		return false;

	for (size_t i = 0; i < fileLength; ++i) {
		mFileData[i] = fileData[i];
	}
	mFileLength = fileLength;
	mIndexRead = 0;
	mIndexWrite = 0;
}

size_t MemoryFile::getLenght() const
{
	return mFileLength;
}

char * MemoryFile::gets(char * str, int size)
{
	size_t i;

	if (mFileData == nullptr || str == nullptr || size <= 0 || mIndexRead >= mFileLength)
		return nullptr;

	for (i = 0; i < size - 1 && mFileData[mIndexRead] != '\n'; ++i) {
		str[i] = mFileData[mIndexRead++];
		
		if (mIndexRead >= mFileLength) break;
	}
	str[i] = '\0';

	return str;
}

int MemoryFile::getc()
{
	if (mIndexRead >= mFileLength)
		return EOF;

	return mFileData[mIndexRead++];
}

int MemoryFile::getInt(bool binaryMode)
{
	if (mFileData == nullptr) {
		mError = NO_DATA_FILE_IN_MEMORY;
		return 0;
	}

	size_t i = 0;
	int val = 0;
	
	if (binaryMode) {
		size_t intSize = sizeof(int);
		if (mFileLength - mIndexRead > intSize) {
			for (i = 0; i < intSize; ++i) {
				((char*)&val)[i] = this->getc();
			}
		}
		// mIndexRead += intSize; - line commented because MemoryFile::getc() control mIndexRead
	}
	else {
		const size_t strLen = 32;
		char strNumber[strLen];

		for (i = 0; i < strLen - 1 && (mFileData[mIndexRead] != '\n' && mFileData[mIndexRead] != '\0' && mFileData[mIndexRead] != ' '); ++i) {
			strNumber[i] = mFileData[mIndexRead++];

			if (mIndexRead >= mFileLength) break;
		}
		strNumber[i] = '\0';

		val = std::atoi(strNumber);
	}

	mError = E_OK;
	return val;
}

int MemoryFile::seek(long int offset, int origin)
{
	if (origin < 0 || origin >= SEEK_COUNT)
		return EOF;

	if (origin == SEEK_SET && offset >= 0 && offset <= mFileLength)
		mIndexRead = offset;
	else if (origin == SEEK_CUR && mIndexRead + offset >= 0 && mFileLength > mIndexRead + offset)
		mIndexRead += offset;
	else if (origin == SEEK_END && mFileLength - offset >= 0 && offset <= 0)
		mIndexRead = mFileLength + offset;
	else
		return EOF;

	return 0;
}

size_t MemoryFile::tellg() const
{
	/*if (mFileData == nullptr) // chyba potrzebne wyjatki do obs³ogi tych bledow ....
		mError = NO_DATA_FILE_IN_MEMORY;*/

	return mIndexRead;
}

size_t MemoryFile::tellp() const
{
	return mIndexWrite;
}

size_t MemoryFile::getFileLength() const
{
	return mFileLength;
}

const char * MemoryFile::getDataPointer() const
{
	return mFileData;
}
