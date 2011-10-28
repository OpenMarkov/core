package org.openmarkov.core.mdp;

/**
 *
 */
public class MatrizAleatoria {
	double [][]matriz;
	int numTransicionesPorEstado;
	
	/**
	 * @param n
	 * @param numTransicionesPorEstado
	 */
	public MatrizAleatoria (int n, int numTransicionesPorEstado) {
		matriz= new double[n][n];
		
		this.numTransicionesPorEstado= numTransicionesPorEstado;
		rellenaAleatorios();
	}
	
	
	public void rellenaAleatorios () {
		int n= matriz.length;
		
		for (int i=0; i<n; i++) {
			double prob= 1.0;
			boolean []usado= new boolean[n];
			int pdtes= numTransicionesPorEstado;
			
			while (pdtes>0) {
				int iEstado= (int) (n*Math.random());
				while (usado[iEstado]) {
					iEstado= (iEstado+1)%n;
				}
				
				usado[iEstado]= true;
				matriz[i][iEstado]= (pdtes==1) ? prob : prob*Math.random();
				prob -= matriz[i][iEstado];
				pdtes--;
			}				
		}
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MatrizAleatoria mat= new MatrizAleatoria(4,2);
		System.out.println(mat);
	}
	
}
