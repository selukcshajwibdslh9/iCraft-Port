package iCraft.client.mp3;

import iCraft.core.ICraft;
import javazoom.jl.decoder.*;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MP3Player {
    private final static int NOTSTARTED = 0;
    private final static int PLAYING = 1;
    private final static int PAUSED = 2;

    private volatile int playerStatus = NOTSTARTED;

    private URL music;
    private int repeatType = 0;

    private AdvancedPlayer player;
    private Thread playThread;
    private InputStream currentStream;

    private volatile int pausedFrame = 0;
    private volatile int currentFrame = 0;
    private volatile int totalFrames = 0;
    private volatile boolean active = false;

    public MP3Player() {
    }

    public void setMusic(URL url) {
        music = url;
    }

    public void setRepeatType(int type) {
        repeatType = type;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public int getPlayerStatus() {
        return playerStatus;
    }

    public boolean hasPlayer() {
        return active;
    }

    public void resetPlayerStatus() {
        playerStatus = NOTSTARTED;
        pausedFrame = 0;
        currentFrame = 0;
        active = false;
        totalFrames = 0;
    }

    public void play() throws IOException {
        if (playerStatus == NOTSTARTED) {
            final URL urlToCount = music;
            new Thread(() -> {
                totalFrames = countFrames(urlToCount);
            }, "MP3FrameCounter").start();

            startPlayback(0);
        } else if (playerStatus == PAUSED) {
            resume();
        }
    }

    private void startPlayback(final int fromFrame) throws IOException {
        currentStream = new BufferedInputStream(music.openStream());

        try {
            player = new AdvancedPlayer(currentStream);
        } catch (JavaLayerException e) {
            throw new RuntimeException(e);
        }

        active = true;
        playerStatus = PLAYING;

        player.setPlayBackListener(new PlaybackListener() {
            @Override
            public void playbackFinished(PlaybackEvent evt) {
                if (playerStatus == PLAYING) {
                    try {
                        onEndOfMedia();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        playThread = new Thread(() -> {
            try {
                playWithFrameCount(fromFrame);
            } catch (Exception ignored) {
            }
        }, "MP3PlayerThread");
        playThread.setDaemon(true);
        playThread.start();
    }

    private void playWithFrameCount(int fromFrame) throws JavaLayerException, IOException {
        Bitstream bitstream = new Bitstream(currentStream);
        Decoder decoder = new Decoder();
        AudioDevice audio;
        try {
            audio = FactoryRegistry.systemRegistry().createAudioDevice();
            audio.open(decoder);
        } catch (JavaLayerException e) {
            bitstream.close();
            return;
        }

        for (int i = 0; i < fromFrame; i++) {
            Header h = bitstream.readFrame();
            if (h == null) {
                audio.close();
                bitstream.close();
                return;
            }
            bitstream.closeFrame();
        }

        currentFrame = fromFrame;

        while (playerStatus == PLAYING) {
            Header h = bitstream.readFrame();
            if (h == null) break;

            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);
            audio.write(output.getBuffer(), 0, output.getBufferLength());
            bitstream.closeFrame();
            currentFrame++;
        }

        audio.flush();
        audio.close();
        try {
            bitstream.close();
        } catch (Exception ignored) {
        }

        if (playerStatus == PLAYING) {
            if (totalFrames == 0) totalFrames = currentFrame;
            try {
                onEndOfMedia();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean pause() {
        if (playerStatus == PLAYING) {
            pausedFrame = currentFrame;
            playerStatus = PAUSED;
            stopInternal();
        }
        return playerStatus == PAUSED;
    }

    public boolean resume() {
        if (playerStatus == PAUSED) {
            try {
                playerStatus = PLAYING;
                startPlayback(pausedFrame);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return playerStatus == PLAYING;
    }

    public void close() {
        playerStatus = CLOSED_MARKER;
        stopInternal();
        active = false;
        playerStatus = NOTSTARTED;
        pausedFrame = 0;
        currentFrame = 0;
        totalFrames = 0;
    }

    private static final int CLOSED_MARKER = 99;

    private void stopInternal() {
        if (player != null) {
            player.stop();
            player = null;
        }
        closeStream();
    }

    private void closeStream() {
        if (currentStream != null) {
            try {
                currentStream.close();
            } catch (IOException ignored) {
            }
            currentStream = null;
        }
    }

    private void onEndOfMedia() throws IOException {
        switch (repeatType) {
            case 0:
            case 1:
                playNextMusic();
                break;
            case 2:
                active = false;
                playerStatus = NOTSTARTED;
                pausedFrame = 0;
                currentFrame = 0;
                play();
                break;
        }
    }

    private void playNextMusic() throws IOException {
        int next = ICraft.currentMusicId + 1;
        boolean atEnd = next > ICraft.musics.size() - 1;

        if (atEnd && repeatType == 0) {
            active = false;
            playerStatus = NOTSTARTED;
            currentFrame = 0;
            return;
        }

        int nextId = atEnd ? 0 : next;
        ICraft.currentMusicId = nextId;

        stopInternal();
        active = false;
        playerStatus = NOTSTARTED;
        pausedFrame = 0;
        currentFrame = 0;
        totalFrames = 0;

        setMusic(ICraft.musics.get(nextId).toURI().toURL());
        play();
    }

    private static int countFrames(URL url) {
        try (InputStream is = new BufferedInputStream(url.openStream())) {
            Bitstream bitstream = new Bitstream(is);
            int count = 0;
            try {
                while (true) {
                    Header h = bitstream.readFrame();
                    if (h == null) break;
                    count++;
                    bitstream.closeFrame();
                }
            } catch (Exception ignored) {
            }
            return count;
        } catch (IOException e) {
            return 0;
        }
    }

    public String getPosition() {
        return formatMs(framesToMs(currentFrame));
    }

    public String getMinDuration() {
        return formatMs(framesToMs(totalFrames));
    }

    public int getMusicStatus(int max) {
        if (totalFrames == 0) return 0;
        return (int) ((long) currentFrame * max / totalFrames);
    }

    public void setVolume(float volume) {
    }

    private static long framesToMs(int frames) {
        return (long) (frames * 1152.0 / 44100.0 * 1000.0);
    }

    private static String formatMs(long ms) {
        return new SimpleDateFormat("mm:ss").format(new Date(ms));
    }
}
