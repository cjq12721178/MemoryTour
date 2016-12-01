package com.cjq.tool.memorytour.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.Book;
import com.cjq.tool.memorytour.bean.Chapter;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.exception.ImportSectionException;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;
import com.cjq.tool.memorytour.ui.activity.NewMissionEditActivity;
import com.cjq.tool.memorytour.ui.dialog.LoadingDialog;
import com.cjq.tool.memorytour.util.Logger;

import org.xml.sax.helpers.DefaultHandler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by KAT on 2016/8/26.
 */
public class SpecialFunctionFragment extends BaseFragment {

    private static final int REQUEST_CODE_FILE_SELECT = 1;
    private CheckBox chkBooks;
    private CheckBox chkChapters;
    private CheckBox chkPassages;
    private CheckBox chkCurrentSections;
    private LoadingDialog loadingDialog = new LoadingDialog();

    private View.OnClickListener onAddToReciteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), NewMissionEditActivity.class);
            startActivity(intent);
        }
    };

    private View.OnLongClickListener onFilePathChangeListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            chkCurrentSections = (CheckBox) v;
            try {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.el_import_title)), REQUEST_CODE_FILE_SELECT);
            } catch (android.content.ActivityNotFoundException ex) {
                promptMessage(R.string.ppt_file_manager_not_installed);
            }
            return true;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK &&
                requestCode == REQUEST_CODE_FILE_SELECT) {
            Uri uri = data.getData();
            String filePath = uri.getPath();
            Log.d("MemoryTour", "file path = " + filePath);
            if (chkCurrentSections != null) {
                chkCurrentSections.setText(filePath);
                chkCurrentSections = null;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private View.OnClickListener onMoveSectionToDatabaseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!chkBooks.isChecked() &&
                    !chkChapters.isChecked() &&
                    !chkPassages.isChecked()) {
                promptMessage(R.string.ppt_enable_import_section);
                return;
            }

            InsertSectionTask insertSectionTask = new InsertSectionTask();
            insertSectionTask.execute(chkBooks.isChecked() ? chkBooks.getText().toString() : null,
                    chkChapters.isChecked() ? chkChapters.getText().toString() : null,
                    chkPassages.isChecked() ? chkPassages.getText().toString() : null);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_special_function, null);
        chkBooks = setSectionImportView(view.findViewById(R.id.il_books_import), R.string.tv_import_books_label, R.string.tv_import_books_path);
        chkChapters = setSectionImportView(view.findViewById(R.id.il_chapters_import), R.string.tv_import_chapters_label, R.string.tv_import_chapters_path);
        chkPassages = setSectionImportView(view.findViewById(R.id.il_passages_import), R.string.tv_import_passages_label, R.string.tv_import_passages_path);
        view.findViewById(R.id.btn_section_save).setOnClickListener(onMoveSectionToDatabaseListener);
        view.findViewById(R.id.btn_add_to_recite).setOnClickListener(onAddToReciteListener);
        return view;
    }

    private CheckBox setSectionImportView(View ilImportGroup, int labelResId, int defaultPathResId) {
        TextView tvSectionType = (TextView)ilImportGroup.findViewById(R.id.tv_section_import_type);
        tvSectionType.setText(labelResId);
        CheckBox chkSection = (CheckBox)ilImportGroup.findViewById(R.id.chk_section_import_path_and_enable);
        chkSection.setText(defaultPathResId);
        chkSection.setOnLongClickListener(onFilePathChangeListener);
        return chkSection;
    }

    private class InsertSectionTask extends AsyncTask<String, Integer, Boolean> {

        private String errorInfo;

        @Override
        protected void onPreExecute() {
            errorInfo = null;
            loadingDialog.show(getFragmentManager(), getString(R.string.ppt_saving_section));
        }

        @Override
        protected void onPostExecute(Boolean moveResult) {
            loadingDialog.dismiss();
            if (errorInfo == null) {
                errorInfo = getString(R.string.btn_insert_section) + (moveResult ? "成功" : "失败");
            }
            promptMessage(errorInfo);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (params != null && params.length != 3)
                return false;

            boolean moveResult = true;
            String bookPath = params[0];
            String chapterPath = params[1];
            String passagePath = params[2];
            try {
                if (bookPath != null) {
                    List<Book> books = importBookFromXml(bookPath);
                    moveResult = moveBookToDatabase(books) & moveResult;
                }
                if (chapterPath != null) {
                    Map<Integer, List<Chapter>> chapters = importChapterFromXml(chapterPath);
                    moveResult = moveChapterToDatabase(chapters) & moveResult;
                }
                if (passagePath != null) {
                    Map<Integer, List<Passage>> passages = importPassageFromXml(passagePath);
                    moveResult = movePassageToDatabase(passages) & moveResult;
                }
            } catch (Exception e) {
                moveResult = false;
                errorInfo = e.getMessage();
            }
            return moveResult;
        }

        private boolean moveBookToDatabase(List<Book> books) {
            if (books == null)
                return false;
            boolean insertResult = true;
            for (Book book:
                    books) {
                insertResult = SQLiteManager.insertBookFromXml(book) & insertResult;
            }
            return insertResult;
        }

        private boolean moveChapterToDatabase(Map<Integer, List<Chapter>> chapterMap) {
            if (chapterMap == null)
                return false;
            boolean insertResult = true;
            for (Map.Entry<Integer, List<Chapter>> chapters:
                    chapterMap.entrySet()) {
                for (Chapter chapter :
                        chapters.getValue()) {
                    insertResult = SQLiteManager.insertChapterFromXml(chapter, chapters.getKey()) & insertResult;
                }
            }
            return insertResult;
        }

        private boolean movePassageToDatabase(Map<Integer, List<Passage>> passageMap) {
            if (passageMap == null)
                return false;
            boolean insertResult = true;
            for (Map.Entry<Integer, List<Passage>> passages :
                    passageMap.entrySet()) {
                for (Passage passage :
                        passages.getValue()) {
                    insertResult = SQLiteManager.insertPassageFromXml(passage, passages.getKey()) & insertResult;
                }
            }
            return insertResult;
        }

        private boolean parseXml(String filePath, DefaultHandler handler) {
            if (filePath.isEmpty())
                throw new ImportSectionException(getString(R.string.ppt_section_file_path_empty));

            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                InputStream is = new FileInputStream(filePath);
                SAXParser parser = factory.newSAXParser();
                parser.parse(is, handler);
                return true;
            } catch (Exception e) {
                Logger.record(e);
                throw new ImportSectionException(getString(R.string.ppt_section_file_read_error));
            }
        }

        private List<Book> importBookFromXml(String bookPath) {
            Book.Importer importer = new Book.Importer();
            return parseXml(bookPath, importer) ? importer.getBooks() : null;
        }

        private Map<Integer, List<Chapter>> importChapterFromXml(String chapterPath) {
            Chapter.Importer importer = new Chapter.Importer();
            return parseXml(chapterPath, importer) ? importer.getChapterMap() : null;
        }

        private Map<Integer, List<Passage>> importPassageFromXml(String passagePath) {
            Passage.Importer importer = new Passage.Importer();
            return parseXml(passagePath, importer) ? importer.getPassageMap() : null;
        }
    };
}
