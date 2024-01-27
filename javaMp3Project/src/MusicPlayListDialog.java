import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MusicPlayListDialog extends JDialog {
    private MusicPlayerGUI musicPlayerGUI;

    //store all of the paths to be written to a txt file (when we load playlist)
    private ArrayList<String> songPaths;

    public MusicPlayListDialog(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
        songPaths = new ArrayList<>();

        //configure dialog
        setTitle("Create PlayList");
        setSize(400, 400);
        setResizable(false);
        getContentPane().setBackground(MusicPlayerGUI.FRAME_COLOR);
        setLayout(null);
        setModal(true); // this property makes it so that the dialog has to be closed to give focus
        setLocationRelativeTo(musicPlayerGUI);

        addDialogComponent();
    }


    private void addDialogComponent() {
        //container to hold each song path
        JPanel songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int) (getWidth() * 0.025), 10, (int) (getWidth() * 0.95), (int) (getHeight() * 0.75));
        add(songContainer);

        //add song button
        JButton addSongBtn = new JButton("Add");
        addSongBtn.setBounds(60, (int) (getHeight() * 0.80), 100, 25);
        addSongBtn.setFont(new Font("Dialog", Font.BOLD, 14));
        addSongBtn.addActionListener(new ActionListener(
        ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                //open file explorer / ifinder
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));
                int result = jFileChooser.showOpenDialog(MusicPlayListDialog.this);

                File selectedFile = jFileChooser.getSelectedFile();
                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    JLabel filePathLabel = new JLabel(selectedFile.getPath());
                    filePathLabel.setFont(new Font("Dailog", Font.BOLD, 12));
                    filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    //add to the list
                    songPaths.add(filePathLabel.getText());

                    //add container
                    songContainer.add(filePathLabel);

                    //refreshes dialog to show newly added JLabel
                    songContainer.revalidate();
                }
            }
        });
        add(addSongBtn);

        //save playlist button
        JButton savePlaylistButton = new JButton("Save");
        savePlaylistButton.setBounds(215, (int) (getHeight() * 0.80), 100, 25);
        savePlaylistButton.setFont(new Font("Dialog", Font.BOLD, 14));
        savePlaylistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    JFileChooser jFileChooser = new JFileChooser();
                    jFileChooser.setCurrentDirectory(new File("src/assets"));

                    int result = jFileChooser.showSaveDialog(MusicPlayListDialog.this);

                    if (result == JFileChooser.APPROVE_OPTION) {

                        //we use getSelectedFile() to get reference to the file that we are about to save
                        File selectedFile = jFileChooser.getSelectedFile();

                        //convert to .txt file if not done so already
                        //this will check to see if the file does not have the ".txt file extension
                        if (!selectedFile.getName().substring(selectedFile.getName().length() - 4).equalsIgnoreCase(".txt")) {
                            selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                        }

                        selectedFile.createNewFile();

                        //now we will write all of the song paths into this file
                        FileWriter fileWriter = new FileWriter(selectedFile);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                        //itarate through our song paths list and write each string into the file
                        //each song will be written in their own row
                        for (String songPath : songPaths) {
                            bufferedWriter.write(songPath + "\n");
                        }
                        bufferedWriter.close();

                        //display sucsess dialog
                        JOptionPane.showMessageDialog(MusicPlayListDialog.this, "Playlist Successfully Created");

                        //close this dialog
                        MusicPlayListDialog.this.dispose();
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        add(savePlaylistButton);
    }
}
