package com.cjq.tool.memorytour.ui.activity;

import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;
import com.cjq.tool.memorytour.ui.adapter.PassageToReciteAdapter;
import com.cjq.tool.memorytour.ui.adapter.SectionToReciteAdapter;
import com.cjq.tool.memorytour.bean.RecitableBook;
import com.cjq.tool.memorytour.bean.RecitableChapter;
import com.cjq.tool.memorytour.bean.RecitablePassage;
import com.cjq.tool.memorytour.ui.dialog.LoadingDialog;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.Logger;

import java.util.Arrays;
import java.util.List;

public class PassageMemoryStateEditActivity extends AppCompatActivity {

    private List<RecitablePassage> recitablePassages;
    private SectionToReciteAdapter<RecitableBook> bookAdapter;
    private SectionToReciteAdapter<RecitableChapter> chapterAdapter;
    private PassageToReciteAdapter passageAdapter;
    private CheckBox chkBook;
    private CheckBox chkChapter;
    private CheckBox chkPassage;
    private LoadingDialog loadingDialog = new LoadingDialog();

    private View.OnClickListener onPassagesAddToReciteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddPassagesToReciteTask addPassagesToReciteTask = new AddPassagesToReciteTask();
            addPassagesToReciteTask.execute();
        }
    };

    private View.OnClickListener onBookEnableReciteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            bookAdapter.setAllRecitable(chkBook.isChecked());
            bookAdapter.notifyDataSetChanged();
            chapterAdapter.notifyDataSetChanged();
            passageAdapter.notifyDataSetChanged();
            chkChapter.setChecked(chkBook.isChecked());
            chkPassage.setChecked(chkBook.isChecked());
        }
    };

    private View.OnClickListener onChapterEnableReciteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            chapterAdapter.setAllRecitable(chkChapter.isChecked());
            chapterAdapter.notifyDataSetChanged();
            passageAdapter.notifyDataSetChanged();
            chkPassage.setChecked(chkChapter.isChecked());
        }
    };

    private View.OnClickListener onPassageEnableReciteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            passageAdapter.setAllRecitable(chkPassage.isChecked());
            passageAdapter.notifyDataSetChanged();
        }
    };

    private SectionToReciteAdapter.OnSectionClickListener<RecitableBook> onBookClickListener = new SectionToReciteAdapter.OnSectionClickListener<RecitableBook>() {
        @Override
        public void onLabelClick(RecitableBook book) {
            List<RecitableChapter> chapters = book.getChapters();
            chapterAdapter.setSections(chapters);
            if (chapters == null || chapters.size() == 0) {
                chapterAdapter.notifyDataSetChanged();
                passageAdapter.setSections(null);
                passageAdapter.notifyDataSetChanged();
            } else {
                chapterAdapter.selectSection(0);
            }
        }

        @Override
        public void onRecitableClick(RecitableBook section, boolean isSelected) {
            if (isSelected) {
                chapterAdapter.notifyDataSetChanged();
                passageAdapter.notifyDataSetChanged();
            }
        }
    };

    private SectionToReciteAdapter.OnSectionClickListener<RecitableChapter> onChapterClickListener = new SectionToReciteAdapter.OnSectionClickListener<RecitableChapter>() {
        @Override
        public void onLabelClick(RecitableChapter chapter) {
            passageAdapter.setSections(chapter.getPassages());
            passageAdapter.notifyDataSetChanged();
        }

        @Override
        public void onRecitableClick(RecitableChapter section, boolean isSelected) {
            if (isSelected) {
                passageAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_mission_edit);

        chkBook = setSectionList(R.id.il_books_to_recite, R.string.book,
                bookAdapter = new SectionToReciteAdapter(this),
                onBookClickListener,
                onBookEnableReciteListener);
        chkChapter = setSectionList(R.id.il_chapters_to_recite, R.string.chapter,
                chapterAdapter = new SectionToReciteAdapter(this),
                onChapterClickListener,
                onChapterEnableReciteListener);
        chkPassage = setPassageList();
        importSections();
    }

    private CheckBox setSectionList(@IdRes int ilSectionId, @StringRes int label,
                                SectionToReciteAdapter adapter,
                                SectionToReciteAdapter.OnSectionClickListener onSectionClickListener,
                                View.OnClickListener onSectionEnableReciteListener) {
        View ilSection = findViewById(ilSectionId);
        TextView tvName = (TextView)ilSection.findViewById(R.id.tv_name_section);
        tvName.setText(label);
        ListView lvSection = (ListView)ilSection.findViewById(R.id.lv_sections);
        lvSection.setAdapter(adapter);
        adapter.setOnSectionClickListener(onSectionClickListener);
        return setSectionEnableReciteListener(ilSection, onSectionEnableReciteListener);
    }

    private CheckBox setPassageList() {
        findViewById(R.id.btn_add_new_passages_to_recite).setOnClickListener(onPassagesAddToReciteListener);
        passageAdapter = new PassageToReciteAdapter(this);
        ListView lvPassage = (ListView)findViewById(R.id.lv_passages_to_recite);
        lvPassage.setAdapter(passageAdapter);
        return setSectionEnableReciteListener(findViewById(R.id.il_header_to_recite_passages), onPassageEnableReciteListener);
    }

    private CheckBox setSectionEnableReciteListener(View ilSection, View.OnClickListener listener) {
        CheckBox chkSection = (CheckBox)ilSection.findViewById(R.id.chk_to_recite_section);
        chkSection.setOnClickListener(listener);
        return chkSection;
    }

    private void importSections() {
        ImportAllSectionTask importAllSectionTask = new ImportAllSectionTask();
        importAllSectionTask.execute();
    }

    private class ImportAllSectionTask extends AsyncTask<Void, Integer, List<RecitableBook>> {

        private String error;

        @Override
        protected List<RecitableBook> doInBackground(Void... params) {
            try {
                List<RecitableBook> books = Arrays.asList(SQLiteManager.importRecitableBooks());
                List<RecitableChapter> chapters = Arrays.asList(SQLiteManager.importRecitableChapters());
                recitablePassages = Arrays.asList(SQLiteManager.importRecitablePassages());
                fillChapters(books, chapters);
                fillPassages(chapters, recitablePassages);
                return books;
            } catch (Exception e) {
                Logger.record(e);
                error = getString(R.string.ppt_section_map_build_error);
            }
            return null;
        }

        private void fillChapters(List<RecitableBook> books, List<RecitableChapter> chapters) {
            int end = 0, start, bookId;
            for (RecitableBook book :
                    books) {
                bookId = book.getId();
                start = end;
                end = chapters.size();
                for (int i = start;i < end;++i) {
                    if (bookId != chapters.get(i).getBookId()) {
                        end = i;
                        break;
                    }
                }
                book.setChapters(chapters.subList(start, end));
            }
        }

        private void fillPassages(List<RecitableChapter> chapters, List<RecitablePassage> passages) {
            int end = 0, start, chapterId;
            for (RecitableChapter chapter :
                    chapters) {
                chapterId = chapter.getId();
                start = end;
                end = passages.size();
                for (int i = start;i < end;++i) {
                    if (chapterId != passages.get(i).getChapterId()) {
                        end = i;
                        break;
                    }
                }
                chapter.setPassages(passages.subList(start, end));
            }
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.show(getSupportFragmentManager(), getString(R.string.ppt_section_map_instructing));
        }

        @Override
        protected void onPostExecute(List<RecitableBook> recitableBooks) {
            loadingDialog.dismiss();
            if (error == null && recitableBooks != null) {
                if (recitableBooks.size() > 0) {
                    bookAdapter.setSections(recitableBooks);
                    bookAdapter.selectSection(0);
                } else {
                    Prompter.show(R.string.ppt_section_null);
                }
            } else {
                Prompter.show(error);
            }
        }
    }

    private class AddPassagesToReciteTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return SQLiteManager.updateRecitablePassages(recitablePassages);
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.show(getSupportFragmentManager(), getString(R.string.ppt_add_passages_to_recite));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            loadingDialog.dismiss();
            if (result) {
                for (RecitablePassage passage :
                        recitablePassages) {
                    if (passage.isEnableRecite() && passage.isRecitable()) {
                        passage.setMemoryState(passage.getRecitableMemoryState());
                    }
                }
                passageAdapter.notifyDataSetChanged();
                Prompter.show(R.string.ppt_recitable_passages_update_success);
            } else {
                Prompter.show(R.string.ppt_recitable_passages_update_failed);
            }
        }
    }
}
