package gitlet;

import java.io.File;
import java.io.Serializable;

import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  This class contains all the command methods and their helpers
 *
 *  @author Anna (Yutong) Zhang
 */
public class Repository implements Serializable {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */


    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    private static final int COMMIT_LENGTH = 40;

    /**
     * At this point, we haven't initialized the Gitlet repository, just a class that
     * may initialize it later
     */
    static void initRepository() {
        GitUtils.setDirectory();
    }

    /**
     * Init should create a gitlet repository if one doesn't exist.
     * And should create a new commit to put in it
     */
    static void init() {

        if (GitUtils.isInited()) {
            String e = "A Gitlet version-control system "
                    + "already exists in the current directory.";
            System.out.println(e);
            System.exit(0);
        } else {
            initRepository();
            Commit initCommit = new Commit();
            GitUtils.writeCommit(initCommit);
            GitUtils.addCommit(initCommit.getHash());
            GitUtils.addBranch("master");
        }
    }

    /**
     * This is the add function for the gitlet class.
     * @param file name
     */
    static void add(String file) {
        File named = Utils.join(CWD, file);
        Blob blob = null;
        if (named.exists()) {
            blob = new Blob(file);
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        String blobH = blob.getHash();
        if (GitUtils.writeStagedToIndex(blobH, file)) {
            GitUtils.writeBlob(blob);
        }

    }


    /**
     * Make a new commit
     * @param message the message associated with the commit
     */

    static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else {

            if (GitUtils.stageIsEmpty()) {
                System.out.println("No changes added to the commit");
                System.exit(0);
            }

            Commit curCommit = GitUtils.getCurrentCommit();
            HashMap<String, String> last = curCommit.getContents();
            HashMap<String, String> cur = new HashMap<>(last);

            GitUtils.preCommitUpdate(cur);

            Commit current = new Commit(message, cur, GitUtils.getHead(), GitUtils.currentBranch());
            GitUtils.writeCommit(current);

            // Clearing "staged for addition" and "staged for removal"
            GitUtils.clearStage();
            GitUtils.addCommit(current.getHash());

        }

    }


    /**
     * Need to unstage a file and mark it to be not tracked during next comm.
     * Also delete the file
     * @param file name
     */

