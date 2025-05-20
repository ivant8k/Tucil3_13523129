<!-- Back to Top Link-->
<a name="readme-top"></a>

<br />
<div align="center">
  <h1 align="center">Tucil 3 - Strategi Algoritma</h1>

  <p align="center">
    <h3>Rush Hour Puzzle Solver</h3>
    <h4>Using UCS, GBFS, A*, and IDA*</h4>
    <br/>
    <a href="https://github.com/ivant8k/Tucil3_13523129/issues">Report Bug</a>
    ·
    <a href="https://github.com/ivant8k/Tucil3_13523129/issues">Request Feature</a>
  </p>
</div>

<!-- CONTRIBUTOR -->
<div align="center" id="contributor">
  <strong>
    <h3>Made By:</h3>
    <h3>13523129</h3>
    <h3>Ivant Samuel Silaban</h3>
  </strong>
  <br>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
        <li><a href="#usage">Usage</a></li>
      </ul>
    </li>
    <li><a href="#features">Features</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- ABOUT THE PROJECT -->
## About The Project

This project is a Rush Hour puzzle solver implemented for Tugas Kecil 3 IF2211 Strategi Algoritma. The program solves Rush Hour puzzles using four different algorithms:
- Uniform Cost Search (UCS)
- Greedy Best First Search (GBFS)
- A* Search
- Iterative Deepening A* (IDA*)

The program features both a command-line interface and a graphical user interface, allowing users to visualize the puzzle-solving process and compare the performance of different algorithms.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->
## Getting Started

### Prerequisites

* Java Development Kit (JDK) 8 or higher
  - Download from: https://www.oracle.com/java/technologies/downloads/

### Installation

1. Clone the repository
   ```sh
   git clone https://github.com/ivant8k/Tucil3_13523129
   ```

2. Navigate to the project directory
   ```sh
   cd Tucil3_13523129
   ```

3. Compile the program
   ```sh
   javac -d bin src/*.java
   ```

### Usage

#### Command Line Interface
1. Run the program
   ```sh
   java -cp bin Main
   ```

2. When prompted, enter the input file name (located in the test directory)
3. Choose the algorithm to use (UCS/GBFS/A*/IDA*)
4. For algorithms other than UCS, choose a heuristic function:
   - Manhattan Distance
   - Euclidean Distance
   - Chebyshev Distance
5. The program will display the solution path and statistics

#### Graphical User Interface
1. Run the GUI version
   ```sh
   java -cp bin MainGUI
   ```

2. Use the interface to:
   - Load puzzle configurations
   - Select algorithms and heuristics
   - View the solution process
   - Save solutions to files

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- FEATURES -->
## Features

1. Multiple Algorithm Support
   - Uniform Cost Search (UCS)
   - Greedy Best First Search (GBFS)
   - A* Search
   - Iterative Deepening A* (IDA*)

2. Multiple Heuristic Functions
   - Manhattan Distance
   - Euclidean Distance
   - Chebyshev Distance

3. Input Validation
   - Board dimension validation
   - Exit position validation
   - Primary piece validation
   - Multiple exit detection
   - Empty board detection

4. Solution Statistics
   - Number of nodes visited
   - Solution path length
   - Execution time
   - Memory usage

5. File I/O
   - Load puzzle configurations from files
   - Save solutions to files

6. Dual Interface
   - Command-line interface
   - Graphical user interface

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- LICENSE -->
## License

This project is licensed under the MIT License - see the LICENSE file for details.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<br>
<h3 align="center">THANK YOU!</h3>