package com.basso.basso.ui.activities;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.basso.basso.R;
import com.basso.basso.utils.BassoUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jaudiotagger.tag.reference.PictureTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class Tagger extends Activity {
    private ImageView imageView;
    private final int SELECT_PHOTO = 1;
    private EditText titleEditText;
    private EditText albumEditText;
    private EditText artistEditText;
    private EditText genreEditText;
    private EditText yearEditText;
    private EditText composerEditText;
    private EditText bpmEditText;
    private EditText commentEditText;
    private EditText languageEditText;
    private LinearLayout mp3TagsContainer;
    private EditText trackLengthEditText;
    private EditText bitrateEditText;
    private EditText channelsEditText;
    private EditText sampleRateEditText;
    private EditText mpegVersionEditText;
    private EditText formatEditText;
    private EditText encoderEditText;
    private EditText emphasisEditText;
    private boolean D = false;
    private String TAG = "Tagger";
    private Button saveButton;
    private Bitmap selectedImage;
    private Uri imageUri;
    private AudioFile f;
    private Tag tag;
    private FlacTag flacTag;
    private String path;
    private boolean photoChanged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_background)));
        }
        setContentView(R.layout.activity_tagger);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            ScrollView scrollView = (ScrollView)findViewById(R.id.scroll_view_tagger);
            scrollView.setPadding(0,getStatusBarHeight(),0,0);
        } else {
            RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.tagger_relative);
            relativeLayout.setPadding(0,0,0,0);
        }
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        View customNav = LayoutInflater.from(this).inflate(R.layout.action_bar_tagger, null);
        saveButton = (Button)customNav.findViewById(R.id.save_button);
        ((TextView)customNav.findViewById(R.id.action_bar_title_tagger)).setText(getString(R.string.edit_tags));
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_background)));
        getActionBar().setCustomView(customNav, lp);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        imageView = (ImageView)findViewById(R.id.cover);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
        titleEditText = (EditText)findViewById(R.id.title_edittext);
        albumEditText = (EditText)findViewById(R.id.album_edittext);
        artistEditText = (EditText)findViewById(R.id.artist_edittext);
        genreEditText = (EditText)findViewById(R.id.genre_edittext);
        yearEditText = (EditText)findViewById(R.id.year_edittext);
        composerEditText = (EditText)findViewById(R.id.composer_edittext);
        bpmEditText = (EditText)findViewById(R.id.bpm_edittext);
        commentEditText = (EditText)findViewById(R.id.comment_edittext);
        languageEditText = (EditText)findViewById(R.id.language_edittext);
        mp3TagsContainer = (LinearLayout)findViewById(R.id.mp3tags_container);
        trackLengthEditText = (EditText)findViewById(R.id.track_length_edittext);
        bitrateEditText = (EditText)findViewById(R.id.bitrate_edittext);
        channelsEditText = (EditText)findViewById(R.id.channels_edittext);
        sampleRateEditText = (EditText)findViewById(R.id.sample_rate_edittext);
        mpegVersionEditText = (EditText)findViewById(R.id.mpeg_version_edittext);
        formatEditText = (EditText)findViewById(R.id.format_edittext);
        encoderEditText = (EditText)findViewById(R.id.encoder_edittext);
        emphasisEditText = (EditText)findViewById(R.id.emphasis_edittext);

        try{
            TagOptionSingleton.getInstance().setAndroid(true);
            path  = getIntent().getStringExtra("path");
            if (D) Log.i(TAG,"Path" + path);
            f =AudioFileIO.read(new File(path));
            if(path.endsWith(".mp3")) {
                mp3TagsContainer.setVisibility(View.VISIBLE);
                MP3File mp3File = new MP3File(new File(path));
                MP3AudioHeader mp3AudioHeader = mp3File.getMP3AudioHeader();
                trackLengthEditText.setText(mp3AudioHeader.getTrackLengthAsString());
                trackLengthEditText.setKeyListener(null);
                bitrateEditText.setText(mp3AudioHeader.getBitRate() + " kbits");
                bitrateEditText.setKeyListener(null);
                channelsEditText.setText(mp3AudioHeader.getChannels());
                channelsEditText.setKeyListener(null);
                sampleRateEditText.setText(mp3AudioHeader.getSampleRate() + " Hz");
                sampleRateEditText.setKeyListener(null);
                mpegVersionEditText.setText(mp3AudioHeader.getMpegVersion());
                mpegVersionEditText.setKeyListener(null);
                formatEditText.setText(mp3AudioHeader.getFormat());
                formatEditText.setKeyListener(null);
                encoderEditText.setText(mp3AudioHeader.getEncoder());
                encoderEditText.setKeyListener(null);
                emphasisEditText.setText(mp3AudioHeader.getEmphasis());
                emphasisEditText.setKeyListener(null);

            } else{
                mp3TagsContainer.setVisibility(View.GONE);
            }
            if(!path.endsWith(".flac")) {
                tag = f.getTagOrCreateAndSetDefault();
                if (tag == null) {
                    if (D) Log.i(TAG, "Mp3 tag is nulll");
                    tag = f.createDefaultTag();
                }
            } else {
                flacTag = (FlacTag)f.getTagOrCreateAndSetDefault();
            }
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (D) Log.i(TAG,"Button clicked");
                    if(tag != null && f != null && !path.endsWith(".flac")){
                        try {
                            if(tag.hasField(FieldKey.TITLE)) {
                                if (D) Log.i(TAG,"Setting title field");
                                tag.setField(FieldKey.TITLE, titleEditText.getText().toString().trim());
                            } else {
                                if (D) Log.i(TAG,"Adding title field");
                                tag.addField(FieldKey.TITLE, titleEditText.getText().toString().trim());
                            }
                            if(tag.hasField(FieldKey.TITLE_SORT)) {
                                if (D) Log.i(TAG,"Setting title field");
                                tag.setField(FieldKey.TITLE_SORT, titleEditText.getText().toString().trim());
                            } else {
                                if (D) Log.i(TAG,"Adding title field");
                                tag.addField(FieldKey.TITLE_SORT, titleEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong title", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(tag.hasField(FieldKey.ALBUM)) {
                                tag.setField(FieldKey.ALBUM, albumEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.ALBUM, albumEditText.getText().toString().trim());
                            }
                            if(tag.hasField(FieldKey.ALBUM_SORT)) {
                                tag.setField(FieldKey.ALBUM_SORT, albumEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.ALBUM_SORT, albumEditText.getText().toString().trim());
                            }
                            if(tag.hasField(FieldKey.ORIGINAL_ALBUM)) {
                                tag.setField(FieldKey.ORIGINAL_ALBUM, albumEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.ORIGINAL_ALBUM, albumEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong album", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(tag.hasField(FieldKey.ARTIST)) {
                                tag.setField(FieldKey.ARTIST, artistEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.ARTIST, artistEditText.getText().toString().trim());
                            }
                            if(tag.hasField(FieldKey.ARTIST_SORT)) {
                                tag.setField(FieldKey.ARTIST_SORT, artistEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.ARTIST_SORT, artistEditText.getText().toString().trim());
                            }
                            if(tag.hasField(FieldKey.ORIGINAL_ARTIST)) {
                                tag.setField(FieldKey.ORIGINAL_ARTIST, artistEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.ORIGINAL_ARTIST, artistEditText.getText().toString().trim());
                            }
                            if(tag.hasField(FieldKey.ALBUM_ARTIST)) {
                                tag.setField(FieldKey.ALBUM_ARTIST, artistEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.ALBUM_ARTIST, artistEditText.getText().toString().trim());
                            }
                            if(tag.hasField(FieldKey.ARTIST_SORT)) {
                                tag.setField(FieldKey.ARTIST_SORT, artistEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.ARTIST_SORT, artistEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong artist", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(tag.hasField(FieldKey.GENRE)) {
                                tag.setField(FieldKey.GENRE, genreEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.GENRE, genreEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong genre", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(tag.hasField(FieldKey.YEAR)) {
                                tag.setField(FieldKey.YEAR, yearEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.YEAR, yearEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong year", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(tag.hasField(FieldKey.COMPOSER)){
                                tag.setField(FieldKey.COMPOSER, composerEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.COMPOSER, composerEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong composer", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(tag.hasField(FieldKey.BPM)) {
                                tag.setField(FieldKey.BPM, bpmEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.BPM, bpmEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong BPM", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(tag.hasField(FieldKey.LANGUAGE)) {
                                tag.setField(FieldKey.LANGUAGE, languageEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.LANGUAGE, languageEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong language", Toast.LENGTH_SHORT).show();
                        }

                        try {
                            if(tag.hasField(FieldKey.COMMENT)) {
                                tag.setField(FieldKey.COMMENT, commentEditText.getText().toString().trim());
                            } else {
                                tag.addField(FieldKey.COMMENT, commentEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong comment", Toast.LENGTH_SHORT).show();
                        }

                        if(photoChanged && selectedImage != null){
                            try {
                                tag.deleteArtworkField();
                                tag.addField(ArtworkFactory.createArtworkFromFile(new File(getRealPathFromURI(imageUri))));
                            }catch (FieldDataInvalidException ex){
                                Toast.makeText(Tagger.this, "Can't save artwork", Toast.LENGTH_SHORT).show();
                            }catch (IOException ex1){
                                Toast.makeText(Tagger.this, "Wrong path to artwork", Toast.LENGTH_SHORT).show();
                            }
                        }
                        try {
                            f.commit();
                        }catch (CannotWriteException ex){
                            Toast.makeText(Tagger.this, "Can't write tags to file", Toast.LENGTH_SHORT).show();
                        }
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
                    }
                    if(flacTag != null && f != null && path.endsWith(".flac")){

                        try {
                            if(flacTag.hasField(FieldKey.TITLE)) {
                                if (D) Log.i(TAG,"Setting title field");
                                flacTag.setField(FieldKey.TITLE, titleEditText.getText().toString().trim());
                            } else {
                                if (D) Log.i(TAG,"Adding title field");
                                flacTag.addField(FieldKey.TITLE, titleEditText.getText().toString().trim());
                            }
                            if(flacTag.hasField(FieldKey.TITLE_SORT)) {
                                if (D) Log.i(TAG,"Setting title field");
                                flacTag.setField(FieldKey.TITLE_SORT, titleEditText.getText().toString().trim());
                            } else {
                                if (D) Log.i(TAG,"Adding title field");
                                flacTag.addField(FieldKey.TITLE_SORT, titleEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong title", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(flacTag.hasField(FieldKey.ALBUM)) {
                                flacTag.setField(FieldKey.ALBUM, albumEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.ALBUM, albumEditText.getText().toString().trim());
                            }
                            if(flacTag.hasField(FieldKey.ALBUM_SORT)) {
                                flacTag.setField(FieldKey.ALBUM_SORT, albumEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.ALBUM_SORT, albumEditText.getText().toString().trim());
                            }
                            if(flacTag.hasField(FieldKey.ORIGINAL_ALBUM)) {
                                flacTag.setField(FieldKey.ORIGINAL_ALBUM, albumEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.ORIGINAL_ALBUM, albumEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong album", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(flacTag.hasField(FieldKey.ARTIST)) {
                                flacTag.setField(FieldKey.ARTIST, artistEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.ARTIST, artistEditText.getText().toString().trim());
                            }
                            if(flacTag.hasField(FieldKey.ARTIST_SORT)) {
                                flacTag.setField(FieldKey.ARTIST_SORT, artistEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.ARTIST_SORT, artistEditText.getText().toString().trim());
                            }
                            if(flacTag.hasField(FieldKey.ORIGINAL_ARTIST)) {
                                flacTag.setField(FieldKey.ORIGINAL_ARTIST, artistEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.ORIGINAL_ARTIST, artistEditText.getText().toString().trim());
                            }
                            if(flacTag.hasField(FieldKey.ALBUM_ARTIST)) {
                                flacTag.setField(FieldKey.ALBUM_ARTIST, artistEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.ALBUM_ARTIST, artistEditText.getText().toString().trim());
                            }
                            if(flacTag.hasField(FieldKey.ARTIST_SORT)) {
                                flacTag.setField(FieldKey.ARTIST_SORT, artistEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.ARTIST_SORT, artistEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong artist", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(flacTag.hasField(FieldKey.GENRE)) {
                                flacTag.setField(FieldKey.GENRE, genreEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.GENRE, genreEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong genre", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(flacTag.hasField(FieldKey.YEAR)) {
                                flacTag.setField(FieldKey.YEAR, yearEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.YEAR, yearEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong year", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(flacTag.hasField(FieldKey.COMPOSER)){
                                flacTag.setField(FieldKey.COMPOSER, composerEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.COMPOSER, composerEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong composer", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(flacTag.hasField(FieldKey.BPM)) {
                                flacTag.setField(FieldKey.BPM, bpmEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.BPM, bpmEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong BPM", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if(flacTag.hasField(FieldKey.LANGUAGE)) {
                                flacTag.setField(FieldKey.LANGUAGE, languageEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.LANGUAGE, languageEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong language", Toast.LENGTH_SHORT).show();
                        }

                        try {
                            if(flacTag.hasField(FieldKey.COMMENT)) {
                                flacTag.setField(FieldKey.COMMENT, commentEditText.getText().toString().trim());
                            } else {
                                flacTag.addField(FieldKey.COMMENT, commentEditText.getText().toString().trim());
                            }
                        }catch (FieldDataInvalidException e){
                            Toast.makeText(Tagger.this, "Wrong comment", Toast.LENGTH_SHORT).show();
                        }

                        if(photoChanged && selectedImage != null){
                            try {

                                RandomAccessFile imageFile = new RandomAccessFile(new File(getRealPathFromURI(imageUri)), "r");
                                byte[] imagedata = new byte[(int) imageFile.length()];
                                imageFile.read(imagedata);
                                flacTag.setField(flacTag.createArtworkField(imagedata,
                                        PictureTypes.DEFAULT_ID,
                                        ImageFormats.MIME_TYPE_PNG,
                                        "test",
                                        200,
                                        200,
                                        24,
                                        0));}catch (FieldDataInvalidException ex){
                                Toast.makeText(Tagger.this, "Can't save artwork", Toast.LENGTH_SHORT).show();
                            }catch (IOException ex1){
                                Toast.makeText(Tagger.this, "Wrong path to artwork", Toast.LENGTH_SHORT).show();
                            }
                        }
                        try {
                            f.commit();
                        }catch (CannotWriteException ex){
                            Toast.makeText(Tagger.this, "Can't write tags to file", Toast.LENGTH_SHORT).show();
                        }
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
                    }
                }
            });
            if(tag != null && !path.endsWith(".flac")) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] art = artwork.getBinaryData();
                    Bitmap cover = BitmapFactory.decodeByteArray(art, 0, art.length);
                    imageView.setImageBitmap(cover);
                } else {
                    imageView.setImageBitmap(BassoUtils.drawTextToBitmap(Tagger.this, BitmapFactory.decodeResource(getResources(), R.drawable.ic_empty_cover_big), "NO COVER"));
                }
                TagField TITLE = tag.getFirstField(FieldKey.TITLE);
                if (TITLE != null) {
                    titleEditText.setText(getText(TITLE.toString()));
                }

                TagField ALBUM = tag.getFirstField(FieldKey.ALBUM);
                if (ALBUM != null) {
                    albumEditText.setText(getText(ALBUM.toString()));
                }

                TagField ARTIST = tag.getFirstField(FieldKey.ARTIST);
                if (ARTIST != null) {
                    artistEditText.setText(getText(ARTIST.toString()));
                }

                TagField GENRE = tag.getFirstField(FieldKey.GENRE);
                if (GENRE != null) {
                    genreEditText.setText(getText(GENRE.toString()));
                }

                TagField YEAR = tag.getFirstField(FieldKey.YEAR);
                if (YEAR != null) {
                    yearEditText.setText(getText(YEAR.toString()));
                }

                TagField COMPOSER = tag.getFirstField(FieldKey.COMPOSER);
                if (COMPOSER != null) {
                    composerEditText.setText(getText(COMPOSER.toString()));
                }

                TagField BPM = tag.getFirstField(FieldKey.BPM);
                if (BPM != null) {
                    bpmEditText.setText(getText(BPM.toString()));
                }

                TagField COUNTRY = tag.getFirstField(FieldKey.COMMENT);
                if (COUNTRY != null) {
                    commentEditText.setText(getText(COUNTRY.toString()));
                }

                TagField LANGUAGE = tag.getFirstField(FieldKey.LANGUAGE);
                if (LANGUAGE != null) {
                    languageEditText.setText(getText(LANGUAGE.toString()));
                }
            }

            if(flacTag != null && path.endsWith(".flac")){
                Artwork artwork = flacTag.getFirstArtwork();
                if (artwork != null) {
                    byte[] art = artwork.getBinaryData();
                    Bitmap cover = BitmapFactory.decodeByteArray(art, 0, art.length);
                    imageView.setImageBitmap(cover);
                } else {
                    imageView.setImageBitmap(BassoUtils.drawTextToBitmap(Tagger.this, BitmapFactory.decodeResource(getResources(), R.drawable.ic_empty_cover_big), "NO COVER"));
                }
                TagField TITLE = flacTag.getFirstField(FieldKey.TITLE);
                if (TITLE != null) {
                    titleEditText.setText(getText(TITLE.toString()));
                }

                TagField ALBUM = flacTag.getFirstField(FieldKey.ALBUM);
                if (ALBUM != null) {
                    albumEditText.setText(getText(ALBUM.toString()));
                }

                TagField ARTIST = flacTag.getFirstField(FieldKey.ARTIST);
                if (ARTIST != null) {
                    artistEditText.setText(getText(ARTIST.toString()));
                }

                TagField GENRE = flacTag.getFirstField(FieldKey.GENRE);
                if (GENRE != null) {
                    genreEditText.setText(getText(GENRE.toString()));
                }

                TagField YEAR = flacTag.getFirstField(FieldKey.YEAR);
                if (YEAR != null) {
                    yearEditText.setText(getText(YEAR.toString()));
                }

                TagField COMPOSER = flacTag.getFirstField(FieldKey.COMPOSER);
                if (COMPOSER != null) {
                    composerEditText.setText(getText(COMPOSER.toString()));
                }

                TagField BPM = flacTag.getFirstField(FieldKey.BPM);
                if (BPM != null) {
                    bpmEditText.setText(getText(BPM.toString()));
                }

                TagField COUNTRY = flacTag.getFirstField(FieldKey.COMMENT);
                if (COUNTRY != null) {
                    commentEditText.setText(getText(COUNTRY.toString()));
                }

                TagField LANGUAGE = flacTag.getFirstField(FieldKey.LANGUAGE);
                if (LANGUAGE != null) {
                    languageEditText.setText(getText(LANGUAGE.toString()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public int getStatusBarHeight(){
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private String getText(String raw){
        if(path.endsWith(".mp3")) {
            if (D) Log.i(TAG, "Original string " + raw);
            int indexOfText = raw.indexOf("Text=\"");
            if (indexOfText == -1)
                return "";
            int lastIndexOfQuotes = raw.lastIndexOf("\";");
            if (D)
                Log.i(TAG, "Resulted string " + raw.substring(indexOfText + 6, lastIndexOfQuotes) + " index of text " + indexOfText);
            return raw.substring(indexOfText + 6, lastIndexOfQuotes);
        } else
            return raw;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        selectedImage = BitmapFactory.decodeStream(imageStream);
                        photoChanged = true;
                        imageView.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
