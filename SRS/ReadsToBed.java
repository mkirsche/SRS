package SRS;

import java.io.*;
public class ReadsToBed {
public static void main(String[] args) throws IOException
{
	if(args.length != 1)
	{
		System.out.println("Usage: java ReadsToBed <readsfile>);");
		return;
	}
	
	String fn = args[0];
	ReadUtils.PeekableScanner input = new ReadUtils.PeekableScanner(new FileInputStream(new File(fn)));
	ReadUtils.FileType ft = ReadUtils.getFileType(input);
	
	while(input.hasNext())
	{
		String name = ReadUtils.getName(input, ft);
		String[] parts = name.split("_");
		System.out.print("ref");
		for(int i = 1; i<parts.length; i++)
		{
			System.out.print("\t" + parts[i]);
		}
		System.out.println();
	}
}
}
