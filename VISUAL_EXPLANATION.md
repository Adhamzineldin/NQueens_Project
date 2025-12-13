# VISUAL EXPLANATION OF N-QUEENS PROJECT
## Diagrams and Flow Charts for Better Understanding

---

## 1. PROBLEM VISUALIZATION

### N-Queens Problem (4-Queens Example)

```
Invalid Configuration:
Q . . .     ← Queen at (0,0)
. . Q .     ← Queen at (1,2) - CONFLICT! Same diagonal
. Q . .     ← Queen at (2,1) - CONFLICT! Same diagonal
. . . Q     ← Queen at (3,3)

Valid Solution:
. Q . .     ← Queen at (0,1)
. . . Q     ← Queen at (1,3)
Q . . .     ← Queen at (2,0)
. . Q .     ← Queen at (3,2)

All queens are safe! ✓
```

---

## 2. ALGORITHM FLOW

### Backtracking Process (4-Queens)

```
START: solveFromRow(0)

Row 0:
  Try col=0: Place Q → Check safe? YES
    ↓
    Row 1:
      Try col=0: Check safe? NO (same column)
      Try col=1: Check safe? NO (same diagonal)
      Try col=2: Place Q → Check safe? YES
        ↓
        Row 2:
          Try col=0: Check safe? NO
          Try col=1: Check safe? NO
          Try col=2: Check safe? NO
          Try col=3: Check safe? NO
          → BACKTRACK to Row 1
        Remove Q from (1,2)
      Try col=3: Place Q → Check safe? YES
        ↓
        Row 2:
          Try col=0: Check safe? NO
          Try col=1: Check safe? NO
          Try col=2: Place Q → Check safe? YES
            ↓
            Row 3:
              Try col=0: Check safe? NO
              Try col=1: Check safe? NO
              Try col=2: Check safe? NO
              Try col=3: Check safe? NO
              → BACKTRACK to Row 2
          Remove Q from (2,2)
        Try col=3: Check safe? NO
        → BACKTRACK to Row 1
      Remove Q from (1,3)
    → BACKTRACK to Row 0
  Remove Q from (0,0)
  
  Try col=1: Place Q → Check safe? YES
    ↓
    ... continue exploring ...
```

### isSafe() Method Visualization

```
Checking if (row=2, col=2) is safe:

Board state:
Q . . .     ← Queen at (0,0)
. . Q .     ← Queen at (1,2)
. . ? .     ← Want to place here (2,2)
. . . .

Checks:
1. Column check: Look up column 2
   (0,2) = . ✓
   (1,2) = Q ✗ CONFLICT! Same column

Result: NOT SAFE
```

```
Checking if (row=2, col=3) is safe:

Board state:
Q . . .     ← Queen at (0,0)
. . Q .     ← Queen at (1,2)
. . . ?     ← Want to place here (2,3)
. . . .

Checks:
1. Column check: Look up column 3
   (0,3) = . ✓
   (1,3) = . ✓

2. Upper-left diagonal: Move up-left
   (1,2) = Q ✗ CONFLICT! Same diagonal

Result: NOT SAFE
```

```
Checking if (row=2, col=1) is safe:

Board state:
Q . . .     ← Queen at (0,0)
. . Q .     ← Queen at (1,2)
. ? . .     ← Want to place here (2,1)
. . . .

Checks:
1. Column check: Look up column 1
   (0,1) = . ✓
   (1,1) = . ✓

2. Upper-left diagonal: Move up-left
   (1,0) = . ✓
   (0,-1) = out of bounds ✓

3. Upper-right diagonal: Move up-right
   (1,2) = Q ✗ CONFLICT! Same diagonal

Result: NOT SAFE
```

---

## 3. MULTI-THREADING VISUALIZATION

### Thread Division (N=8, 4 Threads)

