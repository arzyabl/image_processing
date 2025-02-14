import java.io.*;
import java.util.Stack;
import java.util.TreeSet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
 
public class Demo extends Component implements ActionListener {
    

    String descs[] = {
        "Original", 
        "Negative",
    };
 
    int opIndex;  //option index for 
    int lastOp;

    private Stack<BufferedImage> undoStack = new Stack<>();

    private BufferedImage bi, biFiltered, originalImage;   // the input image saved as bi;//
    int w, h;
    
    public Demo() {
        try {
            bi = ImageIO.read(new File("default.jpg"));

            w = bi.getWidth(null);
            h = bi.getHeight(null);

            originalImage = deepCopy(bi); // Save a deep copy of the original image

            System.out.println(bi.getType());
            if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics big = bi2.getGraphics();
                big.drawImage(bi, 0, 0, null);
                biFiltered = bi = bi2;
                saveToUndoStack();
            }
        } catch (IOException e) {      // deal with the situation that th image has problem;/
            System.out.println("Image could not be read");

            System.exit(1);
        }
    }                         
 
    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }
 

    String[] getDescriptions() {
        return descs;
    }

    // Return the formats sorted alphabetically and in lower case
    public String[] getFormats() {
        String[] formats = {"bmp","gif","jpeg","jpg","png"};
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());
        }
        return formatSet.toArray(new String[0]);
    }
 
    void setOpIndex(int i) {
        opIndex = i;
    }
 
    //    Image Negative
    public BufferedImage ImageNegative(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        // Image Negative Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = 255-ImageArray[x][y][1];  //r
                ImageArray[x][y][2] = 255-ImageArray[x][y][2];  //g
                ImageArray[x][y][3] = 255-ImageArray[x][y][3];  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }


    public void filterImage() {
 
        if (opIndex == lastOp) {
            return;
        }

        saveToUndoStack();

        lastOp = opIndex;
        switch (opIndex) {
        case 0: biFiltered = bi; /* original */
                return; 
        case 1: biFiltered = ImageNegative(bi); /* Image Negative */
                return;
        //************************************
        // case 2:
        //      return;
        //************************************

        }
 
    }

    // Method to save the current state to the undo stack
    private void saveToUndoStack() {
        try {
            BufferedImage copy = deepCopy(biFiltered);
            undoStack.push(copy);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to perform undo
    public void undo() {
        if (!undoStack.isEmpty()) {
            biFiltered = undoStack.pop();
            repaint();
        }
    }

    // Helper method to create a deep copy of a BufferedImage
    private BufferedImage deepCopy(BufferedImage original) throws IOException {
        ColorModel colorModel = original.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = original.copyData(null);
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }
 
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source instanceof JComboBox) {
            JComboBox cb = (JComboBox) source;
            if (cb.getActionCommand().equals("SetFilter")) {
                setOpIndex(cb.getSelectedIndex());
                repaint();
            }
        } else if (source instanceof JButton) {
            JButton button = (JButton) source;
            if (button.getActionCommand().equals("Undo")) {
                undo();
            }
        } else if (source instanceof JComboBox && e.getActionCommand().equals("Formats")) {
            String format = (String) ((JComboBox<?>) source).getSelectedItem();
            // Rest of the code for handling the format JComboBox
        }
    };

    public static void main(String s[]) {

        JFrame f = new JFrame("Image Processing Demo");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        Demo de = new Demo();
        f.add("Center", de);

        // Undo button
        JButton undoButton = new JButton("Undo");
        undoButton.setActionCommand("Undo");
        undoButton.addActionListener(de);

        JComboBox choices = new JComboBox(de.getDescriptions());
        choices.setActionCommand("SetFilter");
        choices.addActionListener(de);
        JComboBox formats = new JComboBox(de.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(de);

        // JPanel for the operations and undo button layout
        JPanel panel = new JPanel();
        panel.add(undoButton);
        panel.add(choices);
        panel.add(new JLabel("Save As"));
        panel.add(formats);

        f.add("North", panel);
        f.pack();
        f.setVisible(true);
    } 
}