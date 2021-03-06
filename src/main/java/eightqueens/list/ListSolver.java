package eightqueens.list;

import java.util.ArrayList;
import java.util.List;

import eightqueens.AbstractBoard;
import eightqueens.ISolver8Q;
import eightqueens.naive.Board;

/**
 * Solves the 8-queen puzzle. Keeps 4 lists of available rows, columns, NE and NW diagonals.
 * Every time a queen is placed on the board, an entry from each of the aforementioned lists
 * is removed. Models lists with bitmaps, so expect some heavy bit-fiddling.
 * @author ggeorgovassilis
 *
 */
public class ListSolver implements ISolver8Q {

	// bitmaps work in reverse here: 0 means TRUE, 1 means FALSE. Turns out to
	// be faster this way because
	// checking whether it's 0 or 1 takes the same time, but we are ever only
	// unsetting (never setting), which would require an OR and a XOR.
/*
	private int getNeDiagonal(int row, int column) {
		return row + column;
	}

	private int getSeDiagonal(int row, int column) {
		return row + 7 - column;
	}
*/	
	private int getSeDiagonalBitShift(int row, int column){
		// this would normally be 1 << getSeDiagonal(row, column);
		// but it's equal to:
		return ((1 << 7) << row) >> column;
	}

	private int getNeDiagonalBitShift(int row, int column){
		// this would normally be 1 << getNeDiagonal(row, column);
		//but it's equal to:
		return (1 <<row)<<column;
	}

	@Override
	public List<AbstractBoard> solve() {
		int queens[] = new int[8];
		int carry = 0;
		List<AbstractBoard> solutions = new ArrayList<AbstractBoard>(92);
		while (carry == 0) {
			int rowsBitmap = 0;
			int neDiagonalsBitmap = 0;
			int seDiagonalsBitmap = 0;

			boolean ok = true;
			for (int column = 0; ok && column < queens.length; column++) {
				int row = queens[column];
				// the joy of inverted logic: instead of logical operators we
				// can add the results, because only if all are 0 (we don't
				// expect negatives) the sum is 0.
				
				//an observation core i7, jdk 8: A+B+C==0 is faster than (A==0)&&(B==0)&&(C==0)
				//this is surprising because the getNeDiagonal computation is often unnecessary and
				//I'd expect the && checks to avoid the unnecessary getNeDiagonal invocation
				if (((rowsBitmap & (1 << row)) + (seDiagonalsBitmap & getSeDiagonalBitShift(row, column))
						+ (neDiagonalsBitmap & getNeDiagonalBitShift(row, column))) == 0) {
					// no operations on columns: there can be always only a
					// single queen in a column, thus we don't need to set/unset
					// them. reminder: inverted logic: 0 = TRUE, 1 = FALSE
					rowsBitmap |= 1 << row;
					// keeping getSeDiagonal and getNeDiagonal in an extra
					// variable (for the IF earlier) is an extra overhead, since
					// this block is executed very rarely (only if a solution is
					// found)
					seDiagonalsBitmap |= getSeDiagonalBitShift(row, column);
					neDiagonalsBitmap |= getNeDiagonalBitShift(row, column);
				} else
					ok = false;
			}
			if (ok) {
				AbstractBoard board = new Board();
				for (int column = 0; column < queens.length; column++) {
					board.set(queens[column], column, true);
				}
				solutions.add(board);
			}

			carry = 1;
			for (int column = 0; column < 8; column++) {
				int c = queens[column] + carry;
				queens[column] = c % 8;
				carry = c >> 3; // shift by 3 = division by 8. Marginally (3ms) faster than
								// the division itself.
				if (carry == 0)
					break;
			}
		}
		return solutions;
	}

}