```
Board Columns:  0  1  2  3  4  5  6  7
                │  │  │  │  │  │  │  │
                ▼  ▼  ▼  ▼  ▼  ▼  ▼  ▼
Thread 0:      [0][1]                    (columns 0-1)
Thread 1:              [2][3]            (columns 2-3)
Thread 2:                      [4][5]    (columns 4-5)
Thread 3:                              [6][7] (columns 6-7)
```

### Parallel Execution

```
Time →

Thread 0:  [Col 0] → Solve rows 1-7
           [Col 1] → Solve rows 1-7
           
Thread 1:  [Col 2] → Solve rows 1-7
           [Col 3] → Solve rows 1-7
           
Thread 2:  [Col 4] → Solve rows 1-7
           [Col 5] → Solve rows 1-7
           
Thread 3:  [Col 6] → Solve rows 1-7
           [Col 7] → Solve rows 1-7

All threads run simultaneously!
```

### Solution Collection

```
Thread 0 finds: Solutions 1, 5, 12, 18, ...
Thread 1 finds: Solutions 2, 8, 15, 22, ...
Thread 2 finds: Solutions 3, 9, 16, 23, ...
Thread 3 finds: Solutions 4, 11, 19, 25, ...

StateManager collects all:
Total Solutions = 92 (for 8-Queens)
```

---

## 4. ARCHITECTURE DIAGRAM

### System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    USER INTERFACE                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ Control Panel│  │ Board Panels │  │Solutions Panel│  │
│  │              │  │ (per thread) │  │              │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────┘
                          │
                          │ (reads state)
                          ▼
┌─────────────────────────────────────────────────────────┐
│                   STATE MANAGER                          │
│  ┌──────────────────┐  ┌──────────────────┐              │
│  │ current_states   │  │   solutions     │              │
│  │ HashMap          │  │   HashMap       │              │
│  │ (thread_id →    │  │  (thread_id →  │              │
│  │  StepBoard)     │  │   Solutions)    │              │
│  └──────────────────┘  └──────────────────┘              │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ (updates state)
                          │
┌─────────────────────────────────────────────────────────┐
│                  THREAD MANAGER                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐│
│  │ Thread 0 │  │ Thread 1 │  │ Thread 2 │  │ Thread 3 ││
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘│
└─────────────────────────────────────────────────────────┘
                          │
                          │ (each thread)
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  SOLVER THREAD                           │
│  ┌──────────────────────────────────────────────┐       │
│  │  For each column in assigned range:          │       │
│  │    Create Board with first queen             │       │
│  │    Create NQueenSolver                      │       │
│  │    Call solveFromRow(1)                      │       │
│  └──────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────┘
                          │
                          │ (uses)
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  N-QUEEN SOLVER                          │
│  ┌──────────────────────────────────────────────┐       │
│  │  solveFromRow(row):                           │       │
│  │    For each column:                           │       │
│  │      If isSafe(row, col):                     │       │
│  │        Place queen                             │       │
│  │        solveFromRow(row+1)  [RECURSIVE]       │       │
│  │        Remove queen        [BACKTRACK]        │       │
│  └──────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────┘
                          │
                          │ (checks)
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      BOARD                               │
│  ┌──────────────────────────────────────────────┐       │
│  │  int[][] cells  (0=empty, 1=queen)          │       │
│  │  isSafe(row, col) - constraint checking      │       │
│  │  place(row, col) - add queen                 │       │
│  │  remove(row, col) - remove queen            │       │
│  └──────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────┘
```

---

## 5. DATA FLOW DIAGRAM

### Complete Data Flow

```
USER ACTION
    │
    │ (clicks START)
    ▼
GUI.startSolving()
    │
    │ (creates threads)
    ▼
ThreadManager.startSolving()
    │
    │ (divides columns)
    ▼
┌─────────────────────────────────────┐
│  Create SolverThread 0              │
│  Create SolverThread 1              │
│  Create SolverThread 2              │
│  Create SolverThread 3              │
└─────────────────────────────────────┘
    │
    │ (each thread starts)
    ▼
