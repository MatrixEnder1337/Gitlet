package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.join;

public class GitUtils {
    /** gitlet directory */
    static final File OBJECTS = Utils.join(".gitlet", "objects");

    static final File COMMITS = Utils.join(OBJECTS, "commits");

    static final File BLOBS = Utils.join(OBJECTS, "blobs");

    static final File INDEX = Utils.join(".gitlet", "INDEX");
    static final File INDEX_RM = Utils.join(".gitlet", "INDEX_RM");
    static final File LOCAL_HEAD = Utils.join(".gitlet", "refs", "heads");
    static final File HEAD = Utils.join(".gitlet", "HEAD");

    static final File COMMITLIST = Utils.join(".gitlet", "commitList");

    static final File BRANCHLIST = Utils.join(".gitlet", "branchList");

    /** The current working directory. */
    static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * set up dog gitlet folder and subfolders
     */
    static void setDirectory() {
        GITLET_DIR.mkdirs();

        OBJECTS.mkdirs();
        COMMITS.mkdirs();
        BLOBS.mkdirs();

        LOCAL_HEAD.mkdirs();
        HashMap<String, String> index = new HashMap<String, String>();
        Utils.writeObject(INDEX, index);
        HashMap<String, String> indexRM = new HashMap<String, String>();
        Utils.writeObject(INDEX_RM, indexRM);

        HashSet<String> commits = new HashSet<>();
        Utils.writeObject(COMMITLIST, commits);

        ArrayList<String> branches = new ArrayList<>();
        Utils.writeObject(BRANCHLIST, branches);

        Utils.writeContents(HEAD, "master");
    }

    /**
     * Whether the repo is inited
     */
    static boolean isInited() {
        return GITLET_DIR.exists();
    }

    /**
     * Add a branch to the branch list
     * @param branchName
     */
    static void addBranch(String branchName) {
        ArrayList<String> branches = Utils.readObject(BRANCHLIST, ArrayList.class);
        branches.add(branchName);
        Utils.writeObject(BRANCHLIST, branches);
    }

    /**
     * Return a list of all the branches
     */
    static ArrayList<String> getBranches() {
        ArrayList<String> branches = Utils.readObject(BRANCHLIST, ArrayList.class);
        return branches;
    }

    /**
     * Return a list of all the commits
     */
    static HashSet<String> getCommits() {
        HashSet<String> commits = Utils.readObject(COMMITLIST, HashSet.class);
        return commits;
    }

    /**
     * Remove a branch from the branch list && from local heads
     * @param branchName
     */
    static void removeBranch(String branchName) {
        ArrayList<String> branches = Utils.readObject(BRANCHLIST, ArrayList.class);
        branches.remove(branchName);
        Utils.writeObject(BRANCHLIST, branches);

        File branch = join(LOCAL_HEAD, branchName);
        branch.delete();
    }

    /**
     * Add a commit to the commitList (hash)
     * @param commitHash
     */
    static void addCommit(String commitHash) {
        HashSet<String> commits = Utils.readObject(COMMITLIST, HashSet.class);
        commits.add(commitHash);
        Utils.writeObject(COMMITLIST, commits);
    }

    /**
     * @param branch Update the branch the head pointer is pointing to
     */
    static void writeHead(String branch) {
        Utils.writeContents(HEAD, branch);
    }

    /**
     * get current HEAD commit hash as a string
     * @return hash of the current commit/HEAD
     */
    static String getHead() {
        String curBranch = Utils.readContentsAsString(HEAD);
        File branchHead = Utils.join(LOCAL_HEAD, curBranch);
        return Utils.readContentsAsString(branchHead);
    }

    /**
     * get the current hash of the given branch
     * @param branchName
     * @return
     */

    static String getBranchHead(String branchName) {
        File head = Utils.join(LOCAL_HEAD, branchName);
        return Utils.readContentsAsString(head);
    }

    /**
     * return the name (not hash) of the current branch
     * @return
     */

    static String currentBranch() {
        return Utils.readContentsAsString(HEAD);
    }

    /**
     * write local branch head in .gitlet/refs/heads
     * @param hash   hash of the newest commit
     * @param branch branch to update
     */
    static void updateBranchHead(String hash, String branch) {
        File branchHead = Utils.join(LOCAL_HEAD, branch);
        Utils.writeContents(branchHead, hash);
    }

