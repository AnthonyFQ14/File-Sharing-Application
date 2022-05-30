import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.Socket;
import java.io.File;
import java.util.ArrayList;

public class WordCountClient {

    static ArrayList<MyFile> myFiles = new ArrayList<>();
    // Label that has the file name.
    static JLabel jlFileName = new JLabel("Choose a file to send.");
    // Frame to hold everything.
    static JFrame jFrameFilesInServer = new JFrame("File's in Server");
    public static void main(String[] args) {


        // Accessed from within inner class needs to be final or effectively final.
        final File[] fileToSend = new File[1];

        // Set the frame to house everything.
        JFrame jFrame = new JFrame("WordCount Client");
        // Set the size of the frame.
        jFrame.setSize(400, 400);
        // Make the layout to be box layout that places its children on top of each other.
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        // Make it so when the frame is closed the program exits successfully.
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Title above panel.
        JLabel jlTitle = new JLabel("File Sender");
        // Change the font family, size, and style.
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        // Add a border around the label for spacing.
        jlTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
        // Make it so the title is centered horizontally.
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Change the font.
        jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
        // Make a border for spacing.
        jlFileName.setBorder(new EmptyBorder(50, 0, 0, 0));
        // Center the label on the x axis (horizontally).
        jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Panel that contains the buttons.
        JPanel jpButton = new JPanel();
        // Border for panel that houses buttons.
        jpButton.setBorder(new EmptyBorder(75, 0, 10, 0));
        // Create send file button.
        JButton jbSendFile = new JButton("Send File");
        // Set preferred size works for layout containers.
        jbSendFile.setPreferredSize(new Dimension(150, 75));
        // Change the font style, type, and size for the button.
        jbSendFile.setFont(new Font("Arial", Font.BOLD, 20));
        // Make the second button to choose a file.
        JButton jbChooseFile = new JButton("Choose File");
        // Set the size which must be preferred size for within a container.
        jbChooseFile.setPreferredSize(new Dimension(150, 75));
        // Set the font for the button.
        jbChooseFile.setFont(new Font("Arial", Font.BOLD, 20));


        JButton jbGetFileNames = new JButton("Get File Names");
        jbGetFileNames.setPreferredSize(new Dimension(300, 75));
        jbGetFileNames.setFont(new Font("Arial", Font.BOLD, 20));



        // Add the buttons to the panel.
        jpButton.add(jbSendFile);
        jpButton.add(jbChooseFile);
        jpButton.add(jbGetFileNames);





        // Button action for choosing the file.
        // This is an inner class so we need the fileToSend to be final.
        jbChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create a file chooser to open the dialog to choose a file.
                JFileChooser jFileChooser = new JFileChooser();
                // Set the title of the dialog.
                jFileChooser.setDialogTitle("Choose a file to send.");
                // Show the dialog and if a file is chosen from the file chooser execute the following statements.
                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    // Get the selected file.
                    fileToSend[0] = jFileChooser.getSelectedFile();
                    // Change the text of the java swing label to have the file name.
                    jlFileName.setText("The file you want to send is: " + fileToSend[0].getName());
                }
            }
        });


        // Sends the file when the button is clicked.
        jbSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If a file has not yet been selected then display this message.
                if (fileToSend[0] == null) {
                    jlFileName.setText("Please choose a file to send first!");
                    // If a file has been selected then do the following.
                }
                else if(!WordCountServer.getFileExtension(fileToSend[0].getName()).equalsIgnoreCase("txt")){
                    jlFileName.setText("File must be a .txt file please try again!");
                }
                else {
                    try {

                        // Create an input stream into the file you want to send.
                        FileInputStream fileInputStream = new FileInputStream(fileToSend[0].getAbsolutePath());
                        // Create a socket connection to connect with the server.
                        Socket socket = new Socket("localhost", 1234);
                        // Create an output stream to write to write to the server over the socket connection.
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        // Get the name of the file you want to send and store it in filename.
                        String fileName = fileToSend[0].getName();
                        // Convert the name of the file into an array of bytes to be sent to the server.
                        byte[] fileNameBytes = fileName.getBytes();
                        // Create a byte array the size of the file so don't send too little or too much data to the server.
                        byte[] fileBytes = new byte[(int) fileToSend[0].length()];
                        // Put the contents of the file into the array of bytes to be sent so these bytes can be sent to the server.
                        fileInputStream.read(fileBytes);
                        //send the operation so the server knows what to do with the file
                        dataOutputStream.writeInt(0);
                        // Send the length of the name of the file so server knows when to stop reading.
                        dataOutputStream.writeInt(fileNameBytes.length);
                        // Send the file name.
                        dataOutputStream.write(fileNameBytes);
                        // Send the length of the byte array so the server knows when to stop reading.
                        dataOutputStream.writeInt(fileBytes.length);
                        // Send the actual file.
                        dataOutputStream.write(fileBytes);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });


        // request for all files stored on data tier
        jbGetFileNames.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a socket connection to connect with the server.
                    Socket socket = new Socket("localhost", 1234);
                    // Create an output stream to write to write to the server over the socket connection.
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    //send the operation so the server knows what to do with the file
                    dataOutputStream.writeInt(5);
                    dataOutputStream.writeInt(1);

                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());


                    int numberOfFiles = dataInputStream.readInt();

                    myFiles.removeAll(myFiles);
                    if(numberOfFiles > 0){

                        for(int i = 0; i < numberOfFiles; i++){
                            int id = dataInputStream.readInt();

                            int fileNameLength = dataInputStream.readInt();
                            byte[] fileNameBytes = new byte[fileNameLength];
                            dataInputStream.readFully(fileNameBytes, 0, fileNameLength);
                            String name = new String(fileNameBytes);

                            int fileContentsLength = dataInputStream.readInt();
                            byte [] fileContents = new byte[fileContentsLength];
                            dataInputStream.readFully(fileContents, 0, fileContentsLength);

                            int extensionLength = dataInputStream.readInt();
                            byte[] extensionBytes = new byte[fileNameLength];
                            dataInputStream.readFully(extensionBytes, 0, extensionLength);
                            String extension = new String(extensionBytes);

                            myFiles.add(new MyFile(id, name, fileContents, extension));

                        }
                        JFrame jfPreview = createFrame();
                        jfPreview.setVisible(true);
                        jlFileName.setText("Number of files = " + numberOfFiles);
                    }
                    else{
                        jlFileName.setText("There are no files in the server");
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        });

            // Add everything to the frame and make it visible.
            jFrame.add(jlTitle);
            jFrame.add(jlFileName);
            jFrame.add(jpButton);
            jFrame.setVisible(true);



    }

    public static MouseListener getMyMouseListener() {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get the source of the click which is the JPanel.
                JPanel jPanel = (JPanel) e.getSource();
                // Get the ID of the file.
                int fileId = Integer.parseInt(jPanel.getName());
                // Loop through the file storage and see which file is the selected one.
                for (int i = 0; i < myFiles.size(); i++) {
                    if (myFiles.get(i).getId() == fileId) {
                        JFrame jfPreview = createFrame(myFiles.get(i).getId(), myFiles.get(i).getName(), myFiles.get(i).getData(), myFiles.get(i).getFileExtension());
                        jfPreview.setVisible(true);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
    }
    public static JFrame createFrame() {


        // Set the size of the frame.
        jFrameFilesInServer.setSize(400, 400);

        // Panel to hold everything.
        JPanel jPanel = new JPanel();
        // Make the layout a box layout with child elements stacked on top of each other.
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        // Title above panel.
        JLabel jlTitle = new JLabel("File's in Server");
        // Center the label title horizontally.
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Change the font family, size, and style.
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        // Add spacing on the top and bottom of the element.
        jlTitle.setBorder(new EmptyBorder(20,0,10,0));
        // Label to prompt the user if they are sure they want to download the file.
        JLabel jlPrompt = new JLabel("Click file to choose next action");
        // Change the font style, size, and family of the label.
        jlPrompt.setFont(new Font("Arial", Font.BOLD, 20));
        // Add spacing on the top and bottom of the label.
        jlPrompt.setBorder(new EmptyBorder(20,0,10,0));
        // Center the label horizontally.
        jlPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Make it scrollable when the data gets in jpanel.
        JScrollPane jScrollPane = new JScrollPane(jPanel);
        // Make it so there is always a vertical scrollbar.
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        // Add everything to the panel before adding to the frame.
        jPanel.add(jlTitle);
        jPanel.add(jlPrompt);
        jFrameFilesInServer.add(jScrollPane);
        // Make the GUI show up.
        jFrameFilesInServer.setVisible(true);


        for(int i = 0; i < myFiles.size(); i++){

            JPanel jpFileRow = new JPanel();
            jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.Y_AXIS));
            jpFileRow.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel jlFileName =new JLabel(myFiles.get(i).getName());
            jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
            jlFileName.setBorder(new EmptyBorder(10, 0, 10, 0));

            jpFileRow.setName((String.valueOf(myFiles.get(i).getId())));
            jpFileRow.add(jlFileName);
            jPanel.add(jpFileRow);

            jpFileRow.addMouseListener(getMyMouseListener());

        }

        // Return the jFrame so it can be passed the right data and then shown.
        return jFrameFilesInServer;

    }

    public static JFrame createFrame(int id, String fileName, byte[] fileData, String fileExtension) {

        // Frame to hold everything.
        JFrame jFrame = new JFrame("WordCount Client");
        // Set the size of the frame.
        jFrame.setSize(700, 700);

        // Panel to hold everything.
        JPanel jPanel = new JPanel();
        // Make the layout a box layout with child elements stacked on top of each other.
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        // Title above panel.
        JLabel jlTitle = new JLabel(fileName + " selected.");
        // Center the label title horizontally.
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Change the font family, size, and style.
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        // Add spacing on the top and bottom of the element.
        jlTitle.setBorder(new EmptyBorder(20,0,10,0));

        // Label to prompt the user if they are sure they want to download the file.
        JLabel jlPrompt = new JLabel("Please choose an option below");
        // Change the font style, size, and family of the label.
        jlPrompt.setFont(new Font("Arial", Font.BOLD, 20));
        // Add spacing on the top and bottom of the label.
        jlPrompt.setBorder(new EmptyBorder(20,0,10,0));
        // Center the label horizontally.
        jlPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        // No button for rejecting the download.
        JButton jbCancel = new JButton("Cancel");
        // Change the size of the button must be preferred because if not the layout will ignore it.
        jbCancel.setPreferredSize(new Dimension(150, 75));
        // Set the font for the button.
        jbCancel.setFont(new Font("Arial", Font.BOLD, 20));
        jbCancel.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Label to hold the content of the file whether it be text of images.
        JLabel jlWordsInFile = new JLabel();
        jlWordsInFile.setFont(new Font("Arial", Font.PLAIN, 20));
        // Add spacing on the top and bottom of the label.
        jlWordsInFile.setBorder(new EmptyBorder(20,0,10,0));
        // Align the label horizontally.
        jlWordsInFile.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Label to hold the content of the file whether it be text of images.
        JLabel jlLinesInServer = new JLabel();
        jlLinesInServer.setFont(new Font("Arial", Font.PLAIN, 20));
        // Add spacing on the top and bottom of the label.
        jlLinesInServer.setBorder(new EmptyBorder(20,0,10,0));
        // Align the label horizontally.
        jlLinesInServer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Label to hold the content of the file whether it be text of images.
        JLabel jlCharactersInServer = new JLabel();
        jlCharactersInServer.setFont(new Font("Arial", Font.PLAIN, 20));
        // Add spacing on the top and bottom of the label.
        jlCharactersInServer.setBorder(new EmptyBorder(20,0,10,0));
        // Align the label horizontally.
        jlCharactersInServer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton jbUpdateFile = new JButton("Update File");
        jbUpdateFile.setPreferredSize(new Dimension(150, 75));
        jbUpdateFile.setFont(new Font("Arial", Font.BOLD, 20));
        jbUpdateFile.setAlignmentX(Component.CENTER_ALIGNMENT);



        // Panel to hold the yes and no buttons and make the next to each other left and right.
        JPanel jpButtons = new JPanel();
        // Add spacing around the panel.
        jpButtons.setBorder(new EmptyBorder(20, 0, 10, 0));
        //jpButtons.setLayout(new BoxLayout(jpButtons, BoxLayout.Y_AXIS));


        JButton jbCountWords = new JButton("Count Words in File");
        jbCountWords.setPreferredSize(new Dimension(300, 75));
        jbCountWords.setFont(new Font("Arial", Font.BOLD, 20));
        jbCountWords.setAlignmentX(Component.CENTER_ALIGNMENT);


        JButton jbCountLines = new JButton("Count Lines in File");
        jbCountLines.setPreferredSize(new Dimension(300, 75));
        jbCountLines.setFont(new Font("Arial", Font.BOLD, 20));
        jbCountLines.setAlignmentX(Component.CENTER_ALIGNMENT);


        JButton jbCountCharacters = new JButton("Count Characters in File");
        jbCountCharacters.setPreferredSize(new Dimension(300, 75));
        jbCountCharacters.setFont(new Font("Arial", Font.BOLD, 20));
        jbCountCharacters.setAlignmentX(Component.CENTER_ALIGNMENT);


        JButton jbReadFile = new JButton("Read Contents of File");
        jbReadFile.setPreferredSize(new Dimension(300, 75));
        jbReadFile.setFont(new Font("Arial", Font.BOLD, 20));
        jbReadFile.setAlignmentX(Component.CENTER_ALIGNMENT);


        JButton jbCountServerTotals = new JButton("Count Server Totals");
        jbCountServerTotals.setPreferredSize(new Dimension(300, 75));
        jbCountServerTotals.setFont(new Font("Arial", Font.BOLD, 20));
        jbCountServerTotals.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton jbRemoveFile = new JButton("Remove File");
        jbRemoveFile.setPreferredSize(new Dimension(150, 75));
        jbRemoveFile.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbStoreFile = new JButton("Store File");
        jbStoreFile.setPreferredSize(new Dimension(150, 75));
        jbStoreFile.setFont(new Font("Arial", Font.BOLD, 20));



        JTextArea jTextArea = new JTextArea();
        jTextArea.setBounds(200, 200, 200, 200);


        jbCountWords.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                    try {
                        jlLinesInServer.setText("");
                        jlCharactersInServer.setText("");

                        // Create a socket connection to connect with the server.
                        Socket socket = new Socket("localhost", 1234);
                        // Create an output stream to write to write to the server over the socket connection.
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        // Convert the name of the file into an array of bytes to be sent to the server.
                        byte[] fileNameBytes = fileName.getBytes();
                        //send the operation so the server knows what to do with the file
                        dataOutputStream.writeInt(2);
                        // Send the length of the name of the file so server knows when to stop reading.
                        dataOutputStream.writeInt(fileNameBytes.length);
                        // Send the file name.
                        dataOutputStream.write(fileNameBytes);
                        // Send the length of the byte array so the server knows when to stop reading.
                        dataOutputStream.writeInt(fileData.length);
                        // Send the actual file.
                        dataOutputStream.write(fileData);

                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        int wordsInFile = dataInputStream.readInt();

                        jlWordsInFile.setText("The number of words in " + fileName + " is " + wordsInFile);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }

        });



        jbCountLines.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    jlLinesInServer.setText("");
                    jlCharactersInServer.setText("");
                    // Create a socket connection to connect with the server.
                    Socket socket = new Socket("localhost", 1234);
                    // Create an output stream to write to write to the server over the socket connection.
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    // Convert the name of the file into an array of bytes to be sent to the server.
                    byte[] fileNameBytes = fileName.getBytes();

                    //send the operation so the server knows what to do with the file
                    dataOutputStream.writeInt(3);
                    // Send the length of the name of the file so server knows when to stop reading.
                    dataOutputStream.writeInt(fileNameBytes.length);
                    // Send the file name.
                    dataOutputStream.write(fileNameBytes);
                    // Send the length of the byte array so the server knows when to stop reading.
                    dataOutputStream.writeInt(fileData.length);
                    // Send the actual file.
                    dataOutputStream.write(fileData);

                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    int linesInFile = dataInputStream.readInt();

                    jlWordsInFile.setText("The number of lines in " + fileName + " is " + linesInFile);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }

        });

        jbCountCharacters.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    jlLinesInServer.setText("");
                    jlCharactersInServer.setText("");
                    // Create a socket connection to connect with the server.
                    Socket socket = new Socket("localhost", 1234);
                    // Create an output stream to write to write to the server over the socket connection.
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    // Convert the name of the file into an array of bytes to be sent to the server.
                    byte[] fileNameBytes = fileName.getBytes();

                    //send the operation so the server knows what to do with the file
                    dataOutputStream.writeInt(4);
                    // Send the length of the name of the file so server knows when to stop reading.
                    dataOutputStream.writeInt(fileNameBytes.length);
                    // Send the file name.
                    dataOutputStream.write(fileNameBytes);
                    // Send the length of the byte array so the server knows when to stop reading.
                    dataOutputStream.writeInt(fileData.length);
                    // Send the actual file.
                    dataOutputStream.write(fileData);

                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    int charactersInFile = dataInputStream.readInt();

                    jlWordsInFile.setText("The number of characters in " + fileName + " is " + charactersInFile);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }

        });

        // No so close window.
        jbReadFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jlLinesInServer.setText("");
                jlCharactersInServer.setText("");
                // Wrap it with <html> so that new lines are made.
                jlWordsInFile.setText("<html>" + new String(fileData) + "</html>");

            }
        });

        // No so close window.
        jbStoreFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File fileToStore = new File(fileName);
                try {
                    // Create a stream to write data to the file.
                    FileOutputStream fileOutputStream = new FileOutputStream(fileToStore);
                    // Write the actual file data to the file.
                    fileOutputStream.write(fileData);
                    // Close the stream.
                    fileOutputStream.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                jlFileName.setText("File Stored");
                jFrame.dispose();
                jFrameFilesInServer.dispose();
            }
        });

        // No so close window.
        jbRemoveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Create a socket connection to connect with the server.
                Socket socket = null;
                try {

                    socket = new Socket("localhost", 1234);
                    // Create an output stream to write to write to the server over the socket connection.
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    //send operation
                    dataOutputStream.writeInt(0);
                    dataOutputStream.writeInt(123);

                    //send file id to be removed
                    dataOutputStream.writeInt(id);

                    myFiles.remove(myFiles.get(id));
                    jlFileName.setText("File Removed");

                    jFrame.dispose();
                    jFrameFilesInServer.dispose();


                } catch (IOException ex) {
                    ex.printStackTrace();
                }


            }
        });

        // Update content of the file and send it to server
        jbUpdateFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFrame jfPreview = createFrame(id, fileName, fileData, jlWordsInFile);
                jfPreview.setVisible(true);
                jfPreview.validate();

            }
        });

        // No so close window.
        jbCountServerTotals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a socket connection to connect with the server.
                    Socket socket = new Socket("localhost", 1234);
                    // Create an output stream to write to write to the server over the socket connection.
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    //send the operation so the server knows what to do with the file
                    dataOutputStream.writeInt(6);
                    dataOutputStream.writeInt(1);

                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    int wordsInServer = dataInputStream.readInt();
                    int linesInServer = dataInputStream.readInt();
                    int charactersInServer = dataInputStream.readInt();

                    jlWordsInFile.setText("The total number of words in the server is " + wordsInServer);
                    jlLinesInServer.setText("The total number of lines in the server is " + linesInServer);
                    jlCharactersInServer.setText("The total number of characters in the server is " + charactersInServer);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }


            }
        });

        // No so close window.
        jbCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // User clicked no so don't download the file but close the jframe.
                jFrame.dispose();
            }
        });


        // Add the yes and no buttons.
        jpButtons.add(jbCountWords);
        jpButtons.add(jbCountLines);
        jpButtons.add(jbCountCharacters);
        jpButtons.add(jbReadFile);
        jpButtons.add(jbCountServerTotals);
        jpButtons.add(jbUpdateFile);
        jpButtons.add(jbRemoveFile);
        jpButtons.add(jbStoreFile);
        jpButtons.add(jbCancel);

        // Add everything to the panel before adding to the frame.
        jPanel.add(jlTitle);
        jPanel.add(jlPrompt);
        jPanel.add(jlWordsInFile);
        jPanel.add(jlLinesInServer);
        jPanel.add(jlCharactersInServer);
        jPanel.add(jpButtons);
        // Add panel to the frame.
        jFrame.add(jPanel);

        // Return the jFrame so it can be passed the right data and then shown.
        return jFrame;

    }
    public static JFrame createFrame(int id, String fileName, byte[] fileData, JLabel jLabel){
        // Frame to hold everything.
        JFrame jFrame = new JFrame("WordCount Client");
        // Set the size of the frame.
        jFrame.setSize(700, 500);

        // Panel to hold everything.
        JPanel jPanel = new JPanel();
        // Make the layout a box layout with child elements stacked on top of each other.
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        // Title above panel.
        JLabel jlTitle = new JLabel(fileName + " selected.");
        // Center the label title horizontally.
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Change the font family, size, and style.
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        // Add spacing on the top and bottom of the element.
        jlTitle.setBorder(new EmptyBorder(20,0,10,0));

        // Label to prompt the user if they are sure they want to download the file.
        JLabel jlPrompt = new JLabel("Edit the text below and click update");
        // Change the font style, size, and family of the label.
        jlPrompt.setFont(new Font("Arial", Font.BOLD, 20));
        // Add spacing on the top and bottom of the label.
        jlPrompt.setBorder(new EmptyBorder(20,0,10,0));
        // Center the label horizontally.
        jlPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea jTextArea = new JTextArea();
        jTextArea.setBounds(200, 200, 200, 200);
        jTextArea.setText(new String(fileData));

        JPanel jpButtons = new JPanel();

        JButton jbUpdate = new JButton("Update");
        jbUpdate.setPreferredSize(new Dimension(300, 75));
        jbUpdate.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbCancel = new JButton("Cancel");
        jbCancel.setPreferredSize(new Dimension(300, 75));
        jbCancel.setFont(new Font("Arial", Font.BOLD, 20));



        // No so close window.
        jbUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                String updatedFileContents = jTextArea.getText();
                jLabel.setText(updatedFileContents);
                for(int i = 0; i < myFiles.size(); i++){
                    if(myFiles.get(i).getId() == id){
                        myFiles.get(i).setData(updatedFileContents.getBytes());

                        // Create a socket connection to connect with the server.
                        Socket socket = null;
                        try {
                            socket = new Socket("localhost", 1234);

                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                            dataOutputStream.writeInt(7);
                            dataOutputStream.writeInt(1);

                            dataOutputStream.writeInt(id);
                            dataOutputStream.writeInt(updatedFileContents.getBytes().length);
                            dataOutputStream.write(updatedFileContents.getBytes());


                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }


                    }
                }
                jlPrompt.setText("File Updated");
                // User clicked no so don't download the file but close the jframe.
                jFrame.dispose();
            }
        });


        // No so close window.
        jbCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // User clicked no so don't download the file but close the jframe.
                jFrame.dispose();
            }
        });

        jPanel.add(jlTitle);
        jPanel.add(jlPrompt);
        jPanel.add(jTextArea);
        jpButtons.add(jbUpdate);
        jpButtons.add(jbCancel);
        jPanel.add(jpButtons);
        jFrame.add(jPanel);


        return jFrame;
    }
}
