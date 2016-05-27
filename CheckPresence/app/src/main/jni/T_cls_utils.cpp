#include "T_cls_utils.h"
#include "T_img_utils.h"
#include "T_draw_utils.h"


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

	//odwiedzaj wszystkie przyległe piksele ktore są danego koloru i jeszcze nie były odwiedzone
	//co jakiś czas znalezione zostaną wszystkie piksele danego klastra, zapisujemy współrzędną pierwszego z pikseli,
	//zeby pozniej mieć ich ewidencję i szybko lokalizować sasiednie klastry, nie będzie dużo tych początków, więc możemy dodawać do struktury
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

	//odwiedzaj wszystkie przyległe piksele ktore są danego koloru i jeszcze nie były odwiedzone i kasuj je z obrazka
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

int T_ClearOneClasterAndCopy(unsigned char*a, unsigned char*b, int w, int h, unsigned char color, unsigned char tlo, unsigned int index)
{
#define is_same_color(i) ( a[i] == color )
	//zerujemy w obrazku wejsciowym jednoczesnie przepisujac do wyjsciowego b
#define stack_push(d)  a[d]=b[d]=tlo, stack[stack_top++]=(d)
#define stack_pop(d)  (d) = stack[--stack_top]

	unsigned int *stack2 = 0;
	unsigned int *stack = stack1;
	int stack_top, stack_bottom;
	TClass_Uint_Buf heapstack;

	//odwiedzaj wszystkie przyległe piksele ktore są danego koloru i jeszcze nie były odwiedzone i kasuj je z obrazka
	unsigned int d, x, y;
	unsigned int pix_count = 0;
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
	return pix_count;
}


int create_contour(unsigned char ** a, unsigned char ** b, int h, int w)
{
	int i, j, b1, b2, b3, b4, index = 0;

	//wykasuj piksele na obrzezach obrazka aby nie sprawdzać ciągle warukow brzegowych
	for (i = 0; i<w; ++i)	a[0][i] = a[h - 1][i] = 0;		//pierwszy i ostatni wiersz
	for (i = 0; i<h; ++i)	a[i][0] = a[i][w - 1] = 0;		//pierwsza i ostatnia kolumna



	//zrob kopie obrazka a, w b bedzie przechowywany znaleziony kontur
	for (i = 0; i<h; i++) for (j = 0; j<w; j++) b[i][j] = a[i][j];

	//usun piksele ktore maja 4 bialych sasiadow wokol siebie
	for (i = 1; i<h - 1; i++)
		for (j = 1; j<w - 1; j++) {

			if (a[i][j] == 0) continue;

			b1 = a[i + 1][j] == 255;
			b2 = a[i - 1][j] == 255;
			b3 = a[i][j + 1] == 255;
			b4 = a[i][j - 1] == 255;

			if (b1 && b2 && b3 && b4)
				b[i][j] = 0;
			else
				index = i*w + j;
		}

	return index;		//zwróć index jednego z punktow nalezacego do konturu
}

