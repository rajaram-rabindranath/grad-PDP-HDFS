
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


public class BucketSort 
{

	public static void main(String args[]) throws IOException, URISyntaxException, ClassNotFoundException,InterruptedException
	{
		/** 	Need 
		 * 1. PROBLEM_SIZE
		 * 2. IN_FILE 
		 * 3. OUT_FILE
		 * 4. HDFS_PATH
		 * 5. # TASKS == REDUCERS
		 * **/
		if(args.length !=5)
		{
			System.out.println("Wrong number of arguments");
		}
		
		prasedArgs a = parseArgs(args);
		// must call args parser
		if(a == null) 
		{
			return;
		}
		String problemSize=Integer.toString(a.problemSize);
		String path=File.separator+"user"+File.separator+a.username+File.separator;
		String in_file=a.in_file;
		String out_file=a.out_file;
		String numReducers=Integer.toString(a.numreducers); // must put a condition here that 
		
		/*String problemSize = args[0];
		String path="hdfs://localhost:54310/bucket/";
		String in_file="store.txt";
		String out_file="result";
		String numReducers=args[1];
		*/
		System.out.println("Num reducers"+numReducers);
		System.out.println("Problem size "+problemSize);
		System.out.println("in_file "+in_file);
		System.out.println("out_file "+out_file);
		System.out.println("path ="+path);
		
		/**
		 * Launch MR jobs
		 */
		int max_value = Integer.valueOf(problemSize)*10;
		Configuration conf = new Configuration();
		long milliSeconds = 1800000*2; 
		conf.setLong("mapred.task.timeout", milliSeconds);
		FileSystem fs = FileSystem.get(conf);
		Path inFile = new Path(path+in_file);
		Path outFile = new Path(path+out_file);
		if (fs.exists(outFile))
		{
			fs.delete(outFile,true);
		}
		
		
		/**
		 * Setting params for the job
		 */
		conf.set("limit",Integer.toString(max_value));
		conf.set("numBuckets",numReducers);
		
		
		
		Job job = new Job(conf,"BSORT_HADOOP");
		job.setJarByClass(BucketSort.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		job.setNumReduceTasks(Integer.valueOf(numReducers));
		
		job.setMapperClass(BucketSortMapper.class);
		job.setPartitionerClass(BucketPartitioner.class);
		job.setReducerClass(BucketSortReducer.class);
		    
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		    
		FileInputFormat.addInputPath(job,inFile);
		FileOutputFormat.setOutputPath(job,outFile);
		
		long startMilli =System.currentTimeMillis();
		long startNano = System.nanoTime();    
		job.submit();
		
		
		// collect job level statistics to time the job
		try
		{
			job.waitForCompletion(true);
			long estMilli=System.currentTimeMillis() - startMilli;
			long estNano = System.nanoTime() - startNano;
			
			// read data
			if(a.print)
			{
				readReducer_out(path+out_file);
			}
			
			int size =  Integer.valueOf(problemSize)+1;
			
			if(job.isSuccessful())
			{
				System.out.println("\n\n JOB WAs SUCCESSFUL\n\n");
			}
			
			System.out.println(size+" ::time taken ::"+(double)estNano/1000000+" ms");
			System.out.println(problemSize+" ::estimated milli "+estMilli+" ms");
			
			/**
			 * Print over all time -- a perspective from main()
			 */
			ArrayList<TaskCompletionEvent> completionEvents = new ArrayList<TaskCompletionEvent>();
			while (true) 
			{
			      try 
			      {
			        TaskCompletionEvent[] bunchOfEvents;
			        bunchOfEvents = job.getTaskCompletionEvents(completionEvents.size());
			       
			        if (bunchOfEvents == null || bunchOfEvents.length == 0) 
			        {
			          break;
			        }
			        completionEvents.addAll(Arrays.asList(bunchOfEvents));
			      } 
			      catch (IOException e) 
			      {
			        break;
			      }
			}
			
			
			System.out.println("=================== TIME TAKEN ====================");
			for (int i = 0; i < completionEvents.size(); i++)
			{
				
				TaskCompletionEvent task = completionEvents.get(i);
				if(!task.isMapTask())
				{
					System.out.print("Task name ::"+task.getTaskAttemptId());
					System.out.println(" took "+completionEvents.get(i).getTaskRunTime()+" ms");
				}
			}  
		}
		catch(ClassNotFoundException clnf)
		{
			clnf.printStackTrace();
			System.out.println("Class not found exception!");
		}
		catch(InterruptedException iex)
		{
			iex.printStackTrace();
		}
	
		
	}
	
	
	public static class BucketPartitioner extends Partitioner<Text, DoubleWritable> 
	{
		 
        @Override
        public int getPartition(Text key, DoubleWritable value, int numReduceTasks) 
        {
 
            int reducer_num = Integer.valueOf(key.toString());
            System.out.println("num reduce taks"+numReduceTasks);
            
            //this is done to avoid performing mod with 0
            if(numReduceTasks == 0)
                return 0;
            return (reducer_num-1);
        }
    }
	
	/**
	 * 
	 * 
	 * @author dev
	 *
	 */
	public static class BucketSortMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> 
	{
	   private Text bucket = new Text();
	   public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException 
	   {
		   
		   	/**
		   	 * Size of the data known
		   	 * Max limit of the data also known
		   	 * Read the data from input split
		   	 */
	    	Configuration conf = context.getConfiguration();
	    	int maxLimit=Integer.valueOf(conf.get("limit"));
	    	int numbuckets=Integer.valueOf(conf.get("numBuckets"));
	    	double mean = maxLimit/2;
	        double var = (double) Math.sqrt(maxLimit);
	        
	        String line = value.toString();
	        StringTokenizer tokenizer = new StringTokenizer(line,",");
	        
	        while (tokenizer.hasMoreTokens()) 
	        {
	        	Double val = Double.parseDouble(tokenizer.nextToken());
	        	bucket.set(bucket(numbuckets,mean,var,Double.valueOf(val.toString())));
	        	context.write(bucket,new DoubleWritable(val));
	        }
	    }
	   private String bucket(int buckets,double mean,double var,double value)
	   {
		   String buck="";
		   
		   if(buckets == 2)
		   {
			   if(value < mean)
			   {
				   buck= "1";
			   }
			   else
			   {
				   buck= "2";
			   }
		   }
		   else if(buckets == 4)
		   {
			   buck=bucket4(mean,var,value);
		   }
		   else if(buckets == 8)
		   {
			   buck=bucket8(mean,var,value);
		   }
		   else if (buckets == 16)
		   {
			   buck=bucket16(mean,var,value);
		   }
		   else
		   {
			   buck=bucket4(mean,var,value);
		   }
		   return buck;
	   }
	   
	   public String bucket4(double mean,double var,double val)
	   {
		   System.out.println("SPLITTING INTO 4 BUCKETS");
		   double bucket1 = (double)(mean - 0.67*var);
		   double bucket3 = (double)(mean + 0.67*var);
		   
		   if(val < bucket1)
		   {
			   return "1";
		   }
		   else if(val < mean && val >= bucket1)
		   {
			   return "2";
			   
		   }
		   else if(val < bucket3 && val >= mean)
		   {
			   return "3";
		   }
		   else if(val >= bucket3)
		   {
			   return "4";
		   }
		   else
		   {
			   return "1";
		   }
		   
	   }
	   public String bucket8(double mean,double var,double val)
	   {
		   System.out.println("SPLITTING INTO 8 BUCKETS");
		   double bucket1 = (double)(mean - 1.23*var);
		   double bucket2 = (double)(mean - 0.67*var);
		   double bucket3 = (double)(mean - 0.32*var);
			// mean
		   double bucket4 = (double)(mean + 0.32*var);
		   double bucket5 = (double)(mean + 0.67*var);
		   double bucket6 = (double)(mean + 1.23*var);
		
		   
		   if(val < bucket1)
			{
				return "1";
			}
			else if(val < bucket2 && val >= bucket1)
			{
				return "2";
			}
			else if(val < bucket3 && val >= bucket2)
			{
				return "3";
			}
			else if(val < mean && val >=bucket3)
			{
				return "4";
			}
			else if(val < bucket4 && val >= mean)
			{
				return "5";
			}
			else if(val < bucket5 && val >= bucket4)
			{
				return "6";
			}
			else if(val < bucket6 && val >= bucket5)
			{
				return "7";
			}
			else if(val >= bucket6)
			{
				return "8";
			}
			else
			{
				return "1";
			}
		   
	   }
	   public String bucket16(double mean,double var,double val)
	   {
		   
		   System.out.println("SPLITTING INTO 16 BUCKETS");
		   	double bucket1 = (double)(mean - 1.53*var); // .625
			double bucket2 = (double)(mean - 1.23*var); // .125
			double bucket3 = (double)(mean - 0.89*var); // .185
			double bucket4 = (double)(mean - 0.67*var); // .25
			double bucket5 = (double)(mean - 0.49*var); // .31
			double bucket6 = (double)(mean - 0.32*var); // .37
			double bucket7 = (double)(mean - 0.16*var); // .43
			
			double bucket8 = (double)(mean + 0.16*var); // .43
			double bucket9 = (double)(mean + 0.32*var); // .37
			double bucket10 = (double)(mean + 0.49*var); // .31
			double bucket11 = (double)(mean + 0.67*var); // .25
			double bucket12 = (double)(mean + 0.89*var); // .185
			double bucket13 = (double)(mean + 1.23*var); // .125
			double bucket14 = (double)(mean + 1.53*var); // .625
			if(val < mean)
			{
				if(val < bucket1)
				{
					return "1";
				}
				else if(val < bucket2 && val >= bucket1)
				{
					return "2";
				}
				else if(val < bucket3 && val >= bucket2)
				{
					return "3";
				}
				else if(val < bucket4 && val >= bucket3)
				{
					return "4";
				}
				else if(val < bucket5 && val >= bucket4)
				{
					return "5";
				}
				else if(val < bucket6 && val >= bucket5)
				{
					return "6";
				}
				else if(val < bucket7 && val >= bucket6)
				{
					return "7";
				}
				else if(val < mean && val >= bucket7)
				{
					return "8";
				}
				else
				{
					return "1";
				}
				
			}
			else
			{
				System.out.println("I am here");
				if(val < bucket8 && val >=mean)
				{
					return "9";
				}
				else if(val < bucket9 && val >=bucket8)
				{
					return "10";
				}
				else if(val < bucket10 && val>=bucket9)
				{
					return "11";
				}
				else if(val < bucket11 && val>=bucket10)
				{
					return "12";
				}
				else if(val < bucket12 && val>=bucket11)
				{
					return "13";
				}
				else if(val < bucket13 && val>= bucket12)
				{
					return "14";
				}
				else if(val < bucket14 && val >= bucket13)
				{
					return "15";
				}
				else if(val >= bucket14)
				{
					return "16";
				}
				else
				{
					return "1";
				}
			}
			
			
	   }
	 } 
	  
	/**
	 * 
	 * @author dev
	 *
	 */
	 public static class BucketSortReducer extends Reducer<Text,DoubleWritable, Text, DoubleWritable> 
	 {
		public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException 
	    {
			ArrayList<Double> store = new ArrayList<Double>();
			
			try
			{
				for(DoubleWritable value : values) 
		        {
		        	store.add(new Double(value.toString()));
		        }
			}
			catch(NumberFormatException nex)
			{
				System.out.println("We have a number format exception");
			}
			
			System.out.println(key+" size "+ store.size());
			
        	double []array = new double[store.size()];
        	for(int i =0;i<store.size();i++)
        	{
        		array[i] = store.get(i);
        	}
        	//array=insertionSort(array);
        	int n = array.length;
            for (int j = 1; j < n; j++) 
            {
                double k = array[j];
                int i = j-1;
                while ( (i > -1) && ( array [i] > k ) ) 
                {
                    array [i+1] = array [i];
                    i--;
                }
                array[i+1] = k;
            }
            
        	// write all the data in sorted order
	        for(int i=0;i<array.length;i++)
	        {
	        	context.write(key,new DoubleWritable(array[i]));
	        }
	    }
	 }
	
	public static boolean writeHDFS(File inFile) 
	{
		Configuration configuration = new Configuration();
		System.out.println("Writing data to hdfs");
		try
		{
			InputStream inputStream = new BufferedInputStream(new FileInputStream(inFile));
			FileSystem hdfs = FileSystem.get(new URI("hdfs://localhost:54310"), configuration);
			OutputStream outputStream = hdfs.create(new Path("hdfs://localhost:54310/bucket/store.txt"),
			new Progressable() 
			{  
				@Override
				public void progress()
				{
					System.out.println("....");
				}
			});
			try
			{
				IOUtils.copyBytes(inputStream, outputStream, 4096, false); 
			}
			finally
			{
				IOUtils.closeStream(inputStream);
				IOUtils.closeStream(outputStream);
			}
		}
		catch(URISyntaxException uriex)
		{
			uriex.printStackTrace();
			return false;
		}
		catch (IOException ioex) 
		{
			ioex.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public static Double[] insertionSort(Double array[]) 
	{
        int n = array.length;
        for (int j = 1; j < n; j++) 
        {
            Double key = array[j];
            int i = j-1;
            while ( (i > -1) && ( array [i] > key ) ) 
            {
                array [i+1] = array [i];
                i--;
            }
            array[i+1] = key;
        }
        
        return array;
    }	
	public static void printUsage()
	{
		System.out.println("------------USAGE-------------");
		System.out.println("$java <app_name> -t <problemSize>");
		System.out.println("or");
		System.out.println("$java <app_name> <problemSize>");
		System.out.println("------------USAGE-------------");
	}
	
	
	public static prasedArgs parseArgs(String args[]) 
	{
		
		prasedArgs a = new prasedArgs();
		/*		
			## ARGUMENTS for the run
			## $1 size of the problem
			## $2 in_file
			## $3 out_folder
			## $4 username 
			## $5 num_reducers
			(or)
			## ARGUMENTS for the run
			## $1 -t 
			## $2 size of the problem
			## $3 in_file
			## $4 out_folder
			## $5 username
			## $6 num_reducers
		 */
		int index=0;
		// not legit num of arguments
		if(!(args.length == 6 || args.length == 5))
		{
			return null;
		}
		else
		{
			int offset=-1;
			if(args.length == 6)
			{
				offset+=1;
				if(args[offset].equals("-t"))
				{
					a.print=true;
				}
			}
			offset+=1;
			try
			{
				a.problemSize = Integer.valueOf(args[offset]);
				offset+=1;
				a.in_file=args[offset];
				offset+=1;
				a.out_file=args[offset];
				offset+=1;
				a.username=args[offset];
				offset+=1;
				a.numreducers=Integer.valueOf(args[offset]);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				System.out.println("SOMETHING WRONG WITH THE ARGUMENT");
				for(String arg : args)
				{
					System.out.println(arg);
				}
				printUsage();return null;
			}
		}
		return a;
	}
	
	public static void readReducer_out(String targetFolder)
	{
		try
		{
			System.out.println("=================== OUTPUT ====================");
            FileSystem fs = FileSystem.get(new Configuration());
            FileStatus[] status = fs.listStatus(new Path(targetFolder));
            for (int i=0;i<status.length;i++)
            {
            	//System.out.println("filename "+status[i].getPath());
            	if(status[i].isDir())
            	{
            		continue;
            	}
            	// do some pattern matching
                BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(status[i].getPath())));
                String line;
                line=br.readLine();
                while (line != null)
                {
                        System.out.println(line);
                        line=br.readLine();
                }
            }
		}
		catch(Exception e)
		{
            System.out.println("File not found");
		}
	}
	
	public static class mapper extends Mapper<LongWritable, Text, FloatWritable, Text>
	{
		Text word = new Text();
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			/**
		   	 * Size of the data known
		   	 * Max limit of the data also known
		   	 * Read the data from input split
		   	 */
		   
	    	Configuration conf = context.getConfiguration();
	        float bucketSize = Integer.valueOf(conf.get("limit"))/2;
	        String line = value.toString();
	        StringTokenizer tokenizer = new StringTokenizer(line,",");
	        /*
	         * Generate key,value pairs of the like
	         * bucket_assignment,float_value
	         * think of even distribution -- later
	         */
	        while (tokenizer.hasMoreTokens()) 
	        {
	        	Float val = Float.parseFloat(tokenizer.nextToken());
	        	if(val < bucketSize)
	        	{
	        		word.set("1");
	        	}
	        	else
	        	{
	        		word.set("2");
	        	}
	            context.write(new FloatWritable(val),word);
	        }
		}
	}
	