    /**
     * write git INDEX file to record staged file
     * @param hash     hash of the blob of staged file
     * @param filename filename of the stage file in the CWD
     * @return true if write to index
     */
    static boolean writeStagedToIndex(String hash, String filename) {
        HashMap<String, String> files = Utils.readObject(INDEX, HashMap.class);
        Commit curCommit = getCurrentCommit();
        HashMap<String, String> curFiles = curCommit.getContents();

        if (files.containsKey(filename)) {
            // Not supposed to stage
            if (hash.equals(files.get(filename))) {
                return false;
            }
        }

        if (curFiles.containsKey(filename) && hash.equals(curFiles.get(filename))) {
            // Not supposed to stage
            // Remove from staging area
            removeFromStagingArea(filename);
            return false;
        }

        files.put(filename, hash);
        Utils.writeObject(INDEX, files);
        return true;

    }

    /**
     * Remove a file from the staging area (both addition and removal)
     * @param file Filename
     */
    static void removeFromStagingArea(String file) {
        HashMap<String, String> addition = Utils.readObject(INDEX, HashMap.class);
        HashMap<String, String> removal = Utils.readObject(INDEX_RM, HashMap.class);

        if (addition.containsKey(file)) {
            addition.remove(file);
        }
        if (removal.containsKey(file)) {
            removal.remove(file);
        }

        Utils.writeObject(INDEX, addition);
        Utils.writeObject(INDEX_RM, removal);
    }

    /**
     * Write a commit to .gitlet
     * Update branch heads and stuff
     * @param commit Commit
     */
    static void writeCommit(Commit commit) {
        String hash = commit.getHash();
        File loc = Utils.join(COMMITS, hash);

        Utils.writeObject(loc, commit);
        String curBranch = currentBranch();
        writeHead(curBranch);
        updateBranchHead(hash, curBranch);
    }

    /**
     * Write a blob to .gitlet
     * @param blob Blob
     */
    static void writeBlob(Blob blob) {
        String hash = blob.getHash();
        File loc = Utils.join(BLOBS, hash);

        Utils.writeObject(loc, blob);
    }

    /**
     * Get all commits
     */
    static List<String> getAllCommits() {
        return Utils.plainFilenamesIn(COMMITS);
    }

    /**
     * Get the current commit
     * @return: current commit
     */
    static Commit getCurrentCommit() {
        File commit = Utils.join(COMMITS, getHead());
        return Utils.readObject(commit, Commit.class);
    }

    /**
     * Get a commit based on its hash
     * @return: commit
     */
    static Commit getCommit(String hash) {
        File loc = Utils.join(COMMITS, hash);
        Commit commit = Utils.readObject(loc, Commit.class);
        return commit;
    }

    /**
     * Helper for the rm command
     * @param file The name of the file to be removed
     * @return: boolean whether or not the removal is successful
     */
    static boolean removeHelper(String file) {

        //read from staging area
        HashMap<String, String> stageEntries = Utils.readObject(INDEX, HashMap.class);
        HashMap<String, String> removalStaged = Utils.readObject(INDEX_RM, HashMap.class);

        //read from current commit
        Commit curHead = getCurrentCommit();
        HashMap<String, String> curFiles = curHead.getContents();



        if (stageEntries.containsKey(file)) {
            stageEntries.remove(file);
            Utils.writeObject(INDEX, stageEntries);
            return true;
        } else if (curFiles.containsKey(file)) {

            Utils.restrictedDelete(file);
            String blobH = curFiles.get(file);
            removalStaged.put(file, blobH);
            Utils.writeObject(INDEX_RM, removalStaged);
            return true;
        }
        return false;

    }

    /**
     * Clear the staging area (including both addition and removal)
     */
    static void clearStage() {
        //read from staging area
        HashMap<String, String> stageEntries = Utils.readObject(INDEX, HashMap.class);
        HashMap<String, String> removalStaged = Utils.readObject(INDEX_RM, HashMap.class);
        stageEntries.clear();
        removalStaged.clear();

        Utils.writeObject(INDEX, stageEntries);
        Utils.writeObject(INDEX_RM, removalStaged);
    }

    /**
     * @param curFiles: files tracked by current commit
     */

    static void preCommitUpdate(HashMap<String, String> curFiles) {
        HashMap<String, String> staged = Utils.readObject(INDEX, HashMap.class);
        HashMap<String, String> removalStaged = Utils.readObject(INDEX_RM, HashMap.class);

        for (String file : staged.keySet()) {
            curFiles.put(file, staged.get(file));
        }

        if (!removalStaged.isEmpty()) {
            for (String file: removalStaged.keySet()) {
                curFiles.remove(file);
            }
        }
    }

    /**
     * @return whether or not the staging area is empty
     */
    static boolean stageIsEmpty() {
        HashMap<String, String> stageEntries = Utils.readObject(INDEX, HashMap.class);
        HashMap<String, String> removalStaged = Utils.readObject(INDEX_RM, HashMap.class);
        return (stageEntries.isEmpty()) && (removalStaged.isEmpty());
    }

