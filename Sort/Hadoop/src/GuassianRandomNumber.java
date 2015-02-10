
import java.util.Random;
import java.math.*;

public class GuassianRandomNumber 
{
	public static void randomNumberGeneratorNormal(float[] arr, int problemSize)
	{
		
		float maxNum = (float) (problemSize * 10.0);
		float mean = (float) (maxNum/2.0);
		float var = (float) Math.sqrt(maxNum);
		Random rnd = new Random();
		
		for( int i = 0; i < problemSize; i++){
			arr[i] = (float) (mean + rnd.nextGaussian()*var);
		}
		
	}
	
	//this is the test code
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		float[] arr = new float[10];
		randomNumberGeneratorNormal(arr, 10);
		for ( int i = 0; i < 10; i++)
		{
			System.out.println(Float.toString(arr[i]));
		}
	}

}
