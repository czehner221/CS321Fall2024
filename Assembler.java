// Honor Pledge:
//
// I pledge that I have neither given nor
// received any help on this assignment.
//
// czehner

import java.io. *;
import java.util.*;

// New object for entries of Symbol Table
class Entry
{
	// Symbol table has a symbol and its value
	public String symbol;
	public int value;
	
	// Entry constructor
	public Entry(String symbol, int value)
	{
		this.symbol = symbol;
		this.value = value; 
	}
	
	// Accessors
	public int getValue()
	{
		return value;
	}
	
	public String getSymbol()
	{
		return symbol;
	}
}

class Assembler
{
	public static void main(String [] args)
	throws IOException
	{
		// Keep track of line number
		int lineCount = 0;
		
		// Keeps track of symbols 
		int symbolCount = 0;
		
		// Used for updating temporary values to an updated memory location
		int updatedMemoryLocation = 16;
		
		// User enters name of file
		Scanner cin = new Scanner(System.in);
		String filename;
		System.out.print("What is your file's name? ");
		filename = cin.next();
		
		System.out.println(filename);
		
		// Create a symbol table using an object array of entries
		Entry[] symbolTable = new Entry[20000];
		
		
		// Read from text file
		Scanner file = new Scanner(new FileReader(filename));
		
		// Loops through file
		while(file.hasNext())
		{
			// Identify and remove comments 
			String line = file.nextLine().trim();
			if(line.contains("//"))
			{
				int pos = line.indexOf("//");
				line = line.substring(0, pos).trim();
			}
			
			// Extract labels
			if(line.startsWith("(") && line.endsWith(")"))
			{	
				String symbol = line.substring(1, line.length() - 1);
				
				// Updates if its a label
				boolean updated = updateSymbol(symbolTable, symbol, lineCount, symbolCount);
				
				// If it's not already in the table, it is added
				if(!updated)
				{
					if(symbolCount < symbolTable.length)
					{
						symbolTable[symbolCount++] = new Entry(symbol, lineCount);
					}
					else
					{
						System.out.println("Symbol Table is full.");
					}
				}
				
			}
			
			// Increase line count if it's an A or C instruction
			if(line.startsWith("@") || line.contains("=") || line.contains(";"))
			{
				lineCount++;
			}
			
			// Extract A instructions
			if(line.startsWith("@"))
			{
				String symbol = line.substring(1).trim();
				
				// If the A instruction is numeric (@100)
				if (isNumeric(symbol.trim())) 
				{
					// Add the numeric value to the symbol table with its value
					int value = Integer.parseInt(symbol);

					// If it doesn't already exists it is added to the symbol table
					if (!symbolFound(symbolTable, symbol, symbolCount)) 
					{
						if (symbolCount < symbolTable.length) 
						{
							symbolTable[symbolCount++] = new Entry(symbol, value);
						}
					}
				} 
				
				// If the symbol is not found in the table it will be added
				if(!symbolFound(symbolTable, symbol, symbolCount))
				{
					// Default value for undefined symbols
					int value = -1;
					
					// If the symbol is a Register
					if(symbol.startsWith("R") && symbol.length() > 1)
					{
						// Parse the symbol to get the register value
						int registerVal = Integer.parseInt(symbol.substring(1));
						
						// Set the value of the register symbol to its register number if it is within registers 0-15
						if(registerVal >= 0 && registerVal <= 15)
						{
							value = registerVal;
						}
					}
					else // Other predefined symbols
					{
						if(symbol.equals("SCREEN"))		// SCREEN
						{
							value = 16384;
						}
						else if(symbol.equals("KBD"))	// KEYBOARD
						{
							value = 24576;
						}
						else if(symbol.equals("SP"))	// SP
						{
							value = 0;
						}
						else if(symbol.equals("LCL"))	// LCL
						{
							value = 1;
						}
						else if(symbol.equals("ARG"))	// ARG
						{
							value = 2;
						}
						else if(symbol.equals("THIS"))	// THIS
						{
							value = 3;
						}
						else if(symbol.equals("THAT"))	// THAT
						{
							value = 4;
						}
					}
					
					// Add entry to symbol table
					if(value != -1 || symbolCount < symbolTable.length)
					{
						symbolTable[symbolCount++] = new Entry(symbol, value);
					}
				}
			}
		}
		
		// Updates memory location for temporary values (-1 turns into appropriate memory location starting with 16)
		for(int i = 0; i < symbolCount; i++)
		{
			if(symbolTable[i].value == -1)
			{
				updateSymbol(symbolTable, symbolTable[i].symbol, updatedMemoryLocation++, symbolCount);
			}
		}
		
		// Print the symbol table
		System.out.println("Symbol Table");
		for(int i = 0; i < symbolCount; i++)
		{
			System.out.println(symbolTable[i].symbol + " | " + symbolTable[i].value);
		}
		
		// Close the file.
		file.close();
		
		// PASS 2
		System.out.println("\nPass 2:\n");
		
		Pass2(filename, symbolTable);
		
	}
	
