FOLDER STRUCTURE:
twitter	-- Kmeans and NaiveBayes
		NaiveBayes -- contains the train and test vectors for which I got maximum accuracy
		also have "outputs" folder which contains all the outputs that i have got
Sort 	-- Contains "Sequential" folder and "Hadoop" folder
		Hadoop -- outputs folder contains all the outputs that i have got

HOW - TO:

VERY IMPOTANT NOTICE:

When running the program -- please make sure that 
	FOR Sequential code -- problem size has to be divisible by 5
	FOR Hadoop code -- follow the instructions given below

------------------------------------------------Sort/Sequential---------------------------------------------------
runner_sort_SEQ.sh -- the runner script for OMP

	$ ./runner_sort_SEQ.sh

The runner file has to be modified to change problem size
The things to be modified are right at the top of the file and have FIXME tags

// to set the problem size fill the primeLimits array like so 
## FIXME here is where one can set probSize like so --- probSize=(24)
probSize=(25)



--- to run in non batch and to execute local machine do the following:

	# to print output + time
	$ ./bucketSort <-t probSize> 

	# to print just time
	$ ./bucketSort <probSize> 


 $ make
 $ ./bucketSort -t 45

------------------------------------------------Sort/Hadoop---------------------------------------------------
I have provided the following : NOTE the outputs folder contains --- all the outputs that i have got

#### General execution:

$ hadoop jar in.jar BucketSort -t <problemSize> <fileName> <result_folderName> <username> <reducer#>

#### SLURM SCRIPTS

SLURM_HADOOP_2 (for 2 nodes)
SLURM_HAROOP_4 (for 4 nodes)
SLURM_HADOOP_8 (for 8 nodes)
SLURM_HADOOP_16 (for 16 nodes)

##### Generating input

$ cd input
$ java GenInput 100000 100K
$ cp 100K ../


###### Running the program ----- modify the "exec.sh" shell script the following lines

problemSize=(100000) ### for problemsize
file=(100K) ### name of the file being used as input

##### Now run the program like so using slurm files given
$ ./exec.sh SLURM_HADOOP_2

##### results will be out in the "result" folder please take a look at them

------------------------------------------ twitter ---------------------------------------

##### Kmeans --- do make changes to the output folder --- IMPORTANT
sbatch SLURM_kmeans.sh


#### Naive Bayes
sbatch SLURM_rajaramr_mahout_naive_bayes.sh









