package basso;

import android.graphics.Bitmap;

interface IBassoService
{
    void openFile(String path);
    void open(in long [] list, int position);
    void stop();
    void pause();
    void play();
    void prev();
    void next();
    void enqueue(in long [] list, int action);
    void setQueuePosition(int index);
    void setShuffleMode(int shufflemode);
    void setRepeatMode(int repeatmode);
    void moveQueueItem(int from, int to);
    void toggleFavorite();
    void refresh();
    boolean isFavorite();
    boolean isPlaying();
    long [] getQueue();
    long duration();
    long position();
    long seek(long pos);
    long getAudioId();
    long getArtistId();
    long getAlbumId();
    String getArtistName();
    String getTrackName();
    String getAlbumName();
    String getPath();
    int getQueuePosition();
    int getShuffleMode();
    int removeTracks(int first, int last);
    int removeTrack(long id); 
    int getRepeatMode();
    int getMediaMountedCount();
    int getAudioSessionId();

    int getNumberOfBands();
    int getBandLevelRange(int level);
    int getNumberOfPresets();
    void enableEqualizer();
    void disableEqualizer();
    String getPresetName(int i);
    int getBandLevel(int band);
    void setBandLeve(int band, int level);
    int getCurrentPreset();
    void usePreset(int preset);
    int getCenterFreq(int band);
    void deletePreset(int position);
    void updatePref();
    void savePreset(String result);

    void setBalance(float balance);
    void setLeftChannelVolume(float vol);
    void setRightChannelVolume(float vol);
    float getLeftVolumet();
    float getRightVoluemt();

    boolean isEqualizerEnabled();
    void setEqualizerEnabled(boolean enabled);
}

