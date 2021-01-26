package it.uninsubria.pdm.audiotodolist;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.format.DateUtils;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.time.Duration;

public class MediaPlayerManager {
    private static MediaPlayerManager instance;
    private MaterialButton playButton, stopButton;
    private Slider slider;
    private TextView progress;
    private Context context;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private final Runnable updateSliderJob = () -> updateSlider();
    private Uri currentUri;
    private boolean isFinished = false;

    private MediaPlayerManager(Context context, MaterialButton playButton, MaterialButton stopButton, Slider slider, TextView progress) {
        this.playButton = playButton;
        this.stopButton = stopButton;
        this.slider = slider;
        this.context = context;
        this.progress = progress;
    }

    private MediaPlayerManager(Context context) {
        this.context = context;
    }

    public static void attach(Context context, MaterialButton play, MaterialButton stop, Slider slider, TextView progress, Uri fileUri) {
        if (instance == null) {
            instance = new MediaPlayerManager(context, play, stop, slider, progress);
            initPlayer();
        } else {
            if (instance.mediaPlayer != null) {
                if (instance.mediaPlayer.isPlaying()) {
                    stop();
                }
                reset();
            }
            initPlayer();
            instance.playButton = play;
            instance.stopButton = stop;
            instance.slider = slider;
            instance.context = context;
            instance.progress = progress;
        }
        instance.playButton.setOnClickListener(v -> {
            if (instance.playButton.isChecked()) {
                play();
            } else {
                pause();
            }
        });
        instance.stopButton.setOnClickListener(v -> {
            stop();
            reset();
        });
        try {
            setFile(fileUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void attachAndPlay(Context context, MaterialButton play, MaterialButton stop, Slider slider, TextView progress, Uri fileUri) {
        if (instance == null) {
            instance = new MediaPlayerManager(context, play, stop, slider, progress);
            initPlayer();
        } else {
            if (instance.mediaPlayer != null) {
                if (instance.mediaPlayer.isPlaying()) {
                    stop();
                }
                reset();
            }
            initPlayer();
            instance.playButton = play;
            instance.stopButton = stop;
            instance.slider = slider;
            instance.context = context;
            instance.progress = progress;
        }
        instance.playButton.setOnClickListener(v -> {
            if (instance.playButton.isChecked()) {
                play();
            } else {
                pause();
            }
        });
        instance.stopButton.setOnClickListener(v -> {
            stop();
            reset();
        });
        try {
            setFileAndPlay(fileUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setFile(Uri uri) throws IOException {
        instance.currentUri = uri;
        if (instance.mediaPlayer == null) {
            return;
        }
        instance.mediaPlayer.setDataSource(instance.context, instance.currentUri);
        instance.mediaPlayer.setOnCompletionListener(mp -> instance.isFinished = true);
        instance.mediaPlayer.setOnPreparedListener(mp -> {
            instance.slider.setValueTo(mp.getDuration());
            instance.slider.addOnChangeListener((slider, value, fromUser) -> {
                if (fromUser) {
                    instance.mediaPlayer.seekTo(Math.round(value));
                }
            });
        });
        instance.mediaPlayer.prepareAsync();
    }

    private static void setFileAndPlay(Uri uri) throws IOException {
        instance.currentUri = uri;
        instance.mediaPlayer.setDataSource(instance.context, instance.currentUri);
        instance.mediaPlayer.setOnCompletionListener(mp -> instance.isFinished = true);
        instance.mediaPlayer.setOnPreparedListener(mp -> {
            instance.slider.setValueTo(mp.getDuration());
            instance.slider.addOnChangeListener((slider, value, fromUser) -> {
                if (fromUser) {
                    instance.mediaPlayer.seekTo(Math.round(value));
                }
            });
            instance.mediaPlayer.start();
            instance.handler.postDelayed(instance.updateSliderJob, 0);
        });
        instance.mediaPlayer.prepareAsync();
    }

    private static void initPlayer() {
        instance.isFinished = false;
        instance.handler = new Handler();
        instance.mediaPlayer = new MediaPlayer();
        instance.mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
    }

    private static void updateSlider() {
        if (instance.mediaPlayer != null) {
            int pos = instance.mediaPlayer.getCurrentPosition();
            if (pos >= 0 & pos <= instance.slider.getValueTo()) {
                instance.slider.setValue(pos);
                Duration current = Duration.ofMillis(pos);
                instance.progress.setText(DateUtils.formatElapsedTime(current.getSeconds()));
                if (!instance.isFinished) {
                    instance.handler.postDelayed(instance.updateSliderJob, 1000);
                } else {
                    reset();
                }
            }
        }
    }

    private static void play() {
        if (instance.mediaPlayer == null) {
            initPlayer();
            try {
                setFileAndPlay(instance.currentUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            instance.mediaPlayer.start();
            instance.handler.postDelayed(instance.updateSliderJob, 0);
        }
    }

    private static void pause() {
        if (instance.mediaPlayer != null) {
            instance.mediaPlayer.pause();
        }
    }

    private static void stop() {
        if (instance.mediaPlayer != null) {
            instance.mediaPlayer.stop();
        }
    }

    private static void reset() {
        if (instance.mediaPlayer != null) {
            instance.mediaPlayer.reset();
            instance.mediaPlayer.release();
            instance.mediaPlayer = null;
        }
        if (instance.playButton != null) {
            instance.playButton.setChecked(false);
        }
        if (instance.slider != null) {
            instance.slider.setValue(0);
        }
        Duration current = Duration.ZERO;
        if (instance.progress != null) {
            instance.progress.setText(DateUtils.formatElapsedTime(current.getSeconds()));
        }
    }

}
