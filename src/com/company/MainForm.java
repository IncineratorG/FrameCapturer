package com.company;


import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainForm {
    private JPanel panel;
    private JButton startVideoButton;
    private JLabel imageLabel;
    private JButton choosePathButton;

    private static VideoCapture videoCapture;

    private static final String IMAGE_FOLDER = "C:\\Empty files\\image_2.jpg";
    private static final String VIDEO_FOLDER = "C:\\Empty files\\VIDEOS\\video_2.avi";
    private String GOOGLE_DRIVE_FOLDER = "";

    private boolean MAGIC_BOOLEAN_VARIABLE = false;


    public static void main(String[] args) {
        try {
            System.load("C:\\OpenCV_3.3.0\\build\\java\\x64\\opencv_java330.dll");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("CANNOT_LOAD_LIBRARY");
            return;
        }

        JFrame frame = new JFrame("FrameCapturer");
        frame.setContentPane(new MainForm().panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public MainForm() {
        startVideoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getCameraImage();
            }
        });

        choosePathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String watchedFolderPath = chooseWatchedFolder();
                // ==
                System.out.println(watchedFolderPath);
                // ==
                watchForFolder(watchedFolderPath);
            }
        });
    }


    private void getCameraImage() {
        Mat mat = new Mat();

        videoCapture = new VideoCapture(0);
        videoCapture.read(mat);
        if (mat.empty()) {
            System.out.println("MAT_IS_EMPTY");
            return;
        }

        BufferedImage bufferedImage = toBufferedImage(mat);
        Image image = bufferedImage.getScaledInstance(imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);

        imageLabel.setIcon(new ImageIcon(image));

        File imageFile = new File("C:\\Users\\Alexander\\Google Диск\\TEST\\image.jpg");
        try {
            ImageIO.write(bufferedImage, "jpg", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        videoCapture.release();
    }

    private BufferedImage toBufferedImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }



    private String chooseWatchedFolder() {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        int returnValue = chooser.showOpenDialog(null);

        String folderPath = null;

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();

            folderPath = selectedFile.getAbsolutePath();
            folderPath = folderPath.substring(0, folderPath.lastIndexOf("\\"));
        }

        return folderPath;
    }

    private void watchForFolder(String watchedFolderPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("WATCHER_START - " + watchedFolderPath);

                Path dir = Paths.get(watchedFolderPath);

                try {
                    WatchService watcher = dir.getFileSystem().newWatchService();
                    dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

                    while (true) {
                        WatchKey watchKey = watcher.poll(10, TimeUnit.SECONDS);

                        if (watchKey != null) {
                            List<WatchEvent<?>> events = watchKey.pollEvents();

                            for (WatchEvent event : events) {
                                Path changedPath = (Path) event.context();

                                if (changedPath.endsWith("file")) {
                                    System.out.println(dir.resolve(changedPath).toString());
                                    Thread.sleep(500);
                                    String fileText = readFile(dir.resolve(changedPath).toString());

                                    if (fileText != null && !fileText.isEmpty())
                                        getCameraImage();
//                                        putImageToFolder(watchedFolderPath);
                                }
                            }
                            watchKey.reset();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void putImageToFolder(String folderPath) {
        System.out.println(folderPath);

        String imageSourcePath = "C:\\Empty files\\image.jpg";
        if (MAGIC_BOOLEAN_VARIABLE)
            imageSourcePath = "C:\\Empty files\\image_3.jpg";

        String imageDestinationPath = folderPath + "\\image.jpg";

        try {
            Files.copy(Paths.get(imageSourcePath), Paths.get(imageDestinationPath), StandardCopyOption.REPLACE_EXISTING);
            MAGIC_BOOLEAN_VARIABLE = !MAGIC_BOOLEAN_VARIABLE;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setImageToLabel() {
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(IMAGE_FOLDER));

            Image image = bufferedImage.getScaledInstance(imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void choosePath() {
        JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = jFileChooser.showOpenDialog(null);
        // int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jFileChooser.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
        }
    }
    private void choosePath_V2() {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = chooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("WATCHER_START");

                    String folderToWatch = selectedFile.getAbsolutePath();
                    folderToWatch = folderToWatch.substring(0, folderToWatch.lastIndexOf("\\"));
                    System.out.println("FOLDER_TO_WATCH: " + folderToWatch);

                    Path dir = Paths.get(folderToWatch);

                    try {
                        WatchService watcher = dir.getFileSystem().newWatchService();
                        dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

                        while (true) {
                            WatchKey watchKey = watcher.poll(10, TimeUnit.SECONDS);

                            if (watchKey != null) {
                                List<WatchEvent<?>> events = watchKey.pollEvents();

                                for (WatchEvent event : events) {
                                    Path changedPath = (Path) event.context();
                                    if (changedPath.endsWith("1.txt")) {
                                        readFile(folderToWatch + "\\" + changedPath);
                                    }
                                }
                                watchKey.reset();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private String readFile(String filePath) {
        String line = null;
        StringBuilder sb = new StringBuilder();

        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            line = null;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }


    private void watchFotPath_Thread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Path dir = Paths.get("C:\\Empty files\\IMAGES");

                try {
                    WatchService watcher = dir.getFileSystem().newWatchService();
                    dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

                    while (true) {
                        WatchKey watchKey = watcher.poll(10, TimeUnit.SECONDS);
                        if (watchKey != null) {
                            watchKey.pollEvents().stream().forEach(event -> System.out.println(event.context()));
                            System.out.println("===========================");
                        }
                        if (watchKey != null)
                            watchKey.reset();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    private void watchForPath() {
//        Path dir = Paths.get("C:\\Empty files\\IMAGES");
//
//        try {
//            WatchService watcher = dir.getFileSystem().newWatchService();
//            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
//
//            WatchKey watchKey = watcher.take();
//
//            java.util.List<WatchEvent<?>> events = watchKey.pollEvents();
//            for (WatchEvent event : events) {
//                Path changedPath = (Path) event.context();
//                System.out.println(changedPath);
//
//                if (changedPath.endsWith("3.png")) {
//                    System.out.println("FILE_CHANGED");
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
