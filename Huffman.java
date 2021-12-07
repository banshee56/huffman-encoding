import java.io.*;
import java.util.*;
import java.util.PriorityQueue;

/** Using Huffman encoding to create trees containing characters and the frequency with which they appear
 * in a given text file.
 *
 * Compresses the contents of the original text file by writing in bits.
 * Decompresses the compressed file to recreate a copy of the original file.
 *
 * Also creates a map of characters and their corresponding code paths in the tree according to Huffman encoding
 * where 0's represent left edges and 1's represent right edges of the tree.
 *
 * @author Bansharee Ireen
 */


public class Huffman {
    private static final boolean debug = false;     // set to true when testing

    // declaring and instantiating the map of characters and their code paths of 0's and 1's according to Huffman
    private static final Map<Character, String> codeMap = new TreeMap<>();
    private static final ArrayList<Character> keyList = new ArrayList<>();   // a list of unique characters/keys in map

    /**
     * loads the text file into a string of text that will be used to build the tree
     * @param originalFileName     the name of the original text file
     * @return                     returns the string created from text in text file
     * @throws IOException         exception cause by FileReader
     */
    private static String loadFileIntoString(String originalFileName) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(originalFileName));   // reads the original file
        String str = "", line;      // str is the string containing the contents of the file

        // while lines exist, keep adding contents of file to str
        while ((line = in.readLine()) != null) {
            // adding \r\n to end of each line to maintain original text format if text uses enter key
            str += line+ "\r\n";
        }

        in.close();                 // finish reading
        return str;                 // return string copy of text file
    }

    /**
     * creates a frequency table using a map where keys are unique characters in the text and their values are
     * the frequency with which they appear
     * @param text      the original text file
     * @return          returns a character-frequency map that says which character is written how many times
     */
    private static Map<Character, Integer> frequencyTable(String text) {
        Map<Character, Integer> characterFrequencies = new TreeMap<>(); // created a frequency map for characters
        String[] stringArray = text.split("");      // turning string into an array of letters

        for (String s : stringArray) {      // reading one letter at a time
            char character = s.charAt(0);   // getting the letter's character

            // if we have seen this character before
            if (characterFrequencies.containsKey(character)) {
                // update the frequency map by increasing frequency of that character by 1
                characterFrequencies.put(character, characterFrequencies.get(character) + 1);
            } else {
                // create a new key for that character with frequency initialized to 1
                characterFrequencies.put(character, 1);
                keyList.add(character);   // add unique characters to keyList
            }
        }

        return characterFrequencies;
    }

    /**
     * creates a priority queue and uses it to create code tree from original text file
     * code tree holds each character of the original text file and their frequencies in the tree's leaves
     * @param text      the text loaded from original text file
     * @return          returns completed tree with characters in text as leaves
     */
    private static BinaryTree<TreeData> codeTree(String text) {
        if (text == null) return null;           // in case text stays null (text initialized to null in main())
        else if (text.equals("")) return null;   // boundary case: returns null if the text file is empty

        // getting our frequency table/character-frequency map
        Map<Character, Integer> characterFrequencies = frequencyTable(text);

        // creating our own comparator class
        // returns -1 if c1 freq < c2 freq; 0 if c1 freq = c2 freq; 1 if c1 freq > c2 freq.
        class TreeComparator implements Comparator<BinaryTree<TreeData>> {
            public int compare(BinaryTree<TreeData> c1, BinaryTree<TreeData> c2) {
                return c1.data.getFrequency() - c2.data.getFrequency();
            }
        }

        // using our comparator to sort the created priority queue so that characters with the least freq are first
        Comparator<BinaryTree<TreeData>> treeCompare = new TreeComparator();
        PriorityQueue<BinaryTree<TreeData>> treeQueue = new PriorityQueue<>(treeCompare);


        // creating a separate tree node for each character
        for (char character : keyList) {        // getting each unique character from keyList

            // using our TreeData class to hold character and its frequency as data for the character's node in tree
            TreeData data = new TreeData(character, characterFrequencies.get(character));
            BinaryTree<TreeData> treeNode = new BinaryTree<>(data);     // creating the character's node

            treeQueue.add(treeNode);    // adding initial character trees to a priority queue
        }

        /* boundary case: only runs if the original text file has only a single character or only a single character
        is repeated multiple times
        creates an unbalanced tree that has just one left child
        */
        if (treeQueue.size() == 1) {     // if priority queue has only one character node
            BinaryTree<TreeData> t1 = treeQueue.remove();   // remove the node from the queue

            // creating new data that holds the character's frequency
            // the node holding this data will be the parent of our character, so we represent it with '*'
            TreeData rData = new TreeData('*', t1.data.getFrequency());

            // creating a new node that holds the above data with the only character node as its left child
            BinaryTree<TreeData> r = new BinaryTree<>(rData, t1, null);

            treeQueue.add(r);   // adding this final node r to the queue
        }

        // if our treeQueue has more than 1 character node, this runs instead
        while (treeQueue.size() > 1) {  // while the queue has more than 1 node left in it

            // removing first 2 nodes of the sorted priority queue
            BinaryTree<TreeData> t1 = treeQueue.remove();
            BinaryTree<TreeData> t2 = treeQueue.remove();

            // creating new data that holds the above 2 nodes' combined frequencies
            // this combined freq node does not represent any characters, so we represent that with '*'
            TreeData rData = new TreeData('*', t1.data.getFrequency()+t2.data.getFrequency());

            // creating a new node that holds the above data with the first 2 nodes as its children
            BinaryTree<TreeData> r = new BinaryTree<>(rData, t1, t2);

            treeQueue.add(r);   // adding this node to the queue to be sorted
        }

        // in the end, the queue only holds one root node of the completed tree
        return treeQueue.remove();
    }

    /**
     * traverses the tree once to create a map of characters as keys with their code words as values
     * @param tree          the tree to traverse
     * @param codePath      the path taken so far, initially just a string
     * @return              returns the completed code map
     */
    private static Map<Character, String> mapCharacters(BinaryTree<TreeData> tree, String codePath) {
        if (tree == null) return null;

        if (tree.isLeaf()) {        // adds character and code path to map when it finds a character at a fringe
            codeMap.put(tree.data.getCharacter(), codePath);
        }

        if (tree.hasLeft()) {       // left edges represent 0 in the code path, so adds 0 to codePath
            mapCharacters(tree.getLeft(), codePath+"0");
        }

        if (tree.hasRight()) {       // right edges represent 1 in the code path
            mapCharacters(tree.getRight(), codePath+"1");
        }

        return codeMap;
    }

    /**
     * compresses the given original file
     * @param pathName              the original text file
     * @param compressedPathName    the compressed output is written in this file
     * @throws IOException          exceptions resulting from FileReader and BufferedBitWriter
     */
    private static void compression(String pathName, String compressedPathName) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(pathName));        // beginning reading
        BufferedBitWriter bitOutput = new BufferedBitWriter(compressedPathName);    // beginning writing in bits

        int cInt = input.read(); // Read next character's integer representation

        while (cInt != -1) {            // running until the file is empty
            char c = (char)cInt;        // converting unicode to character, c

            // getting codeword in terms of 0 and 1 for c from codeMap
            String codeWord = codeMap.get(c);
            String[] codeArray = codeWord.split("");    // turning codeWord into an iterable String array

            for (String s : codeArray) {      // reading one letter at a time
                char character = s.charAt(0);   // getting character from string

                boolean bit = false;        // initializing bit as false, which we get for 0 in codeword
                if (character == '1') bit = true;    // change bit to true bit if 1 in codeword

                bitOutput.writeBit(bit);        // writing the bit
            }

            cInt = input.read(); // Read next character's integer representation
        }
        bitOutput.close();       // finished writing in bits
        input.close();           // finished reading
    }

    /**
     * decompresses the given compressed file
     * @param compressedPathName        the compressed file to decode
     * @param decompressedPathName      the decompressed output is written in this file
     * @param tree                      the tree we navigate to reach character leaves
     * @throws IOException              exception rises from FileWriter and BufferedBitReader
     */
    private static void decompression(String compressedPathName, String decompressedPathName,
                                  BinaryTree<TreeData> tree) throws IOException {
        BinaryTree<TreeData> currNode = tree;       // setting current node to be the root of tree
        char decodedCharacter;                      // the character we decode when we reach the leaf

        BufferedBitReader bitInput = new BufferedBitReader(compressedPathName);             // begin reading bits
        BufferedWriter output = new BufferedWriter(new FileWriter(decompressedPathName));   // begin writing

        while (bitInput.hasNext()) {            // while we have bits to read
            boolean bit = bitInput.readBit();   // read the bit which is either false or true

            if (!bit) {                         // if bit is false, go left of tree
                currNode = currNode.getLeft();
            }
            else {
                currNode = currNode.getRight(); // if bit is true, go right of true
            }

            if (currNode.isLeaf()) {                                // if we hit the leaf
                decodedCharacter = currNode.data.getCharacter();    // get the decoded character from leaf
                currNode = tree;                                    // set current node to be root again
                output.write(decodedCharacter);                     // write decoded character
            }
        }

        output.close();     // finished writing
        bitInput.close();   // finished reading bits
    }

    public static void main(String[] args) {
        String originalFileName = "inputs/WarAndPeace";        // only need to change original file name here

        // no need to change file names below
        String compressedFileName = originalFileName+"_compressed.txt";     // name of compressed file
        String decompressedFileName = originalFileName+"_decompressed.txt"; // name of decompressed file

        String text = null;     // initializing text first
        try {
            text = loadFileIntoString(originalFileName + ".txt");   // loads file to create text
        } catch (IOException e) {
            System.err.println("Can't read original file.");
        }

        BinaryTree<TreeData> tree = codeTree(text);     // uses text to create tree with characters as leaves

        Map<Character, String> map = mapCharacters(tree, "");   // creating the codeMap


        if (debug) {    // only runs when set to debug
            // printing out the code tree
            System.out.println(tree);

            // printing out the code map
            System.out.println("code map: " + map);
        }

        try {
            compression(originalFileName+".txt", compressedFileName);   // compressing file
            decompression(compressedFileName, decompressedFileName, tree);       // decompressing compressed file
        } catch (IOException e) {
            System.err.println("Issue with opening/closing files in compression.");
        }

        try {
            decompression(compressedFileName, decompressedFileName, tree);       // decompressing compressed file
        } catch (IOException e) {
            System.err.println("Issue with opening/closing files in decompression.");
        }
    }
}
