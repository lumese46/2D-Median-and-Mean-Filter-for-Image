import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.util.Arrays;
import java.awt.Color;

public class MedianFilterSerial {
    public static String inputImageName;// this holds the inputimage names
    public static String outputImageName;// this holds the outputimage name
    public static int windowWidth;// this holds the size of window e.g 3x3
    private static BufferedImage img = null;// this is the image container
    private static BufferedImage img_out = null;
    private static int _length;// this holds the length of the array
    private static int width = 0;// this is the width of the picture
    private static int height = 0;// this is the hieght of the picture
    private static File f = null;// this is used to read into a file

    public static void main(String[] args) throws IIOException {
        // this checks idf the length of commandline arguments are valid if not exits
        if (args.length > 2) {
            try {
                // sets window width checks if its odd
                windowWidth = Integer.parseInt(args[2]);
                if (!is_windowWidth(windowWidth)) {
                    System.out.println("windowWidth not odd");
                    System.exit(1);
                }
                // sets input image name
                inputImageName = args[0];
                // sets out image name
                outputImageName = args[1];

            } catch (NumberFormatException e) {
                System.out.println("argument not integer");
                System.exit(1);
            }

        } else {
            System.out.println("NO args given or not enough given");
            System.exit(1);
        }

        // calculates length of array
        int windowWidth_squared = windowWidth * windowWidth;

        _length = windowWidth_squared;// set the length of the array
        long startTime = -1;// sets the start time

        // set the sizes of the arrays equal to the window squared
        int[] alpha = new int[windowWidth_squared];
        int[] Red_values = new int[windowWidth_squared];
        int[] Blue_values = new int[windowWidth_squared];
        int[] Green_values = new int[windowWidth_squared];
        int[] Kanel_p = new int[windowWidth_squared];
        // read the image and set the width
        Read_image_And_SetWidthHeight(inputImageName);
        // starts timing my sequential median filter method
        startTime = System.currentTimeMillis();
        // run median filter method
        Set_Median_filter_two(img, windowWidth, alpha, Red_values, Blue_values, Green_values, Kanel_p);
        // calculate elapsed time after the method is run
        long elap = System.currentTimeMillis() - startTime;
        double seconds = elap / 1000f;
        // write into the output image
        Write_image(outputImageName);
        System.out.println("Time : " + seconds);

    }

    /*
     * this method takes an image and calculates its median filter and sets it
     */
    public static void Set_Median_filter_two(BufferedImage _img, int _windowWidth, int[] _alpha,
            int[] _Red_values, int[] _Blue_values, int[] _Green_values, int[] _Kanel_p) {
        // this is for the each pixel in an image
        for (int x = _windowWidth; x < width - _windowWidth; x++) {
            for (int y = _windowWidth; y < height - _windowWidth; y++) {
                int _index = 0; // this is used as index for _kanel_p of the filter
                int _start = (_windowWidth / 2) * -1;// this is for the fisrt surronding image
                int _end = (_windowWidth / 2) + 1;// this is for the last surrounding image

                // this is my kanel
                for (int a = _start; a < _end; a++) {
                    for (int b = _start; b < _end; b++) {
                        _Kanel_p[_index] = img.getRGB(x + a, y + b);
                        _index++;
                    }
                }

                // adding the RGB values in an array

                for (int d = 0; d < _length; d++) {
                    _alpha[d] = (_Kanel_p[d] >> 24) & 0xff;

                    _Red_values[d] = (_Kanel_p[d] >> 16) & 0xff;
                    _Green_values[d] = (_Kanel_p[d] >> 8) & 0xff;
                    _Blue_values[d] = _Kanel_p[d] & 0xff;
                }

                // we are sorting the arrays
                Arrays.sort(_alpha);
                Arrays.sort(_Red_values);
                Arrays.sort(_Green_values);
                Arrays.sort(_Blue_values);
                int a = _alpha[_length / 2];
                int r = _Red_values[_length / 2];
                int g = _Green_values[_length / 2];
                int b = _Blue_values[_length / 2];
                // get pixel
                int pixel = (a << 24) | (r << 16) | (g << 8) | b;
                // set pixel
                _img.setRGB(x, y, pixel);
                // setting the new pixel values

            }
        }

    }

    // this function reads the img
    public static void Read_image_And_SetWidthHeight(String _imgName) {
        try {
            f = new File(_imgName); // sherry.jpg
            img = ImageIO.read(f);
            width = img.getWidth();
            height = img.getHeight();

        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

    }

    // this function writes img
    public static void Write_image(String _imgName) {
        try {

            f = new File(_imgName);
            ImageIO.write(img, "jpg", f);

        } catch (IOException e) {
            System.out.println("could not write into file");
        }

    }

    // this function checks if the window width is odd
    public static boolean is_windowWidth(int _oddd) {
        // write code here to check if this is an odd or greater than 3
        if (_oddd == 1) {
            return false;
        }

        return _oddd % 2 == 1;
    }

}
