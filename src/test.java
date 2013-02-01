
import java.awt.Color;
import java.awt.geom.Point2D;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		float [] Xtab= metropolis.metropolisRendu(-5, 5, 100, (float)Math.pow(2, 17), (float)1, 512);

		
		Point2D.Double[] donnees= new Point2D.Double[Xtab.length];
		
		
		
		for (int i=0;i<Xtab.length;i++) {
            donnees[i] = new Point2D.Double(i, Xtab[i]);
            //System.out.println(donnees[i]);            
        }
		
		QuickChart courbe = new QuickChart();
		courbe.add("Evolution de x en fonction de l'itération", donnees);
		courbe.color("Evolution de x en fonction de l'itération",Color.BLUE);
		courbe.refresh();
		
	}

}
