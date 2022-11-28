package gitlet;

import java.util.Date;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Anna (Yutong) Zhang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateArgs(args, 1);
                Repository.init();
                break;
            case "add":
                checkGitInit();
                validateArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                checkGitInit();
                validateArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                checkGitInit();
                validateArgs(args, 2);
                Repository.remove(args[1]);
                break;
            case "log":
                checkGitInit();
                validateArgs(args, 1);
                Repository.log();
                break;
            case "global-log":
                checkGitInit();
                validateArgs(args, 1);
                Repository.globall();
                break;
            case "find":
                checkGitInit();
                validateArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                checkGitInit();
                validateArgs(args, 1);
                Repository.status();
                break;
            case "checkout":
                checkGitInit();
                if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkout(args[3], args[1]);
                } else if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkoutCurrent(args[2]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                checkGitInit();
                validateArgs(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                checkGitInit();
                validateArgs(args, 2);
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                checkGitInit();
                validateArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                checkGitInit();
                validateArgs(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }

        return;
    }

    private static void validateArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    private static void checkGitInit() {
        if (!GitUtils.isInited()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

    }
}
