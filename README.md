# Sudoku Solver – Java (Bitmask + MRV Heuristic + Forward Checking)

A high-performance **9x9 Sudoku solver** implemented in Java, using **bitmasking**, the **Minimum Remaining Value (MRV)** heuristic, and **forward checking** to solve puzzles efficiently.  
The solver takes a puzzle as input from the console and outputs the solved grid, along with performance statistics.

---

## 🚀 Features
- **Optimized Backtracking** using MRV heuristic to minimize branching.
- **Bitmasking** for fast candidate checking and constraint propagation.
- **Forward Checking** to detect dead ends early and reduce computation.
- Validates initial puzzle for duplicate entries before solving.
- Prints execution time and number of recursive steps taken.

---

## 🛠️ Technologies Used
- **Java** (Core Java)
- **Bitmask Operations**
- **Recursive Backtracking**
- **Heuristics for Optimization**

---

## 📂 How to Run
1. **Compile the Program**
   ```bash
   javac SudokuSolver.java
