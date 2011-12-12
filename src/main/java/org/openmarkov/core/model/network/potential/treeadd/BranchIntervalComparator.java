/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.treeadd;

import java.util.Comparator;

public class BranchIntervalComparator implements Comparator<BranchInterval> {
	static private double epsilon= EpsilonValueAproximation.value;

	@Override
	public int compare(BranchInterval o1, BranchInterval o2) {
		if (o1.equals(o2)) {
			return 0;
		}
		
		// Si ambos tienen el mismo valor izquierdo			
		if (Math.abs(o1.left-o2.left)<=epsilon) {
			// ... son de igual caracteristica (abierto o cerrado)
			if (o1.leftClosed == o2.leftClosed) {
				// ... y además tienen el mismo valor derecho			
				if (Math.abs(o1.right-o2.right)<=epsilon) {
					// ... la diferencia sólo estará en cual de los dos es abierto (intervalo + pequeño)
					return o1.rightClosed ? +1 : -1;
				}
				else {
					// si tienen diferente tamaño, el más pequeño
					return o1.right < o2.right ? -1 : +1;
				}
			}
			else {
				// ... la diferencia estará cual de los dos es cerrado (el cerrado está + a la izquierda)
				return o1.leftClosed ? -1 : +1;
			}
		}
		else {
			return o1.left < o2.left ? -1 : +1;
		}
	}
}
