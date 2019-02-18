package SRS;

import java.util.*;
import java.io.*;
public class SeparateContained {
public static void main(String[] args) throws IOException
{
	if(args.length != 3)
	{
		System.out.println("Usage: java SeparateContained <simmed_readfile> <containedoutfile> <uncontainedoutfile>");
		return;
	}
	String fn = args[0];
	String ofn1 = args[1];
	String ofn2 = args[2];
	String[] ofns = new String[] {ofn1, ofn2};
	
	ReadUtils.PeekableScanner input = new ReadUtils.PeekableScanner(new FileInputStream(new File(fn)));
	ReadUtils.FileType ft = ReadUtils.getFileType(input);
	ArrayList<Read> reads = new ArrayList<Read>();
	
	// Get read names from file and extract genomic intervals
	while(true)
	{
		String[] readData = ReadUtils.getLabelledRead(input, ft);
		if(readData == null)
		{
			break;
		}
		String name = readData[0];
		String read = readData[1];
		reads.add(new Read(name, read));
	}
	
	// Partition into contained vs non-contained
	HashSet<Read>[] partition = calculateContainment(reads);
	
	// Output each partition to its own file
	for(int i = 0; i<partition.length && i < ofns.length; i++)
	{
		PrintWriter out = new PrintWriter(new File(ofns[i]));
		for(Read r : partition[i])
		{
			out.println(">" + r.name);
			out.println(r.read);
		}
		out.close();
	}
}
/*
 * Separates reads by name into two sets
 * 0: Contained
 * 1: Non-contained
 */
static HashSet<Read>[] calculateContainment(ArrayList<Read> reads)
{
	Collections.sort(reads);
	HashSet<Read>[] res = new HashSet[2];
	for(int i = 0; i<res.length; i++)
	{
		res[i] = new HashSet<Read>();
	}
	Collections.sort(reads);
	int maxEnd = -1;
	for(Read r : reads)
	{
		if(r.b < maxEnd)
		{
			res[0].add(r);
		}
		else
		{
			res[1].add(r);
			maxEnd = r.b;
		}
	}
	return res;
}
static class Read implements Comparable<Read>
{
	String name;
	String read;
	int a, b;
	Read(String nn, String rr)
	{
		name = nn;
		read = rr;
		String[] parts = name.split("_");
		a = Integer.parseInt(parts[1]);
		b = Integer.parseInt(parts[2]);
	}
	Read(String nn, String rr, int aa, int bb)
	{
		a = aa; b = bb; name = nn; read = rr;
	}
	@Override
	public int compareTo(Read o) {
		if(a != o.a) return a - o.a;
		return o.b - b;
	}
}
}