SolverThread.run()
    │
    │ (for each column)
    ▼
Create Board with first queen
    │
    │ (solve)
    ▼
NQueenSolver.solveFromRow(1)
    │
    │ (at each step)
    ▼
notifyStep(Action, row, col)
    │
    │ (synchronized)
    ▼
StateManager.updateState()
    │
    │ (stores in HashMap)
    ▼
current_states[thread_id] = StepBoard
    │
    │ (periodically)
    ▼
GUI Timer fires
    │
    │ (reads state)
    ▼
GUI.updateUI()
    │
    │ (updates display)
    ▼
BoardPanel.repaint()
    │
    │ (shows on screen)
    ▼
USER SEES REAL-TIME UPDATES
```

### Solution Collection Flow

```
NQueenSolver finds solution
    │
    │ (row == N)
    ▼
Create Solution object
    │
    │ (synchronized)
    ▼
StateManager.addSolution(thread_id, solution)
    │
    │ (stores in HashMap)
    ▼
solutions[thread_id].add(solution)
    │
    │ (periodically)
    ▼
GUI.updateSolutionsDisplay()
    │
    │ (creates preview)
    ▼
SolutionMiniPanel added to GUI
    │
    │ (user sees)
    ▼
SOLUTION APPEARS IN PANEL
```

---

## 6. THREAD SAFETY VISUALIZATION

### Without Synchronization (PROBLEM)

```
Thread 0:                    Thread 1:
StateManager.updateState()   StateManager.updateState()
    │                            │
    │ Read solutions             │ Read solutions
    │ (solutions = [1,2,3])     │ (solutions = [1,2,3])
    │                            │
    │ Add solution 4             │ Add solution 5
    │ (solutions = [1,2,3,4])   │ (solutions = [1,2,3,5])
    │                            │
    │ Write back                │ Write back
    │                            │
    ▼                            ▼
solutions = [1,2,3,4]        solutions = [1,2,3,5]

RESULT: Solution 4 is LOST! ✗
```

### With Synchronization (SOLUTION)

```
Thread 0:                    Thread 1:
StateManager.updateState()   StateManager.updateState()
    │                            │
    │ [LOCK ACQUIRED]            │ [WAITING...]
    │                            │
    │ Read solutions             │
    │ (solutions = [1,2,3])      │
    │                            │
    │ Add solution 4             │
    │ (solutions = [1,2,3,4])   │
    │                            │
    │ Write back                │
    │                            │
    │ [LOCK RELEASED]            │
    │                            │ [LOCK ACQUIRED]
    │                            │
    │                            │ Read solutions
    │                            │ (solutions = [1,2,3,4])
    │                            │
    │                            │ Add solution 5
    │                            │ (solutions = [1,2,3,4,5])
    │                            │
    │                            │ Write back
    │                            │
    │                            │ [LOCK RELEASED]
    ▼                            ▼
solutions = [1,2,3,4,5]     ✓ All solutions preserved!
```

---

## 7. GUI UPDATE CYCLE

### Update Timeline

```
Time (ms)    Event
─────────────────────────────────────────────────────
0            User clicks START
10           Thread 0: Place queen at (0,0)
             → StateManager.updateState()
20           Thread 1: Place queen at (0,2)
             → StateManager.updateState()
30           Thread 0: Place queen at (1,2)
             → StateManager.updateState()
40           Thread 2: Place queen at (0,4)
             → StateManager.updateState()
50           GUI Timer fires (delay=50ms)
             → GUI.updateUI()
             → Reads from StateManager
             → BoardPanel 0 repaints (shows queens at 0,0 and 1,2)
             → BoardPanel 1 repaints (shows queen at 0,2)
             → BoardPanel 2 repaints (shows queen at 0,4)
60           Thread 0: Backtrack, remove queen at (1,2)
             → StateManager.updateState()
