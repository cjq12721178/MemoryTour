package com.cjq.tool.memorytour.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cjq.tool.memorytour.R;
import com.cjq.tool.memorytour.bean.Book;
import com.cjq.tool.memorytour.bean.Chapter;
import com.cjq.tool.memorytour.bean.Passage;
import com.cjq.tool.memorytour.bean.UserInfo;
import com.cjq.tool.memorytour.exception.ImportSectionException;
import com.cjq.tool.memorytour.io.sqlite.SQLiteManager;
import com.cjq.tool.memorytour.ui.dialog.LoadingDialog;
import com.cjq.tool.memorytour.ui.toast.Prompter;
import com.cjq.tool.memorytour.util.Logger;
import com.cjq.tool.memorytour.util.Tag;
import com.cjq.tool.memorytour.util.UriHelper;

import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PassageSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_FILE_SELECT = 1;
    private static final int RC_READ_EXTERNAL_STORAGE = 2;
    private static final int RC_DATABASE_SELECT = 3;
    private static final String CONFIG_FILE_NAME = "config";

    private CheckBox chkBooks;
    private CheckBox chkChapters;
    private CheckBox chkPassages;
    private CheckBox chkCurrentSections;
    private LoadingDialog loadingDialog = new LoadingDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passage_setting);

        SharedPreferences config = getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        chkBooks = setSectionImportView(findViewById(R.id.il_books_import),
                R.string.tv_import_books_label,
                config,
                Tag.SP_BOOK_PATH,
                R.string.tv_import_books_path);
        chkChapters = setSectionImportView(findViewById(R.id.il_chapters_import),
                R.string.tv_import_chapters_label,
                config,
                Tag.SP_CHAPTER_PATH,
                R.string.tv_import_chapters_path);
        chkPassages = setSectionImportView(findViewById(R.id.il_passages_import),
                R.string.tv_import_passages_label,
                config,
                Tag.SP_PASSAGE_PATH,
                R.string.tv_import_passages_path);
        findViewById(R.id.btn_section_save).setOnClickListener(onMoveSectionToDatabaseListener);
        findViewById(R.id.btn_add_to_recite).setOnClickListener(onAddToReciteListener);
        findViewById(R.id.btn_export_local_database).setOnClickListener(this);
        findViewById(R.id.btn_import_database).setOnClickListener(this);
    }

    private View.OnClickListener onAddToReciteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(PassageSettingActivity.this, PassageMemoryStateEditActivity.class);
            startActivity(intent);
        }
    };

    private View.OnLongClickListener onFilePathChangeListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            chkCurrentSections = (CheckBox) v;
            try {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.el_import_title)), REQUEST_CODE_FILE_SELECT);
            } catch (android.content.ActivityNotFoundException ex) {
                Prompter.show(R.string.ppt_file_manager_not_installed);
            }
            return true;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE_SELECT: {
                    Uri uri = data.getData();
                    String filePath = uri.getPath();
                    if (chkCurrentSections != null &&
                            !chkCurrentSections.getText().toString().equals(filePath)) {
                        chkCurrentSections.setText(filePath);
                        getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putString(chkCurrentSections.getTag().toString(), filePath)
                                .commit();
                        chkCurrentSections = null;
                    }
                } break;
                case RC_DATABASE_SELECT: {
                    String filePath = UriHelper.getRealFilePath(this, data.getData());
                    if (TextUtils.isEmpty(filePath)) {
                        Prompter.show(R.string.ppt_import_database_empty);
                    } else {
                        if (SQLiteManager.replaceLocalDatabase(this, filePath)) {
                            setResult(RESULT_OK);
                            Prompter.show(R.string.ppt_import_database_success);
                        } else {
                            Prompter.show(R.string.ppt_import_database_failed);
                        }
                    }
                } break;
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
                Prompter.show(R.string.ppt_enable_import_section);
                return;
            }

            InsertSectionTask insertSectionTask = new InsertSectionTask();
            insertSectionTask.execute(chkBooks.isChecked() ? chkBooks.getText().toString() : null,
                    chkChapters.isChecked() ? chkChapters.getText().toString() : null,
                    chkPassages.isChecked() ? chkPassages.getText().toString() : null);
        }
    };

    private CheckBox setSectionImportView(View ilImportGroup,
                                          @StringRes int labelResId,
                                          SharedPreferences preferences,
                                          String configKey,
                                          int defaultPathResId) {
        TextView tvSectionType = (TextView)ilImportGroup.findViewById(R.id.tv_section_import_type);
        tvSectionType.setText(labelResId);
        CheckBox chkSection = (CheckBox)ilImportGroup.findViewById(R.id.chk_section_import_path_and_enable);
        String customPath = preferences.getString(configKey, null);
        if (customPath != null) {
            chkSection.setText(customPath);
        } else {
            chkSection.setText(defaultPathResId);
        }
        chkSection.setTag(configKey);
        chkSection.setOnLongClickListener(onFilePathChangeListener);
        return chkSection;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_export_local_database:
                if (SQLiteManager.exportLocalDatabase(this)) {
                    Prompter.show(R.string.ppt_export_database_success);
                } else {
                    Prompter.show(R.string.ppt_export_database_failed);
                }
                break;
            case R.id.btn_import_database:
                chooseDatabase();
                break;
        }
    }

    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
    private void chooseDatabase() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(UriHelper.getUriByPath(this, Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "MemoryStorage.db"), "*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.ppt_choose_import_database)), RC_DATABASE_SELECT);
            } catch (ActivityNotFoundException e) {
                Prompter.show(R.string.ppt_file_manager_not_installed);
            }
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.ppt_request_read_external_storage_permission),
                    RC_READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private class InsertSectionTask extends AsyncTask<String, Integer, Boolean> {

        private String errorInfo;

        @Override
        protected void onPreExecute() {
            errorInfo = null;
            loadingDialog.show(getSupportFragmentManager(), getString(R.string.ppt_saving_section));
        }

        @Override
        protected void onPostExecute(Boolean moveResult) {
            loadingDialog.dismiss();
            if (errorInfo == null) {
                errorInfo = getString(R.string.btn_insert_section) + (moveResult ? "成功" : "失败");
            }
            Prompter.show(errorInfo);
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
