import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

public class MusicPlayerGUI extends JFrame {

    //color configurations
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;

    //allows us to file explorer/ifinder in our app
    private JFileChooser jFileChooser;

    private JLabel songTitle, songArtist;
    private JPanel playbackBtns;
    private JSlider playbackSlider;

    public MusicPlayerGUI(){

        //cals JFrame constructor to configure out gui and set the title header to "Music Player"
        super ("Music Player");

        //set width and height
        setSize(400,600);

        //end process when app is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //launch the app center of the screen
        setLocationRelativeTo(null);

        //prevent the app from being resized
        setResizable(false);

        //set layout to null which allows us to control the (x,y) coordinates of our components
        //and also set the height and weight
        setLayout(null);

        //change the frame color
        getContentPane().setBackground(FRAME_COLOR);

        jFileChooser = new JFileChooser();
        musicPlayer = new MusicPlayer(this);

        //set a default path for file explorer / ifinder
        jFileChooser.setCurrentDirectory(new File("src/assets"));

        //filter file chooser to only see .mp3 files
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3","mp3"));


        addGuiComponents();
        }

        private void addGuiComponents(){

        //add toolbar
            addToolBar();

        //add record image
            JLabel songImage = new JLabel(loadImage("src/assets/record.png"));
            songImage.setBounds(5,50,getWidth()-12,225);
            add(songImage);
            songImage.setBackground(null);

        //add Song title
            songTitle = new JLabel("Song Title");
            songTitle.setBounds(0,285,getWidth()-10,30);
            songTitle.setFont(new Font("Dialog", Font.BOLD,24));
            songTitle.setForeground(TEXT_COLOR);
            songTitle.setHorizontalAlignment(SwingConstants.CENTER);
            add(songTitle);

        //song artist
            songArtist = new JLabel("Artist");
            songArtist.setBounds(0,315,getWidth()-10,30);
            songArtist.setFont(new Font("Dialog", Font.PLAIN,20));
            songArtist.setForeground(TEXT_COLOR);
            songArtist.setHorizontalAlignment(SwingConstants.CENTER);
            add(songArtist);

        //add slider
            playbackSlider = new JSlider(JSlider.HORIZONTAL, 0 ,100, 0);
            playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300,40);
            playbackSlider.setBackground(null);
            playbackSlider.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    //when the user is holding the tick we want to pause the song
                    musicPlayer.pauseSong();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    //when the user drops the tick
                    JSlider source = (JSlider) e.getSource();

                    //get the frame value from where the user wants to playback to
                    int frame = source.getValue();

                    //update the current frame in the music player to this frame
                    musicPlayer.setCurrentFrame(frame);

                    //update current time milli as well
                    musicPlayer.setCurrentTimeInMilli((int) (frame / (1.3 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));

                    //resume the song
                    musicPlayer.playCurrentSong();

                    //toggle on pause button and toggle off play button
                    enablePauseButtonDisablePlayButton();
                }
            });
            add(playbackSlider);

        //playback buttons (previous, next , pause)
            addPlaybackBtns();

    }
    //this will be used to update our slider from the player music class
    public void setPlaybackSliderValue(int frame){
        playbackSlider.setValue(frame);
    }

    public void updatePlaybackSlider(Song song){
        //update max count for slider
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        //create the song lenght label
        Hashtable<Integer,JLabel> labelTable = new Hashtable<>();

        //beginning will be 00:00
        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog",Font.BOLD,18));
        labelBeginning.setForeground(TEXT_COLOR);

        //end will vary depending on the song
        JLabel labelEnd = new JLabel(song.getSongLenght());
        labelEnd.setFont(new Font("Dialog",Font.BOLD,18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0,labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(),labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);


    }

    public void addToolBar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0,0,getWidth(),20);

        //prevent toolbar from being moved
        toolBar.setFloatable(false);

        //add drop down menu
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        //now we will add a song menu where we will place the loading song option
        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        //add the "load song" item in the songMenu
        JMenuItem loadSong = new JMenuItem("Load Song");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //on intgere is returned to us to let us know what the user did
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if (result==JFileChooser.APPROVE_OPTION && selectedFile!= null){
                    //create a song obj based on selected file
                    Song song = new Song(selectedFile.getPath());

                    //load song in musicplayer
                    musicPlayer.loadSong(song);

                    //update song title and artist
                    updateSongTitleAndArtist(song);

                    //update playbackslider
                    updatePlaybackSlider(song);

                    //toggle on pause button and toggle off play button
                    enablePauseButtonDisablePlayButton();
                }
            }
        });
        songMenu.add(loadSong);



        //now we will add "Playlist" menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        //then add the items to the playlist menu
        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //laod music playlist dialog
                new MusicPlayListDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist",".txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if (result == JFileChooser.APPROVE_OPTION && selectedFile!=null){
                    //stop the music
                    musicPlayer.stopSong();

                    //load playlist
                    musicPlayer.loadPlaylist(selectedFile);
                }
            }
        });
        playlistMenu.add(loadPlaylist);

        toolBar.setBackground(Color.GRAY);

        add(toolBar);

    }

    public void updateSongTitleAndArtist(Song song){
        songTitle.setText(song.getSongTitle());
        songArtist.setText((song.getSongArtist()));
    }

    public ImageIcon loadImage(String imagePath) {
        try {
            //read the image file from the given path
            BufferedImage image = ImageIO.read(new File(imagePath));

            //returns an img icon so that our component can render the image
            return new ImageIcon(image);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //could not find resource
        return null;
    }

    public void addPlaybackBtns(){
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0,435,getWidth()-10 ,80);
        playbackBtns.setBackground(null);


        //previous button
        JButton prevBtn = new JButton(loadImage("src/assets/previous.png"));
        prevBtn.setBorderPainted(false);
        prevBtn.setBackground(null);
        prevBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //go to prev song
                musicPlayer.previousSong();
            }
        });
        playbackBtns.add(prevBtn);

        //play button
        JButton playBtn = new JButton(loadImage("src/assets/play.png"));
        playBtn.setBorderPainted(false);
        playBtn.setBackground(null);
        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //toggle off play button and toggle on pause button
                enablePauseButtonDisablePlayButton();

                //play or resume the song
                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playBtn);

        //pause button
        JButton pauseBtn = new JButton(loadImage("src/assets/pause.png"));
        pauseBtn.setBorderPainted(false);
        pauseBtn.setBackground(null);
        pauseBtn.setVisible(false);
        pauseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //toggle of pause button and toggle on play button
                enablePlayButtonDisablePauseButton();

                //pause the song
                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseBtn);

        //next button
        JButton nextBtn = new JButton(loadImage("src/assets/next.png"));
        nextBtn.setBackground(null);
        nextBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //go to the next song
                musicPlayer.nextSong();
            }
        });
        nextBtn.setBorderPainted(false);
        playbackBtns.add(nextBtn);

        add(playbackBtns);
    }

    public void enablePauseButtonDisablePlayButton(){
        //retrieve reference to play button from playbackBtns Panel
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        //turn off play button
        playButton.setVisible(false);
        playButton.setEnabled(false);

        //turn on pause button
        pauseButton.setEnabled(true);
        pauseButton.setVisible(true);
    }

    public void enablePlayButtonDisablePauseButton(){
        //retrieve reference to play button from playbackBtns Panel
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        //turn off pause button
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);

        //turn on play button
        playButton.setEnabled(true);
        playButton.setVisible(true);
    }
}