    static void remove(String file) {
        if (!GitUtils.removeHelper(file)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

    }

    /**
     * Displays the commits from current head to the initial commit.
     */
    static void log() {

        Commit curCommit = GitUtils.getCurrentCommit();

        while (curCommit.getParent() != null) {
            System.out.println("===");
            System.out.println("commit " + curCommit.getHash());
            if (curCommit.isMerge()) {
                String firstP = curCommit.getParent().substring(0, 7);
                String secP = curCommit.getMergeParent().substring(0, 7);
                System.out.println("Merge: " + firstP + " " + secP);
            }
            System.out.println("Date: " + curCommit.getTime());
            System.out.println(curCommit.getMessage());
            System.out.println("");

            curCommit = GitUtils.getCommit(curCommit.getParent());
        }

        System.out.println("===");
        System.out.println("commit " + curCommit.getHash());
        System.out.println("Date: " + curCommit.getTime());
        System.out.println(curCommit.getMessage());
        System.out.println("");

    }

    /**
     * This is the way to call all the logs.
     */

    static void globall() {

        HashSet<String> commits = GitUtils.getCommits();
        for (String s : commits) {

            Commit curCommit = GitUtils.getCommit(s);
            System.out.println("===");
            System.out.println("commit " + curCommit.getHash());
            if (curCommit.isMerge()) {
                String firstP = curCommit.getParent().substring(0, 7);
                String secP = curCommit.getMergeParent().substring(0, 7);
                System.out.println("Merge: " + firstP + " " + secP);
            }
            System.out.println("Date: " + curCommit.getTime());
            System.out.println(curCommit.getMessage());
            System.out.println("");
        }
    }

    /**
     * This will find a particular message.
     * @param message The message one wants to find
     */
    static void find(String message) {
        boolean found = false;
        HashSet<String> commits = GitUtils.getCommits();
        for (String s : commits) {
            Commit commit = GitUtils.getCommit(s);
            String commitMessage = commit.getMessage();
            if (message.equals(commitMessage)) {
                System.out.println(commit.getHash());
                found = true;
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Print out the status of Gitlet repository
     */
    static void status() {

        Commit curCommit = GitUtils.getCurrentCommit();

        ArrayList<String> branches = GitUtils.getBranches();
        HashMap<String, String> staged = GitUtils.getStage();
        HashMap<String, String> removalStaged = GitUtils.getRemoved();

        List<String> untracked = GitUtils.untrackedFiles(curCommit);
        List<String> modified = GitUtils.modifiedFiles(curCommit);

        ArrayList<String> stage = new ArrayList<>(
                staged.keySet());
        ArrayList<String> unstage = new ArrayList<>(removalStaged.keySet());

        // Sort
        Collections.sort(stage);
        Collections.sort(unstage);
        Collections.sort(modified);
        Collections.sort(untracked);
        Collections.sort(branches);

        String currentBranch = GitUtils.currentBranch();

        System.out.println("=== Branches ===");
        for (String b : branches) {
            if (b.equals(currentBranch)) {
                System.out.print("*");
            }
            System.out.println(b);
        }
        System.out.println("");

        System.out.println("=== Staged Files ===");
        for (String b : stage) {
            System.out.println(b);
        }
        System.out.println("");

        System.out.println("=== Removed Files ===");
        for (String b : unstage) {
            System.out.println(b);
        }
        System.out.println("");


        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String s : modified) {
            System.out.println(s);
        }
        System.out.println("");


        System.out.println("=== Untracked Files ===");
        for (String s : untracked) {
            System.out.println(s);
        }
        System.out.println("");
    }

    /**
     * Add a new branch
     * @param branchName the name of the branch
     */
    static void branch(String branchName) {
        ArrayList<String> branches = GitUtils.getBranches();
        if (branches.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            GitUtils.addBranch(branchName);
            GitUtils.updateBranchHead(GitUtils.getHead(), branchName);
        }
    }

    /**
     * Change the current version of the file in CWD
     * to the version stored in the specified commit
     * @param name   File name
     * @param commit Commit specified (by hash)
     */
    static void checkout(String name, String commit) {
        HashSet<String> commits = GitUtils.getCommits();
        String fullCommitHash = commit;

        boolean found = false;
        int length = commit.length();

        if (length < COMMIT_LENGTH) {
            for (String i : commits) {
                if (commit.equals(i.substring(0, length))) {
                    fullCommitHash = i;
                    found = true;
                }
            }
        } else {
            if (commits.contains(commit)) {
                found = true;
            }
        }

        // Condition check
        if (!found) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit specCommit = GitUtils.getCommit(fullCommitHash);
        HashMap<String, String> files = specCommit.getContents();
        if (!files.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String blobH = files.get(name);
        File cwdFile = Utils.join(CWD, name);
        GitUtils.updateRepoFile(cwdFile, blobH);
    }

    /**
     * Checkout file in the current commit.
     * @param name File name
     */
    static void checkoutCurrent(String name) {
        String currentHead = GitUtils.getHead();
        checkout(name, currentHead);
    }

    /**
     * Checkout a branch
     * @param branchName Branch name
     */
    static void checkoutBranch(String branchName) {
        ArrayList<String> branches = GitUtils.getBranches();
        if (branches.contains(branchName)) {
            String currentBranch = GitUtils.currentBranch();
            if (currentBranch.equals(branchName)) {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
            }

            HashMap<String, String> staged = GitUtils.getStage();
            HashMap<String, String> removalStaged = GitUtils.getRemoved();
            Commit curCommit = GitUtils.getCurrentCommit();
            List<String> untrackedFiles = GitUtils.untrackedFiles(curCommit);

            Commit newCommit = GitUtils.getCommit(GitUtils.getBranchHead(branchName));
            HashMap<String, String> newFiles = newCommit.getContents();

            if (!untrackedFiles.isEmpty()) {
                System.out.println("There is an untracked "
                        + "file in the way; delete it, "
                        + "or add and commit it first.");
                System.exit(0);
            }

            //put each of the file in the cwd
            for (String file : newFiles.keySet()) {
                File update = Utils.join(CWD, file);
                GitUtils.updateRepoFile(update, newFiles.get(file));
            }

            //delete tracked files that are not present in the checkout branch
            HashSet<String> trackedFiles = GitUtils.trackedFiles();
            for (String file : trackedFiles) {
                if (!newFiles.containsKey(file)) {
                    Utils.restrictedDelete(file);
                }
            }

            //update HEAD to new branch
            GitUtils.writeHead(branchName);

            //clear staging area
            GitUtils.clearStage();

        } else {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

    }

    /**
     * This removes a branch and its corresponding head.
     * @param branchName the name of the branch to remove
     */

    static void removeBranch(String branchName) {
        ArrayList<String> branches = GitUtils.getBranches();
        if (branches.contains(branchName)) {
            String currentBranch = GitUtils.currentBranch();
            if (currentBranch.equals(branchName)) {
                System.out.println("Cannot remove the current branch.");
                System.exit(0);
            }
            GitUtils.removeBranch(branchName);
        } else {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

    }

    /**
     * Reset to a previous commit.
     * @param commit a string for hash value of commit to reset to
     */

    static void reset(String commit) {
        HashSet<String> commits = GitUtils.getCommits();

        if (!commits.contains(commit)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        HashMap<String, String> staged = GitUtils.getStage();
        HashMap<String, String> removalStaged = GitUtils.getRemoved();
        Commit curCommit = GitUtils.getCurrentCommit();
        Commit newCommit = GitUtils.getCommit(commit);

        List<String> untrackedFiles = GitUtils.untrackedFiles(curCommit);
        HashMap<String, String> newFiles = newCommit.getContents();

        if (!untrackedFiles.isEmpty()) {
            System.out.println("There is an untracked file "
                    + "in the way; delete it, "
                    + "or add and commit it first.");
            System.exit(0);
        }


        //put each of the file in the cwd
        for (String file : newFiles.keySet()) {
            File update = Utils.join(CWD, file);
            GitUtils.updateRepoFile(update, newFiles.get(file));
        }

        //delete tracked files that are not present in the checkout branch
        HashSet<String> trackedFiles = GitUtils.trackedFiles();
        for (String file : trackedFiles) {
            if (!newFiles.containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }

        //update HEAD and branch ref to new commit (doesn't have to move head since
        // it will still be pointing to the branch ref)
        GitUtils.updateBranchHead(commit, GitUtils.currentBranch());

        //clear staging area
        GitUtils.clearStage();

    }

    /**
     * Perform condition check before merge
     * to prevent failure cases
     * @param branchName the given branch
     */
    static void mergeCheck(String branchName) {
        HashMap<String, String> staged = GitUtils.getStage();
        HashMap<String, String> removalStaged = GitUtils.getRemoved();
        if (!staged.isEmpty() || !removalStaged.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        ArrayList<String> branches = GitUtils.getBranches();
        if (!branches.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        String curBranch = GitUtils.currentBranch();
        if (curBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        List<String> untrackedFiles = GitUtils.untrackedFiles(GitUtils.getCurrentCommit());
        if (!untrackedFiles.isEmpty()) {
            System.out.println("There is an untracked file in the "
                    + "way; delete it, or add and commit it first. ");
            System.exit(0);
        }



    }

    /**
     * Compare three sources and select/modify merged files
     * @param curFiles
     * @param givenFiles
     * @param splitFiles
     */
    static HashSet<String> mergeCompare(HashMap<String, String> curFiles,
                                        HashMap<String, String> givenFiles,
                                        HashMap<String, String> splitFiles) {

        HashSet<String> mergeConflictFiles = new HashSet<>();

        for (String file : curFiles.keySet()) {
            String curVer = curFiles.get(file);

            if (givenFiles.containsKey(file)) {
                String givenVer = givenFiles.get(file);

                if (splitFiles.containsKey(file)) {
                    String splitVer = splitFiles.get(file);

                    // Modified in given but not modified in current; present in split
                    if (splitVer.equals(curVer) && !splitVer.equals(givenVer)) {
                        GitUtils.writeStagedToIndex(givenVer, file);
                        File cwdFile = Utils.join(CWD, file);
                        GitUtils.updateRepoFile(cwdFile, givenVer);

                        // If modified differently in cur and given
                        // Split is different from both current and given
                    } else if (!splitVer.equals(curVer)
                            && !splitVer.equals(givenVer)
                            && !curVer.equals(givenVer)) {
                        mergeConflictFiles.add(file);
                    } // if only modified in current but not in split and given, don't do anything

                } else {
                    // File absent in split point
                    if (!givenVer.equals(curVer)) {
                        mergeConflictFiles.add(file);
                    }

                }
            } else {
                // File absent in given branch
                if (splitFiles.containsKey(file)) {
                    // If split point contains the file
                    String splitVer = splitFiles.get(file);
                    // Merge conflict
                    if (!splitVer.equals(curVer)) {
                        mergeConflictFiles.add(file);
                    } else {
                        // Remove and (untrack) -- > later, will clear stage
                        GitUtils.stageForRemoval(file, curVer);
                        Utils.restrictedDelete(file);
                    }
                }

            }
        }

        // not in cur branch
        for (String file : givenFiles.keySet()) {

            if (!curFiles.containsKey(file)) {
                String givenVer = givenFiles.get(file);
                // If in split point
                if (splitFiles.containsKey(file)) {
                    String splitVer = splitFiles.get(file);
                    if (!splitVer.equals(givenVer)) {
                        mergeConflictFiles.add(file);
                    } // else, don't do anything
                } else {
                    // Only in given branch
                    GitUtils.writeStagedToIndex(givenVer, file);
                    File cwdFile = Utils.join(CWD, file);
                    GitUtils.updateRepoFile(cwdFile, givenVer);
                }
            }
        }

        return mergeConflictFiles;
    }

    /**
     * Merge!!!
     * @param branchName branch to merge from
     */
    static void merge(String branchName) {

        // Perform pre-merge condition check
        mergeCheck(branchName);

        String curCommit = GitUtils.getHead();
        String givenCommit = GitUtils.getBranchHead(branchName);

        HashSet<String> curAncestors = Commit.getAncestors(curCommit);
        HashSet<String> givenAncestors = Commit.getAncestors(givenCommit);

        if (curAncestors.contains(givenCommit)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        } else if (givenAncestors.contains(curCommit)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        // Lowest common ancestor of the two branches
        String lowestCommonAncestor = Commit.lowestCommonAncestor(curCommit, givenAncestors);

        HashMap<String, String> curFiles = GitUtils.getCommit(curCommit).getContents();
        HashMap<String, String> givenFiles = GitUtils.getCommit(givenCommit).getContents();
        HashMap<String, String> splitFiles = GitUtils.getCommit(lowestCommonAncestor).getContents();
        HashSet<String> mergeConflictFiles = mergeCompare(curFiles,
                givenFiles, splitFiles);

        mergeConflictHelper(mergeConflictFiles,
                curFiles, givenFiles);

        GitUtils.preCommitUpdate(curFiles);
        Commit mergeCommit = new Commit(curFiles, curCommit, givenCommit,
                GitUtils.currentBranch(), branchName);
        GitUtils.writeCommit(mergeCommit);

        // Clearing "staged for addition" and "staged for removal"
        GitUtils.clearStage();
        GitUtils.addCommit(mergeCommit.getHash());

    }

    /**
     * Print merge conflict message
     * @param mergeConflictFiles
     * @param curFiles
     * @param givenFiles
     */
    static void mergeConflictHelper(HashSet<String> mergeConflictFiles,
                                    HashMap<String, String> curFiles,
                                    HashMap<String, String> givenFiles) {
        if (mergeConflictFiles.isEmpty()) {
            return;
        }

        String output = "Encountered a merge conflict.";

        for (String file : mergeConflictFiles) {
            String cur = "";
            String given = "";
            String content = "";

            if (curFiles.containsKey(file)) {
                cur = GitUtils.getBlobContent(curFiles.get(file));
            }

            if (givenFiles.containsKey(file)) {
                given = GitUtils.getBlobContent(givenFiles.get(file));
            }

            content += "<<<<<<< HEAD\n" + cur + "=======\n" + given + ">>>>>>>\n";

            File loc = Utils.join(CWD, file);
            Utils.writeContents(loc, content);
            Blob newB = new Blob(file);
            String hash = newB.getHash();

            GitUtils.writeBlob(newB);
            GitUtils.writeStagedToIndex(hash, file);

        }

        System.out.println(output);
    }


}
