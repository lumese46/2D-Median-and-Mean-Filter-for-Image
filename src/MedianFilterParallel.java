import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.util.Arrays;
import java.awt.Color;

public class MedianFilterParallel  extends RecursiveAction{
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
    private static     int[] alpha;
    private static     int[] Red_values;
    private static     int[] Blue_values;
    private static    int[] Green_values;
    private static     int[] Kanel_p;
    
    // for the parrallel part
    int low;
    int high;
    // for the parrallel part
    
    MedianFilterParallel(int _low, int _high  ) {
       low = _low; // this is for the first for loop 
       high = _high; // this is for the first for loop
    }
    protected void compute(){
        // this is my sequential cuttoff
        if(high - low   <=  width/4) {
            //// this is for the each pixel in an image using split values
            for(int x = low; x < high;x++){
                for(int y= windowWidth;y < height - windowWidth;y++){
                    int _index = 0; // this is used as index for _kanel_p of the filter
                    int _start = (windowWidth/2) * -1;
                    int _end = (windowWidth/2) + 1;

                    //this is my kanel
                    for (int a = _start; a < _end; a++){
                        for(int b = _start; b < _end ; b++){
                            Kanel_p[_index ] = img.getRGB(x + a ,y + b);
                            _index++;
                        }
                    }

                    // adding the RGB values in an array
                
                    for(int d = 0;d <_length ; d ++){
                    
                        alpha[d] = (Kanel_p[d]>>24) & 0xff;
                        
                        Red_values[d] = (Kanel_p[d]>>16) & 0xff;
                        Green_values[d] = (Kanel_p[d]>>8) & 0xff;
                        Blue_values[d] = Kanel_p[d] & 0xff;
                    }
                    // sort pixel 
                    Arrays.sort(alpha);
                    Arrays.sort(Red_values);
                    Arrays.sort(Green_values);
                    Arrays.sort(Blue_values);
                    int a = alpha[_length/2];
                    int r = Red_values[_length/2];
                    int g = Green_values[_length/2];
                    int b = Blue_values[_length/2];
                    // get pixel
                    int pixel = (a<<24) | (r<<16) | (g<<8) | b;
                    // set pixel
                    img.setRGB(x, y, pixel);
                    //img.setRGB(x,y,new Color(Red_values[_length/2],Green_values[_length/2], Blue_values[_length/2]).getRGB());        
                }  
            }   
        }
        else {
            MedianFilterParallel left = new MedianFilterParallel(low,(high+low)/2);
            MedianFilterParallel right= new MedianFilterParallel((high+low)/2,high);
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
        alpha = new int[windowWidth_squared];
        Red_values = new int[windowWidth_squared];
        Blue_values = new int[windowWidth_squared];
        Green_values = new int[windowWidth_squared];
        Kanel_p = new int[windowWidth_squared];
        // read the image and set the width
        Read_image_And_SetWidthHeight(inputImageName); //"down.jpeg

        long startTime = -1;
        int _high = width - windowWidth;
        int _low = windowWidth;
        
        MedianFilterParallel test = new MedianFilterParallel(_low , _high);//the task to be done, divide and conquer
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