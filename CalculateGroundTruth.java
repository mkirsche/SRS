package SRS;

import java.util.*;
import java.io.*;
import SRS.ReadUtils;
public class CalculateGroundTruth {
public static void main(String[] args) throws IOException
{
	if(args.length != 3)
	{
		System.out.println("Usage: java CalculateGroundTruth <simmed_readfile> <ctfile> <outfile>");
		return;
	}
	String fn = args[0];
	String ctFn = args[1];
	String ofn = args[2];
	
	ReadUtils.PeekableScanner input = new ReadUtils.PeekableScanner(new FileInputStream(new File(fn)));
	ReadUtils.FileType ft = ReadUtils.getFileType(input);
	ArrayList<Read> reads = new ArrayList<Read>();
	
	// Get read names from file and extract genomic intervals
	while(true)
	{
		String name = ReadUtils.getName(input, ft);
		if(name == null)
		{
			break;
		}
		else
		{
			reads.add(new Read(name));
		}
	}
	
	// Partition into contained vs non-contained
	HashSet<String>[] partition = calculateContainment(reads);
	
	// Read in containment threshold and add ground truth info to each line
	// Meanwhile, write updated lines to output file
	Scanner ctInput = new Scanner(new FileInputStream(new File(ctFn)));
	PrintWriter out = new PrintWriter(new File(ofn));
	while(ctInput.hasNext())
	{
		String line = ctInput.nextLine();
		String name = line.split(" ")[0];
		for(int i = 0; i<partition.length; i++)
		{
			if(partition[i].contains(name))
			{
				out.println(line+" "+i);
				break;
			}
		}
	}
	out.close();
}
/*
 * Separates reads by name into two sets
 * 0: Contained
 * 1: Non-contained
 */
static HashSet<String>[] calculateContainment(ArrayList<Read> reads)
{
	Collections.sort(reads);
	HashSet<String>[] res = new HashSet[2];
	for(int i = 0; i<res.length; i++)
	{
		res[i] = new HashSet<String>();
	}
	Collections.sort(reads);
	int maxEnd = -1;
	for(Read r : reads)
	{
		if(r.b < maxEnd)
		{
			res[0].add(r.name);
		}
		else
		{
			res[1].add(r.name);
			maxEnd = r.b;
		}
	}
	return res;
}
static class Read implements Comparable<Read>
{
	String name;
	int a, b;
	boolean isContained;
	Read(String nn)
	{
		name = nn;
		String[] parts = name.split("_");
		a = Integer.parseInt(parts[1]);
		b = Integer.parseInt(parts[2]);
	}
	Read(String nn, int aa, int bb)
	{
		a = aa; b = bb; name = nn;
	}
	@Override
	public int compareTo(Read o) {
		if(a != o.a) return a - o.a;
		return o.b - b;
	}
}
}
