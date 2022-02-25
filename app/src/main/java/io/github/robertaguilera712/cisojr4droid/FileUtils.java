package io.github.robertaguilera712.cisojr4droid;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;

public class FileUtils implements DefaultLifecycleObserver {
    private final ActivityResultRegistry mRegistry;
    private final ContentResolver resolver;
    private final SharedPreferences sharedPref;
    private final Context context;
    private final RecyclerViewUtils recyclerViewUtils;
    private ActivityResultLauncher<Intent> requestRomsFolder;

    public FileUtils(ActivityResultRegistry mRegistry, ContentResolver resolver, SharedPreferences sharedPref, Context context, RecyclerViewUtils recyclerViewUtils) {
        this.mRegistry = mRegistry;
        this.resolver = resolver;
        this.sharedPref = sharedPref;
        this.context = context;
        this.recyclerViewUtils = recyclerViewUtils;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        requestRomsFolder = mRegistry.register("key", owner,
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri treeUri = data.getData();
                            saveDirectory(treeUri);
                            recyclerViewUtils.loadRoms(listRoms());
                        }
                    }
                });
    }

    public void selectRomsFolder() {
        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION;
        data.addFlags(flags);
        requestRomsFolder.launch(data);
    }

    private void saveDirectory(Uri dirUri) {
        if (dirUri == null) return;
        Uri existingDirectory = loadSavedDirectory();
        if (existingDirectory != null) {
            resolver.releasePersistableUriPermission(existingDirectory, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        resolver.takePersistableUriPermission(dirUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    private Uri loadSavedDirectory() {
        List<UriPermission> list = resolver.getPersistedUriPermissions();
        return list.size() != 0 ? list.get(0).getUri() : null;
    }

    public ArrayList<Rom> listRoms() {
        final ArrayList<Rom> roms = new ArrayList<>();
        final Uri treeUri = loadSavedDirectory();
        if (treeUri != null) {
            int compressionLevel = sharedPref.getInt(context.getString(R.string.saved_compression_level_key), 1);
            boolean deleteAfter = sharedPref.getBoolean(context.getString(R.string.saved_delete_after_key), false);
            final DocumentFile romsFolder = DocumentFile.fromTreeUri(context, treeUri);
            if (romsFolder != null) {
                Rom.setRomsFolder(romsFolder);
                final String inputMimeType = Rom.getInputMimeType();
                final String inputFileExtension = Rom.getInputFileExtension();
                final String outputFileExtension = Rom.getOutputFileExtension();
                for (DocumentFile file : romsFolder.listFiles()) {
                    if (file.isFile()) {
                        final String fileName = file.getName();
                        final String fileExtension = fileName.substring(fileName.lastIndexOf("."));
                        final String fileMimeType = file.getType();
                        if (inputMimeType.equals(fileMimeType) && inputFileExtension.equals(fileExtension)) {
                            final Uri inputUri = file.getUri();
                            final String inputFilename = file.getName();
                            final String outputFilename = inputFilename.replaceFirst(inputFileExtension + "$", outputFileExtension);
                            final String status = context.getString(R.string.status_added);
                            roms.add(new Rom(inputUri, inputFilename, outputFilename, compressionLevel, deleteAfter, status, 0));
                        }

                    }
                }
            }
        } else {
            Toast.makeText(context, context.getString(R.string.no_roms_folder), Toast.LENGTH_SHORT).show();
        }

        return roms;
    }
}