	// Method to check if the symbol already exists in the symbol table
	private static boolean symbolFound(Entry[] symbolTable, String symbol, int count)
	{
		for(int i = 0; i < count; i++)
		{
			// If the symbol is matched, returns true
			if(symbolTable[i].symbol.equals(symbol))
			{
				return true;
			}
		}
	
		return false;
	}
	
	// Updates value of a symbol in the table
	private static boolean updateSymbol(Entry[] symbolTable, String symbol, int newValue, int count)
	{
		for(int i = 0; i < count; i++)
		{
			// If the symbol is found, the value is updated
			if(symbolTable[i].symbol.equals(symbol))
			{
				symbolTable[i].value = newValue;
				
				return true;
			}
		}
		
		return false;
	}
	
	// Checks for numeric value for A instruction (Ex. @100's value is 100)
	private static boolean isNumeric(String symbol)
	{
		// Tries to parse the string into an int. If it doesn't work it returns false.
		try 
		{
			Integer.parseInt(symbol);
			return true;
		} 
		catch (NumberFormatException e) 
		{
			return false;
		}
	}
	
	// PASS 2
	private static void Pass2(String filename, Entry[] symbolTable) throws IOException
	{
		// Continues with the file that the user enters in main
		Scanner file = new Scanner(new FileReader(filename));
		
		// Print writer for the binary output
		PrintWriter writer = new PrintWriter(new FileWriter("binary.txt"));
		
		// While the file has a next line...
		while(file.hasNext())
		{
			String line = file.nextLine().trim();
			
			// Gets rid of comments
			if(line.contains("//"))
			{
				
				int pos = line.indexOf("//");
				line = line.substring(0, pos).trim();
				
			}
			
			// Ignores Labels and Empty lines
			if(line.startsWith("(") || line.isEmpty())
			{
				continue;
			}
			
			//System.out.println(line);
			
			// A instruction
			if(line.contains("@"))
			{
				// Extracts the symbol
				String symbol = line.substring(1).trim();
				
				// Gets the value for the symbol
				int value = getSymbolTableValue(symbolTable, symbol);
				
				// Converts the symbol to binary
				String binary = convertBinary(value);
				
				// Prints the binary to the console and the txt file
				System.out.println(binary);
				writer.println(binary);
				
			}
			else // C Instruction
			{
				// Destination, computation, and jump strings
				String destination = "";
				String computation = "";
				String jump = "";
				
				// Parsing the destination
				if(line.contains("="))
				{
					int equalsIndex = line.indexOf("=");
					destination = line.substring(0, equalsIndex).trim();
					line = line.substring(equalsIndex + 1).trim();
				}
				
				// Parsing the computation and jump
				if(line.contains(";"))
				{
					int semiIndex = line.indexOf(";");
					computation = line.substring(0, semiIndex).trim();
					jump = line.substring(semiIndex + 1).trim();
				}
				else // Computation
				{
					computation = line.trim();
				}
				
				// Concatenate the 3 strings to make the binary for the C instruction
				String binary = "111" + compBinary(computation) + destBinary(destination) + jumpBinary(jump);
				
				// Print the binary to the console and the binary.txt
				System.out.println(binary);
				writer.println(binary);
				
			}
			
		}
		
		// Close files
		file.close();
		writer.close();
		
		// End of pass 2 message
		System.out.println("\nEnd of Pass 2. Binary output written to binary.txt");
		
	}
	
