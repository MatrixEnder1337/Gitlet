package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;


/** Represents a gitlet commit object.
 * Contains several methods to create different types of commits
 * @author Anna (Yutong) Zhang
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The hash "name" of the parent Commit. */
    private String parent;

    /** Name of merge parent. */
    private String mergeParent;

    /** Whether the commit is a merge commit */
    private boolean merge;

    /** All the file blobs corresponding to this commit
     * (identified by their hashes)
     * */
    private HashMap<String, String> contents;

    /** The message of this Commit. */
    private String message;

    /** Name of the branch that the commit is made */
    private String branch;

    /** The time at which the commit is made */
    private String time;

    /** The hash of given commit */
    private String hash;


    /**
     * the snapshot of deleted files
     */
    private HashSet<String> deletedSnapshot = new HashSet<>();

    /** Constructor EXCLUSIVELY for the initial commit */
    public Commit() {
        parent = null;
        contents = new HashMap<>();
        message = "initial commit";
        branch = "master";

        merge = false;
        mergeParent = null;

        Date date = new Date(0);
        String pattern = "EEE MMM d HH:mm:ss y Z";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        time = dateFormat.format(date);

        hash = hashVal();

    }

    /** Create a normal commit
     * @param message the message for the commit
     * @param files the contents
     * @param parent the parent of the commit
     * @param branch the branch it belongs to*/
    public Commit(String message, HashMap<String, String> files, String parent,
                  String branch) {
        this.message = message;
        this.contents = files;
        this.parent = parent;
        this.branch = branch;

        this.merge = false;
        this.mergeParent = null;

        this.time = createTime();

        this.hash = hashVal();
    }


    /** Create a merge commit
     * @param files
     * @param parent1
     * @param parent2
     * @param branch1
     * @param branch2
     */
    public Commit(HashMap<String, String> files,
                  String parent1, String parent2,
                  String branch1, String branch2) {
        this.message = "Merged " + branch2 + " into " + branch1 + ".";
        this.contents = files;
        this.parent = parent1;
        this.mergeParent = parent2;
        this.merge = true;
        this.branch = branch1;

        this.time = createTime();
        this.hash = hashVal();
    }

    public String createTime() {
        Date date = new Date();
        String pattern = "EEE MMM d HH:mm:ss y Z";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        return dateFormat.format(date);
    }

    public String getHash() {
        return this.hash;
    }

    private String hashVal() {
        List<Object> tmp = new ArrayList<>();

        tmp.add(message);
        tmp.add(branch);
        tmp.add(time);

        if (parent == null) {
            return Utils.sha1(tmp);
        }

        tmp.add(parent);
        if (merge) {
            tmp.add(mergeParent);
        }
        for (String blob : contents.values()) {
            tmp.add(blob);
        }

        return Utils.sha1(tmp);
    }

    public List<String> getBlob() {
        List<String> lst = new ArrayList<String>(contents.values());
        return lst;
    }

    public HashMap<String, String> getContents() {
        return contents;
    }

    public String getParent() {
        return parent;
    }

    public String getMergeParent() {
        return mergeParent;
    }

    public boolean isMerge() {
        return merge;
    }

    public String getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public static HashSet<String> getAncestors(String commitHash) {

        Deque<String> queue = new ArrayDeque<>();
        queue.addLast(commitHash);
        HashSet<String> res = new HashSet<>();

        while (!queue.isEmpty()) {
            String hash = queue.poll();
            res.add(hash);
            Commit commit = GitUtils.getCommit(hash);

            if (commit.getParent() != null) {
                queue.addLast(commit.getParent());
            }
            if (commit.getMergeParent() != null) {
                queue.addLast(commit.getMergeParent());
            }

        }

        return res;
    }

    public static String lowestCommonAncestor(String curCommit, HashSet<String> otherAncestors) {
        TreeMap<Integer, String> ancestors = new TreeMap<>();
        Deque<String> queue = new ArrayDeque<>();
        int level = 0;

        queue.addLast(curCommit);

        // Kind of like a BFS???
        while (!queue.isEmpty()) {

            // The size of a given level
            int levelCount = queue.size();

            for (int i = 0; i < levelCount; i++) {
                String hash = queue.poll();
                Commit commit = GitUtils.getCommit(hash);

                if (commit.getParent() != null) {
                    String p1 = commit.getParent();
                    queue.addLast(p1);
                    if (otherAncestors.contains(p1)) {
                        ancestors.put(level, p1);
                    }
                }

                if (commit.getMergeParent() != null) {
                    String p2 = commit.getMergeParent();
                    queue.addLast(p2);
                    if (otherAncestors.contains(p2)) {
                        ancestors.put(level, p2);
                    }
                }
            }

            level++;

        }
        return ancestors.firstEntry().getValue();

    }

}
