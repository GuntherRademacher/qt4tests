import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.file.Files;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class Main {
    public static void main(String[] args) throws Exception {

        Parser parser = Parser.htmlParser();

        File dir = new File("../../files/");
        File outDir = new File("../../results/");
        File[] files = dir.listFiles();

        for (File f : files) {
            try {
                System.err.println("Processing " + f.getName());
                // replace Windows line separators
                String html = Files.readString(f.toPath()).replace("\r\n", "\n");
                Document doc = parser.parseInput(html, dir.toURI().toString());
                String xml = serialize(doc);
                File outFile = new File(outDir, f.getName().replace(".html", ".xhtml"));
                outFile.createNewFile();
                try (PrintStream ps = new PrintStream(outFile)) {
                    // install system line separators
                    xml = xml.replace("\n", System.lineSeparator());
                    ps.print(xml);
                }
            } catch (Exception e) {
                System.err.println("**** Failed to convert " + f.getName() + " - " + e.getMessage());
            }
        }
    }

    public static String serialize(Document doc) throws SaxonApiException {
        org.w3c.dom.Document dom = ConvertToDom.convert(doc);
        Processor proc = new Processor(true);
        StringWriter sw = new StringWriter();
        Serializer serializer = proc.newSerializer(sw);
        serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
        serializer.setOutputProperty(Serializer.Property.INDENT, "no");
        serializer.setOutputWriter(sw);
        XdmNode wrappedDom = proc.newDocumentBuilder().wrap(dom);
        serializer.serialize(wrappedDom.asSource());
        return sw.toString();
    }

}