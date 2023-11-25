import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.util.Arrays;
import java.awt.Color;

public class MeanFilterParallel  extends RecursiveAction{
    public  static String inputImageName;// this holds the inputimage name
    public  static String outputImageName;// this holds the outputimage name
    public static int windowWidth;// this holds the size of window e.g 3x3 
    private static BufferedImage img = null;// this is the image container
    private static BufferedImage img_out = null;
    private static int _length;// this holds the length of the array
    private static int width = 0;// this is the width of the picture
    private static int height = 0;// this is the hieght of the picture
    private static File f = null;// this is used to read into a file
    
    
    private static     int windowWidth_squared;

    // holds the rgb values
    private static     int alpha;
    private static     int Red_values;
    private static     int Blue_values;
    private static    int Green_values;
    private static    int[]  Kanel_p;
    
    // for the parrallel part
    int low;
    int high;
    // for the parrallel part
    
    MeanFilterParallel(int _low, int _high  ) {
       low = _low; // this is for the first for loop 
       high = _high; // this is for the first for loop
    }
    protected void compute(){
        // this is my sequential cuttoff
        if(high - low   <=  width/4) {
            //// this is for the each pixel in an image using split values
            for(int x =low; x <  high;x++){
            for(int y= windowWidth;y < height - windowWidth;y++){
                    int index = 0; // this is used as index for _kanel_p of the filter
                    int start = (windowWidth/2) * -1;// this is for the fisrt surronding image
                    int end = (windowWidth/2) + 1;// this is for the last surrounding image
                    //this is my kanel
                   for (int a = start; a < end; a++){
                        for(int b = start; b < end ; b++){
                            Kanel_p[index ] = img.getRGB(x + a ,y + b);
                            index++;
                        }
                    }
               // int test = 0;
               // used for incrementing
                int alpha2 = 0;
                int Red_values2 = 0;
                int Blue_values2 = 0;
                int Green_values2 = 0;

                // adding the RGB values of surrounding pixels including the target
                // adding the RGB values of surrounding pixels including the target
                for(int d = 0;d <_length ; d ++){
                    // get value and add it
                    alpha =  (Kanel_p[d]>>24) & 0xff;
                    alpha2 = alpha2 + alpha;
                    // get value and add it
                    Red_values = (Kanel_p[d]>>16) & 0xff;
                    Red_values2 = Red_values2 + Red_values;
                    // get value and add it
                    Blue_values =  Kanel_p[d] & 0xff;
                    Blue_values2 = Blue_values2 +Blue_values;
                    // get value and add it
                    Green_values = (Kanel_p[d]>>8) & 0xff;
                    Green_values2 = Green_values2 + Green_values;
                    
                }
                
                 int a = alpha2/_length;
                int r = Red_values2/_length;
                int g = Green_values2/_length;
                int b = Blue_values2/_length;
                // get pixel
                int pixel = (a<<24) | (r<<16) | (g<<8) | b;
                // set pixel
                img.setRGB(x, y, pixel);


   



               

                
               //setting the new pixel values
                //img.setRGB(x,y,new Color(Red_values/_length,Green_values/_length, Blue_values/_length).getRGB());
                // initialize the RGB temp variable for the new pixel calculations
                Red_values = 0;
                Green_values = 0;
                Blue_values = 0;
            }
        }
        }
        else {
            MeanFilterParallel left = new MeanFilterParallel(low,(high+low)/2);
            MeanFilterParallel right= new MeanFilterParallel((high+low)/2,high);
            left.fork();
            left.join();
            right.compute();
            
            
       }
    } 
    // this function reads the img
    public static void Read_image_And_SetWidthHeight(String _imgName){
        try {
            f =  new File(_imgName); // sherry.jpg
            img = ImageIO.read(f);
            
           
            width = img.getWidth();
            height = img.getHeight();
            //System.out.println("read file");
            

        
        } catch (IOException e) {
            System.out.println("WRONG FILE NAME! can't read file");
            System.exit(1);
        }

    }
        // this function writes img
    public static void Write_image(String _imgName){
        try {
            f = new File(_imgName);
            ImageIO.write(img, "jpg", f);    
        } catch (IOException e) {
            System.out.println("can;t write to file");
            System.exit(1);

        }

    }

    public static boolean is_windowWidth(int _oddd){
        // write code here to check if this is an odd or greater than 3
        if (_oddd == 1){
            return false;
        }
        
        
        return _oddd%2==1;
    }
    
    public static void main(String[] args) throws IIOException{
        // this checks idf the length of commandline arguments are valid if not exits
        if ( args.length > 2){
            try{
                // sets window width checks if its odd
                windowWidth = Integer.parseInt(args[2]);
                if(!is_windowWidth(windowWidth)){
                    System.out.println("windowWidth not odd");
                    System.exit(1);
                }
                // sets input image name 
                inputImageName = args[0];
                 // sets out image name 
                outputImageName = args[1];
                
                
            }catch(NumberFormatException e){
                System.out.println("argument not integer");
                System.exit(1);
            }
            
        }else{
            System.out.println("NO args given or not enough given");
            System.exit(1);
        }
        // initialize arrays
        windowWidth_squared = windowWidth * windowWidth;
        _length = windowWidth_squared;
        Red_values = 0;
        Blue_values = 0;
        Green_values = 0;
        Kanel_p = new int[windowWidth_squared];
        // read the image and set the width
        Read_image_And_SetWidthHeight(inputImageName); //"down.jpeg

        long startTime = -1;
        int _high = width - windowWidth;
        int _low = windowWidth;
        
        MeanFilterParallel test = new MeanFilterParallel(_low , _high);//the task to be done, divide and conquer
        ForkJoinPool pool = new ForkJoinPool(); //the pool of worker threads
        startTime = System.currentTimeMillis();// starts timing my parrallel median filter method
        pool.invoke(test); //start everything running - give the task to the pool
        long elap = System.currentTimeMillis() - startTime;// calculate elapsed time after the method is run
        double seconds = elap / 1000f;
        // write into the output image
        Write_image(outputImageName);
        System.out.println("Time : " + seconds);
    
    }
}

