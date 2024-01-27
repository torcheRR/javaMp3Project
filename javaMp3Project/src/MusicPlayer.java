import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.ArrayList;

public class MusicPlayer extends PlaybackListener {

    //this will be used to update isPaused more sync
    private static final Object playSignal = new Object();
    //need reference so that we can update the gui in this class
    private MusicPlayerGUI musicPlayerGUI;

    //we'll need a way to store our song's details, so we'll be creating a song class
    private Song currentSong;
    public Song getCurrentSong(){
        return currentSong;
    }

    private ArrayList<Song> playlist;

    //we will need to keep track the index we are in the playlist
    private int currentPlaylistIndex;

    //boolean flag used to tell when the song has finished
    private boolean songFinished;

    private boolean pressedNext, pressedPrev;

    //use JLayer library to create an AdvancedPlayer obj which will handle playing the music
    private AdvancedPlayer advancedPlayer;

    //pause boolean flag used to indicate whether the player has been paused
    private boolean isPaused;

    // stores in the last frame when the playback is finished  (used for pausing or finishing method)
    private int currentFrame;
    public void setCurrentFrame(int frame){
        currentFrame=frame;
    }

    //track how many milliseconds has passed since playing the song (used for updating slider)
    private int currentTimeInMilli;

    public void setCurrentTimeInMilli(int currentTimeInMilli) {
        this.currentTimeInMilli = currentTimeInMilli;
    }

    //constructor
    public MusicPlayer(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
    }

    public void loadPlaylist(File playlistFile){
        playlist = new ArrayList<>();

        //store the paths from text file into the playlist array list
        try {
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            //reach each line from the text file and store the next into the songPath variable
            String songPath;
            while ((songPath = bufferedReader.readLine()) != null){
                //create song obg based on song path
                Song song = new Song(songPath);

                //add to playlist array list
                playlist.add(song);
            }
        }
        catch (Exception exception){
            exception.printStackTrace();
        }
        if (playlist.size()>0){
            //reset playback slider
            musicPlayerGUI.setPlaybackSliderValue(0);
            currentTimeInMilli=0;

            //update current song to the first song in the playlist
            currentSong= playlist.get(0);

            //start from the beginning frame
            currentFrame=0;

            //update gui
            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.updateSongTitleAndArtist(currentSong);
            musicPlayerGUI.updatePlaybackSlider(currentSong);

            //start song
            playCurrentSong();
        }
    }

    public void loadSong(Song song) {
        currentSong = song;
        playlist=null;

        //stop the current song if possible
        if (!songFinished){
            stopSong();
        }


        //play the current song if not null
        if (currentSong != null) {
            currentFrame = 0;
             currentTimeInMilli=0;
             musicPlayerGUI.enablePauseButtonDisablePlayButton();
             musicPlayerGUI.updatePlaybackSlider(currentSong);
             musicPlayerGUI.setPlaybackSliderValue(currentFrame);
            playCurrentSong();
        }
    }

    public void pauseSong() {
        if (advancedPlayer != null) {
            //update isPaused flag
            isPaused = true;

            //then we want to stop the player
            stopSong();
        }
    }

    public void stopSong() {
        if (advancedPlayer != null) {
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void nextSong(){
        //no need to go to the next song there is no playlist
        if (playlist==null)return;

        //check to see if we have reached the end of the playlist, if so then don't do anything
        if (currentPlaylistIndex+1 > playlist.size()-1)return;

        pressedNext=true;

        //stop song if possible
        if (!songFinished){
            stopSong();
        }


        //increase current playlist index
        currentPlaylistIndex++;

        //update current song
        currentSong = playlist.get(currentPlaylistIndex);

        //reset frame
        currentFrame=0;

        //reset current time in milli
        currentTimeInMilli=0;

        //update gui
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        //play the song
        playCurrentSong();
    }

    public void previousSong(){
        //no need to go to the next song there is no playlist
        if (playlist==null)return;

        //check to see if we can go to the previous song
        if (currentPlaylistIndex-1<0)return;

        pressedPrev=true;

        //stop song if possible
        if (!songFinished){
            stopSong();
        }

        //decrease the playlist index
        currentPlaylistIndex--;

        //update the current index
        currentSong=playlist.get(currentPlaylistIndex);

        //update frame
        currentFrame=0;

        //update Current time in milli
        currentTimeInMilli =0;

        //update gui
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.enablePauseButtonDisablePlayButton();

        //play song
        playCurrentSong();
    }

    public void playCurrentSong() {
        if (currentSong == null) return;

        try {
            //read audio mp3 data
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            //create a new advanced player
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            if (!songFinished){
                //start music
                startMusicThread();

                //start playback slider thread
                startPlaybackSliderThread();
            }else{
                currentFrame=0;
                currentTimeInMilli=0;
                musicPlayerGUI.setPlaybackSliderValue(currentFrame);
                musicPlayerGUI.enablePauseButtonDisablePlayButton();

                //start music
                startMusicThread();

                //start playback slider thread
                startPlaybackSliderThread();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //create a thread that will handle playing the music
    private void startMusicThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isPaused) {
                        synchronized (playSignal){
                            //update flag
                            isPaused = false;

                            //notify the other thread to continue (makes sure that isPaused is updated to false properly)
                            playSignal.notify();
                        }
                        //resume music
                        advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                    } else {
                        //play music from the beginning
                        advancedPlayer.play();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //create a thread that will handle updating  the slider
    private void startPlaybackSliderThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isPaused){
                    try {
                        //wai till it gets notified by other thread to continue
                        //makes sure that isPaused boolean flag updates to false before continuing
                        synchronized (playSignal){
                            playSignal.wait();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                while (!isPaused && !songFinished && !pressedNext && !pressedPrev) {
                    try {
                        //increment current time milli
                        currentTimeInMilli++;

                        //calculate into from value
                        int calculatedFrame = (int) ((double) currentTimeInMilli *1.3* currentSong.getFrameRatePerMilliseconds());

                        //update gui
                        musicPlayerGUI.setPlaybackSliderValue(calculatedFrame);

                        //mimic 1 millisecond using thread.sleep
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        //this method gets called at beginning of the song
        songFinished=false;
        pressedPrev=false;
        pressedNext=false;
        System.out.println("Playback Started");
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        //this method gets called when the song finished or if the player gets closed
        System.out.println("Playback Finished");

        if (isPaused) {
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        }
        else {
            //if the user pressed next or prev button we don't need to execute the rest of the code
            if (pressedNext || pressedPrev)return;

            //when the song ends
            songFinished=true;

            if (playlist==null){
                //update gui
                musicPlayerGUI.enablePlayButtonDisablePauseButton();
            }else{
                //last song in the playlist
                if (currentPlaylistIndex == playlist.size()-1){
                    //update gui
                    musicPlayerGUI.enablePlayButtonDisablePauseButton();
                }else {
                    //go to the next song in the playlist
                    nextSong();
                }
            }
        }

    }

}

