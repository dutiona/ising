
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

/*
 * Author: Stefan Bornhofen, EISTI
 * Version: v1.0
 * Date: Feb 2009
 * 
 * public methods:
 * 
 * public QuickChart()                              // constructor
 * public void background(Color color)              // set background color
 * public void axis(boolean axisX, boolean axisY)   // draw the axes?
 * public void center(double centerX, double centerY)   // set the center coordinates
 * public void scale(double scaleX, double scaleY)   // set the scale (pixel/unit ratio)
 * public void log(boolean logX, boolean logY)      // set the axes to log scale
 * public void add(String key, Point2D.Double[] vertices) // add a chart
 * public void connect(String key, int connect)     // the connection type of the vertices
 * public void edges(String key, Point[] edge)      // the indices of the connected points
 * public void color(String key, Color color)       // set chart color
 * public void width(String key, double width)      // set chart width
 * public void remove(String key)                   // remove chart
 * public void refresh()                            // refresh graphics
*/

public class QuickChart extends JFrame {
    private Hashtable<String,Chart> charts = null;
    private ChartPanel panel = null;
    
    public final static int CONNECT_NONE = 0;
    public final static int CONNECT_LINE = 1;
    public final static int CONNECT_CYCLE = 2;
    public final static int CONNECT_EDGE = 3;
    public final static int CONNECT_ALL = 4;
    
