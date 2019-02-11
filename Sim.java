/*
 * Simulates a set of reads from a genome of a given length and a fixed (point mutation) error rate
 * TODO: Add reverse complement, using error rate to produce errors in reads
 */
import java.io.*;
import java.util.*;

public class Sim {
	static double errorRate;
	static int genomeLength;
	static int coverage;
	static String sampleFn;
public static void main(String[] args) throws IOException
{
	if(parseArgs(args) != 0)
	{
		usage();
		return;
	}
	simulateReads();
}
static void usage()
{
	System.out.println("java Sim -l <genomeLength> -c <coverage> -s <sampleFn> -e <errorRate>");
	System.out.println("\nArgs:");
	System.out.println("\tgenomeLength: The length of the genome to simulate reads from");
	System.out.println("\tcoverage: The coverage of reads to simulate");
	System.out.println("\tsampleFn: A file of the type of reads being used for getting the length distribution");
	System.out.println("\terrorRate: The proportion of (point) mutations to use");
}
static int parseArgs(String[] args)
{
	boolean hasLength = false, hasCoverage = false, hasError = false, hasSample = false;
	try
	{
		for(int i = 0; i<args.length-1; i++)
		{
			if(args[i].equalsIgnoreCase("-l"))
			{
				hasLength = true;
				genomeLength = Integer.parseInt(args[i+1]);
			}
			else if(args[i].equalsIgnoreCase("-c"))
			{
				hasCoverage = true;
				coverage = Integer.parseInt(args[i+1]);
			}
			else if(args[i].equalsIgnoreCase("-e"))
			{
				hasError = true;
				errorRate = Double.parseDouble(args[i+1]);
			}
			else if(args[i].equalsIgnoreCase("-s"))
			{
				hasSample = true;
				sampleFn = args[i+1];
			}
		}
		if(!hasLength || !hasCoverage || !hasError || !hasSample)
		{
			return 1;
		}
	} 
	catch(Exception e)
	{
		return 1;
	}
	return 0;
}
static void simulateReads() throws IOException
{
	Random r = new Random(101);
	ReadUtils.OrderedFrequencyMap<Integer> ofm = ReadUtils.getLengths(sampleFn);
	int seed = 171;
	Distribution d = new Distribution(ofm, seed);
	long totalLength = (long)genomeLength * coverage;
	long curLength = 0;
	ArrayList<Interval> intervals = new ArrayList<Interval>();
	while(curLength < totalLength)
	{
		int len = d.sample();
		if(len > genomeLength) len = genomeLength;
		curLength += len;
		int startPos = r.nextInt(genomeLength - len + 1);
		int endPos = startPos + len;
		intervals.add(new Interval(startPos, endPos));
	}
	Collections.sort(intervals);
	int atStart = intervals.get(0).a, atEnd = intervals.get(0).a;
	int n = intervals.size();
	CharacterQueue cq = new CharacterQueue(101);
	for(int i = 0; i<n; i++)
	{
		cq.removeCharacters(intervals.get(i).a - atStart);
		atStart = intervals.get(i).a;
		if(intervals.get(i).b > atEnd)
		{
			cq.addRandomCharacters(intervals.get(i).b - atEnd);
		}
		String curRead = cq.errorSubstring(0, intervals.get(i).b - atStart);
		int strand = r.nextInt(2);
		if(strand == 1)
		{
			curRead = revComp(curRead);
		}
		System.out.println(">read_"+intervals.get(i).a+"_"+intervals.get(i).b+"_"+strand);
		System.out.println(curRead);
	}
}
static String revComp(String s)
{
	char[] rc = new char[256];
	rc[rc['A'] = 'T'] = 'A';
	rc[rc['C'] = 'G'] = 'C';
	char[] res = new char[s.length()];
	for(int i = 0; i<s.length(); i++) res[i] = rc[s.charAt(s.length()-1-i)];
	return new String(res);
}
static class Interval implements Comparable<Interval>
{
	int a, b;
	Interval(int aa, int bb)
	{
		a = aa; b = bb;
	}
	public int compareTo(Interval o)
	{
		if(a != o.a) return a - o.a;
		return b - o.b;
	}
}
/*
 * Supports the following operations:
 *   add x random A/C/G/T characters to the end
 *   remove first y characters
 *   find arbitrary substring
 *   clear
 */
static class CharacterQueue
{
	int maxLength;
	char[] data;
	int head;
	int tail;
	static char[] alpha = {'A', 'C', 'G', 'T'};
	static int charToInt(char c)
	{
		for(int i = 0; i<alpha.length; i++)
		{
			if(c == alpha[i])
			{
				return i;
			}
		}
		return -1;
	}
	Random r;
	CharacterQueue(int seed)
	{
		maxLength = 16384;
		data = new char[maxLength];
		r = new Random(seed);
	}
	boolean isEmpty()
	{
		return head == tail;
	}
	void clear()
	{
		head = tail;
	}
	void addRandomCharacters(int count)
	{
		int length = length();
		int nlength = length + count;
		if(nlength > maxLength)
		{
			char[] oldString = substring(0, length).toCharArray();
			while(maxLength < nlength) maxLength *= 2;
			data = new char[maxLength];
			for(int i = 0; i<length; i++) data[i] = oldString[i];
			head = 0;
			tail = length;
		}
		for(int i = 0; i<count; i++)
		{
			data[tail] = alpha[r.nextInt(alpha.length)];
			tail++;
			if(tail == maxLength) tail = 0;
		}
		length = nlength;
	}
	void removeCharacters(int count)
	{
		if(count > length())
		{
			clear();
			return;
		}
		head += count;
		if(head > maxLength) head -= maxLength;
	}
	String substring(int start, int end)
	{
		int length = length();
		if(start < 0) start = 0;
		if(end > length) end = length;
		char[] res = new char[end-start];
		int at = head+start;
		if(at > maxLength) at -= maxLength;
		for(int i = 0; i<end-start; i++)
		{
			res[i] = data[at];
			at++;
			if(at == maxLength) at = 0;
		}
		return new String(res);
	}
	String errorSubstring(int start, int end)
	{
		int length = length();
		if(start < 0) start = 0;
		if(end > length) end = length;
		char[] res = new char[end-start];
		int at = head+start;
		if(at > maxLength) at -= maxLength;
		for(int i = 0; i<end-start; i++)
		{
			double rand = r.nextDouble();
			if(rand < errorRate)
			{
				int offset = 1 + r.nextInt(3);
				res[i] = alpha[(charToInt(data[at]) + offset)&3];
			}
			else
			{
				res[i] = data[at];
			}
			at++;
			if(at == maxLength) at = 0;
		}
		return new String(res);
	}
	int length()
	{
		if(head <= tail) return tail - head;
		return maxLength - head + tail;
	}
}
/*
 * Allows sampling from a discrete distribution given the counts of all elements in it
 */
static class Distribution
{
	int total;
	TreeMap<Integer, Integer> invFrequency;
	Random r;
	Distribution(ReadUtils.OrderedFrequencyMap<Integer> ofm, int seed)
	{
		invFrequency = new TreeMap<Integer, Integer>();
		int cFreq = 0;
		for(int x : ofm.freq.keySet())
		{
			cFreq += ofm.count(x);
			invFrequency.put(cFreq-1, x);
		}
		r = new Random(seed);
		total = cFreq;
	}
	int sample()
	{
		int cur = r.nextInt(total);
		return invFrequency.get(invFrequency.ceilingKey(cur));
	}
}
}
