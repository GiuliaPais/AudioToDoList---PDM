package it.uninsubria.pdm.audiotodolist;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.database.AppDatabase;
import it.uninsubria.pdm.audiotodolist.database.FolderDAO;
import it.uninsubria.pdm.audiotodolist.database.MemoDAO;
import it.uninsubria.pdm.audiotodolist.database.TagDAO;
import it.uninsubria.pdm.audiotodolist.database.VoiceMemoCrossTagsDAO;
import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemoCrossTags;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public class ManyToManyRelTest {
    private AppDatabase db;
    private VoiceMemoCrossTagsDAO voiceMemoCrossTagsDAO;
    private MemoDAO memoDAO;
    private FolderDAO folderDAO;
    private TagDAO tagDAO;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        voiceMemoCrossTagsDAO = db.voiceMemoCrossTagsDAO();
        memoDAO = db.memoDAO();
        tagDAO = db.tagDAO();
        folderDAO = db.folderDAO();
        folderDAO.insert(new Folder("ALL"));
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void memoWithNullTagsRetrieveTest() {
        VoiceMemo voiceMemo1 = new VoiceMemo();
        voiceMemo1.title = "voice_memo1";
        voiceMemo1.duration = Duration.of(100, ChronoUnit.SECONDS);
        voiceMemo1.dateTime = LocalDateTime.now();
        voiceMemo1.path = "testpath";
        voiceMemo1.folder = DefaultFolders.ALL.name();
        //Insert only in VoiceMemoTable since tags are null
        memoDAO.insertMemo(voiceMemo1);
        List<MemoWithTags> found = voiceMemoCrossTagsDAO.getAllMemosWithTagsFlat();
        assertThat(found.get(0).voiceMemo.title.equals("voice_memo1"));
    }

    @Test
    public void memoWithTagsRetrieveTest() {
        VoiceMemo voiceMemo1 = new VoiceMemo();
        voiceMemo1.title = "voice_memo1";
        voiceMemo1.duration = Duration.of(100, ChronoUnit.SECONDS);
        voiceMemo1.dateTime = LocalDateTime.now();
        voiceMemo1.path = "testpath";
        voiceMemo1.folder = DefaultFolders.ALL.name();
        MemoWithTags memoWithTags = new MemoWithTags();
        memoWithTags.voiceMemo = voiceMemo1;
        Tag t1 = new Tag();
        t1.tagName = "tag1";
        Tag t2 = new Tag();
        t2.tagName = "tag2";
        memoWithTags.tagList = List.of(t1, t2);
        //Insert in 3 tables
        memoDAO.insertMemo(voiceMemo1);
        tagDAO.insertTag(t1, t2);
        VoiceMemoCrossTags cross1 = new VoiceMemoCrossTags();
        cross1.tagName = t1.tagName;
        cross1.title = voiceMemo1.title;
        VoiceMemoCrossTags cross2 = new VoiceMemoCrossTags();
        cross2.tagName = t2.tagName;
        cross2.title = voiceMemo1.title;
        voiceMemoCrossTagsDAO.insert(cross1, cross2);
        List<MemoWithTags> found = voiceMemoCrossTagsDAO.getAllMemosWithTagsFlat();
        assertThat(found.size() == 1);
        assertThat(found.get(0).voiceMemo.title.equals("voice_memo1"));
        assertThat(found.get(0).tagList.size() == 2);
        assertThat(found.get(0).tagList.get(0).equals(t1.tagName));
        assertThat(found.get(0).tagList.get(1).equals(t2.tagName));
    }

    @Test
    public void memoWithTagsDeleteTest() {
        VoiceMemo voiceMemo1 = new VoiceMemo();
        voiceMemo1.title = "voice_memo1";
        voiceMemo1.duration = Duration.of(100, ChronoUnit.SECONDS);
        voiceMemo1.dateTime = LocalDateTime.now();
        voiceMemo1.path = "testpath";
        voiceMemo1.folder = DefaultFolders.ALL.name();
        MemoWithTags memoWithTags = new MemoWithTags();
        memoWithTags.voiceMemo = voiceMemo1;
        Tag t1 = new Tag();
        t1.tagName = "tag1";
        Tag t2 = new Tag();
        t2.tagName = "tag2";
        memoWithTags.tagList = List.of(t1, t2);
        //Insert in 3 tables
        memoDAO.insertMemo(voiceMemo1);
        tagDAO.insertTag(t1, t2);
        VoiceMemoCrossTags cross1 = new VoiceMemoCrossTags();
        cross1.tagName = t1.tagName;
        cross1.title = voiceMemo1.title;
        VoiceMemoCrossTags cross2 = new VoiceMemoCrossTags();
        cross2.tagName = t2.tagName;
        cross2.title = voiceMemo1.title;
        voiceMemoCrossTagsDAO.insert(cross1, cross2);
        //Delete a tag and test it cascades
        tagDAO.deleteTag(t1);
        List<MemoWithTags> found = voiceMemoCrossTagsDAO.getAllMemosWithTagsFlat();
        assertThat(found.size() == 1);
        assertThat(found.get(0).tagList.size() == 1);
        assertThat(found.get(0).tagList.get(0).equals(t2.tagName));
        //Delete a voice memo and test it cascades
        memoDAO.deleteMemo(voiceMemo1);
        List<MemoWithTags> found2 = voiceMemoCrossTagsDAO.getAllMemosWithTagsFlat();
        assertThat(found2.size() == 0);
    }
}
