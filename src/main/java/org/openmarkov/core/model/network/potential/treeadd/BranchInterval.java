/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.treeadd;

import java.text.NumberFormat;

public class BranchInterval {
	protected double left;
	protected double right;
	protected boolean leftClosed;
	protected boolean rightClosed;

	static private double epsilon= EpsilonValueAproximation.value;
	
	public BranchInterval (double left, double right, boolean leftClosed, boolean rightClosed) {
		this.left= left;
		this.right= right;
		this.leftClosed= leftClosed;
		this.rightClosed= rightClosed;
	}

	public double getLeft() {
		return left;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getRight() {
		return right;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public boolean isLeftClosed() {
		return leftClosed;
	}

	public void setLeftClosed(boolean leftClosed) {
		this.leftClosed= leftClosed;
	}

	public boolean isRightClosed() {
		return rightClosed;
	}

	public void setRightClosed(boolean rightClosed) {
		this.rightClosed = rightClosed;
	}
	
	public boolean equals (Object obj) {
		if (this == obj) {
			return true;
		}
		
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}

		BranchInterval other= (BranchInterval) obj;
		
		if (leftClosed!=other.leftClosed || rightClosed!=other.rightClosed) {
			return false;
		}
		
		if (Math.abs (left-other.left)>epsilon) {
			return false;
		}

		if (Math.abs (right-other.right)>epsilon) {
			return false;
		}
		
		return true;
	}
	
	public boolean overlaps (BranchInterval other) {
		if (other.equals(this)) {
			return true;
		}

		boolean difA= Math.abs (left-other.right)>epsilon;
		boolean difB= Math.abs (other.left-right)>epsilon;
		
		if (!difA) {
			// Possible join at 'left' value
			return leftClosed || other.rightClosed;
		}

		if (!difB) {
			// Possible join at 'right' value			
			return rightClosed || other.leftClosed;
		}
		
		double value= Math.signum (left - other.right) * Math.signum (other.left - right);
		
		return value>=epsilon;
	}
	
	public boolean isSinglePointInterval() {
		if (Math.abs (left-right)>epsilon) {
			return false;
		}
		
		if (!leftClosed || !rightClosed) {
			throw new RuntimeException("Impossible interval: " + this);					
		}
		
		return true;
	}
	
	public static BranchInterval join (BranchInterval bi, BranchInterval bj) {
		if (!bi.overlaps(bj)) {
			throw new RuntimeException("Intervals don't overlaps: " + bi + ", " + bj);				
		}
		
		double newLeft= Math.min (bi.left, bj.left);
		double newRight= Math.max (bi.right, bj.right);

		boolean newLeftClosed= bi.left < bj.left ? bi.leftClosed : bj.leftClosed;
		boolean newRightClosed= bi.right > bj.right ? bi.rightClosed : bj.rightClosed;

		if (Math.abs (bi.left-bj.left)<=epsilon) {
			newLeftClosed= bi.leftClosed || bj.leftClosed;
		}

		if (Math.abs(bi.right-bj.right)<=epsilon) {
			newRightClosed= bi.rightClosed || bj.rightClosed;
		}
		
		return new BranchInterval (newLeft, newRight, newLeftClosed, newRightClosed);
	}
	
	/** @param value. <code>double</code>
	 * @return boolean */
	public boolean containsValue(double value) {
		return ((value == left && leftClosed) ||
				((value > left) && (value < right)) || 
				(value == right && rightClosed));
	}
	
	public String toString() {
		String result= leftClosed ? "[" : "(";
		
		NumberFormat nf= NumberFormat.getInstance();
		result += nf.format (left);
		result += ", ";
		result += nf.format (right);
		
		result+= rightClosed ? "]" : ")";
		
		return result;
	}
}
