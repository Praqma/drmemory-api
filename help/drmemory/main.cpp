#include <stdlib.h>

int main() {

	char *ptr = (char *)malloc( sizeof( char ) );

	*ptr = '7';
	*(++ptr) = '8';

	char arr[2] = { '1', '2' };

	return 0;
}