	// Returns the value of a symbol as an integer
	public static int getSymbolTableValue(Entry[] symbolTable, String symbol)
	{
		// Runs for length of symbol table to find the matching symbol
		for(int i = 0; i < symbolTable.length; i++)
		{
			// If symbol at i is equal to the passed symbol, returns the value of the symbol
			if(symbolTable[i].symbol.equals(symbol))
			{
				return symbolTable[i].getValue();
			}
		}
		
		// If the symbol isn't found
		return -1;
	}
	
	// Converts an integer to 16 bit binary string
	private static String convertBinary(int value)
	{
		// Returns builtin binary format function
		return String.format("%16s", Integer.toBinaryString(value)).replace(' ', '0');
	}
	
	// Gets the binary for destination
	private static String destBinary(String dest)
	{
		// Checks all destination symbols
		if(dest.equals(""))
		{
			return "000";
		}
		else if(dest.equals("M"))
		{
			return "001";
		}
		else if(dest.equals("D"))
		{
			return "010";
		}
		else if(dest.equals("MD"))
		{
			return "011";
		}
		else if(dest.equals("A"))
		{
			return "100";
		}
		else if(dest.equals("AM"))
		{
			return "101";
		}
		else if(dest.equals("AD"))
		{
			return "110";
		}
		else if(dest.equals("AMD"))
		{
			return "111";
		}
		
		// Defaul return
		return "000";
	}
	
	// Gets the binary for computation
	private static String compBinary(String comp)
	{
		// Checks all possible computation symbols
		if(comp.equals("0"))
		{
			return "0101010";
		}
		else if(comp.equals("1"))
		{
			return "0111111";
		}
		else if(comp.equals("-1"))
		{
			return "0111010";
		}
		else if(comp.equals("D"))
		{
			return "0001100";
		}
		else if(comp.equals("A"))
		{
			return "0110000";
		}
		else if(comp.equals("!D"))
		{
			return "0001101";
		}
		else if(comp.equals("!A"))
		{
			return "0110001";
		}
		else if(comp.equals("-D"))
		{
			return "0001111";
		}
		else if(comp.equals("-A"))
		{
			return "0110011";
		}
		else if(comp.equals("D+1"))
		{
			return "0011111";
		}
		else if(comp.equals("A+1"))
		{
			return "0110111";
		}
		else if(comp.equals("D-1"))
		{
			return "0001110";
		}
		else if(comp.equals("A-1"))
		{
			return "0110010";
		}
		else if(comp.equals("D+A"))
		{
			return "0000010";
		}
		else if(comp.equals("D-A"))
		{
			return "0010011";
		}
		else if(comp.equals("A-D"))
		{
			return "0000111";
		}
		else if(comp.equals("D&A"))
		{
			return "0000000";
		}
		else if(comp.equals("D|A"))
		{
			return "0010101";
		}
		else if(comp.equals("M"))	// When a = 1
		{
			return "1110000";
		}
		else if(comp.equals("!M"))
		{
			return "1110001";
		}
		else if(comp.equals("-M"))
		{
			return "1110011";
		}
		else if(comp.equals("M+1"))
		{
			return "1110111";
		}
		else if(comp.equals("M-1"))
		{
			return "1110010";
		}
		else if(comp.equals("D+M"))
		{
			return "1000010";
		}
		else if(comp.equals("D-M"))
		{
			return "1010011";
		}
		else if(comp.equals("M-D"))
		{
			return "1000111";
		}
		else if(comp.equals("D&M"))
		{
			return "1000000";
		}
		else if(comp.equals("D|M"))
		{
			return "1010101";
		}
		
		// Default return
		return "0000000";
	}
	
	// Gets the jump binary
	private static String jumpBinary(String jump)
	{
		// Goes through all possible jump symbols
		if(jump.equals(""))
		{
			return "000";
		}
		else if(jump.equals("JGT"))
		{
			return "001";
		}
		else if(jump.equals("JEQ"))
		{
			return "010";
		}
		else if(jump.equals("JGE"))
		{
			return "011";
		}
		else if(jump.equals("JLT"))
		{
			return "100";
		}
		else if(jump.equals("JNE"))
		{
			return "101";
		}
		else if(jump.equals("JLE"))
		{
			return "110";
		}
		else if(jump.equals("JMP"))
		{
			return "111";
		}
		
		// Default return
		return "000";
	}
}