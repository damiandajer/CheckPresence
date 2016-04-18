#include "T_cls_utils.h"
#include "T_img_utils.h"


#define MAXSTACK 102400

unsigned int stack1[MAXSTACK];			//stack to keep pixels states for non maksimum suprression


void T_FindOneClaster(unsigned char*a, int w, int h, unsigned char color, unsigned char tlo, unsigned int index, CLSV &clusters) {

#define is_same_color(i) ( a[i] == color )
#define stack_push(d)  a[d]=tlo, stack[stack_top++]=(d)
#define stack_pop(d)  (d) = stack[--stack_top]

	unsigned int *stack2 = 0;
	unsigned int *stack = stack1;
	int stack_top, stack_bottom;
	TClass_Uint_Buf heapstack;

	//odwiedzaj wszystkie przyleg�e piksele ktore s� danego koloru i jeszcze nie by�y odwiedzone
	//co jaki� czas znalezione zostan� wszystkie piksele danego klastra, zapisujemy wsp�rz�dn� pierwszego z pikseli, 
	//zeby pozniej mie� ich ewidencj� i szybko lokalizowa� sasiednie klastry, nie b�dzie du�o tych pocz�tk�w, wi�c mo�emy dodawa� do struktury
	//alokowanej dynamicznie

	unsigned int d, x, y;
	d = x = y = 0;
	unsigned int pix_count = 0;
	unsigned int total_pix_count = 0;
	//unsigned int n=0;

	//zwiazane z obliczaniem granic
	int minx, maxx, minx_index, maxx_index, miny_index, maxy_index, pomx;


	pix_count = 0;
	stack_top = stack_bottom = 0;
	stack_push(index);						//odkladamy na stos pierwszy piksel od ktorego zaczynamy i kasujemy go z obrazu


											//zwiazane z obliczaniem granic
	miny_index = maxy_index = minx_index = maxx_index = index;
	minx = maxx = index%w;

	while (stack_top > stack_bottom) {

		if ((stack_top + 8) > MAXSTACK && !stack2) stack = stack2 = heapstack.Create_and_copy_data(w*h, stack1, MAXSTACK);	//korekta stosu jesli wyszlismy poza zakres

		stack_pop(d);
		pix_count++;

		//korekta granic
		pomx = d%w;
		if ((pomx < minx) || (pomx == minx && d<minx_index)) { minx = pomx; minx_index = d; }
		else if ((pomx > maxx) || (pomx == maxx && d>minx_index)) { maxx = pomx; maxx_index = d; }
		if (d < miny_index) miny_index = d;
		else if (d > maxy_index) maxy_index = d;


		if is_same_color(d - 1) stack_push(d - 1);					//badamy wszystkich 8 sasiadow
		if is_same_color(d + 1) stack_push(d + 1);
		if is_same_color(d - w - 1) stack_push(d - w - 1);
		if is_same_color(d - w) stack_push(d - w);
		if is_same_color(d - w + 1) stack_push(d - w + 1);
		if is_same_color(d + w - 1) stack_push(d + w - 1);
		if is_same_color(d + w) stack_push(d + w);
		if is_same_color(d + w + 1) stack_push(d + w + 1);
	}//while

	 //n a koncu zachowujmy informacje o pikselach
	CLS cls;
	cls.maxx_index = maxx_index;
	cls.maxy_index = maxy_index;
	cls.minx_index = minx_index;
	cls.miny_index = miny_index;
	cls.size = pix_count;
	clusters.push_back(cls);
	total_pix_count += pix_count;

}

void T_ClearOneClaster(unsigned char*a, int w, int h, unsigned char color, unsigned char tlo, unsigned int index)
{

#define is_same_color(i) ( a[i] == color )
#define stack_push(d)  a[d]=tlo, stack[stack_top++]=(d)
#define stack_pop(d)  (d) = stack[--stack_top]

	unsigned int *stack2 = 0;
	unsigned int *stack = stack1;
	int stack_top, stack_bottom;
	TClass_Uint_Buf heapstack;

	//odwiedzaj wszystkie przyleg�e piksele ktore s� danego koloru i jeszcze nie by�y odwiedzone i kasuj je z obrazka
	unsigned int d, x, y;
	d = x = y = 0;
	unsigned int pix_count = 0;
	unsigned int total_pix_count = 0;
	//unsigned int n=0;

	pix_count = 0;
	stack_top = stack_bottom = 0;
	stack_push(index);						//odkladamy na stos pierwszy piksel od ktorego zaczynamy i kasujemy go z obrazu

	while (stack_top > stack_bottom) {

		if ((stack_top + 8) > MAXSTACK && !stack2) stack = stack2 = heapstack.Create_and_copy_data(w*h, stack1, MAXSTACK);	//korekta stosu jesli wyszlismy poza zakres

		stack_pop(d);
		pix_count++;

		if is_same_color(d - 1) stack_push(d - 1);					//badamy wszystkich 8 sasiadow
		if is_same_color(d + 1) stack_push(d + 1);
		if is_same_color(d - w - 1) stack_push(d - w - 1);
		if is_same_color(d - w) stack_push(d - w);
		if is_same_color(d - w + 1) stack_push(d - w + 1);
		if is_same_color(d + w - 1) stack_push(d + w - 1);
		if is_same_color(d + w) stack_push(d + w);
		if is_same_color(d + w + 1) stack_push(d + w + 1);
	}//while
}


void T_FindAllClusters(unsigned char**img, int w, int h, unsigned char color, CLSV &clusters)
{
	int x, y, i;
	x = y = i = 0;

	//tworzymy kopie robocza obrazka wejsciowego poniewaz bedziemy zmieniac wartosci pikseli 
	unsigned char *img_data1 = img[0];
	unsigned char *img_data2 = new unsigned char[w*h];;
	for (i = 0; i < w*h; i++)	img_data2[i] = img_data1[i];

	unsigned char *a = img_data2;
	//algorytm wymaga aby conajmniej jeden piksel brzegowy by� koloru t�a - zapobiega to sprawdzaniu za ka�dym razem czy nie wyszlismy poza granice obrazka
	unsigned char tlo = 255 - color;			//tlo - kolor t�a - dowolny kolor inny ni� ten zwiazany z klastrami ktorych poszukujemy
	for (int i = 0; i<w; ++i)	a[i] = a[(h - 1)*w + i] = tlo;		//pierwszy i ostatni wiersz
	for (int i = 0; i<h; ++i)	a[i*w] = a[i*w + (w - 1)] = tlo;		//pierwsza i ostatnia kolumna


	for (i = 0; i < w*h; i++) {
		if (a[i] == color) {	//napotykamy piksel nalezacy do klastra...
			T_FindOneClaster(a, w, h, color, tlo, i, clusters);
		}
	}

	//if( writePGMB_image("test.pgm", a, h, w, 255) == 0)	   exit(1);
	delete[] img_data2;
}


int test_how_much_neighburs_pixel(unsigned char** a, int y, int x, unsigned char color)
{
	int total = 0;

	if (a[y - 1][x - 1] == 255) total++;
	if (a[y - 1][x] == 255) total++;
	if (a[y - 1][x + 1] == 255) total++;
	if (a[y][x - 1] == 255) total++;
	if (a[y][x + 1] == 255) total++;
	if (a[y + 1][x - 1] == 255) total++;
	if (a[y + 1][x] == 255) total++;
	if (a[y + 1][x + 1] == 255) total++;

	return total;
}