	public static class reducer extends Reducer<FloatWritable,Text, Text, FloatWritable>
	{
		public void reduce(FloatWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			ArrayList<Double> store = new ArrayList<Double>();
			for(Text value : values) 
	        {
				context.write(value,key);
	        }
		}
	}
}

class prasedArgs
{
	
	/*## ARGUMENTS for the run
	## $1 size of the problem
	## $2 in_file
	## $3 out_folder
	## $4 username 
	*/
	int problemSize;
	boolean print=false;
	String in_file;
	String out_file;
	String username;
	int numreducers;
}


/*int argsCount = args.length;
prasedArgs a = new prasedArgs();
System.out.println("count of args "+argsCount);
*//**
 * Arguments must be --- 
 * problemSize or
 * -t problem size
 *//*
if(argsCount!=2)
{
	BucketSort.printUsage();
	return;
}
if(!argsParsing(args,a))
{
	BucketSort.printUsage();
	return;
}

if(a.problemSize==0) return;

max_value =a.problemSize*10;

// get the random numbers
float[] data =  new float[a.problemSize];
GuassianRandomNumber.randomNumberGeneratorNormal(data,a.problemSize);
for(int i =0;i<data.length;i++)
{
	System.out.println(i+" value "+data[i]);
}

// write data to file
String filename="/home/hduser/assign/storeFile.txts";
File store = new File(filename);
if(!store.createNewFile())
{
	store.delete();
	store.createNewFile();
}
// lets make it a 10 CSV elements / row
BufferedWriter bw = new BufferedWriter(new FileWriter(store));
StringBuffer n = new StringBuffer("");


try
{
	int j=1;
	for(int i =0;i<data.length;i++,j++)
	{
		n.append(data[i]);
		n.append(",");
		if(j%10==0)
		{
			bw.write(n.toString());
			bw.write("\n");
			n= new StringBuffer("");
		}
	}
	
	bw.flush();
	bw.close();
}
catch(IOException ioex)
{
	ioex.printStackTrace();
	System.out.println("Could not write to file!");
}

if(!writeHDFS(store))
{
	System.out.println("Could not transfer data to hdfs");
}
*/