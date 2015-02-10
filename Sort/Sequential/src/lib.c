/*
 * lib.c
 *
 *  Created on: Oct 24, 2014
 *      Author: dev
 */

#include <stdio.h>
#include <stdlib.h>
#include <appMacros.h>
#include <string.h>
#include <lib.h>

void printBucket(float* bucket,int low, int high)
{
	for(int i=low;i<high;i++)
	{
		printf("the val %f \n",bucket[i]);
	}

}

void printUsage(char* appName)
{
	printf("---------------- Wrong number of arguments ---------------\n");
	printf("%s <PROBLEM_SIZE>\n",appName);
	printf("%s <-t PROBLEM_SIZE> \n",appName);
	printf("---------------- Wrong number of arguments ---------------\n");
}

int parseArgs(char** argv,int* problemSize,int* print,int arg_count)
{
	if(arg_count == 3)
	{
		if(!strcmp(argv[ARGS_P_SIZE_ID],ARGS_P_SIZE_ID_CODE))
		{
			*print=TRUE;
			*problemSize=atoi(argv[ARGS_P_SIZE]);
		}
	}
	else if(arg_count == 2)
	{
		*problemSize =atoi(argv[1]);
	}
	return SUCCESS;
}

void insertionSort(float* data, int size)
{
	float tmp=0.0f;
	int j=0;
	for(int i=1; i < size; i++)
	{
		tmp=data[i];
		j = i-1;
		while(tmp<data[j] && j>=0)
		{
			data[j+1] = data[j];
			j = j-1;
		}
		data[j+1]=tmp;
	}
}



void  swap(float *a,float *b)
{
	float t=*a;
	*a=*b;
	*b=t;
}

float kthsmallest(float data[],int size, int k)
{
	int i=0,foundK=0;
	int p,q;
	float r;
	p=0;
	r=size-1;
	k--;
	while(!foundK)
	{
		q=partition_for_K(data,p,r);
		if(q==k)
		{
			foundK=1;
		}
		else if(k<q)
		{
			r=q-1;
		}
		else
		{
			p=q+1;
		}
	}
	return data[k];
}


float partition_for_K(float data[],int p,int r)
{
	int i,j;
	float pivot;
	i=p-1;
	pivot=data[r];
	for(j=p;j<r;j++)
	{
	   if(data[j]<pivot)
	  {
		i++;
		swap(&data[j],&data[i]);
	   }
	}
	swap(&data[i+1],&data[r]);
	return i+1;
}
