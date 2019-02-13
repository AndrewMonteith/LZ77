package experimentation;

import coders.LZ77;
import coders.LZ77CodedMessage;
import coders.LZ77Triple;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LZ77ProgramInterface {
    private String[] args;
    private File fileToProcess;

    public boolean wantsToEncode() {
        return args[0].equals("encode");
    }

    private void printAllTuples(LZ77CodedMessage message) {
        for (var i = 0; i < message.getNumberOfTriples(); ++i) {
            System.out.println(message.getTriple(i));
        }
    }

    private void writeSize(DataOutputStream dos, LZ77CodedMessage message) throws IOException {
        dos.writeInt(message.getSize());
    }

    private void writeTriples(DataOutputStream dos, LZ77CodedMessage message) throws IOException {
        for (var i = 0; i < message.getNumberOfTriples(); ++i) {
            LZ77Triple triple = message.getTriple(i);

            dos.writeInt(triple.d);
            dos.writeByte(triple.l);
            dos.writeByte(triple.next);
        }
    }

    private void writeEncodedMessageToFile(File originalFile, LZ77CodedMessage message) {
        Path encodedPath = Paths.get(originalFile.toString() + ".encoded");

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(encodedPath.toFile()))) {
            writeSize(dos, message);
            writeTriples(dos, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void encodeFile(boolean printTuples) {
        var lz77 = new LZ77();
        try {
            var encodedMessage = lz77.encode(Files.readAllBytes(fileToProcess.toPath()));

            if (printTuples) {
                printAllTuples(encodedMessage);
            }

            writeEncodedMessageToFile(fileToProcess, encodedMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void encode() {
        boolean printTuples = args.length == 3;
        encodeFile(printTuples);
    }

    public boolean wantsToDecode() {
        return args[0].equals("decode");
    }

    private LZ77Triple readTriple(DataInputStream dis) throws IOException {
        int d = dis.readInt();
        char l = (char) dis.readByte();
        byte next = dis.readByte();

        return new LZ77Triple(d, l, next);
    }

    private LZ77CodedMessage readEncodedMessage(DataInputStream dis) throws IOException {
        int size = 0, decodedFileSize = dis.readInt();
        List<LZ77Triple> triples = new ArrayList<>();

        while (size < decodedFileSize) {
            LZ77Triple triple = readTriple(dis);
            size += 6;
            triples.add(triple);
        }

        return new LZ77CodedMessage(triples, size);
    }

    private Path getPathForDecodedFile(File encodedFile) {
        if (encodedFile.getName().endsWith(".encoded")) {
            String strPath = encodedFile.toString();
            return Paths.get(strPath.subSequence(0, strPath.length()-8).toString());
        } else {
            return Paths.get(encodedFile.toString() + ".decoded");
        }
    }

    public void decode()  {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileToProcess))) {
            LZ77CodedMessage encodedMessage = readEncodedMessage(dis);

            var lz77 = new LZ77();
            byte[] decodedMessage = lz77.decode(encodedMessage);

            Files.write(
                    getPathForDecodedFile(fileToProcess),
                    decodedMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printUsage() {
        System.out.println("-- Help --");
        System.out.println("java LZ77 encode <file> [-print_tuples]");
        System.out.println("    > java LZ77 encode testfile.jpg");
        System.out.println("    > java LZ77 encode peterpiper.txt -print_tuples");
        System.out.println("java LZ77 decode <encoded_file>");
        System.out.println("    > java LZ77 decode testfile.jpg.encoded");
    }

    private void checkForFile() throws FileNotFoundException {
        if (args.length < 2) {
            printUsage();
        }

        fileToProcess = new File(args[1]);
        if (!fileToProcess.exists()) {
            throw new FileNotFoundException("could not find the file " + args[1]);
        }
    }

    public LZ77ProgramInterface(String[] args) throws FileNotFoundException {
        this.args = args;

        checkForFile();
    }
}
