import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class GenInput 
{
	
	public static void main(String args[])
	{
		
		int problemSize=Integer.valueOf(args[0]);
		String filepath=args[1]; 
		float[] data = new float[problemSize];
		GuassianRandomNumber.randomNumberGeneratorNormal(data,problemSize);
		try
		{
			File store = new File(filepath);
			if(!store.createNewFile())
			{
				store.delete();
				store.createNewFile();
			}
			// lets make it a 10 CSV elements / row
			BufferedWriter bw = new BufferedWriter(new FileWriter(store));
			StringBuffer n = new StringBuffer("");
			
		
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
	}
}
