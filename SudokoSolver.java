import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Fast Sudoku solver (9x9) using bitmasks + MRV heuristic + forward checking.
 * Input: 9 lines, each with 9 characters: digits '1'-'9' or '.' or '0' for empty.
 * Example input line: 53..7.... 
 */
public class SudokuSolver {
    private static final int SIZE = 9;
    private static final int ALL = (1 << SIZE) - 1; // 0b1_1111_1111 == 511
    private final int[][] board = new int[SIZE][SIZE];
    private final int[] rowMask = new int[SIZE];
    private final int[] colMask = new int[SIZE];
    private final int[] boxMask = new int[SIZE];

    // Stats (optional)
    private long steps = 0;

    public SudokuSolver() { }

    // Parse lines of input (9 lines) where '.' or '0' => empty, otherwise '1'..'9'
    public void loadFromLines(List<String> lines) {
        if (lines.size() != SIZE) throw new IllegalArgumentException("Expect 9 lines");
        for (int r = 0; r < SIZE; r++) {
            String s = lines.get(r).trim();
            if (s.length() < SIZE) throw new IllegalArgumentException("Line " + (r+1) + " too short");
            for (int c = 0; c < SIZE; c++) {
                char ch = s.charAt(c);
                if (ch == '.' || ch == '0') {
                    board[r][c] = 0;
                } else if (ch >= '1' && ch <= '9') {
                    board[r][c] = ch - '0';
                } else {
                    throw new IllegalArgumentException("Invalid character at row " + (r+1) + " col " + (c+1));
                }
            }
        }
        initMasks();
    }

    private void initMasks() {
        for (int i = 0; i < SIZE; i++) {
            rowMask[i] = 0;
            colMask[i] = 0;
            boxMask[i] = 0;
        }
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int val = board[r][c];
                if (val != 0) {
                    int bit = 1 << (val - 1);
                    int b = boxIndex(r, c);
                    // Basic validity check (duplicate)
                    if ((rowMask[r] & bit) != 0 || (colMask[c] & bit) != 0 || (boxMask[b] & bit) != 0) {
                        throw new IllegalArgumentException("Initial puzzle has duplicates at row " + (r+1) + " col " + (c+1));
                    }
                    rowMask[r] |= bit;
                    colMask[c] |= bit;
                    boxMask[b] |= bit;
                }
            }
        }
    }

    private static int boxIndex(int r, int c) {
        return (r / 3) * 3 + (c / 3);
    }

    // Solve and return true if solved
    public boolean solve() {
        steps = 0;
        return solveRecursive();
    }

    private boolean solveRecursive() {
        steps++;
        // Find an empty cell with the fewest candidates (MRV)
        int bestR = -1, bestC = -1, bestCount = Integer.MAX_VALUE;
        int bestCandidates = 0;

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] != 0) continue;
                int used = rowMask[r] | colMask[c] | boxMask[boxIndex(r, c)];
                int candidates = (~used) & ALL;
                int cnt = Integer.bitCount(candidates);
                if (cnt == 0) return false; // forward-checking: dead end
                if (cnt < bestCount) {
                    bestCount = cnt;
                    bestR = r;
                    bestC = c;
                    bestCandidates = candidates;
                    if (cnt == 1) break; // can't do better than 1
                }
            }
            if (bestCount == 1) break;
        }

        // If no empty found, solved
        if (bestR == -1) return true;

        // Try candidates (iterate over set bits)
        int cand = bestCandidates;
        while (cand != 0) {
            int pick = cand & -cand; // lowest set bit
            int digit = Integer.numberOfTrailingZeros(pick) + 1; // 1..9
            cand -= pick;

            // place
            place(bestR, bestC, digit);
            if (solveRecursive()) return true;
            // undo
            remove(bestR, bestC, digit);
        }
        return false;
    }

    private void place(int r, int c, int d) {
        board[r][c] = d;
        int bit = 1 << (d - 1);
        rowMask[r] |= bit;
        colMask[c] |= bit;
        boxMask[boxIndex(r, c)] |= bit;
    }

    private void remove(int r, int c, int d) {
        board[r][c] = 0;
        int bit = ~(1 << (d - 1));
        rowMask[r] &= bit;
        colMask[c] &= bit;
        boxMask[boxIndex(r, c)] &= bit;
    }

    public void printBoard() {
        for (int r = 0; r < SIZE; r++) {
            if (r % 3 == 0 && r != 0) System.out.println("------+-------+------");
            for (int c = 0; c < SIZE; c++) {
                if (c % 3 == 0 && c != 0) System.out.print("| ");
                System.out.print(board[r][c] == 0 ? ". " : board[r][c] + " ");
            }
            System.out.println();
        }
    }

    public long getSteps() { return steps; }

    // --- simple CLI for testing ---
    public static void main(String[] args) throws Exception {
        System.out.println("Enter 9 lines with 9 chars (digits 1-9, or . / 0 for empty). Example: 53..7....");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            String line;
            do {
                line = br.readLine();
                if (line == null) throw new IllegalArgumentException("Not enough lines");
                line = line.trim();
            } while (line.isEmpty());
            lines.add(line);
        }

        SudokuSolver solver = new SudokuSolver();
        solver.loadFromLines(lines);

        long t0 = System.nanoTime();
        boolean solved = solver.solve();
        long t1 = System.nanoTime();

        if (solved) {
            System.out.println("\nSolved Sudoku:");
            solver.printBoard();
            System.out.printf("\nSolved in %.3f ms, recursive steps: %d\n", (t1 - t0) / 1e6, solver.getSteps());
        } else {
            System.out.println("No solution found (puzzle invalid or unsolvable).");
        }
    }
}