70           Thread 1: Place queen at (1,3)
             → StateManager.updateState()
...
100          GUI Timer fires again
             → GUI.updateUI()
             → All panels repaint with latest state
...
```

---

## 8. MEMORY LAYOUT

### Object Relationships

```
GUI Instance
    │
    ├── StateManager
    │       │
    │       ├── current_states: HashMap
    │       │       ├── 0 → StepBoard (Thread 0's state)
    │       │       ├── 1 → StepBoard (Thread 1's state)
    │       │       └── 2 → StepBoard (Thread 2's state)
    │       │
    │       └── solutions: HashMap
    │               ├── 0 → [Solution1, Solution5, ...]
    │               ├── 1 → [Solution2, Solution8, ...]
    │               └── 2 → [Solution3, Solution9, ...]
    │
    ├── ThreadManager
    │       │
    │       └── threads: List<SolverThread>
    │               ├── SolverThread 0
    │               │       └── NQueenSolver
    │               │               └── Board (independent copy)
    │               ├── SolverThread 1
    │               │       └── NQueenSolver
    │               │               └── Board (independent copy)
    │               └── SolverThread 2
    │                       └── NQueenSolver
    │                               └── Board (independent copy)
    │
    └── threadBoardPanels: HashMap
            ├── 0 → BoardPanel (displays Thread 0)
            ├── 1 → BoardPanel (displays Thread 1)
            └── 2 → BoardPanel (displays Thread 2)
```

---

## 9. EXECUTION TIMELINE

### Complete Solving Process

```
┌─────────────────────────────────────────────────────────────┐
│ Phase 1: Initialization (0-100ms)                           │
│   - User clicks START                                       │
│   - GUI clears previous state                               │
│   - Creates BoardPanels for each thread                     │
│   - ThreadManager calculates column distribution            │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ Phase 2: Thread Creation (100-200ms)                        │
│   - ThreadManager creates SolverThread instances            │
│   - Each thread assigned column range                       │
│   - All threads start simultaneously                        │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ Phase 3: Parallel Solving (200ms - completion)               │
│   Thread 0:  Exploring columns 0-1                         │
│   Thread 1:  Exploring columns 2-3                          │
│   Thread 2:  Exploring columns 4-5                          │
│   Thread 3:  Exploring columns 6-7                          │
│   - Each thread independently solves                        │
│   - StateManager collects updates                           │
│   - GUI displays progress in real-time                      │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ Phase 4: Solution Discovery (throughout solving)             │
│   - When solution found: StateManager.addSolution()          │
│   - GUI updates Solutions Panel                             │
│   - Statistics updated                                       │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ Phase 5: Completion (when all threads finish)                │
│   - All threads complete                                    │
│   - ThreadManager.waitForCompletion() returns               │
│   - GUI auto-stops                                          │
│   - Final statistics displayed                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 10. KEY CONCEPTS VISUALIZED

### Backtracking Concept

```
Decision Tree:
                    Start
                     │
        ┌────────────┼────────────┐
        │            │            │
    Col 0         Col 1        Col 2
        │            │            │
    [Q][ ][ ][ ]  [ ][Q][ ][ ]  [ ][ ][Q][ ]
        │            │            │
    Try Row 1    Try Row 1   Try Row 1
        │            │            │
    ✗ Conflict   ✓ Safe      ✗ Conflict
        │            │            │
    BACKTRACK    Continue    BACKTRACK
                     │
              Try Row 2...
```

### Multi-Threading Benefit

```
Single Thread:
Time:  [████████████████████] 100 seconds

4 Threads:
Time:  [████] 25 seconds (Thread 0)
       [████] 25 seconds (Thread 1)
       [████] 25 seconds (Thread 2)
       [████] 25 seconds (Thread 3)
       
Total: 25 seconds (4x speedup!)
```

---

**Use these diagrams to explain the project visually during your discussion!**