    /**
     * compare cwd and commit and output a list of untracked files
     * @param commit
     * @return
     */
    static LinkedList<String> untrackedFiles(Commit commit) {
        HashMap<String, String> stageEntries = Utils.readObject(INDEX, HashMap.class);
        HashMap<String, String> removalStaged = Utils.readObject(INDEX_RM, HashMap.class);
        HashMap<String, String> commitFiles = commit.getContents();

        List<String> workingDir = Utils.plainFilenamesIn(CWD);
        LinkedList<String> lst = new LinkedList<>();

        for (String file : workingDir) {
            if (!commitFiles.containsKey(file)) {
                lst.add(file);
            }
        }

        for (Iterator<String> it = lst.iterator(); it.hasNext();) {
            String file = it.next();
            if (stageEntries.containsKey(file) || removalStaged.containsKey(file)) {
                it.remove();
            }
        }
        return lst;
    }

    /**
     * Output tracked files in the current commit
     * @return
     */
    static HashSet<String> trackedFiles() {
        HashMap<String, String> stageEntries = Utils.readObject(INDEX, HashMap.class);
        HashMap<String, String> removalStaged = Utils.readObject(INDEX_RM, HashMap.class);
        HashMap<String, String> commitFiles = getCurrentCommit().getContents();

        HashSet<String> lst = new HashSet<>();
        for (String file : stageEntries.keySet()) {
            lst.add(file);
        }
        for (String file : removalStaged.keySet()) {
            lst.add(file);
        }
        for (String file : commitFiles.keySet()) {
            lst.add(file);
        }

        return lst;
    }

    /**
     * survey the current working directory to output a list of
     * modified or deleted but not staged files for log command
     * @param commit current commit to be compared
     * @return list of modified files
     */

    static LinkedList<String> modifiedFiles(Commit commit) {
        HashMap<String, String> stageEntries = Utils.readObject(INDEX, HashMap.class);
        HashMap<String, String> removalStaged = Utils.readObject(INDEX_RM, HashMap.class);
        HashMap<String, String> commitFiles = commit.getContents();
        List<String> workingDir = Utils.plainFilenamesIn(CWD);

        LinkedList<String> modified = new LinkedList<>();

        for (String file : commitFiles.keySet()) {
            if (workingDir.contains(file)) {
                Blob nowB = new Blob(file);
                String nowHash = nowB.getHash();
                if (!nowHash.equals(commitFiles.get(file)) && !stageEntries.containsKey(file)) {
                    modified.add(file + " (modified)");
                }
            } else {
                if (!removalStaged.containsKey(file)) {
                    modified.add(file + " (deleted)");
                }
            }
        }

        for (String file : stageEntries.keySet()) {
            if (workingDir.contains(file)) {
                Blob blob = new Blob(file);
                String blobH = blob.getHash();
                if (!blobH.equals(stageEntries.get(file))) {
                    modified.add(file + " (modified)");
                }
            } else {
                modified.add(file + " (deleted)");
            }
        }

        return modified;

    }

    /**
     * Return the staged entries
     */
    static HashMap<String, String> getStage() {
        HashMap<String, String> stageEntries = Utils.readObject(INDEX, HashMap.class);
        return stageEntries;
    }

    /**
     * Return the files that are staged for removal
     */
    static HashMap<String, String> getRemoved() {
        HashMap<String, String> removalStaged = Utils.readObject(INDEX_RM, HashMap.class);
        return removalStaged;
    }

    /**
     * replace/create cwd file with the repo version with the given blobHash
     * @param cwdNew cwd file to be written
     * @param blobH hash of the blob in the repo
     */
    static void updateRepoFile(File cwdNew, String blobH) {
        File retrieve = Utils.join(BLOBS, blobH);
        Blob b = Utils.readObject(retrieve, Blob.class);
        Utils.writeContents(cwdNew, b.getContentByte());
    }

    /**
     * General command: Stage a file for removal
     * @param file The name of the file to be removed
     * @param hash Blob hash of the file
     */
    static void stageForRemoval(String file, String hash) {
        HashMap<String, String> removalStaged = Utils.readObject(INDEX_RM, HashMap.class);
        removalStaged.put(file, hash);
        Utils.writeObject(INDEX_RM, removalStaged);
    }

    /**
     * Get the string content of a blob, given the blob hash
     * @param blobH Blob Hash
     */
    static String getBlobContent(String blobH) {
        File loc = Utils.join(BLOBS, blobH);
        Blob b = Utils.readObject(loc, Blob.class);
        return b.getContentString();
    }

}
