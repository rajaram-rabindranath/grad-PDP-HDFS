#!/bin/bash

#### Please generate input files as specified on the README
#### cd input
#### java GenInput 100000 100K Then do the following
 
file=(100K) # 10Mill)
problemSize=(100000) #  1000000 10000000)

username=`whoami`
slurm=$1
## ARGUMENTS for the run
## $1 size of the problem
## $2 in_file
## $3 out_folder
## $4 username


for (( i = 0 ; i < ${#problemSize[@]} ; i++ )) 
do
	:
	sbatch $slurm ${file[$i]} ${problemSize[$i]} ${file[$i]} result_${file[$i]} $username
done






