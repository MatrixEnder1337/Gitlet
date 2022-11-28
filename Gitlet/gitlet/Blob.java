package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Utils.join;

public class Blob implements Serializable {
    private String name;
    private byte[] contentByte;
    private String contentString;
    private String hash;

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** This initializes the blob class.
     * @param filename name of file**/
    public Blob(String filename) {
        name = filename;
        File file = Utils.join(CWD, filename);
        contentByte = Utils.readContents(file);
        contentString = Utils.readContentsAsString(file);
        List<Object> preHash = new ArrayList<>();
        preHash.add(name);
        preHash.add(contentByte);
        preHash.add(contentString);
        hash = Utils.sha1(preHash);

    }

    /** This initializes the blob class.
     * @param filename name of file
     * @param location location of file **/

    public Blob(String filename, String location) {
        name = filename;
        File file = Utils.join(location, filename);
        contentByte = Utils.readContents(file);
        contentString = Utils.readContentsAsString(file);
        List<Object> preHash = new ArrayList<>();
        preHash.add(name);
        preHash.add(contentByte);
        preHash.add(contentString);
        hash = Utils.sha1(preHash);
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public String getContentString() {
        return contentString;
    }

    public byte[] getContentByte() {
        return contentByte;
    }

}
