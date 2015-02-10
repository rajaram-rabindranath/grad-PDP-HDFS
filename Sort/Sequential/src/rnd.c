#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <math.h>

#include "ziggurat.h"
#include "rnd.h"


// generate random number in uniform distribution
// input: arr, number arr
// input: size, size of the array
// input: max_number, maximum number
void random_number_generator_simple(float* arr, int size, int max_number)
{
	srand(time(NULL));
	for (int i = 0; i < size; i++)
	{
		arr[i] = ( rand() / ((double)RAND_MAX+1)) * (max_number+1);
	}
}

// generate random number in normal distribution
// input: arr, number arr
// input: size, size of the array
// input: max_number, maximum number
void random_number_generator_normal(float* arr, int size, int max_number)
{
	uint32_t kn[128];
	float fn[128], wn[128];
	r4_nor_setup ( kn, fn, wn);
	float rnd;
	uint32_t seed = (uint32_t)time(NULL);
	float var = sqrt(max_number);
	for ( int i = 0; i < size; i++)
	{
		rnd = r4_nor(&seed, kn, fn, wn);
		arr[i] = max_number/2 + rnd*var;
	}
}