void T_ClearContur_and_copy_to_vector(unsigned char * a, int w, int h, unsigned char color, unsigned char tlo, unsigned int index, CLS_piksels & piksels)
{

#define is_same_color(i) ( a[i] == color )
#define stack_push(d)  a[d]=tlo, stack[stack_top++]=(d), piksels.push_back(d)
#define stack_pop(d)  (d) = stack[--stack_top]


	unsigned int *stack2 = 0;
	unsigned int *stack = stack1;
	int stack_top, stack_bottom;
	TClass_Uint_Buf heapstack;

	//odwiedzaj wszystkie przyległe piksele ktore są danego koloru i jeszcze nie były odwiedzone i kasuj je z obrazka
	unsigned int d, x, y;
	unsigned int pix_count = 0;
	unsigned int total_pix_count = 0;
	//unsigned int n=0;

	//wykasuj kilka pikseli po prawej stronie startowego piksela aby odwiedzić wszystkie w kierunku na lewo najpierw
	a[index + 1] = a[index - w + 1] = a[index + w + 1] = tlo;

	pix_count = 0;
	stack_top = stack_bottom = 0;
	stack_push(index);						//odkladamy na stos pierwszy piksel od ktorego zaczynamy i kasujemy go z obrazu

	while (stack_top > stack_bottom) {

		if ((stack_top + 8) > MAXSTACK && !stack2) stack = stack2 = heapstack.Create_and_copy_data(w*h, stack1, MAXSTACK);	//korekta stosu jesli wyszlismy poza zakres

		stack_pop(d);
		pix_count++;

		if is_same_color(d + w) stack_push(d + w);
		if is_same_color(d + 1) stack_push(d + 1);
		if is_same_color(d - w) stack_push(d - w);
		if is_same_color(d - 1) stack_push(d - 1);					//badamy wszystkich 8 sasiadow
		if is_same_color(d + w + 1) stack_push(d + w + 1);
		if is_same_color(d + w - 1) stack_push(d + w - 1);
		if is_same_color(d - w - 1) stack_push(d - w - 1);
		if is_same_color(d - w + 1) stack_push(d - w + 1);

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
	//algorytm wymaga aby conajmniej jeden piksel brzegowy był koloru tła - zapobiega to sprawdzaniu za każdym razem czy nie wyszlismy poza granice obrazka
	unsigned char tlo = 255 - color;			//tlo - kolor tła - dowolny kolor inny niż ten zwiazany z klastrami ktorych poszukujemy
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

int Find_Top_Extremas(CLS_piksels &p, unsigned char **a, int *e, int h, int w)
{
	//przechodzimu po pikselach konturu ulozonych wczesniej od lewej do gory i do prawej strony
	//bierzemy pod uwage tylko te grupy pikseli ktore są poniżej połowy obrazka i na prawo od wczesniejszej grupy

	int psize = p.size();
	int finger = 0;
	int d = 35;
	for (int i = d; i<p.size() - d; i++) {

		int yi = p[i] / w;							//wsp. srodka podciagu
		int xi = p[i] % w;							//wsp. srodka podciagu
		int y1 = p[i - d] / w;							//wsp. poczatku podciagu
		int y2 = p[i + d] / w;							//wsp. końca podciagu

		if ((yi>h*0.3 && xi <= w*0.5) || (yi>h*0.5 && xi>w*0.5)) continue;						//nie bierzemy pod uwage pikseli lezących pwyzej połowy obrazka

		if ((y1 - yi>20) && (y2 - yi>20)) {		//szukamy punktu przegięcia

			int ex = i;							//znaleziono przedział w którym jest ekstremum, teraz szukamy samego ekstremum
			for (int j = i - d; j<i + d; j++) {
				if (p[j] / w < p[ex] / w) ex = j;
			}

			e[8 + finger++] = ex;		//indeks czubka palca wskazujacego to 8, potem kolejno dalsze 3 palce
			if (finger>3) return finger;
			i += d;
		}
	}// for i

	return finger;
}

int Find_Bottom_Extremas(CLS_piksels &p, unsigned char **a, int *e, int h, int w)
{
	//przechodzimu po pikselach konturu ulozonych wczesniej od lewej (na prawo od czubka palca wskazujacego) do prawej strony - czubka malego palca
	//bierzemy pod uwage tylko te grupy pikseli ktore są poniżej połowy obrazka i na prawo od wczesniejszej grupy

	int start_index = e[8];
	int end_index = e[11];
	int finger = 0;
	int d = 35;

	for (int i = start_index + d; i<end_index - d; i++) {

		int yi = p[i] / w;							//wsp. srodka podciagu
		int y1 = p[i - d] / w;							//wsp. poczatku podciagu
		int y2 = p[i + d] / w;							//wsp. końca podciagu

		if (yi>h*0.7) continue;						//nie bierzemy pod uwage pikseli lezących pwyzej połowy obrazka

		if ((yi - y1>20) && (yi - y2>20)) {		//szukamy punktu przegięcia

			int ex = i;							//znaleziono przedział w którym jest ekstremum, teraz szukamy samego ekstremum
			for (int j = i - d; j<i + d; j++)
				if (p[j] / w > p[ex] / w) ex = j;

			e[3 + finger++] = ex;
			i += d;
		}
	}// for i

	return finger;
}

int Find_Thumb_Bottom_Extremum(unsigned char **a, CLS_piksels &p, int *e, int h, int w)
{
	//przechodzimu po pikselach konturu ulozonych wczesniej na lewo od czubka palca wskazujacego
	int d = 35;
	int finger = 0;

	int end_index = e[8];

	for (int i = end_index - d; i>d; i--) {

		int yi = p[i] / w;							//wsp. srodka podciagu
		int xi = p[i] - yi*w;
		int y1 = p[i - d] / w;							//wsp. poczatku podciagu
		int y2 = p[i + d] / w;							//wsp. końca podciagu

		//piksels.push_back(p[i]);
		//rysuj_pixele(mat, piksels);

		if (yi<h*0.5 || xi>w*0.5) continue;						//nie bierzemy pod uwage pikseli lezących pwyzej połowy obrazka i na prawo

		if ((yi - y1>5) && (yi - y2>5)) {		//szukamy punktu przegięcia

			int ex = i;							//znaleziono przedział w którym jest ekstremum, teraz szukamy samego ekstremum
			for (int j = i - d; j<i + d; j++)
				if ((p[j] / w > p[ex] / w) || ((p[j] / w == p[ex] / w) && (p[j] % w > p[ex] % w))) ex = j;

			e[1] = ex;
			return 1;
		}
	}// for i

	return 0;
}

int Find_Thumb_Top_Extremum(CLS_piksels &p, int *e, int h, int w)
{
	//przechodzimu po pikselach konturu ulozonych wczesniej na lewo od czubka palca wskazujacego
	int end_index = e[8];
	int d = 35;
	for (int i = end_index - d; i>d; i--) {

		int yi = p[i] / w;							//wsp. srodka podciagu
		int xi = p[i] % w;
		int y1 = p[i - d] / w;							//wsp. poczatku podciagu
		int x1 = p[i - d] % w;
		int y2 = p[i + d] / w;							//wsp. końca podciagu
		int x2 = p[i + d] % w;

		if (yi<h*0.4 || xi>w*0.3) continue;			//nie bierzemy pod uwage pikseli lezących pwyzej połowy obrazka i na prawo

		if ((x1 - xi>10) && (x2 - xi>10)) {			//szukamy punktu przegięcia po lewej stronie

			int ex = i;							//znaleziono przedział w którym jest ekstremum, teraz szukamy samego ekstremum
			for (int j = i - d; j<i + d; j++) {
				if (p[j] % w < p[ex] % w || (p[j] % w == p[ex] % w) && (p[j] / w < p[ex] / w)) ex = j;
			}

			e[7] = ex;
			return 1;
		}
	}// for i

	return 0;
}

float dist(int i, int j, int w)
{
	int y1 = i / w;
	int x1 = i - y1*w;
	int y2 = j / w;
	int x2 = j - y2*w;
	return (sqrt((float)((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2))));
}

int Find_Left_Side_Bottom_Extremum(CLS_piksels &p, int *e, int h, int w)
{
	float d = 0;
	float min = 99999;
	int min_i = 0;
	int start_index = e[1];
	int end_index = e[8];
	int fix_i = p[e[3]];

	//szukamy punktu pomiedzy kciukiem i wskazujacym najblizszego od podstawy wskazującego
	for (int i = start_index + 25; i<end_index - 25; i++) {
		if ((d = dist(p[i], fix_i, w)) < min) min = d, min_i = i;
	}// for i

	if (min_i) {
		e[2] = min_i;
		return 1;
	}
	return 0;
}

int Find_Right_Side_Bottom_Extremum(CLS_piksels &p, int *e, int h, int w)
{
	float d = 0;
	float min = 99999;
	int min_i = 0;
	int fix_i = p[e[5]];
	int last_y = fix_i / w + 50;
	int start_index = e[11];


	//szukamy punktu pomiedzy kciukiem i wskazujacym najblizszego od podstawy wskazującego
	for (int i = start_index + 25; p[i] / w < last_y && i<p.size(); i++) {
		if ((d = dist(p[i], fix_i, w)) < min) min = d, min_i = i;
	}// for i

	if (min_i) {
		e[6] = min_i;
		return 1;
	}
	return 0;
}

int Find_Thumb_Side_Bottom_Extremum(CLS_piksels &p, int *e, int h, int w)
{
	float d = 0;
	float min = 99999;
	int min_i = 0;
	int fix_i = p[e[1]];
	int first_x = fix_i%w;		//czyli punkty na lewo od e[3]
	int end_index = e[7];

	//szukamy punktu pomiedzy kciukiem i wskazujacym najblizszego od podstawy wskazującego
	for (int i = 0; i<end_index - 25; i++) {
		if (p[i] % w < first_x && (d = dist(p[i], fix_i, w)) < min) min = d, min_i = i;
	}// for i

	if (min_i) {
		e[0] = min_i;
		return 1;
	}
	return 0;
}

int Find_Hand_width(CLS_piksels &p, int *e, int h, int w, int *index, float *szerokosc_dloni)
{
	int start_index = e[6];
	int fix_i = p[e[1]];
	int first_y = fix_i / w;		//czyli punkty na lewo od e[3]


	for (int i = start_index; i< p.size(); i++) {
		if (p[i] / w == first_y) {
			*szerokosc_dloni = dist(p[i], fix_i, w);
			*index = p[i];
			return 1;
		}
	}// for i

	return 0;
}


//return middle point index
int middle_point(int p1, int p2, int w)
{
	int p1_x = p1%w;
	int p1_y = p1 / w;
	int p2_x = p2%w;
	int p2_y = p2 / w;

	return(((p2_y - p1_y) / 2 + p1_y)*w + (p2_x - p1_x) / 2 + p1_x);
}


//szukanie okregu wpisanego w obszar
//podajemy obrazek typu
//	00000000000
//	01111000000
//	01000100000
//	01000100000
//	01000010000
//	00100001000
//	00011100100
//	00000011000
//obszar w ktorym ma byc wsadzone koło ograniczony jedynkami
void dt(unsigned *_d, unsigned char *_bimg, int _h, int _w)
{
	unsigned *dd;
	unsigned *f;
	int *v;
	int *z;
	int i;
	int j;
	int k;
	int n;

	dd = new unsigned[_h*_w];
	n = _h>_w ? _h : _w;
	v = new int[n];
	z = new int[n + 1];
	f = new unsigned[_h];

	for (i = 0; i<_h; i++)
	{
		k = -1;
		for (j = 0; j<_w; j++)if (_bimg[i*_w + j])
			{
				int s;
				s = k<0 ? 0 : (v[k] + j >> 1) + 1;
				v[++k] = j;
				z[k] = s;
			}

		if (k<0)
			for (j = 0; j<_w; j++)dd[j*_h + i] = UINT_MAX;
		else
		{
			int zk;
			z[k + 1] = _w;
			j = k = 0;
			do
			{
				int d1;
				int d2;
				d1 = j - v[k];
				d2 = d1*d1;
				d1 = d1 << 1 | 1;
				zk = z[++k];
				for (;;)
				{
					dd[j*_h + i] = (unsigned)d2;
					if (++j >= zk)break;
					d2 += d1;
					d1 += 2;
				}
			} while (zk<_w);
		}
	}

	for (j = 0; j<_w; j++)
	{
		int v2;
		int q2;
		k = -1;
		for (i = q2 = 0; i<_h; i++)
		{
			unsigned d;
			d = dd[j*_h + i];
			if (d<UINT_MAX)
			{
				int s;
				if (k<0)s = 0;
				else for (;;)
					{
						s = q2 - v2 + d - f[k];
						if (s>0)
						{
							s = s / (i - v[k] << 1) + 1;
							if (s>z[k])break;
						}
						else s = 0;
						if (--k<0)break;
						v2 = v[k] * v[k];
					}
				if (s<_h) {
					v[++k] = i;
					f[k] = d;
					z[k] = s;
					v2 = q2;
				}
			}
			q2 += i << 1 | 1;
		}

		if (k<0)
		{
			memcpy(_d, dd, _w*_h*sizeof(*_d));
			break;
		}
		else
		{
			int zk;
			z[k + 1] = _h;
			i = k = 0;
			do
			{
				int d2;
				int d1;
				d1 = i - v[k];
				d2 = d1*d1 + f[k];
				d1 = d1 << 1 | 1;
				zk = z[++k];
				for (;;)
				{
					_d[i*_w + j] = (unsigned)d2;
					if (++i >= zk)break;
					d2 += d1;
					d1 += 2;
				}
			} while (zk<_h);
		}
	}

	delete f;
	delete z;
	delete v;
	delete dd;
}

//kopiujemy odgorna czesc obrazka do napotkania lini ktora dzielila palec, po napotkaniu wzystko dalej jest tlem
//idziemy columnami od lewej do prawej
//cl - kolor linii
void copy_top_image_part(unsigned char **a, unsigned char **b, int h, int w, unsigned char tlo, unsigned char cl)
{
	for (int x = 0; x<w; x++) {
		int top = 1;
		for (int y = 0; y<h; y++) {
			if (a[y][x] == cl) top = 0;
			if (top == 0) b[y][x] = tlo;
			else b[y][x] = a[y][x];
		}
	}
}

void copy_bottom_image_part(unsigned char **a, unsigned char **b, int h, int w, unsigned char tlo, unsigned char cl)
{
	for (int x = 0; x<w; x++) {
		int bottom = 1;
		for (int y = h - 1; y >= 0; y--) {
			if (a[y][x] == cl) bottom = 0;
			if (bottom == 0) b[y][x] = tlo;
			else b[y][x] = a[y][x];
		}
	}
}

void calc_fingers_feature(finger *f, unsigned char **img, int *ex_i, int h, int w)
{
	//kciuk
	f[0].base_first_point = ex_i[0];
	f[0].base_last_point = ex_i[1];
	f[0].top_point = ex_i[7];
	//wskazujacy
	f[1].base_first_point = ex_i[2];
	f[1].base_last_point = ex_i[3];
	f[1].top_point = ex_i[8];
	//srodkowy
	f[2].base_first_point = ex_i[3];
	f[2].base_last_point = ex_i[4];
	f[2].top_point = ex_i[9];
	//serdeczny
	f[3].base_first_point = ex_i[4];
	f[3].base_last_point = ex_i[5];
	f[3].top_point = ex_i[10];
	//maly
	f[4].base_first_point = ex_i[5];
	f[4].base_last_point = ex_i[6];
	f[4].top_point = ex_i[11];

	for (int j = 0; j<5; j++) {
		f[j].base_center_point = middle_point(f[j].base_first_point, f[j].base_last_point, w);
		f[j].base_length = dist(f[j].base_first_point, f[j].base_last_point, w);
		f[j].length = dist(f[j].base_center_point, f[j].top_point, w);
		f[j].distance_from_thumb = dist(f[j].base_center_point, f[0].base_last_point, w);
	}

	for (int j = 0; j<5; j++) {
		//tworzymy kopie obrazka wejsciowego
		unsigned char **a = get_copy_char_image(img, h, w);

		//tworzymy kopie jednego palca reki  - rysujemy czarne linie "odcinajace" kolejne palce, liczac przy okazji ople powierzchni palca
		unsigned char **b = new_char_image(h, w);
		memset((unsigned char*)(b[0]), 1, h*w*sizeof(unsigned char));
		draw_parallel_line_through_whole_image(a, h, w, f[j].base_first_point, f[j].base_last_point, f[j].base_first_point, 0, 3);
		f[j].area = T_ClearOneClasterAndCopy(a[0], b[0], w, h, 255, 0, f[j].top_point);

		//w b mamy jednego palca... możemy go kontrolnie narysować...
		//for(int y=0;y<h;y++) for(int x=0;x<w;x++) b[y][x]= b[y][x]==1?255:b[y][x];		//zamieniamy zerowe wartosci tla na 255 zebybyly widoczne
		//writePGMB_image("palec.pgm", b[0], h, w, 255);

		//przygotowujemy tablice wyjsciowa - patrz opis projektu studenckiego
		unsigned int **d = new_uint_image(h, w);

		unsigned max_r = 0;
		unsigned max_index = 0;
		//szukamy najwiekszego okręgu wpisanego w palec - w przypadku kciuka bedzie to tylko jeden okrag
		if (j == 0) {
			dt(d[0], b[0], h, w);
			for (int y = 0; y<h; y++) for (int x = 0; x<w; x++) if (d[y][x]>max_r) { max_r = d[y][x]; max_index = y*w + x; };

			f[0].circle_top_centre = max_index;
			f[0].circle_top_radius = (int)sqrt((float)max_r);
		}
		else {	//dla reszty palcow poza kciukiem dzielimy poszukiawanie okregu na 2 etapy - najpierw gornej polowie palca, a potem w dolnej
			//tworzymy dwie kopie danego palca - b_top i b_bottom
			int middle_finger_point = middle_point(f[j].base_center_point, f[j].top_point, w);

			draw_parallel_line_through_whole_image(b, h, w, f[j].base_first_point, f[j].base_last_point, middle_finger_point, 128, 3);

			//gorna polowa palca
			unsigned char **b_top = new_char_image(h, w);
			copy_top_image_part(b, b_top, h, w, 1, 128);

			dt(d[0], b_top[0], h, w);
			for (int y = 0; y<h; y++) for (int x = 0; x<w; x++) if (d[y][x]>max_r) { max_r = d[y][x]; max_index = y*w + x; };		//wyszukanie najwiekszego okregu w wynikach w d
			f[j].circle_top_centre = max_index;
			f[j].circle_top_radius = (int)sqrt((float)max_r);

			//tylko do celow wizualizacji...
			//for(int y=0;y<h;y++) for(int x=0;x<w;x++) b_top[y][x]= b_top[y][x]==0?255:b_top[y][x];		//zamieniamy zerowe wartosci tla na 255 zebybyly widoczne
			//writePGMB_image("palec_top.pgm", b_top[0], h, w, 255);

			//dolna polowa palca
			max_r = 0;
			max_index = 0;

			unsigned char **b_bottom = new_char_image(h, w);
			copy_bottom_image_part(b, b_bottom, h, w, 1, 128);

			dt(d[0], b_bottom[0], h, w);
			for (int y = 0; y<h; y++) for (int x = 0; x<w; x++) if (d[y][x]>max_r) { max_r = d[y][x]; max_index = y*w + x; };		//wyszukanie najwiekszego okregu w wynikach w d
			f[j].circle_bottom_centre = max_index;
			f[j].circle_bottom_radius = (int)sqrt((float)max_r);

			//tylko do celow wizualizacji...
			//for(int y=0;y<h;y++) for(int x=0;x<w;x++) b_bottom[y][x]= b_bottom[y][x]==0?255:b_bottom[y][x];		//zamieniamy zerowe wartosci tla na 255 zebybyly widoczne
			//writePGMB_image("palec_bottom.pgm", b_bottom[0], h, w, 255);

			delete[] b_top;
			delete[] b_bottom;
		}//else

		delete[] a;
		delete[] b;
		delete[] d;
	}//for
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


