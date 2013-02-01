
public class metropolis {
	
	/**
	 * Fonction � tester pour l'algorithme de M�tropolis
	 * @param x - param�tre de la fonction
	 * @return double
	 */
	public static double f(double x){
		return 10+Math.pow(x, 2)-10*Math.cos(Math.PI*x);
	}
	
	/**
	 * Permet d'obtenir le minimum d'une fonction sur un intervalle donn�
	 * @param inf - borne inf�rieure
	 * @param sup - borne sup�rieure
	 * @param T - temp�rature initiale
	 * @param nmc - norme de monte carlo
	 * @param pas - pas de parcours
	 * @param M - pas � partir duquel on change la temp�rature
	 * @return double - minimum global de la fonction
	 */
	public static float[] metropolisRendu(float inf, float sup, float T, float nmc, float pas, int M){
		float[] Xtab= new float[(int) nmc];
		
		//valeur pr�c�dente
		float Xp;
		//valeur suivante
		float Xs = 0;
		
		//on tire al�atoirement un nombre inclus dans l'intervalle
		Xp = (float) (Math.random()*(sup-inf)+inf);

		int m = 0;
		int nT=0;
		float T0=T;
		for(int n=0; n<nmc; n++){
			//on calcule le suivant selon une loi uniforme {1/2,1/2}
			float temp = (float) Math.random();
			//on calcule al�atoirement une variable de calcul
			float U = (float) Math.random();
			if(temp<0.5){
				Xs=Xp+U*pas;
			}else{
				Xs=Xp-U*pas;
			}
			//on v�rifie que Xs est dans l'intervalle
			if(Xs>sup){
				//on retourne dans l'intervale
				Xs=sup-U*pas;
			}else if(Xs<inf){
				Xs=inf+U*pas;
			}
			
			//on calcule deltaF
			float deltaF = (float) (f(Xs)-f(Xp));
			
			if(deltaF>0){
				//on calcule une probabilit� P d'accepter Xs
				float P = (float) Math.exp(-deltaF/T);
				if(Math.random()<P){
					Xp=Xs;
					Xtab[n]= Xp;
				}
			}else{
				Xp=Xs;
				Xtab[n]= Xp;
			}
			
			m++;
			if(m==M){
				m=0;
				nT++;
				T=T0* (float) Math.pow((1-M*nT/nmc),4);
			}
		}
		
		return Xtab;
	}
}