    public QuickChart()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640,480);
        setTitle("QuickChart v1.0");
        charts = new Hashtable<String,Chart>();
        panel = new ChartPanel(charts);
        add(panel);
        setVisible(true);
    }
    
    public void refresh()
    {
        panel.repaint();
    }
    
    public void background(Color color)
    {
         panel.background = color;
    }
    
    public void axis(boolean axisX, boolean axisY)
    {
        panel.axisX = axisX;
        panel.axisY = axisY;
    }

    public void log(boolean logX, boolean logY)
    {
        panel.logX = logX;
        panel.logY = logY;
        if (panel.center.x<=0) panel.center.x = 1;
        if (panel.center.y<=0) panel.center.y = 1;
    }

    public void scale(double scaleX, double scaleY)
    {
        panel.scale.x = scaleX;
        panel.scale.y = scaleY;
    }
    
    public void center(double centerX, double centerY) 
    {
        panel.center.x = centerX;
        panel.center.y = centerY;
    }
    
    public void add(String key, Point2D.Double[] vertices)
    {
        charts.put(key,new Chart(vertices, Color.BLACK,1,1));
    }
    
    public void color(String key, Color color)
    {
        Chart chart = charts.get(key);
        if (chart!=null) chart.color = color;
    }

    public void width(String key, double width)
    {
        Chart chart = charts.get(key);
        if (chart!=null) chart.width = width;
    }
        
    public void connect(String key, int connect)
    {
        Chart chart = charts.get(key);
        if (chart!=null) chart.connect = connect;
    }
    
    public void edges(String key, Point[] edges) 
    {
        Chart chart = charts.get(key);
        if (chart!=null) chart.edges = edges;
    }

    public void remove(String key)
    {
        charts.remove(key);
    }
    
    // *********** ChartPanel
    private class ChartPanel extends JPanel
    {
        Hashtable<String,Chart> charts = null;
        Point2D.Double center = new Point2D.Double(0,0);
        Point2D.Double mouse = new Point2D.Double(0,0);
        Point2D.Double scale = new Point2D.Double(100,100);
        DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance(Locale.US);
        Color background = Color.WHITE;
        boolean logX = false;
        boolean logY = false;
        boolean axisX = true;
        boolean axisY = true;
         
        ChartPanel(Hashtable<String,Chart> charts)
        {
            this.charts = charts;    

            ChartListener cl = new ChartListener(this);
            addMouseListener(cl);
            addMouseMotionListener(cl);
            addMouseWheelListener(cl);
         }
       
        @Override
        public void paintComponent(Graphics g)
        {        
            Image buffer = createImage(getWidth(),getHeight());
            Graphics2D g2D = (Graphics2D)buffer.getGraphics();
            g2D.setBackground(background);
            g2D.clearRect(0,0,getWidth(),getHeight());

            Enumeration e = charts.keys();
            while(e.hasMoreElements()) 
                drawChart((Chart)charts.get(e.nextElement()),g2D);
            drawFramework(g2D);
            g.drawImage(buffer,0,0,this);
        }
        
        void drawFramework(Graphics g)
        {          
            if (axisX) drawAxisX(g);
            if (axisY) drawAxisY(g);
            
            int n = 0;
            Enumeration e = charts.keys();
            while(e.hasMoreElements()) 
            {
                n++;
                String key = (String)e.nextElement();
                g.setColor(charts.get(key).color);
                g.drawString(key,10,20*n);               
            } 

            g.setColor(Color.BLACK);
            g.drawString("mouse : ("+format(mouse.x,2)+","+format(mouse.y,2)+")",10,getHeight()-50);
            g.drawString("center : ("+format(center.x,2)+","+format(center.y,2)+")",10,getHeight()-30);
            String s = "scale : ("+format(scale.x,2);
            if (logX) s+= "[log]";
            s += ","+format(scale.y,2);
            if (logY) s+= "[log]";
            s += ")";
            g.drawString(s,10,getHeight()-10);              
        }

        Point2D.Double getOrigin()
        {            
            double x0,y0;
            if (!logX) x0 = 0; else x0 = 1;
            if (!logY) y0 = 0; else y0 = 1;
            return new Point2D.Double(x0,y0);
        }

        boolean markX(int n, double unit, Graphics g)
        {
            double x;
            if (!logX) x = n*unit; else x = Math.pow(10,n)*unit;
            Point p = toPixel(new Point2D.Double(x,getOrigin().y));
            if (p!=null)
            {
                g.drawLine(p.x,p.y-6,p.x,p.y+6);
                String s = format(x,1);
                if (n==1 || n==-1 || (n%10==0)) g.drawString(s+"",p.x+5-7*s.length(),p.y+24);
                if (n>0) return (p.x<getWidth());
                else return (p.x>0);
            }
            return false;
        }

        boolean markY(int n, double unit, Graphics g)
        {
            double y;
            if (!logY) y = n*unit; else y = Math.pow(10,n)*unit;
            Point p = toPixel(new Point2D.Double(getOrigin().x,y));
            if (p!=null)
            {
                g.drawLine(p.x-6,p.y,p.x+6,p.y);
                String s = format(y,1);
                if (n==1 || n==-1 || (n%10==0)) g.drawString(s+"",p.x-10-7*s.length(),p.y+4);
                if (n>0) return (p.y>0);
                else return (p.y<getHeight());
            }
            return false;
        }

        void drawAxisX(Graphics g)
        {
            ((Graphics2D)g).setStroke(new BasicStroke(1,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.BLACK);
            Point p = toPixel(getOrigin());
            g.drawLine(0,p.y,getWidth(),p.y);             
            double unit = Math.pow(10,Math.floor(Math.log10(100/scale.x)));
            int n = 0;
            if (axisY) n = 1;
            while (markX(n,unit,g)) n++;
            n = -1;
            while (markX(n,unit,g)) n--;
        }
        
        void drawAxisY(Graphics g)
        {
            ((Graphics2D)g).setStroke(new BasicStroke(1,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.BLACK);
            Point p = toPixel(getOrigin());
            g.drawLine(p.x,0,p.x,getHeight());
            double unit = Math.pow(10,Math.floor(Math.log10(100/scale.y)));
            int n = 0;
            if (axisX) n = 1;
            while (markY(n,unit,g)) n++;
            n = -1;
            while (markY(n,unit,g)) n--;
        }

        Point toPixel(Point2D.Double p)
        {
            if (p==null) return null;
            int px;
            int py;
            if (!logX) px = (int)(getWidth()/2  + scale.x*(p.x-center.x));
            else 
            {
                if (p.x<=0) return null;
                px = (int)(getWidth()/2  + scale.x*(Math.log(p.x)-Math.log(center.x)));
            }            
            if (!logY) py = (int)(getHeight()/2 - scale.y*(p.y-center.y));
            else
            {
                if (p.y<=0) return null;
                py = (int)(getHeight()/2 - scale.y*(Math.log(p.y)-Math.log(center.y)));
            }            
            return new Point(px,py);
        }

        Point.Double toCoord(Point p)
        {
            double px;
            double py;
            if (!logX) px = center.x+(p.x-getWidth()/2)/scale.x;
            else px = Math.exp(Math.log(center.x)+(p.x-getWidth()/2)/scale.x);
            if (!logY) py = center.y-(p.y-getHeight()/2)/scale.y;
            else py = Math.exp(Math.log(center.y)-(p.y-getHeight()/2)/scale.y);          
            return new Point.Double(px,py);
        }
        
        void drawConnection (Chart chart, Graphics g, int i, int j)
        {
            Point p1 = null;
            Point p2 = null;
            if ((i>=0) && (i<chart.vertices.length)) p1 = toPixel(chart.vertices[i]);
            if ((j>=0) && (j<chart.vertices.length)) p2 = toPixel(chart.vertices[j]);
            if (p1==null && p2==null) return;
            else if (p1!=null && p2==null) g.drawLine(p1.x,p1.y,p1.x,p1.y);
            else if (p1==null && p2!=null) g.drawLine(p2.x,p2.y,p2.x,p2.y);
            else g.drawLine(p1.x,p1.y,p2.x,p2.y);    
        }

        void drawChart(Chart chart, Graphics g)
        {
            if (chart.vertices==null) return;
            g.setColor(chart.color);           
            ((Graphics2D)g).setStroke(new BasicStroke((float)chart.width,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (chart.connect)
            {
                case QuickChart.CONNECT_NONE:
                    for (int i=0;i<chart.vertices.length;i++)
                        drawConnection(chart,g,i,-1);
                break;
                case QuickChart.CONNECT_LINE:
                    for (int i=0;i<chart.vertices.length;i++)
                        drawConnection(chart,g,i,i+1);
                break;
                case QuickChart.CONNECT_CYCLE:
                    for (int i=0;i<chart.vertices.length-1;i++)
                        drawConnection(chart,g,i,i+1);
                    drawConnection(chart,g,0,chart.vertices.length-1);
                break;
                case QuickChart.CONNECT_EDGE:
                    if (chart.edges!=null) for (int i=0;i<chart.edges.length;i++)
                       if (chart.edges[i]!=null) drawConnection(chart,g,chart.edges[i].x,chart.edges[i].y);
                break;
                case QuickChart.CONNECT_ALL:
                    for (int i=0;i<chart.vertices.length;i++)
                        for (int j=i;j<chart.vertices.length;j++)
                            drawConnection(chart,g,i,j);
                break;
            } // switch     
        }
        
        String format(double x, int type)
        {
            if (Math.abs(x)>0 && (Math.abs(x)<0.0001 || Math.abs(x)>10000)) formatter.applyPattern("0E0");
            else switch (type)
            {
                case 1: formatter.applyPattern("#0.######################"); break;
                case 2: formatter.applyPattern("#0.0000");; break;
            }
            return formatter.format(x);
        }

    }

    // ******** Chart    
    private class Chart
    {
        Point2D.Double[] vertices = null;
        Point[] edges = null;
        Color color = null;
        double width = 0;
        int connect = 0;
 
        Chart(Point2D.Double[] vertices, Color color, double width, int connect)
        {
            this.vertices = vertices;
            this.edges = null;
            this.color = color;
            this.width = width;
            this.connect = connect;
        }
    }

    // ********* ChartListener    
    private class ChartListener implements MouseListener, MouseMotionListener, MouseWheelListener 
    {
        ChartPanel panel;
        int x;
        int y;
        boolean leftButton = false;
        boolean rightButton = false;
        
        ChartListener(ChartPanel panel)
        {
            this.panel = panel;
        }
        
        // MouseListener
        public void mousePressed(MouseEvent e) {
            if (e.getButton()==MouseEvent.BUTTON1) leftButton = true;
            else if (e.getButton()==MouseEvent.BUTTON3) rightButton = true;
        }

         public void mouseReleased(MouseEvent e) {
            if (e.getButton()==MouseEvent.BUTTON1) leftButton = false;
            else if (e.getButton()==MouseEvent.BUTTON3) rightButton = false;
        }

        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void mouseClicked(MouseEvent e) { }

        // MouseMotionListener
        public void mouseMoved(MouseEvent e) {
            x = e.getX();
            y = e.getY();
            Point2D.Double p = panel.toCoord(new Point(x,y));
            panel.mouse.setLocation(p.x,p.y);
            panel.repaint();
        }   

        public void mouseDragged(MouseEvent e) {
            int diffx = e.getX() - x;
            int diffy = e.getY() - y;
            if (leftButton)
            {
                Point2D.Double p = panel.toCoord(new Point(panel.getWidth()/2-diffx,panel.getHeight()/2-diffy));
                panel.center.x = p.x;
                panel.center.y = p.y;
            
            
            }
            if (rightButton)
            {
                Point2D.Double p = panel.toCoord(new Point(e.getX(),e.getY()));
                panel.mouse.setLocation(p.x,p.y);
                int cx,cy;
                if (p.x<panel.getOrigin().x) cx = -1; else cx = 1;
                if (p.y<panel.getOrigin().y) cy = -1; else cy = 1;
                panel.scale.x *= (1+cx*diffx/500.0);
                panel.scale.y *= (1-cy*diffy/500.0);
            }
            x = e.getX();
            y = e.getY();
            panel.repaint();
         }

        // MouseWheelListener
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            double sc = e.getWheelRotation()/10.0;       
            panel.scale.x *= (1-sc);
            panel.scale.y *= (1-sc);
            Point2D.Double p = panel.toCoord(new Point(x,y));
            panel.mouse.setLocation(p.x,p.y);
            panel.repaint();
        }
    }
} // QuickChart
